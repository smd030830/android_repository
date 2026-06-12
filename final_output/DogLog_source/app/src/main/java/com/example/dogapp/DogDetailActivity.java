package com.example.dogapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DogDetailActivity extends AppCompatActivity {
    private static final String UTF8_FORM_CONTENT_TYPE =
            "application/x-www-form-urlencoded; charset=UTF-8";
    private static final int MAX_PHOTO_SIZE = 1024;
    private static final int MAX_PHOTO_BYTES = 700 * 1024;
    private static final int PHOTO_UPLOAD_TIMEOUT_MS = 30_000;

    private String dogName;
    private String dogBreed;
    private int dogAge;
    private String dogStatus;
    private String dogFosterId;
    private String dogPhotoData;
    private String currentUserID;
    private String userType;
    private DiaryAdapter diaryAdapter;
    private ArrayList<DiaryEntry> diaries;
    private TextView tvDetailDogName;
    private TextView tvDetailDogInfo;
    private TextView tvEmptyDiaries;
    private ImageView imgDetailDogPhoto;
    private LinearLayout layoutOwnerActions;
    private Button btnGoWrite;
    private Button btnGoMessage;
    private Button btnDeleteDog;

    private final ActivityResultLauncher<String> dogPhotoPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null) {
                    return;
                }

                try {
                    String photoData = encodePhoto(uri);
                    updateDogOnServer(dogBreed, dogAge, dogStatus, photoData);
                } catch (IOException e) {
                    Toast.makeText(this, "사진을 읽을 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dog_detail);

        tvDetailDogName = findViewById(R.id.tvDetailDogName);
        tvDetailDogInfo = findViewById(R.id.tvDetailDogInfo);
        imgDetailDogPhoto = findViewById(R.id.imgDetailDogPhoto);
        layoutOwnerActions = findViewById(R.id.layoutOwnerActions);
        Button btnEditDogInfo = findViewById(R.id.btnEditDogInfo);
        Button btnEditDogPhoto = findViewById(R.id.btnEditDogPhoto);
        btnDeleteDog = findViewById(R.id.btnDeleteDog);
        btnGoWrite = findViewById(R.id.btnGoWrite);
        btnGoMessage = findViewById(R.id.btnGoMessage);
        RecyclerView recyclerViewDiaries = findViewById(R.id.recyclerViewDiaries);
        tvEmptyDiaries = findViewById(R.id.tvEmptyDiaries);

        dogName = getIntent().getStringExtra("dogName");
        if (TextUtils.isEmpty(dogName)) {
            dogName = "강아지";
        }
        dogBreed = getIntent().getStringExtra("breed");
        dogAge = getIntent().getIntExtra("age", 0);
        dogStatus = getIntent().getStringExtra("status");
        dogFosterId = getIntent().getStringExtra("fosterId");
        dogPhotoData = getIntent().getStringExtra("photoData");

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentUserID = prefs.getString("userID", "");
        userType = prefs.getString("userType", "");

        applyDogInfo();

        diaries = new ArrayList<>();
        diaryAdapter = new DiaryAdapter(diaries, currentUserID, this::showDeleteDiaryDialog);
        recyclerViewDiaries.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewDiaries.setAdapter(diaryAdapter);
        updateEmptyState();

        btnEditDogInfo.setOnClickListener(v -> showEditDogInfoDialog());
        btnEditDogPhoto.setOnClickListener(v -> openDogPhotoPicker());
        btnDeleteDog.setOnClickListener(v -> showDeleteDogDialog());

        btnGoWrite.setOnClickListener(v -> {
            Intent intent = new Intent(DogDetailActivity.this, WriteActivity.class);
            intent.putExtra("dogName", dogName);
            startActivity(intent);
        });

        btnGoMessage.setOnClickListener(v -> {
            Intent intent = new Intent(DogDetailActivity.this, MessageActivity.class);
            intent.putExtra("recipientId", dogFosterId);
            intent.putExtra("dogName", dogName);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (diaryAdapter == null) {
            return;
        }
        loadDogInfoFromServer();
        loadDiariesFromServer();
    }

    private void loadDogInfoFromServer() {
        String url;
        try {
            url = ServerConfig.endpoint("GetDogDetail.jsp") + "?name=" + URLEncoder.encode(dogName, "UTF-8");
            if (!TextUtils.isEmpty(dogFosterId)) {
                url += "&fosterId=" + URLEncoder.encode(dogFosterId, "UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
            Toast.makeText(this, "강아지 정보를 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    if (response.length() == 0) {
                        applyDogInfo();
                        return;
                    }

                    try {
                        JSONObject dog = response.getJSONObject(0);
                        dogName = dog.optString("name", dogName);
                        dogBreed = dog.optString("breed", dogBreed);
                        dogAge = dog.optInt("age", dogAge);
                        dogStatus = dog.optString("status", dogStatus);
                        dogFosterId = dog.optString("fosterId", dogFosterId);
                        dogPhotoData = dog.optString("photoData", dogPhotoData);
                        applyDogInfo();
                    } catch (JSONException e) {
                        Toast.makeText(this, "강아지 정보를 읽는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> applyDogInfo());
        request.setShouldCache(false);
        Volley.newRequestQueue(this).add(request);
    }

    private void applyDogInfo() {
        tvDetailDogName.setText(dogName);
        String safeBreed = TextUtils.isEmpty(dogBreed) ? "견종 미상" : dogBreed;
        String safeStatus = TextUtils.isEmpty(dogStatus) ? "상태 미상" : dogStatus;
        tvDetailDogInfo.setText("견종: " + safeBreed + "\n나이: 약 " + dogAge + "살\n상태: " + safeStatus);
        setPhoto(imgDetailDogPhoto, dogPhotoData);

        boolean isOwner = "foster".equals(userType) && !TextUtils.isEmpty(currentUserID) && currentUserID.equals(dogFosterId);
        layoutOwnerActions.setVisibility(isOwner ? View.VISIBLE : View.GONE);
        btnGoWrite.setVisibility(isOwner ? View.VISIBLE : View.GONE);
        btnGoMessage.setVisibility("adopter".equals(userType) ? View.VISIBLE : View.GONE);
    }

    private void showEditDogInfoDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = Math.round(16 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, 0);

        EditText editBreed = new EditText(this);
        editBreed.setHint("견종");
        editBreed.setText(TextUtils.isEmpty(dogBreed) ? "" : dogBreed);
        editBreed.setInputType(InputType.TYPE_CLASS_TEXT);
        layout.addView(editBreed);

        EditText editAge = new EditText(this);
        editAge.setHint("나이");
        editAge.setText(String.valueOf(dogAge));
        editAge.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(editAge);

        EditText editStatus = new EditText(this);
        editStatus.setHint("상태");
        editStatus.setText(TextUtils.isEmpty(dogStatus) ? "" : dogStatus);
        editStatus.setInputType(InputType.TYPE_CLASS_TEXT);
        layout.addView(editStatus);

        new AlertDialog.Builder(this)
                .setTitle("강아지 정보 수정")
                .setView(layout)
                .setNegativeButton("취소", null)
                .setPositiveButton("저장", (dialog, which) -> {
                    int age = 0;
                    String ageText = editAge.getText().toString().trim();
                    if (!ageText.isEmpty()) {
                        try {
                            age = Integer.parseInt(ageText);
                        } catch (NumberFormatException e) {
                            Toast.makeText(this, "나이는 숫자로 입력하세요.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    updateDogOnServer(
                            editBreed.getText().toString().trim(),
                            age,
                            editStatus.getText().toString().trim(),
                            dogPhotoData);
                })
                .show();
    }

    private void openDogPhotoPicker() {
        dogPhotoPickerLauncher.launch("image/*");
    }

    private void showDeleteDogDialog() {
        new AlertDialog.Builder(this)
                .setTitle("강아지 삭제")
                .setMessage("이 강아지와 작성한 일지를 삭제할까요?")
                .setNegativeButton("취소", null)
                .setPositiveButton("삭제", (dialog, which) -> deleteDogFromServer())
                .show();
    }

    private void deleteDogFromServer() {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                ServerConfig.endpoint("DeleteDog.jsp"),
                response -> {
                    if ("success".equals(response.trim())) {
                        Toast.makeText(this, "강아지가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "작성자만 강아지를 삭제할 수 있습니다.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "서버 연결을 확인하세요.", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("name", dogName);
                params.put("fosterId", currentUserID);
                return params;
            }

            @Override
            public String getBodyContentType() {
                return UTF8_FORM_CONTENT_TYPE;
            }
        };
        request.setShouldCache(false);
        Volley.newRequestQueue(this).add(request);
    }

    private void updateDogOnServer(String breed, int age, String status, String photoData) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                ServerConfig.endpoint("UpdateDog.jsp"),
                response -> {
                    if ("success".equals(response.trim())) {
                        dogBreed = TextUtils.isEmpty(breed) ? "견종 미상" : breed;
                        dogAge = age;
                        dogStatus = TextUtils.isEmpty(status) ? "임시보호중" : status;
                        dogPhotoData = photoData;
                        applyDogInfo();
                        Toast.makeText(this, "강아지 정보가 수정되었습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "작성자만 강아지 정보를 수정할 수 있습니다.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "서버 연결을 확인하세요.", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("name", dogName);
                params.put("breed", TextUtils.isEmpty(breed) ? "견종 미상" : breed);
                params.put("age", String.valueOf(age));
                params.put("status", TextUtils.isEmpty(status) ? "임시보호중" : status);
                params.put("fosterId", currentUserID);
                params.put("photoData", photoData == null ? "" : photoData);
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

    private void loadDiariesFromServer() {
        String url;
        try {
            url = ServerConfig.endpoint("GetDiaryList.jsp") + "?dogName=" + URLEncoder.encode(dogName, "UTF-8");
            if ("foster".equals(userType) && !currentUserID.isEmpty()) {
                url += "&fosterId=" + URLEncoder.encode(currentUserID, "UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
            Toast.makeText(this, "강아지 이름을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    diaries.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject item = response.getJSONObject(i);
                            diaries.add(new DiaryEntry(
                                    item.optInt("id"),
                                    item.optString("fosterId", dogFosterId),
                                    item.optString("dogName", dogName),
                                    item.optString("dateText"),
                                    item.optInt("foodAmount"),
                                    item.optInt("poopCount"),
                                    item.optString("content"),
                                    item.optString("photoData")));
                        } catch (JSONException e) {
                            Toast.makeText(this, "일지 정보를 읽는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    diaryAdapter.notifyDataSetChanged();
                    updateEmptyState();
                },
                error -> {
                    diaries.clear();
                    diaryAdapter.notifyDataSetChanged();
                    updateEmptyState();
                    Toast.makeText(this, "서버에서 일지를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                });
        request.setShouldCache(false);
        Volley.newRequestQueue(this).add(request);
    }

    private void showDeleteDiaryDialog(DiaryEntry diary) {
        new AlertDialog.Builder(this)
                .setTitle("일지 삭제")
                .setMessage("이 일지를 삭제할까요?")
                .setNegativeButton("취소", null)
                .setPositiveButton("삭제", (dialog, which) -> deleteDiaryFromServer(diary))
                .show();
    }

    private void deleteDiaryFromServer(DiaryEntry diary) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                ServerConfig.endpoint("DeleteDiary.jsp"),
                response -> {
                    if ("success".equals(response.trim())) {
                        int index = diaries.indexOf(diary);
                        if (index >= 0) {
                            diaries.remove(index);
                            diaryAdapter.notifyItemRemoved(index);
                        } else {
                            loadDiariesFromServer();
                        }
                        updateEmptyState();
                        Toast.makeText(this, "일지가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "작성자만 일지를 삭제할 수 있습니다.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "서버 연결을 확인하세요.", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(diary.getId()));
                params.put("fosterId", currentUserID);
                return params;
            }

            @Override
            public String getBodyContentType() {
                return UTF8_FORM_CONTENT_TYPE;
            }
        };
        request.setShouldCache(false);
        Volley.newRequestQueue(this).add(request);
    }

    private void updateEmptyState() {
        tvEmptyDiaries.setVisibility(diaries.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void setPhoto(ImageView imageView, String photoData) {
        if (TextUtils.isEmpty(photoData)) {
            imageView.setImageDrawable(null);
            imageView.setBackgroundResource(R.color.border_gray);
            return;
        }

        try {
            byte[] imageBytes = Base64.decode(photoData, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            if (bitmap == null) {
                imageView.setImageDrawable(null);
                imageView.setBackgroundResource(R.color.border_gray);
                return;
            }
            imageView.setImageBitmap(bitmap);
        } catch (IllegalArgumentException e) {
            imageView.setImageDrawable(null);
            imageView.setBackgroundResource(R.color.border_gray);
        }
    }

    private String encodePhoto(Uri uri) throws IOException {
        Bitmap originalBitmap = decodeSampledBitmap(uri);
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
                Bitmap smallerBitmap = Bitmap.createScaledBitmap(workingBitmap, resizedWidth, resizedHeight, true);
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
}
