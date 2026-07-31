package com.example.dogapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MessageActivity extends AppCompatActivity {
    private String senderId;
    private String recipientId;
    private String dogName;
    private String chatId;
    private ArrayList<ChatMessage> messages;
    private ChatMessageAdapter adapter;
    private DatabaseReference chatRef;
    private ValueEventListener messageListener;
    private RecyclerView recyclerChatMessages;
    private TextView tvChatStatus;
    private TextView tvChatPartner;
    private EditText editChatMessage;
    private ImageButton btnSendChatMessage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        tvChatPartner = findViewById(R.id.tvChatPartner);
        tvChatStatus = findViewById(R.id.tvChatStatus);
        recyclerChatMessages = findViewById(R.id.recyclerChatMessages);
        editChatMessage = findViewById(R.id.editChatMessage);
        btnSendChatMessage = findViewById(R.id.btnSendChatMessage);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        senderId = prefs.getString("userID", "");
        recipientId = getIntent().getStringExtra("recipientId");
        dogName = getIntent().getStringExtra("dogName");

        if (TextUtils.isEmpty(recipientId)) {
            recipientId = getIntent().getStringExtra("fosterId");
        }
        if (TextUtils.isEmpty(dogName)) {
            dogName = "강아지";
        }

        messages = new ArrayList<>();
        adapter = new ChatMessageAdapter(messages, senderId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerChatMessages.setLayoutManager(layoutManager);
        recyclerChatMessages.setAdapter(adapter);

        tvChatPartner.setText(recipientId + "님과 대화");
        tvChatStatus.setText(dogName + " 입양 문의 채팅");

        btnSendChatMessage.setOnClickListener(v -> sendChatMessage());
        editChatMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendChatMessage();
                return true;
            }
            return false;
        });

        setupFirebaseChat();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatRef != null && messageListener != null) {
            chatRef.child("messages").removeEventListener(messageListener);
        }
    }

    private void setupFirebaseChat() {
        if (TextUtils.isEmpty(senderId)) {
            showUnavailable("로그인 후 실시간 대화를 사용할 수 있습니다.");
            return;
        }
        if (TextUtils.isEmpty(recipientId)) {
            showUnavailable("대화 상대 정보가 없습니다.");
            return;
        }

        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp app = FirebaseApp.initializeApp(this);
                if (app == null) {
                    showUnavailable("Firebase 설정 파일을 찾을 수 없습니다. app/google-services.json을 추가해 주세요.");
                    return;
                }
            }
            chatId = buildChatId(senderId, recipientId, dogName);
            chatRef = FirebaseDatabase.getInstance().getReference("doglog_chats").child(chatId);
        } catch (IllegalStateException e) {
            showUnavailable("Firebase 설정 파일을 찾을 수 없습니다. app/google-services.json을 추가해 주세요.");
            return;
        } catch (Exception e) {
            showUnavailable("Firebase 채팅을 시작할 수 없습니다.");
            return;
        }

        saveChatRoomMeta();
        listenMessages();
    }

    private void saveChatRoomMeta() {
        Map<String, Object> room = new HashMap<>();
        room.put("chatId", chatId);
        room.put("dogName", dogName);
        room.put("participantA", minUser(senderId, recipientId));
        room.put("participantB", maxUser(senderId, recipientId));
        room.put("updatedAt", ServerValue.TIMESTAMP);
        chatRef.updateChildren(room);
    }

    private void listenMessages() {
        tvChatStatus.setText("실시간 연결 중...");
        messageListener = chatRef.child("messages").orderByChild("sentAt")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            ChatMessage message = child.getValue(ChatMessage.class);
                            if (message != null) {
                                messages.add(message);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        if (!messages.isEmpty()) {
                            recyclerChatMessages.scrollToPosition(messages.size() - 1);
                        }
                        tvChatStatus.setText(dogName + " 입양 문의 채팅");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        tvChatStatus.setText("연결 오류");
                        Toast.makeText(MessageActivity.this, "Firebase 연결을 확인하세요.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendChatMessage() {
        String text = editChatMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            return;
        }
        if (chatRef == null) {
            Toast.makeText(this, "Firebase 채팅 설정이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String messageId = chatRef.child("messages").push().getKey();
        if (messageId == null) {
            Toast.makeText(this, "메시지를 만들 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> message = new HashMap<>();
        message.put("id", messageId);
        message.put("senderId", senderId);
        message.put("text", text);
        message.put("sentAt", ServerValue.TIMESTAMP);

        Map<String, Object> roomUpdates = new HashMap<>();
        roomUpdates.put("lastMessage", text);
        roomUpdates.put("lastSenderId", senderId);
        roomUpdates.put("updatedAt", ServerValue.TIMESTAMP);

        chatRef.child("messages").child(messageId).setValue(message)
                .addOnSuccessListener(unused -> {
                    editChatMessage.setText("");
                    chatRef.updateChildren(roomUpdates);
                })
                .addOnFailureListener(error ->
                        Toast.makeText(this, "메시지 전송에 실패했습니다.", Toast.LENGTH_SHORT).show());
    }

    private void showUnavailable(String message) {
        tvChatStatus.setText(message);
        editChatMessage.setEnabled(false);
        btnSendChatMessage.setEnabled(false);
        btnSendChatMessage.setAlpha(0.45f);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private String buildChatId(String firstUser, String secondUser, String dog) {
        String first = minUser(firstUser, secondUser);
        String second = maxUser(firstUser, secondUser);
        return sanitize(dog) + "_" + sanitize(first) + "_" + sanitize(second);
    }

    private String minUser(String firstUser, String secondUser) {
        return firstUser.compareTo(secondUser) <= 0 ? firstUser : secondUser;
    }

    private String maxUser(String firstUser, String secondUser) {
        return firstUser.compareTo(secondUser) <= 0 ? secondUser : firstUser;
    }

    private String sanitize(String value) {
        if (TextUtils.isEmpty(value)) {
            return "unknown";
        }
        return value.toLowerCase(Locale.KOREA).replaceAll("[^가-힣a-z0-9_-]", "_");
    }
}
