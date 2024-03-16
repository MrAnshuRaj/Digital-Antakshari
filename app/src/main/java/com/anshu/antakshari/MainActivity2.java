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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

    ActivityResultLauncher<Intent> activityResultLauncherImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);
        nameInput = findViewById(R.id.EntryName);
        //profilePic = findViewById(R.id.ProfilePicImageView);

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

        /* code to take image input from gallery
        activityResultLauncherImage =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult o) {
                        if (o.getResultCode() == Activity.RESULT_OK && o.getData() != null) {
                            Bundle data = o.getData().getExtras();
                            assert data != null;
                            Uri selectedImageUri = (Uri) data.get("data");

                            if (null != selectedImageUri) {
                                // update the preview image in the layout
                                profilePic.setImageURI(selectedImageUri);
                                try {
                                    //storing image in bitmap and saving its path to shared preferences
                                    Bitmap selectedImageBitmap = MediaStore.Images.Media.getBitmap(
                                            MainActivity2.this.getContentResolver(),
                                            selectedImageUri);
                                    String path = saveToInternalStorage(selectedImageBitmap); //local method

                                    //saving path in shared preferences
                                    SharedPreferences sh = getSharedPreferences("digANT", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sh.edit();
                                    editor.putString("ProfilePath", path);
                                    editor.apply();

                                } catch (IOException e) {
                                    e.printStackTrace(System.out);
                                }
                            }
                        }
                    }
                });*/

    }

    public void login(View V) {
        try {
            String[] msg1 = nameInput.getText().toString().split(" ");
            String msg = Character.toUpperCase(msg1[0].charAt(0)) + msg1[0].substring(1);



            editor.putString("Name", msg);
            editor.putBoolean("Logged", true);
            editor.putString("FullName", nameInput.getText().toString());
            //totalWins,winPercentage,totalPoints,highestScore,avgScore,totalWordsPlayed,avgWordLength,fastestResponseTime,timeSpentPlaying;

            editor.putInt("totalGamesPlayed", 0);
            editor.putInt("totalWins", 0);
            // editor.putInt("winPercentage",0);
            editor.putInt("totalPoints", 0);
            editor.putInt("highestScore", 0);
            //editor.putInt("avgScore",0);
            editor.putInt("totalWordsPlayed", 0);
            //editor.putInt("avgWordLength",0);
            editor.putInt("fastestResponseTime", 30);
            editor.putInt("timeSpentPlaying", 0);
            editor.putBoolean("firstPlay", true);
            editor.apply();

            Intent i = new Intent(MainActivity2.this, GameMode.class);
            startActivity(i);
            finish();
        } catch (NullPointerException nul) {
            Toast.makeText(getApplicationContext(), "Please enter your name!", Toast.LENGTH_SHORT).show();
        }

    }

   @SuppressLint("IntentReset")
    public void selectDP(View v) {
        Intent imagePickerIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerIntent.setType("image/*");
        //activityResultLauncherImage.launch(imagePickerIntent);
    }

        private String saveToInternalStorage(Bitmap bitmapImage) {
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            // path to /data/data/yourApp/app_data/imageDir
            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
            // Create imageDir
            File myPath = new File(directory, "profile.jpg");

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(myPath);
                // Use the compress method on the BitMap object to write image to the OutputStream
                bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            } catch (Exception e) {
                e.printStackTrace(System.out);
            } finally {
                try {
                    Objects.requireNonNull(fos).close();
                } catch (IOException e) {
                    e.printStackTrace(System.out);
                }
            }
            return directory.getAbsolutePath();
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

}

