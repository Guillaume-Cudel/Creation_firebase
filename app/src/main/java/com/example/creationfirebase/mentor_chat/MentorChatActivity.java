package com.example.creationfirebase.mentor_chat;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.creationfirebase.R;
import com.example.creationfirebase.api.MessageHelper;
import com.example.creationfirebase.api.UserHelper;
import com.example.creationfirebase.base.BaseActivity;
import com.example.creationfirebase.databinding.ActivityMentorChatBinding;
import com.example.creationfirebase.models.Message;
import com.example.creationfirebase.models.User;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import butterknife.OnClick;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MentorChatActivity extends BaseActivity implements MentorChatAdapter.Listener {

    public ActivityMentorChatBinding binding;

    private static final String CHAT_NAME_ANDROID = "android";
    private static final String CHAT_NAME_BUG = "bug";
    private static final String CHAT_NAME_FIREBASE = "firebase";
    // 1 - Uri of image selected by user
    private Uri uriImageSelected;

    // 1 - STATIC DATA FOR PICTURE
    private static final String PERMS = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final int RC_IMAGE_PERMS = 100;
    private static final int RC_CHOOSE_PHOTO = 200;

    private MentorChatAdapter mentorChatAdapter;
    @Nullable
    private User modelCurrentUser;
    private String currentChatName;


    @Override
    public int getFragmentLayout() {
        return R.layout.activity_mentor_chat;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        binding = ActivityMentorChatBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        //RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        //binding.activityMentorChatRecyclerView.setLayoutManager(layoutManager);

        this.configureRecyclerView(CHAT_NAME_ANDROID);
        this.configureToolbar();
        this.getCurrentUserFromFirestore();
        onClickSendMessage();
        onClickChatButtonsAndroid();
        onClickChatButtonsFirebase();
        onClickChatButtonsBug();
        onClickAddFile();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //  - Calling the appropriate method after activity result
        this.handleResponse(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

        // --------------------
        // ACTIONS
        // --------------------

        public void onClickSendMessage() {
        binding.activityMentorChatSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1 - Check if text field is not empty and current user properly downloaded from Firestore
                if (!TextUtils.isEmpty(binding.activityMentorChatMessageEditText.getText()) && modelCurrentUser != null){
                    // 2 - Create a new Message to Firestore
                    MessageHelper.createMessageForChat(binding.activityMentorChatMessageEditText.getText().toString(),
                            currentChatName, modelCurrentUser).addOnFailureListener(onFailureListener());
                    // 3 - Reset text field
                    binding.activityMentorChatMessageEditText.setText("");
                }
            }
        });
        }

        public void onClickChatButtonsAndroid() {
        binding.activityMentorChatAndroidChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                configureRecyclerView(CHAT_NAME_ANDROID);
            }
        });
    }

    public void onClickChatButtonsFirebase() {
        binding.activityMentorChatFirebaseChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                configureRecyclerView(CHAT_NAME_FIREBASE);
            }
        });
    }

    public void onClickChatButtonsBug() {
        binding.activityMentorChatBugChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                configureRecyclerView(CHAT_NAME_BUG);
            }
        });
    }

   /* @OnClick(R.id.activity_mentor_chat_add_file_button)
    // 3 - Ask permission when accessing to this listener
    @AfterPermissionGranted(RC_IMAGE_PERMS)
    public void onClickAddFile() {
        this.chooseImageFromPhone();
    }*/
    @AfterPermissionGranted(RC_IMAGE_PERMS)
    public void onClickAddFile() {
        binding.activityMentorChatAddFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               chooseImageFromPhone();
                }

        });
        }

        // --------------------
        // REST REQUESTS
        // --------------------
        private void getCurrentUserFromFirestore(){
            UserHelper.getUser(getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    modelCurrentUser = documentSnapshot.toObject(User.class);
                }
            });
        }

        // --------------------
        // UI
        // --------------------
        private void configureRecyclerView(String chatName){
            //Track current chat name
            this.currentChatName = chatName;
            //Configure Adapter & RecyclerView
            this.mentorChatAdapter = new MentorChatAdapter(generateOptionsForAdapter(MessageHelper.getAllMessageForChat(this.
                    currentChatName)), Glide.with(this), this, this.getCurrentUser().getUid());
            mentorChatAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    binding.activityMentorChatRecyclerView.smoothScrollToPosition(mentorChatAdapter.getItemCount()); // Scroll to bottom on new messages
                }
            });
            binding.activityMentorChatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            binding.activityMentorChatRecyclerView.setAdapter(this.mentorChatAdapter);
        }

        // 6 - Create options for RecyclerView from a Query
        private FirestoreRecyclerOptions<Message> generateOptionsForAdapter(Query query){
            return new FirestoreRecyclerOptions.Builder<Message>()
                    .setQuery(query, Message.class)
                    .setLifecycleOwner(this)
                    .build();
        }

        // --------------------
        // CALLBACK
        // --------------------

        @Override
        public void onDataChanged() {
            // 7 - Show TextView in case RecyclerView is empty
            binding.activityMentorChatTextViewRecyclerViewEmpty
                    .setVisibility(this.mentorChatAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }

    // --------------------
    // FILE MANAGEMENT
    // --------------------

    private void chooseImageFromPhone(){
        if (!EasyPermissions.hasPermissions(this, PERMS)) {
            EasyPermissions.requestPermissions(this, getString(R.string.popup_title_permission_files_access), RC_IMAGE_PERMS, PERMS);
            return;
        }
        // 3 - Launch an "Selection Image" Activity
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RC_CHOOSE_PHOTO);
    }

    // 4 - Handle activity response (after user has chosen or not a picture)
    private void handleResponse(int requestCode, int resultCode, Intent data){
        if (requestCode == RC_CHOOSE_PHOTO) {
            if (resultCode == RESULT_OK) { //SUCCESS
                this.uriImageSelected = data.getData();
                Glide.with(this) //SHOWING PREVIEW OF IMAGE
                        .load(this.uriImageSelected)
                        .apply(RequestOptions.circleCropTransform())
                        .into(binding.activityMentorChatImageChosenPreview);
            } else {
                Toast.makeText(this, getString(R.string.toast_title_no_image_chosen), Toast.LENGTH_SHORT).show();
            }
        }
    }

}
