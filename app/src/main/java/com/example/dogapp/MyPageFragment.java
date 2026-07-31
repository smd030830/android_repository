package com.example.dogapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MyPageFragment extends Fragment {
    private static final String UTF8_FORM_CONTENT_TYPE =
            "application/x-www-form-urlencoded; charset=UTF-8";

    private String userID;
    private String userType;
    private ArrayList<ContactMessage> messages;
    private ArrayList<ContactMessage> savedMessages;
    private ArrayList<ContactMessage> realtimeChats;
    private ContactMessageAdapter adapter;
    private TextView tvMyPageUser;
    private TextView tvMessageSectionTitle;
    private TextView tvEmptyMessages;
    private DatabaseReference chatRoomsRef;
    private ValueEventListener chatRoomsListener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREA);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_page, container, false);

        tvMyPageUser = view.findViewById(R.id.tvMyPageUser);
        tvMessageSectionTitle = view.findViewById(R.id.tvMessageSectionTitle);
        tvEmptyMessages = view.findViewById(R.id.tvEmptyMessages);
        Button btnLogout = view.findViewById(R.id.btnLogout);
        RecyclerView recyclerViewMessages = view.findViewById(R.id.recyclerViewMessages);

        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        userID = prefs.getString("userID", "");
        userType = prefs.getString("userType", "");

        if (TextUtils.isEmpty(userID)) {
            Toast.makeText(getActivity(), "로그인 후 마이페이지를 사용할 수 있습니다.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getActivity(), LoginActivity.class));
        }

        messages = new ArrayList<>();
        savedMessages = new ArrayList<>();
        realtimeChats = new ArrayList<>();
        adapter = new ContactMessageAdapter(messages, isFoster(), this::showReplyDialog, this::openChat);
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewMessages.setAdapter(adapter);

        btnLogout.setOnClickListener(v -> logout());
        applyHeader();
        loadMessages();
        listenRealtimeChats();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (chatRoomsRef != null && chatRoomsListener != null) {
            chatRoomsRef.removeEventListener(chatRoomsListener);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            loadMessages();
        }
    }

    private boolean isFoster() {
        return "foster".equals(userType);
    }

    private void applyHeader() {
        String userTypeText = isFoster() ? "임시보호자" : "입양희망자";
        tvMyPageUser.setText("아이디: " + userID + "\n회원 구분: " + userTypeText);
        tvMessageSectionTitle.setText(isFoster() ? "받은 문의와 실시간 대화" : "보낸 문의와 실시간 대화");
        tvEmptyMessages.setText(isFoster() ? "아직 받은 문의나 대화가 없습니다." : "아직 보낸 문의나 대화가 없습니다.");
    }

    private void loadMessages() {
        if (TextUtils.isEmpty(userID)) {
            updateEmptyState();
            return;
        }

        String url;
        try {
            String encodedUserId = URLEncoder.encode(userID, "UTF-8");
            if (isFoster()) {
                url = ServerConfig.endpoint("GetReceivedMessages.jsp") + "?receiverId=" + encodedUserId;
            } else {
                url = ServerConfig.endpoint("GetSentMessages.jsp") + "?senderId=" + encodedUserId;
            }
        } catch (UnsupportedEncodingException e) {
            Toast.makeText(getActivity(), "로그인 정보를 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    savedMessages.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject item = response.getJSONObject(i);
                            savedMessages.add(new ContactMessage(
                                    item.optInt("id"),
                                    item.optString("senderId"),
                                    item.optString("receiverId"),
                                    item.optString("dogName"),
                                    item.optString("title"),
                                    item.optString("content"),
                                    item.optString("replyContent"),
                                    item.optString("createdAt"),
                                    item.optString("repliedAt")));
                        } catch (JSONException e) {
                            Toast.makeText(getActivity(), "메시지를 읽는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    mergeMessageLists();
                },
                error -> Toast.makeText(getActivity(), "메시지를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show());
        request.setShouldCache(false);
        Volley.newRequestQueue(requireActivity()).add(request);
    }

    private void listenRealtimeChats() {
        if (TextUtils.isEmpty(userID)) {
            return;
        }

        try {
            if (FirebaseApp.getApps(requireContext()).isEmpty()) {
                FirebaseApp app = FirebaseApp.initializeApp(requireContext());
                if (app == null) {
                    return;
                }
            }
            chatRoomsRef = FirebaseDatabase.getInstance().getReference("doglog_chats");
        } catch (Exception e) {
            return;
        }

        chatRoomsListener = chatRoomsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                realtimeChats.clear();
                for (DataSnapshot roomSnapshot : snapshot.getChildren()) {
                    ContactMessage chatSummary = buildChatSummary(roomSnapshot);
                    if (chatSummary != null) {
                        realtimeChats.add(chatSummary);
                    }
                }
                mergeMessageLists();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "실시간 대화 목록을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private ContactMessage buildChatSummary(DataSnapshot roomSnapshot) {
        String participantA = roomSnapshot.child("participantA").getValue(String.class);
        String participantB = roomSnapshot.child("participantB").getValue(String.class);
        if (!userID.equals(participantA) && !userID.equals(participantB)) {
            return null;
        }

        String partnerId = userID.equals(participantA) ? participantB : participantA;
        if (TextUtils.isEmpty(partnerId)) {
            return null;
        }

        String dogName = roomSnapshot.child("dogName").getValue(String.class);
        String lastMessage = roomSnapshot.child("lastMessage").getValue(String.class);
        Long updatedAt = roomSnapshot.child("updatedAt").getValue(Long.class);
        String createdAt = updatedAt == null ? "" : dateFormat.format(new Date(updatedAt));
        String content = TextUtils.isEmpty(lastMessage) ? "대화방이 생성되었습니다." : lastMessage;

        String senderId = isFoster() ? partnerId : userID;
        String receiverId = isFoster() ? userID : partnerId;
        return new ContactMessage(
                -1,
                senderId,
                receiverId,
                TextUtils.isEmpty(dogName) ? "강아지" : dogName,
                "실시간 대화",
                content,
                "",
                createdAt,
                "");
    }

    private void mergeMessageLists() {
        messages.clear();
        messages.addAll(realtimeChats);
        messages.addAll(savedMessages);
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void showReplyDialog(ContactMessage message) {
        EditText editReply = new EditText(requireContext());
        editReply.setMinLines(4);
        editReply.setGravity(android.view.Gravity.TOP);
        editReply.setHint("답장 내용을 입력하세요.");
        if (!TextUtils.isEmpty(message.getReplyContent())) {
            editReply.setText(message.getReplyContent());
        }

        int padding = Math.round(16 * getResources().getDisplayMetrics().density);
        editReply.setPadding(padding, padding, padding, padding);

        new AlertDialog.Builder(requireContext())
                .setTitle(message.getSenderId() + "님에게 답장")
                .setView(editReply)
                .setNegativeButton("취소", null)
                .setPositiveButton("보내기", (dialog, which) ->
                        sendReply(message, editReply.getText().toString().trim()))
                .show();
    }

    private void sendReply(ContactMessage message, String replyContent) {
        if (TextUtils.isEmpty(replyContent)) {
            Toast.makeText(getActivity(), "답장 내용을 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest request = new StringRequest(
                Request.Method.POST,
                ServerConfig.endpoint("ReplyMessage.jsp"),
                response -> {
                    if ("success".equals(response.trim())) {
                        Toast.makeText(getActivity(), "답장을 보냈습니다.", Toast.LENGTH_SHORT).show();
                        loadMessages();
                    } else {
                        Toast.makeText(getActivity(), "답장 저장에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getActivity(), "서버 연결을 확인하세요.", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(message.getId()));
                params.put("receiverId", userID);
                params.put("replyContent", replyContent);
                return params;
            }

            @Override
            public String getBodyContentType() {
                return UTF8_FORM_CONTENT_TYPE;
            }
        };
        request.setShouldCache(false);
        Volley.newRequestQueue(requireActivity()).add(request);
    }

    private void openChat(ContactMessage message) {
        String recipientId = isFoster() ? message.getSenderId() : message.getReceiverId();
        if (TextUtils.isEmpty(recipientId)) {
            Toast.makeText(getActivity(), "대화 상대 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(getActivity(), MessageActivity.class);
        intent.putExtra("recipientId", recipientId);
        intent.putExtra("dogName", message.getDogName());
        startActivity(intent);
    }

    private void logout() {
        requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
        Toast.makeText(getActivity(), "로그아웃되었습니다.", Toast.LENGTH_SHORT).show();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).openHome();
        }
    }

    private void updateEmptyState() {
        tvEmptyMessages.setVisibility(messages.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
