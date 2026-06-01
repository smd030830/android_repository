package com.example.dogapp;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 카드를 누르면 강아지 이름이나 ID를 담아서 상세 화면으로 이동
                Intent intent = new Intent(v.getContext(), DogDetailActivity.class);
                intent.putExtra("dogName", currentDog.getName());
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dogList.size();
    }

    public static class DogViewHolder extends RecyclerView.ViewHolder {
        TextView tvDogName, tvDogInfo, tvProtectionDays;

        public DogViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDogName = itemView.findViewById(R.id.tvDogName);
            tvDogInfo = itemView.findViewById(R.id.tvDogInfo);
            tvProtectionDays = itemView.findViewById(R.id.tvProtectionDays);
        }
    }
}