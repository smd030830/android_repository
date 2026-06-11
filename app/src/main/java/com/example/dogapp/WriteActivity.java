package com.example.dogapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class WriteActivity extends AppCompatActivity {
    private static final String UTF8_FORM_CONTENT_TYPE =
            "application/x-www-form-urlencoded; charset=UTF-8";
    private static final int MAX_PHOTO_SIZE = 1024;
    private static final int MAX_PHOTO_BYTES = 700 * 1024;
    private static final int PHOTO_UPLOAD_TIMEOUT_MS = 30_000;

    private String initialDogName;
    private String currentUserID;
    private Uri selectedPhotoUri;
    private ArrayAdapter<String> dogSpinnerAdapter;
    private ArrayList<String> dogNames;
    private ImageView imgSelectedPhoto;
    private TextView tvSelectedPhotoName;
    private TextView tvFoodValue;
    private TextView tvPoopValue;
    private TextView tvWriteTitle;
    private RadioGroup rgDogMode;
    private Spinner spinnerDogs;
    private LinearLayout layoutNewDog;
    private SeekBar seekFood;
    private SeekBar seekPoop;
    private EditText editDiaryContent;
    private EditText editNewDogName;
    private EditText editNewDogBreed;
    private EditText editNewDogAge;
    private Button btnSave;

    private final ActivityResultLauncher<Intent> photoPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != RESULT_OK || result.getData() == null || result.getData().getData() == null) {
                    return;
                }

                selectedPhotoUri = result.getData().getData();
                final int flags = result.getData().getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                try {
                    getContentResolver().takePersistableUriPermission(selectedPhotoUri, flags);
                } catch (SecurityException ignored) {
                }

                imgSelectedPhoto.setImageURI(selectedPhotoUri);
                imgSelectedPhoto.setVisibility(ImageView.VISIBLE);
                tvSelectedPhotoName.setText(getDisplayName(selectedPhotoUri));
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        initialDogName = getIntent().getStringExtra("dogName");
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentUserID = prefs.getString("userID", "");
        String userType = prefs.getString("userType", "");
        if (TextUtils.isEmpty(currentUserID) || !userType.equals("foster")) {
            Toast.makeText(this, "임시보호자로 로그인 후 일지를 작성할 수 있습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvWriteTitle = findViewById(R.id.tvWriteTitle);
        rgDogMode = findViewById(R.id.rgDogMode);
        spinnerDogs = findViewById(R.id.spinnerDogs);
        layoutNewDog = findViewById(R.id.layoutNewDog);
        editNewDogName = findViewById(R.id.editNewDogName);
        editNewDogBreed = findViewById(R.id.editNewDogBreed);
        editNewDogAge = findViewById(R.id.editNewDogAge);
        tvFoodValue = findViewById(R.id.tvFoodValue);
        tvPoopValue = findViewById(R.id.tvPoopValue);
        seekFood = findViewById(R.id.seekFood);
        seekPoop = findViewById(R.id.seekPoop);
        editDiaryContent = findViewById(R.id.editDiaryContent);
        Button btnAddPhoto = findViewById(R.id.btnAddPhoto);
        Button btnCancel = findViewById(R.id.btnCancel);
        btnSave = findViewById(R.id.btnSave);
        imgSelectedPhoto = findViewById(R.id.imgSelectedPhoto);
        tvSelectedPhotoName = findViewById(R.id.tvSelectedPhotoName);

        dogNames = new ArrayList<>();
        dogSpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dogNames);
        dogSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDogs.setAdapter(dogSpinnerAdapter);
        spinnerDogs.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateDogMode();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                updateDogMode();
            }
        });

        updateFoodValue(seekFood.getProgress());
        updatePoopValue(seekPoop.getProgress());
        updateDogMode();
        loadDogsFromServer();

        rgDogMode.setOnCheckedChangeListener((group, checkedId) -> updateDogMode());

        seekFood.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateFoodValue(progress);
            }
        });

        seekPoop.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updatePoopValue(progress);
            }
        });

        btnAddPhoto.setOnClickListener(v -> openPhotoPicker());
        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveDiary());
    }

    private void loadDogsFromServer() {
        String url;
        try {
            url = ServerConfig.endpoint("GetDogList.jsp") + "?fosterId=" + URLEncoder.encode(currentUserID, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Toast.makeText(this, "로그인 정보를 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    dogNames.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject dog = response.getJSONObject(i);
                            String name = dog.optString("name");
                            if (!TextUtils.isEmpty(name) && !dogNames.contains(name)) {
                                dogNames.add(name);
                            }
                        } catch (JSONException e) {
                            Toast.makeText(this, "강아지 목록을 읽는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    dogSpinnerAdapter.notifyDataSetChanged();
                    if (dogNames.isEmpty()) {
                        rgDogMode.check(R.id.rbNewDog);
                    }
                    selectInitialDog();
                },
                error -> Toast.makeText(this, "강아지 목록을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show());
        request.setShouldCache(false);
        Volley.newRequestQueue(this).add(request);
    }

    private void selectInitialDog() {
        if (TextUtils.isEmpty(initialDogName)) {
            updateDogMode();
            return;
        }

        int index = dogNames.indexOf(initialDogName);
        if (index >= 0) {
            spinnerDogs.setSelection(index);
        }
        updateDogMode();
    }

    private void updateDogMode() {
        boolean newDogMode = isNewDogMode();
        spinnerDogs.setVisibility(newDogMode ? View.GONE : View.VISIBLE);
        layoutNewDog.setVisibility(newDogMode ? View.VISIBLE : View.GONE);

        if (newDogMode) {
            tvWriteTitle.setText("새 임시보호 강아지 일지 작성");
            return;
        }

        String selectedDogName = getSelectedDogName();
        if (TextUtils.isEmpty(selectedDogName)) {
            tvWriteTitle.setText("일지 작성");
        } else {
            tvWriteTitle.setText(selectedDogName + " 일지 작성");
        }
    }

    private boolean isNewDogMode() {
        return rgDogMode.getCheckedRadioButtonId() == R.id.rbNewDog;
    }

    private String getSelectedDogName() {
        Object selectedItem = spinnerDogs.getSelectedItem();
        return selectedItem == null ? "" : selectedItem.toString();
    }

    private void openPhotoPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        photoPickerLauncher.launch(intent);
    }

    private void saveDiary() {
        String content = editDiaryContent.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(this, "일지 내용을 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        String photoData;
        try {
            photoData = encodeSelectedPhoto();
        } catch (IOException e) {
            Toast.makeText(this, "사진을 읽을 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);
        if (isNewDogMode()) {
            saveNewDogThenDiary(content, photoData);
            return;
        }

        String dogName = getSelectedDogName();
        if (TextUtils.isEmpty(dogName)) {
            btnSave.setEnabled(true);
            Toast.makeText(this, "일지를 작성할 강아지를 선택하세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        saveDiaryToServer(dogName, content, photoData);
    }

    private void saveNewDogThenDiary(String content, String photoData) {
        String newDogName = editNewDogName.getText().toString().trim();
        if (newDogName.isEmpty()) {
            btnSave.setEnabled(true);
            Toast.makeText(this, "새 강아지 이름을 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        String breed = editNewDogBreed.getText().toString().trim();
        String ageText = editNewDogAge.getText().toString().trim();
        int age = 0;
        if (!TextUtils.isEmpty(ageText)) {
            try {
                age = Integer.parseInt(ageText);
            } catch (NumberFormatException e) {
                btnSave.setEnabled(true);
                Toast.makeText(this, "나이는 숫자로 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        final int dogAge = age;

        StringRequest request = new StringRequest(
                Request.Method.POST,
                ServerConfig.endpoint("SaveDog.jsp"),
                response -> {
                    if ("success".equals(response.trim())) {
                        saveDiaryToServer(newDogName, content, photoData);
                    } else {
                        btnSave.setEnabled(true);
                        Toast.makeText(this, "새 강아지 등록에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "서버 연결을 확인하세요.", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("name", newDogName);
                params.put("breed", TextUtils.isEmpty(breed) ? "견종 미상" : breed);
                params.put("age", String.valueOf(dogAge));
                params.put("status", "임시보호중");
                params.put("fosterId", currentUserID);
                return params;
            }

            @Override
            public String getBodyContentType() {
                return UTF8_FORM_CONTENT_TYPE;
            }
        };
        request.setShouldCache(false);
        request.setRetryPolicy(new DefaultRetryPolicy(
                PHOTO_UPLOAD_TIMEOUT_MS,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(this).add(request);
    }

    private void saveDiaryToServer(String dogName, String content, String photoData) {
        String dateText = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREA).format(new Date());
        StringRequest request = new StringRequest(
                Request.Method.POST,
                ServerConfig.endpoint("SaveDiary.jsp"),
                response -> {
                    btnSave.setEnabled(true);
                    if ("success".equals(response.trim())) {
                        Toast.makeText(this, "일지가 DB에 저장되었습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "일지 저장에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "서버 연결을 확인하세요.", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("dogName", dogName);
                params.put("fosterId", currentUserID);
                params.put("dateText", dateText);
                params.put("foodAmount", String.valueOf(seekFood.getProgress()));
                params.put("poopCount", String.valueOf(seekPoop.getProgress()));
                params.put("content", content);
                params.put("photoData", photoData);
                return params;
            }

            @Override
            public String getBodyContentType() {
                return UTF8_FORM_CONTENT_TYPE;
            }
        };
        request.setShouldCache(false);
        request.setRetryPolicy(new DefaultRetryPolicy(
                PHOTO_UPLOAD_TIMEOUT_MS,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(this).add(request);
    }

    private void updateFoodValue(int progress) {
        tvFoodValue.setText(progress + "g");
    }

    private void updatePoopValue(int progress) {
        tvPoopValue.setText(progress + "회");
    }

    private String getDisplayName(Uri uri) {
        try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex >= 0) {
                    return cursor.getString(nameIndex);
                }
            }
        }
        return "사진이 첨부되었습니다.";
    }

    private String encodeSelectedPhoto() throws IOException {
        if (selectedPhotoUri == null) {
            return "";
        }

        Bitmap originalBitmap = decodeSampledBitmap(selectedPhotoUri);
        if (originalBitmap == null) {
            return "";
        }

        Bitmap bitmap = resizeBitmap(originalBitmap);
        try {
            return Base64.encodeToString(compressPhoto(bitmap), Base64.NO_WRAP);
        } finally {
            if (bitmap != originalBitmap) {
                bitmap.recycle();
            }
            originalBitmap.recycle();
        }
    }

    private Bitmap decodeSampledBitmap(Uri uri) throws IOException {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            if (inputStream == null) {
                return null;
            }
            BitmapFactory.decodeStream(inputStream, null, bounds);
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        int sampleSize = 1;
        while (bounds.outWidth / sampleSize > MAX_PHOTO_SIZE * 2
                || bounds.outHeight / sampleSize > MAX_PHOTO_SIZE * 2) {
            sampleSize *= 2;
        }
        options.inSampleSize = sampleSize;

        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            return inputStream == null ? null : BitmapFactory.decodeStream(inputStream, null, options);
        }
    }

    private byte[] compressPhoto(Bitmap bitmap) {
        Bitmap workingBitmap = bitmap;
        int quality = 85;

        try {
            while (true) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                workingBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
                byte[] bytes = outputStream.toByteArray();
                if (bytes.length <= MAX_PHOTO_BYTES) {
                    return bytes;
                }

                if (quality > 50) {
                    quality -= 10;
                    continue;
                }

                if (Math.max(workingBitmap.getWidth(), workingBitmap.getHeight()) <= 320) {
                    return bytes;
                }

                int resizedWidth = Math.max(1, Math.round(workingBitmap.getWidth() * 0.8f));
                int resizedHeight = Math.max(1, Math.round(workingBitmap.getHeight() * 0.8f));
                if (resizedWidth == workingBitmap.getWidth() && resizedHeight == workingBitmap.getHeight()) {
                    return bytes;
                }

                Bitmap smallerBitmap = Bitmap.createScaledBitmap(
                        workingBitmap,
                        resizedWidth,
                        resizedHeight,
                        true);
                if (workingBitmap != bitmap) {
                    workingBitmap.recycle();
                }
                workingBitmap = smallerBitmap;
                quality = 80;
            }
        } finally {
            if (workingBitmap != bitmap) {
                workingBitmap.recycle();
            }
        }
    }

    private Bitmap resizeBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int longSide = Math.max(width, height);
        if (longSide <= MAX_PHOTO_SIZE) {
            return bitmap;
        }

        float ratio = (float) MAX_PHOTO_SIZE / longSide;
        int resizedWidth = Math.round(width * ratio);
        int resizedHeight = Math.round(height * ratio);
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, true);
    }

    private abstract static class SimpleSeekBarListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }
}
