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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {
String nameOfPlayer, profilePicPath;
int SELECT_PICTURE=200;
TextView name;
TextView totalGamesPlayed,totalWins,winPercentage,totalPoints,highestScore,avgScore,
        totalWordsPlayed,avgWordLength,fastestResponseTime,timeSpentPlaying;
    ImageView profilePic;
    SharedPreferences getSharedPref;
    SharedPreferences.Editor editor;
    Button signOut;
    boolean dpUpdated;
    boolean isGoogleSignIn;

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

        isGoogleSignIn=getSharedPref.getBoolean("GoogleSignIn",false);
        profilePicPath = getSharedPref.getString("ProfilePath","");
        dpUpdated=getSharedPref.getBoolean("dpUpdated",false);

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
                Toast.makeText(ProfileActivity.this,"Signed out successfully",Toast.LENGTH_SHORT).show();
                Intent intent =new Intent(ProfileActivity.this,MainActivity2.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP );

                startActivity(intent);
                finish();


            }
        });

        try {
            loadStatistics();
        }
        catch(Exception e)
        {
            Toast.makeText(ProfileActivity.this,"Play the game to build-up your profile",Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("SetTextI18n")
    private void loadStatistics() {
        if(isGoogleSignIn && !dpUpdated) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            Picasso
                    .get()
                    .load(Objects.requireNonNull(user).getPhotoUrl())
                    .into(profilePic);
        }
        else
        {
            loadImageFromStorage(profilePicPath); //is not google sign
        }

        nameOfPlayer=getSharedPref.getString("FullName","Null");
        name.setText(nameOfPlayer);

        totalGamesPlayed.setText("Total Games : " + getSharedPref.getInt("totalGamesPlayed", 0));
        totalWins.setText("Total Wins : " + getSharedPref.getInt("totalWins", 0));

        double winPercent=(double)getSharedPref.getInt("totalWins",0)/(double)getSharedPref.getInt("totalGamesPlayed",0);
        winPercentage.setText("Win % : " +String.valueOf(winPercent*100).substring(0,4)+" %");

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

        int avgPts=getSharedPref.getInt("totalPoints", 0) / getSharedPref.getInt("totalGamesPlayed", 0);
        avgScore.setText("Average Points : " + avgPts);

        int avgWdLength=getSharedPref.getInt("totalPoints",0)/getSharedPref.getInt("totalWordsPlayed",0);
        avgWordLength.setText("Average Word Length : "+avgWdLength);

    }

    private void loadImageFromStorage(String path)
    {
        try {
            File f=new File(path, "profile.jpg");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));

            profilePic.setImageBitmap(b);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace(System.out);
            Log.w("File","file load error");
        }

    }
    public void updateDP(View v)
    {
        editor.putBoolean("dpUpdated",true); // to update dp locally against google dp
        editor.apply();
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == SELECT_PICTURE) {
                // Get the url of the image from data
                Uri selectedImageUri = data.getData();

                if (null != selectedImageUri) {
                    // update the preview image in the layout
                    profilePic.setImageURI(selectedImageUri);
                    try {
                        //storing image in bitmap and saving its path to shared preferences
                        Bitmap selectedImageBitmap= MediaStore.Images.Media.getBitmap(
                                this.getContentResolver(),
                                selectedImageUri);
                        String path= saveToInternalStorage(selectedImageBitmap); //local method

                        //saving path in shared preferences
                        SharedPreferences sh=getSharedPreferences("digANT",MODE_PRIVATE);
                        SharedPreferences.Editor editor=sh.edit();
                        editor.putString("ProfilePath",path);
                        editor.apply();

                    } catch (IOException e) {
                        e.printStackTrace(System.out);
                    }
                }
            }
        }
    }
    private String saveToInternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourApp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File myPath=new File(directory,"profile.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(myPath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        } finally {
            try {
                assert fos != null;
                fos.close();
            } catch (IOException e) {
                e.printStackTrace(System.out);
            }
        }
        return directory.getAbsolutePath();
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