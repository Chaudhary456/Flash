package com.example.flash;

import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.flash.model.UserModel;
import com.example.flash.utils.AndroidUtil;
import com.example.flash.utils.FirebaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

public class ProfileFragment extends Fragment {

    ImageView profilePic;
    EditText profileUsername;
    EditText profilePhone;
    TextView profileLogout;
    Button profileUpdateBtn;
    ProgressBar profileProgressBar;

    UserModel currentUserModel;
    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profilePic = view.findViewById(R.id.profile_image_view);
        profileUsername = view.findViewById(R.id.profile_username);
        profilePhone = view.findViewById(R.id.profile_phone);
        profileLogout = view.findViewById(R.id.profile_logout);
        profileProgressBar = view.findViewById(R.id.profile_progress_bar);
        profileUpdateBtn = view.findViewById(R.id.profile_update_btn);


        getUserData();

        profileUpdateBtn.setOnClickListener(v -> {
            updateBtnClick();

        });
        profileLogout.setOnClickListener(v -> {
            FirebaseUtil.logout();
            Intent intent =  new Intent(getContext(),SplashActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }

    private void updateBtnClick(){

        String newUsername = profileUsername.getText().toString();
        if(newUsername.isEmpty() || newUsername.length()<3){
            profileUsername.setError("Username length should be at least 3 chars");
            return;
        }
        currentUserModel.setUsername(newUsername);
        updateToFireStore();
    }
    private void updateToFireStore(){

        FirebaseUtil.currentUserDetails().set(currentUserModel)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        AndroidUtil.showToast(getContext(),"Updated Successfully");
                    }else{
                        AndroidUtil.showToast(getContext(),"Update Failed");

                    }
                });
    }

    private void getUserData() {

        setInProgress(true);
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> {
            setInProgress(false);
            currentUserModel = task.getResult().toObject(UserModel.class);
            profileUsername.setText(currentUserModel.getUsername());
            profilePhone.setText(currentUserModel.getPhone());

        });
    }
    private void setInProgress(boolean inProgress){
        if(inProgress){
            profileProgressBar.setVisibility(View.VISIBLE);
        }else{
            profileProgressBar.setVisibility(View.GONE);
        }
    }

}