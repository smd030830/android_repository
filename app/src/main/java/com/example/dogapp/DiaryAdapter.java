package com.example.dogapp;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DiaryAdapter extends RecyclerView.Adapter<DiaryAdapter.DiaryViewHolder> {
    private final ArrayList<DiaryEntry> diaries;

    public DiaryAdapter(ArrayList<DiaryEntry> diaries) {
        this.diaries = diaries;
    }

    @NonNull
    @Override
    public DiaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_diary, parent, false);
        return new DiaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiaryViewHolder holder, int position) {
        DiaryEntry diary = diaries.get(position);
        holder.tvDiaryDate.setText(diary.getDateText());
        holder.tvDiaryAmounts.setText("사료 " + diary.getFoodAmount() + "g · 배변 " + diary.getPoopCount() + "회");
        holder.tvDiaryContent.setText(diary.getContent());

        setPhoto(holder.imgDiaryPhoto, diary.getPhotoData());

        holder.itemView.setOnClickListener(v -> showDiaryDetail(holder, diary));
    }

    private void setPhoto(ImageView imageView, String photoData) {
        if (TextUtils.isEmpty(photoData)) {
            imageView.setImageDrawable(null);
            imageView.setVisibility(View.GONE);
            return;
        }

        try {
            byte[] imageBytes = Base64.decode(photoData, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            if (bitmap == null) {
                imageView.setImageDrawable(null);
                imageView.setVisibility(View.GONE);
                return;
            }
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageBitmap(bitmap);
        } catch (IllegalArgumentException e) {
            imageView.setImageDrawable(null);
            imageView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return diaries.size();
    }

    private void showDiaryDetail(DiaryViewHolder holder, DiaryEntry diary) {
        View detailView = LayoutInflater.from(holder.itemView.getContext()).inflate(R.layout.dialog_diary_detail, null);
        TextView tvDetailDate = detailView.findViewById(R.id.tvDetailDate);
        TextView tvDetailAmounts = detailView.findViewById(R.id.tvDetailAmounts);
        TextView tvDetailContent = detailView.findViewById(R.id.tvDetailContent);
        ImageView imgDetailPhoto = detailView.findViewById(R.id.imgDetailPhoto);

        tvDetailDate.setText(diary.getDateText());
        tvDetailAmounts.setText("사료량: " + diary.getFoodAmount() + "g\n배변 횟수: " + diary.getPoopCount() + "회");
        tvDetailContent.setText(diary.getContent());

        setPhoto(imgDetailPhoto, diary.getPhotoData());

        new AlertDialog.Builder(holder.itemView.getContext())
                .setTitle(diary.getDogName() + " 일지")
                .setView(detailView)
                .setPositiveButton("확인", null)
                .show();
    }

    public static class DiaryViewHolder extends RecyclerView.ViewHolder {
        ImageView imgDiaryPhoto;
        TextView tvDiaryDate, tvDiaryAmounts, tvDiaryContent;

        public DiaryViewHolder(@NonNull View itemView) {
            super(itemView);
            imgDiaryPhoto = itemView.findViewById(R.id.imgDiaryPhoto);
            tvDiaryDate = itemView.findViewById(R.id.tvDiaryDate);
            tvDiaryAmounts = itemView.findViewById(R.id.tvDiaryAmounts);
            tvDiaryContent = itemView.findViewById(R.id.tvDiaryContent);
        }
    }
}
