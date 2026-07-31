package com.example.dogapp;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.ChatViewHolder> {
    private final ArrayList<ChatMessage> messages;
    private final String currentUserId;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.KOREA);

    public ChatMessageAdapter(ArrayList<ChatMessage> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId == null ? "" : currentUserId;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        boolean mine = currentUserId.equals(message.getSenderId());
        String sender = TextUtils.isEmpty(message.getSenderId()) ? "알 수 없음" : message.getSenderId();
        String time = message.getSentAt() > 0 ? timeFormat.format(new Date(message.getSentAt())) : "";

        holder.leftGroup.setVisibility(mine ? View.GONE : View.VISIBLE);
        holder.rightGroup.setVisibility(mine ? View.VISIBLE : View.GONE);

        if (mine) {
            holder.tvRightMessage.setText(message.getText());
            holder.tvRightMeta.setText(time);
        } else {
            holder.tvLeftName.setText(sender);
            holder.tvLeftMessage.setText(message.getText());
            holder.tvLeftMeta.setText(time);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        View leftGroup;
        View rightGroup;
        TextView tvLeftName;
        TextView tvLeftMessage;
        TextView tvLeftMeta;
        TextView tvRightMessage;
        TextView tvRightMeta;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            leftGroup = itemView.findViewById(R.id.leftMessageGroup);
            rightGroup = itemView.findViewById(R.id.rightMessageGroup);
            tvLeftName = itemView.findViewById(R.id.tvLeftName);
            tvLeftMessage = itemView.findViewById(R.id.tvLeftMessage);
            tvLeftMeta = itemView.findViewById(R.id.tvLeftMeta);
            tvRightMessage = itemView.findViewById(R.id.tvRightMessage);
            tvRightMeta = itemView.findViewById(R.id.tvRightMeta);
        }
    }
}
