package com.example.dogapp;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ContactMessageAdapter extends RecyclerView.Adapter<ContactMessageAdapter.MessageViewHolder> {
    public interface ReplyClickListener {
        void onReplyClick(ContactMessage message);
    }

    private final ArrayList<ContactMessage> messages;
    private final boolean receivedMode;
    private final ReplyClickListener replyClickListener;

    public ContactMessageAdapter(
            ArrayList<ContactMessage> messages,
            boolean receivedMode,
            ReplyClickListener replyClickListener) {
        this.messages = messages;
        this.receivedMode = receivedMode;
        this.replyClickListener = replyClickListener;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ContactMessage message = messages.get(position);
        String dogName = TextUtils.isEmpty(message.getDogName()) ? "강아지" : message.getDogName();
        String partner = receivedMode ? message.getSenderId() : message.getReceiverId();
        String partnerLabel = receivedMode ? "보낸 사람" : "받는 사람";

        holder.tvMessageMeta.setText(partnerLabel + ": " + partner + " · " + dogName + " · " + message.getCreatedAt());
        holder.tvMessageTitle.setText(message.getTitle());
        holder.tvMessageContent.setText(message.getContent());

        boolean hasReply = !TextUtils.isEmpty(message.getReplyContent());
        holder.tvMessageReply.setVisibility(hasReply ? View.VISIBLE : View.GONE);
        if (hasReply) {
            holder.tvMessageReply.setText("답장: " + message.getReplyContent());
        }

        holder.btnReplyMessage.setVisibility(receivedMode ? View.VISIBLE : View.GONE);
        holder.btnReplyMessage.setText(hasReply ? "답장 수정" : "답장하기");
        holder.btnReplyMessage.setOnClickListener(v -> {
            if (replyClickListener != null) {
                replyClickListener.onReplyClick(message);
            }
        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageMeta;
        TextView tvMessageTitle;
        TextView tvMessageContent;
        TextView tvMessageReply;
        Button btnReplyMessage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageMeta = itemView.findViewById(R.id.tvMessageMeta);
            tvMessageTitle = itemView.findViewById(R.id.tvMessageTitle);
            tvMessageContent = itemView.findViewById(R.id.tvMessageContent);
            tvMessageReply = itemView.findViewById(R.id.tvMessageReply);
            btnReplyMessage = itemView.findViewById(R.id.btnReplyMessage);
        }
    }
}
