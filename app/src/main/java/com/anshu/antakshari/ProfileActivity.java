package com.anshu.antakshari;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {
    private static final int PICK_OWNER_IMAGE_REQUEST = 125;
    String nameOfPlayer, profilePicPath;
    TextView name;
    TextView totalGamesPlayed,totalWins,winPercentage,totalPoints,highestScore,avgScore,
        totalWordsPlayed,avgWordLength,fastestResponseTime,timeSpentPlaying;
    ImageView profilePic;
    SharedPreferences getSharedPref;
    SharedPreferences.Editor editor;
    Button signOut;
    ImageButton profilePicUpdateBtn;
    boolean isGoogleSignIn;
    private Uri ownerImageUri;
    private FirebaseFirestore db;
    private DocumentReference userRef;
    FirebaseAuth auth;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        profilePic=findViewById(R.id.ProfileActivityDp);
        name=findViewById(R.id.Name);
        getSharedPref=getSharedPreferences("digANT",MODE_PRIVATE);
        editor=getSharedPref.edit();
        totalGamesPlayed=findViewById(R.id.totalgamesPlayed);
        totalWins=findViewById(R.id.totalWins);
        winPercentage=findViewById(R.id.winPercentage);
        totalPoints=findViewById(R.id.totalPoints);
        highestScore=findViewById(R.id.highestScore);
        avgScore=findViewById(R.id.avgScore);
        totalWordsPlayed=findViewById(R.id.totalWordsPlayed);
        avgWordLength=findViewById(R.id.avgWordLength);
        fastestResponseTime=findViewById(R.id.fastestResponseTime);
        timeSpentPlaying=findViewById(R.id.timeSpentPlaying);
        signOut =findViewById(R.id.signOutBtn);
        profilePicUpdateBtn=findViewById(R.id.imageButton);
        profilePicUpdateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser(PICK_OWNER_IMAGE_REQUEST);
            }
        });
        isGoogleSignIn=getSharedPref.getBoolean("GoogleSignIn",false);
        profilePicPath = getSharedPref.getString("ProfilePath","");
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        userRef = db.collection("users").document(auth.getCurrentUser().getUid());

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                editor.putBoolean("dpUpdated",false);
                isGoogleSignIn=false;
                editor.putBoolean("GoogleSignIn",false);
                editor.putBoolean("Logged",false);
                editor.putBoolean("firstPlay", false);
                editor.putString("ProfilePath","");
                editor.apply();
                editor.clear();
                Toast.makeText(ProfileActivity.this,"Signed out successfully",Toast.LENGTH_SHORT).show();
                Intent intent =new Intent(ProfileActivity.this,MainActivity2.class);

                startActivity(intent);
                finish();
            }
        });

        try {
            loadStatistics();
        }
        catch(Exception e)
        {
            Toast.makeText(ProfileActivity.this,"Play the game to build-up your profile \n"+e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }
    @SuppressLint("SetTextI18n")
    private void loadStatistics() {
        // Load profile picture
        if (isGoogleSignIn) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null && user.getPhotoUrl() != null) {
                Picasso.get()
                        .load(user.getPhotoUrl())
                        .into(profilePic);
            }
        } else {
            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists() && document.getString("ownerImage") != null) {
                        Picasso.get().load(document.getString("ownerImage")).into(profilePic);
                    }
                }
            });
        }

        // Load other statistics
        nameOfPlayer = getSharedPref.getString("FullName", "Player123");
        name.setText(nameOfPlayer);

        int totalGames = getSharedPref.getInt("totalGamesPlayed", 0);
        int totalWinsData = getSharedPref.getInt("totalWins", 0);
        int totalPointsData = getSharedPref.getInt("totalPoints", 0);
        int totalWordsData = getSharedPref.getInt("totalWordsPlayed", 0);

        totalGamesPlayed.setText("Total Games : " + totalGames);
        totalWins.setText("Total Wins : " + totalWinsData);

        double winPercent = 0;
        int avgPts = 0, avgWdLength = 0;

        if (totalGames > 0) {
            winPercent = (double) totalWinsData / totalGames;
            avgPts = totalPointsData / totalGames;
        }

        if (totalWordsData > 0) {
            avgWdLength = totalPointsData / totalWordsData;
        }

        winPercentage.setText("Win % : " + String.format("%.2f", winPercent * 100) + " %");
        totalPoints.setText("Total Points : " + totalPointsData);
        highestScore.setText("Highest Points : " + getSharedPref.getInt("highestScore", 0));
        totalWordsPlayed.setText("Total Words Played : " + totalWordsData);

        int fastResp = getSharedPref.getInt("fastestResponseTime", 10);
        fastestResponseTime.setText("Fastest Response : " + fastResp + " seconds");

        int timeSpent = getSharedPref.getInt("timeSpentPlaying", 0) / 1000;
        int hrs = timeSpent / 3600;
        int min = (timeSpent % 3600) / 60;
        int sec = timeSpent % 60;
        timeSpentPlaying.setText("Time Spent :" + hrs + " hrs " + min + " min " + sec + " sec");

        avgScore.setText("Average Points : " + avgPts);
        avgWordLength.setText("Average Word Length : " + avgWdLength);
    }

    @SuppressLint("SetTextI18n")
    private void loadStatistics(int a) {
        if(isGoogleSignIn) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            Picasso
                    .get()
                    .load(Objects.requireNonNull(user).getPhotoUrl())
                    .into(profilePic);
        }
        else
        {
            userRef.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists() && document.getString("ownerImage")!=null) {
                                Picasso.get().load(document.getString("ownerImage")).into(profilePic);
                            }
                        }
                    });
             //is not google sign
        }

        nameOfPlayer=getSharedPref.getString("FullName","Player123");
        name.setText(nameOfPlayer);

        totalGamesPlayed.setText("Total Games : " + getSharedPref.getInt("totalGamesPlayed", 0));
        totalWins.setText("Total Wins : " + getSharedPref.getInt("totalWins", 0));
        double winPercent=1d;
        int avgPts=1, avgWdLength=1;
        if(getSharedPref.getInt("totalGamesPlayed",1)==0 || getSharedPref.getInt("totalWordsPlayed", 1)==0) {
            winPercent= (double) getSharedPref.getInt("totalWins", 0) / (double) getSharedPref.getInt("totalGamesPlayed", 1);
             avgPts= getSharedPref.getInt("totalPoints", 0) / getSharedPref.getInt("totalGamesPlayed", 1);
            avgWdLength = getSharedPref.getInt("totalPoints", 0) / getSharedPref.getInt("totalWordsPlayed", 1);
        }
        winPercentage.setText("Win % : " +String.valueOf((winPercent!=1?winPercent:0)*100).substring(0,4)+" %");

        totalPoints.setText("Total Points : "+getSharedPref.getInt("totalPoints",0));

        highestScore.setText("Highest Points : "+getSharedPref.getInt("highestScore",0));

        totalWordsPlayed.setText("Total Words Played : "+getSharedPref.getInt("totalWordsPlayed",0));

        int fastResp=getSharedPref.getInt("fastestResponseTime",10);

        fastestResponseTime.setText("Fastest Response : "+fastResp+" seconds");

        int timeSpent=getSharedPref.getInt("timeSpentPlaying",0)/1000;
        int hrs=timeSpent/3600;
        int min=timeSpent/60-(hrs*60);
        int sec=timeSpent%60;
        timeSpentPlaying.setText("Time Spent :"+hrs+" hrs "+min+" min "+sec+" sec");

        avgScore.setText("Average Points : " + ((avgPts!=1)?avgPts:0));
        avgWordLength.setText("Average Word Length : "+(avgWdLength!=1?avgWdLength:0));

    }
    private void openFileChooser(int requestCode) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, requestCode);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode,  Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            if (requestCode == PICK_OWNER_IMAGE_REQUEST) {
                ownerImageUri = data.getData();
                Picasso.get().load(ownerImageUri).into(profilePic);
                uploadImageToFirebase(ownerImageUri, "ownerImage");
            }
        }
    }

    private void uploadImageToFirebase(Uri imageUri, String imageType) {
        if (imageUri != null) {
            StorageReference fileReference = storageRef.child("uploads/" + System.currentTimeMillis() + "." + getFileExtension(imageUri));
            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        saveImageUrlToFirestore(imageUrl, imageType);
                    }))
                    .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
    private String getFileExtension(Uri uri) {
        return getContentResolver().getType(uri).split("/")[1];
    }

    private void saveImageUrlToFirestore(String imageUrl, String imageType) {
        String userId = auth.getUid();
        Map<String, Object> updates = new HashMap<>();
        updates.put(imageType, imageUrl);

        db.collection("users").document(Objects.requireNonNull(userId))
                .update(updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Image saved", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show());
    }

    public void updateName(View v)
    {
        Dialog dialog= new Dialog(this);
        dialog.setContentView(R.layout.dialog_update_name);
        Button submitButton=dialog.findViewById(R.id.submitName);
        EditText nameInput=dialog.findViewById(R.id.nameInput);
        submitButton.setOnClickListener(view -> {
            dialog.dismiss();

            try {
                String[] msg1 = nameInput.getText().toString().split(" ");
                String nameUpdated = Character.toUpperCase(msg1[0].charAt(0)) + msg1[0].substring(1);
                //taking capitalized first name in a string variable

                editor.putString("Name", nameUpdated);
                editor.putString("FullName", nameInput.getText().toString());
                editor.apply();
                name.setText(nameInput.getText().toString());
            }
            catch(Exception e)
            {
                Toast.makeText(ProfileActivity.this, "Name should not be empty! Try again",Toast.LENGTH_LONG).show();
            }
        });
        dialog.show();
    }
}