package com.example.dogapp;

import android.content.Intent;
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

public class DogAdapter extends RecyclerView.Adapter<DogAdapter.DogViewHolder> {

    private ArrayList<Dog> dogList;

    public DogAdapter(ArrayList<Dog> dogList) {
        this.dogList = dogList;
    }

    @NonNull
    @Override
    public DogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_dogcard, parent, false);
        return new DogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DogViewHolder holder, int position) {
        Dog currentDog = dogList.get(position);
        holder.tvDogName.setText(currentDog.getName());
        holder.tvDogInfo.setText(currentDog.getInfo());
        holder.tvProtectionDays.setText(currentDog.getDays());
        setPhoto(holder.imgDog, currentDog.getPhotoData());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), DogDetailActivity.class);
                intent.putExtra("dogName", currentDog.getName());
                intent.putExtra("breed", currentDog.getBreed());
                intent.putExtra("age", currentDog.getAge());
                intent.putExtra("status", currentDog.getStatus());
                intent.putExtra("fosterId", currentDog.getFosterId());
                intent.putExtra("photoData", currentDog.getPhotoData());
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dogList.size();
    }

    public static class DogViewHolder extends RecyclerView.ViewHolder {
        ImageView imgDog;
        TextView tvDogName, tvDogInfo, tvProtectionDays;

        public DogViewHolder(@NonNull View itemView) {
            super(itemView);
            imgDog = itemView.findViewById(R.id.imgDog);
            tvDogName = itemView.findViewById(R.id.tvDogName);
            tvDogInfo = itemView.findViewById(R.id.tvDogInfo);
            tvProtectionDays = itemView.findViewById(R.id.tvProtectionDays);
        }
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
}
