package com.anshu.antakshari;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

public class MainActivity2 extends AppCompatActivity {
    EditText nameInput;
    ImageView profilePic;
    Button googleSignIn;
    SignInClient oneTapClient;
    BeginSignInRequest signInRequest;
    private FirebaseAuth mAuth;
    SharedPreferences sh;
    SharedPreferences.Editor editor;
    private EditText etEmail, etPassword;
    private Button btnSignIn;
    private FirebaseFirestore db;
    private TextView tvForgotPassword,btnSignUp;
    ImageView ivShowHidePassword;

    ActivityResultLauncher<Intent> activityResultLauncherImage;
    FirebaseUser currentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignUp = findViewById(R.id.tvSignUp);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        db = FirebaseFirestore.getInstance();
        ivShowHidePassword = findViewById(R.id.ivShowHidePassword);
        ivShowHidePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility(etPassword, ivShowHidePassword);
            }
        });
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInUser();
            }
        });
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect to SignUpActivity
                Intent intent = new Intent(MainActivity2.this, SignUpActivity.class);
                startActivity(intent);
                finish();
            }
        });

        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });
        mAuth = FirebaseAuth.getInstance();
        sh= getSharedPreferences("digANT", MODE_PRIVATE);
        editor= sh.edit();

        mAuth = FirebaseAuth.getInstance();
        Objects.requireNonNull(getSupportActionBar()).hide();
        oneTapClient = Identity.getSignInClient(this);
        signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(getString(R.string.web_client_id))
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                .setAutoSelectEnabled(true)
                .build();

        ActivityResultLauncher<IntentSenderRequest> activityResultLauncher =
        registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {
        if (o.getResultCode() == Activity.RESULT_OK) {
            try {
                SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(o.getData());
                String idToken = credential.getGoogleIdToken();
                if (idToken != null) {
                    String emailId = credential.getId(); //getting email id
                    Toast.makeText(MainActivity2.this,"Signed in with "+emailId, Toast.LENGTH_LONG).show(); //displaying the email id
                    editor.putString("email",emailId);
                    editor.apply();
                    AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
                    mAuth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener(MainActivity2.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "signInWithCredential:success");
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        assert user != null;

                                        String[] msg1 = Objects.requireNonNull(user.getDisplayName()).split(" ");
                                        //Toast.makeText(MainActivity2.this,"Full Name : "+user.getDisplayName()+" First:"+msg1[0],Toast.LENGTH_LONG).show();
                                        String msg = Character.toUpperCase(msg1[0].charAt(0)) + msg1[0].substring(1);
                                        //writing data to shared preferences
                                        SharedPreferences sh = getSharedPreferences("digANT", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sh.edit();
                                        editor.putString("Name", msg); //storing first name in shared prefs
                                        editor.putBoolean("Logged", true);
                                        editor.putBoolean("GoogleSignIn",true);// logged status set to true in shared prefs
                                        editor.putString("FullName", user.getDisplayName()); //storing full name in shared prefs
                                        editor.putInt("totalGamesPlayed", 1);
                                        editor.putInt("totalWins", 0);
                                        // editor.putInt("winPercentage",0);

                                        editor.putInt("highestScore", 0);
                                        //editor.putInt("avgScore",0);
                                        editor.putInt("totalWordsPlayed", 1);
                                        //editor.putInt("avgWordLength",0);
                                        editor.putInt("fastestResponseTime", 30);
                                        editor.putInt("timeSpentPlaying", 0);
                                        editor.putBoolean("firstPlay", true);
                                        editor.apply();
                                        //profilePic.setImageURI(user.getPhotoUrl());

                                        startActivity(new Intent(MainActivity2.this,GameMode.class));
                                    } else {
                                        Log.w(TAG, "signInWithCredential:failure", task.getException());

                                    }
                                }
                            });
                }
            } catch (ApiException e) {
                Toast.makeText(MainActivity2.this,"Sign in not possible", Toast.LENGTH_LONG).show();
            }

        }
                    }

                });
        googleSignIn = findViewById(R.id.googleSignIn);
        googleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oneTapClient.beginSignIn(signInRequest)
                        .addOnSuccessListener(MainActivity2.this, new OnSuccessListener<BeginSignInResult>() {
                            @Override
                            public void onSuccess(BeginSignInResult result) {
                                IntentSenderRequest intentSenderRequest =
                                        new IntentSenderRequest.Builder(result.getPendingIntent().getIntentSender()).build();
                                activityResultLauncher.launch(intentSenderRequest);
                            }
                        })
                        .addOnFailureListener(MainActivity2.this, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // No saved credentials found. Launch the One Tap sign-up flow, or
                                // do nothing and continue presenting the signed-out UI.
                               // Toast.makeText(MainActivity2.this,"OnFailureCalled",Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });


    }

    public void login(View V) {
        try {
            String[] msg1 = nameInput.getText().toString().split(" ");
            String msg = Character.toUpperCase(msg1[0].charAt(0)) + msg1[0].substring(1);


            Intent i = new Intent(MainActivity2.this, GameMode.class);
            startActivity(i);
            finish();
        } catch (NullPointerException nul) {
            Toast.makeText(getApplicationContext(), "Please enter your name!", Toast.LENGTH_SHORT).show();
        }

    }
    private void togglePasswordVisibility(EditText editText, ImageView imageView) {
        if (editText.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())) {
            editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            imageView.setImageResource(R.drawable.ic_eye_off);
        } else {
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            imageView.setImageResource(R.drawable.ic_eye);
        }
        editText.setSelection(editText.getText().length());
    }

   @SuppressLint("IntentReset")
    public void selectDP(View v) {
        Intent imagePickerIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerIntent.setType("image/*");
        //activityResultLauncherImage.launch(imagePickerIntent);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        boolean isGoogleSign=sh.getBoolean("GoogleSignIn",false);
        boolean isLoggedIn=sh.getBoolean("Logged",false);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(isLoggedIn || (isGoogleSign && currentUser!=null))
        {
            finish();
            Intent intent=new Intent(MainActivity2.this,GameMode.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }
    private void signInUser() {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(MainActivity2.this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this,  task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(MainActivity2.this, "Sign in successful", Toast.LENGTH_SHORT).show();
                        // Redirect to main activity or dashboard
                        SharedPreferences sharedPreferences = getSharedPreferences("digANT",MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        db.collection("users").document(Objects.requireNonNull(mAuth.getUid())).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful())
                                {
                                    String name = task.getResult().getString("name");

                                    editor.putString("FullName", name);
                                    String firstName = name.split(" ")[0];
                                    editor.putString("Name",firstName);
                                }
                            }
                        });
                        editor.putBoolean("Logged", true);

                        //totalWins,winPercentage,totalPoints,highestScore,avgScore,totalWordsPlayed,avgWordLength,fastestResponseTime,timeSpentPlaying;

                        editor.putInt("totalGamesPlayed", 1);
                        editor.putInt("totalWins", 1);
                        editor.putInt("highestScore", 0);
                        editor.putInt("totalWordsPlayed", 1);
                        editor.putInt("fastestResponseTime", 30);
                        editor.putInt("timeSpentPlaying", 0);
                        editor.putBoolean("firstPlay", true);
                        editor.apply();

                        startActivity(new Intent(this, GameMode.class));
                        finish();
                    } else {
                        Toast.makeText(MainActivity2.this, "Sign in failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void resetPassword() {
        String email = etEmail.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(MainActivity2.this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity2.this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity2.this, "Failed to send reset email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}

