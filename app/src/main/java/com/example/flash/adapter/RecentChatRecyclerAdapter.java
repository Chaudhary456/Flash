package com.example.flash.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flash.ChatActivity;
import com.example.flash.R;
import com.example.flash.model.ChatRoomModel;
import com.example.flash.model.UserModel;
import com.example.flash.utils.AndroidUtil;
import com.example.flash.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class RecentChatRecyclerAdapter extends FirestoreRecyclerAdapter<ChatRoomModel,RecentChatRecyclerAdapter.ChatRoomModelViewHolder> {

    Context context;


    public RecentChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatRoomModel> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatRoomModelViewHolder holder, int position, @NonNull ChatRoomModel model) {
        FirebaseUtil.getOtherUserFromChatroom(model.getUserIds())
                .get().addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        UserModel otherUserModel = task.getResult().toObject(UserModel.class);

                        Log.d("FIRE_BASE_USERNAME",otherUserModel.getUsername());

                        if(otherUserModel.getUserId().equals(FirebaseUtil.currentUserId())){
                            holder.usernameText.setText(otherUserModel.getUsername()+" (Me)");
                        }else {
                            holder.usernameText.setText(otherUserModel.getUsername());
                        }
                        if(model.getLastMessage()!=null) {
                            boolean lastMessageSentByMe = model.getLastMessageSenderId().equals(FirebaseUtil.currentUserId());
                            if(lastMessageSentByMe){
                                holder.lastMessageText.setText("You : "+model.getLastMessage());
                            }else{
                                holder.lastMessageText.setText(model.getLastMessage());
                            }
                        }
                        if(model.getLastMessageTimestamp()!=null){
                            holder.lastMessageTime.setText(FirebaseUtil.timeStampToString(model.getLastMessageTimestamp()));
                        }

                        holder.itemView.setOnClickListener(v->{

                            Intent intent = new Intent(context, ChatActivity.class);
                            AndroidUtil.passUserModelAsIntent(intent,otherUserModel);
                            intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);

                        });
                    }else{
                        Log.d("FIRE_BASE_MSG","TASK FAILED");
                    }
                });

    }

    @NonNull
    @Override
    public ChatRoomModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recent_chat_recycler_row,parent,false);

        return new ChatRoomModelViewHolder(view);
    }

    class  ChatRoomModelViewHolder extends RecyclerView.ViewHolder{
        TextView usernameText;
        TextView lastMessageText;
        TextView lastMessageTime;
        ImageView profilePic;


        public ChatRoomModelViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.recycler_username);
            lastMessageText = itemView.findViewById(R.id.last_message_text);
            lastMessageTime = itemView.findViewById(R.id.last_message_time_text);
            profilePic = itemView.findViewById(R.id.profile_picture_image_view);
        }
    }
}
