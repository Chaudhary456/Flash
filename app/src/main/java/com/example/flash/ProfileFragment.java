package com.example.flash;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.UploadTask;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class ProfileFragment extends Fragment {

    ImageView profilePic;
    EditText profileUsername;
    TextView profilePhone;
    TextView profileLogout;
    Button profileUpdateBtn;
    ProgressBar profileProgressBar;

    UserModel currentUserModel;
    ImageView editUserBtn;

    ActivityResultLauncher<Intent> imagePickLauncher;
    Uri selectedImageUri;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePickLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result->{

                    if(result.getResultCode() == Activity.RESULT_OK){
                        Intent data = result.getData();
                        if(data != null && data.getData()!=null){
                            selectedImageUri = data.getData();
                            AndroidUtil.setProfilePic(getContext(),selectedImageUri,profilePic);
                        }
                    }

                });
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
        editUserBtn = view.findViewById(R.id.edit_user_btn);


        profileUsername.setEnabled(false);
        getUserData();

        editUserBtn.setOnClickListener(v -> {
            profileUsername.setEnabled(true);
        });

        profileUpdateBtn.setOnClickListener(v -> {
            updateBtnClick();
            profileUsername.setEnabled(false);

        });

        profileLogout.setOnClickListener(v -> {

            FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener(task -> {
               if(task.isSuccessful()){

                   FirebaseUtil.logout();
                   Intent intent =  new Intent(getContext(),SplashActivity.class);
                   intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                   startActivity(intent);
               }

            });

        });

        profilePic.setOnClickListener(v -> {
            ImagePicker.with(this).cropSquare().compress(512).maxResultSize(512,512)
                    .createIntent(new Function1<Intent, Unit>() {
                        @Override
                        public Unit invoke(Intent intent) {
                            imagePickLauncher.launch(intent);
                            return null;
                        }
                    });
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

        if(selectedImageUri!=null){
            FirebaseUtil.getCurrentProfilePicReference().putFile(selectedImageUri)
                    .addOnCompleteListener(task -> {
                        updateToFireStore();
                    });
        }else {
            updateToFireStore();
        }
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

        FirebaseUtil.getCurrentProfilePicReference().getDownloadUrl()
                        .addOnCompleteListener(task -> {
                            if(task.isSuccessful()){
                                Uri uri = task.getResult();
                                AndroidUtil.setProfilePic(getContext(),uri,profilePic);
                            }
                        });

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