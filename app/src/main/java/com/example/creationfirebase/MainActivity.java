package com.example.creationfirebase;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
//import android.support.annotation.Nullable;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.creationfirebase.api.UserHelper;
import com.example.creationfirebase.auth.ProfilActivity;
import com.example.creationfirebase.base.BaseActivity;
import com.example.creationfirebase.databinding.ActivityMainBinding;
import com.example.creationfirebase.mentor_chat.MentorChatActivity;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;

import butterknife.OnClick;

public class MainActivity extends BaseActivity {

    private ActivityMainBinding binding;

    // FOR DATA
    private static final int RC_SIGN_IN = 123;

    @Override
    public int getFragmentLayout() { return R.layout.activity_main; }

   @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       //setContentView(R.layout.activity_main);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

       onClickLoginButton();
       onClickChatButton();
    }

    // ACTION

    private void onClickLoginButton(){
        binding.mainActivityButtonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCurrentUserLogged()){
                    startProfileActivity();
                } else {
                    startSignActivity();
                }
            }
        });
    }

    public void onClickChatButton() {
        binding.mainActivityButtonChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 2 - Check if user is connected before launching MentorActivity
                if (isCurrentUserLogged()){
                    startMentorChatActivity();
                } else {
                    showSnackBar(binding.mainActivityCoordinatorLayout, getString(R.string.error_not_connected));
                }
            }
        });

    }

    // DESIGN

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.handleResponsableAfterSignIn(requestCode, resultCode, data);
    }

    // UI

    private void showSnackBar(CoordinatorLayout coordinatorLayout, String message){
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }

    private void updateUIWhenResuming(){
        this.binding.mainActivityButtonLogin.setText(this.isCurrentUserLogged() ?
                getString(R.string.button_login_text_logged) : getString(R.string.button_login_text_not_logged));
    }

    // UTILS

    private void handleResponsableAfterSignIn(int requestCode, int resultCode, Intent data){
        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (requestCode == RC_SIGN_IN){
            if (resultCode == RESULT_OK){
                this.createUserInFirestore();
                showSnackBar(this.binding.mainActivityCoordinatorLayout,
                        getString(R.string.connection_succeed));
            } else {
                if (response == null){
                    showSnackBar(this.binding.mainActivityCoordinatorLayout,
                            getString(R.string.error_authentication_canceled));
                } else if (response.getErrorCode() == ErrorCodes.NO_NETWORK){
                    showSnackBar(this.binding.mainActivityCoordinatorLayout,
                            getString(R.string.error_no_internet));
                } else if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR){
                    showSnackBar(this.binding.mainActivityCoordinatorLayout,
                            getString(R.string.error_unknown_error));
                }
            }
        }
    }

    //NAVIGATION
    private void startSignActivity(){
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setTheme(R.style.LoginTheme)
                        .setAvailableProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                               new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build()))
                        .setIsSmartLockEnabled(false, true)
                        .setLogo(R.drawable.ic_logo_auth)
                        .build(),
                RC_SIGN_IN);
        //AuthUI.IdpConfig.EmailBuilder().build())
    }

    private void startProfileActivity(){
        Intent intent = new Intent(this, ProfilActivity.class);
        startActivity(intent);
    }

    private void startMentorChatActivity(){
        Intent intent = new Intent(this, MentorChatActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.updateUIWhenResuming();
    }

    // --------------------
    // REST REQUEST
    // --------------------

    private void createUserInFirestore(){

        if (this.getCurrentUser() != null){

            String urlPicture = (this.getCurrentUser().getPhotoUrl() != null) ? this.getCurrentUser().getPhotoUrl().toString() : null;
            String username = this.getCurrentUser().getDisplayName();
            String uid = this.getCurrentUser().getUid();

            UserHelper.createUser(uid, username, urlPicture).addOnFailureListener(this.onFailureListener());
        }
    }




}