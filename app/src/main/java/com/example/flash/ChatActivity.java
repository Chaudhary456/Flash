package com.example.flash;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.flash.adapter.ChatRecyclerAdapter;
import com.example.flash.adapter.RecentChatRecyclerAdapter;
import com.example.flash.adapter.SearchUserRecyclerAdapter;
import com.example.flash.model.ChatMessageModel;
import com.example.flash.model.ChatRoomModel;
import com.example.flash.model.UserModel;
import com.example.flash.utils.AndroidUtil;
import com.example.flash.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;

public class ChatActivity extends AppCompatActivity {
    String chatroomId;
    UserModel otherUser;
    ChatRoomModel chatRoomModel;
    EditText messageInput;
    ImageButton sendMessageBtn;
    ImageButton backBtn;
    TextView otherUserName;
    RecyclerView recyclerView;
    ChatRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageInput = findViewById(R.id.chat_message_input);
        sendMessageBtn = findViewById(R.id.chat_message_send_btn);
        backBtn = findViewById(R.id.chat_back_btn);
        otherUserName = findViewById(R.id.chat_other_username);
        recyclerView = findViewById(R.id.chat_recycler_view);

        otherUser = AndroidUtil.getUserModelFromIntent(getIntent());
        chatroomId = FirebaseUtil.getChatRoomId(FirebaseUtil.currentUserId(),otherUser.getUserId());

        backBtn.setOnClickListener(v -> {
            onBackPressed();
        });
        otherUserName.setText(otherUser.getUsername());

        sendMessageBtn.setOnClickListener(v -> {

            String message =  messageInput.getText().toString().trim();
            if(message.isEmpty()){
                return;
            }
            sendMessageToUser(message);

        });

        getOrCreateChatRoomModel();
        setUpChatRecyclerAdapter();

    }
    private void setUpChatRecyclerAdapter() {
        Query query = FirebaseUtil.getChatMessageReference(chatroomId)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChatMessageModel> options = new FirestoreRecyclerOptions.Builder<ChatMessageModel>()
                .setQuery(query,ChatMessageModel.class).build();

        adapter= new ChatRecyclerAdapter(options,getApplicationContext());
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setAdapter(adapter);
        adapter.startListening();

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                recyclerView.smoothScrollToPosition(0);
            }
        });
    }

    private void sendMessageToUser(String message) {

        chatRoomModel.setLastMessageTimestamp(Timestamp.now());
        chatRoomModel.setLastMessageSenderId(FirebaseUtil.currentUserId());
        chatRoomModel.setLastMessage(message);
        FirebaseUtil.getChatRoomReference(chatroomId).set(chatRoomModel);

        ChatMessageModel chatMessageModel = new ChatMessageModel(message,FirebaseUtil.currentUserId(),Timestamp.now());
        FirebaseUtil.getChatMessageReference(chatroomId).add(chatMessageModel).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if(task.isSuccessful()){
                    messageInput.setText("");
                }
            }
        });
    }

    private void getOrCreateChatRoomModel() {
        FirebaseUtil.getChatRoomReference(chatroomId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    chatRoomModel = task.getResult().toObject(ChatRoomModel.class);
                    if(chatRoomModel == null){
                        chatRoomModel = new ChatRoomModel(
                                chatroomId,
                                Arrays.asList(FirebaseUtil.currentUserId(),otherUser.getUserId()),
                                Timestamp.now(),
                                "",
                                ""
                        );
                        FirebaseUtil.getChatRoomReference(chatroomId).set(chatRoomModel);
                    }
                }
            }
        });
    }


}