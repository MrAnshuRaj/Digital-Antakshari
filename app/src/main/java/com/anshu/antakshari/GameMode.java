package com.anshu.antakshari;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.net.InternetDomainName;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class GameMode extends AppCompatActivity {

    SharedPreferences getSharedPref;
    SharedPreferences.Editor editor;
    String profilePicPath, nameFull, firstName;
    CircleImageView profilePic;
    TextView playerName, greeting, level;
    EditText searchBox;
    boolean isGoogleSignIn;
    private DocumentReference userRef;
    private FirebaseFirestore db;
    FirebaseAuth auth;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);
        profilePic = findViewById(R.id.dpDB);
        playerName = findViewById(R.id.playerNameDB);
        searchBox = findViewById(R.id.searchWord);
        greeting = findViewById(R.id.greeting);
        level = findViewById(R.id.level);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userRef = db.collection("users").document(auth.getCurrentUser().getUid());
        Objects.requireNonNull(getSupportActionBar()).hide();

        getSharedPref = getSharedPreferences("digANT", MODE_PRIVATE);
        editor = getSharedPref.edit();
        if(getSharedPref.getString("Gender","None").equals("None"))
        {
            showGenderDialog();
        }
        loadData();

    }

    @SuppressLint("SetTextI18n")

    public void loadData() {
        nameFull = getSharedPref.getString("FullName", "DigAnt User");
        playerName.setText(nameFull);
        db.collection("Leaderboard").document("User: "+auth.getCurrentUser().getEmail()).
                get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful() && task.getResult().exists())
                        {
                            int totalPointsDB = Objects.requireNonNull(task.getResult().getLong("points")).intValue();
                            editor.putInt("totalPoints",totalPointsDB);
                            editor.apply();
                        }
                    }
                });
        isGoogleSignIn = getSharedPref.getBoolean("GoogleSignIn", false);
        firstName = getSharedPref.getString("Name", "player123");
        greeting.setText("Play Now, " + firstName + "!");

        int levelPlayer = level(getSharedPref.getInt("totalPoints", 0));
        level.setText("Level " + levelPlayer);

        profilePicPath = getSharedPref.getString("ProfilePath", "");
        //
        if (isGoogleSignIn) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            Picasso
                    .get()
                    .load(Objects.requireNonNull(user).getPhotoUrl())
                    .into(profilePic);
        } else {
            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists() && document.getString("ownerImage")!=null) {
                        Picasso.get().load(document.getString("ownerImage")).into(profilePic);
                    }
                }
            }); //is not google sign
        }
    }


    //various modes onClick methods
    public void PlayWithComputer(View v) {
        Intent playWithComputer = new Intent(GameMode.this, com.anshu.antakshari.playWithComputer.class);
        startActivity(playWithComputer);
        //setting total games played stats
        editor.putInt("totalGamesPlayed", getSharedPref.getInt("totalGamesPlayed", 0) + 1);
        editor.apply();
    }

    public void PlayOffline(View v) {
        Intent playOffline = new Intent(GameMode.this, MainActivity.class);
        startActivity(playOffline);
        //setting total games played stats
        editor.putInt("totalGamesPlayed", getSharedPref.getInt("totalGamesPlayed", 0) + 1);
        editor.apply();
    }

    public void PlayOnline(View v) {
        if (isDeviceOnline()) {
            Intent playOnline = new Intent(GameMode.this, MainSelection.class);
            startActivity(playOnline);
            //setting total games played stats
            editor.putInt("totalGamesPlayed", getSharedPref.getInt("totalGamesPlayed", 0) + 1);
            editor.apply();
        } else {
            Toast.makeText(GameMode.this, "Internet not connected. Turn on WiFi or Mobile Data!", Toast.LENGTH_LONG).show();
        }
    }

    public void LearnEnglish(View v) {
        if (isDeviceOnline()) {
            Intent intent = new Intent(GameMode.this, LearnEnglish.class);
            startActivity(intent);
        } else {
            Toast.makeText(GameMode.this, "Internet not connected. Turn on WiFi or Mobile Data!", Toast.LENGTH_LONG).show();
        }
    }

    public void globalLeaderBoard(View v) {
        if (isDeviceOnline()) {
            startActivity(new Intent(GameMode.this, GlobalLeaderBoardActivity.class));
        } else {
            Toast.makeText(GameMode.this, "Internet not connected. Turn on WiFi or Mobile Data!", Toast.LENGTH_LONG).show();
        }
    }

    public void timeAttackMode(View v) {
            //customised dialog for time Attack mode
            String[] itemsTimeAttack = {"30s", "60s", "120s", "180s", "300s"};
            Intent intent = new Intent(GameMode.this, timeAttackMode.class);
            //finish();
            new AlertDialog.Builder(this).setTitle("Select Configuration").setSingleChoiceItems(itemsTimeAttack, 1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    intent.putExtra("choiceIndex", i);
                }
            }).setPositiveButton("Play", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                    startActivity(intent);
                }
            }).create().show();
    }

    //to check internet connectivity status
    public boolean isDeviceOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void searchWord(View v) {
        dictionary(searchBox.getText().toString().trim().toLowerCase());
    }
    private void showGenderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_gender_selection, null);
        builder.setView(dialogView);
        Spinner genderSpinner = dialogView.findViewById(R.id.spinner_gender);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);
        builder.setPositiveButton("Done", (dialog, which) -> {
            String selectedGender = genderSpinner.getSelectedItem().toString();
            editor.putString("Gender", selectedGender);
            editor.apply();
            int avatarResource = getRandomAvatarResource(selectedGender);
            if(!isGoogleSignIn)
                profilePic.setImageResource(avatarResource);
            Uri avatarUri = Uri.parse("android.resource://" + getPackageName() + "/" + avatarResource);
            uploadImageToFirebase(avatarUri, "ownerImage");
            Toast.makeText(GameMode.this, "Selected Gender: " + selectedGender, Toast.LENGTH_SHORT).show();
        }).setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public void dictionary(String s) {
        final String[] wordMeaning = new String[1];
        RequestQueue requestQueue;
        requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                "https://api.api-ninjas.com/v1/dictionary?word=" + s, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    wordMeaning[0] = response.getString("definition");
                    String[] wdMean = wordMeaning[0].split(";", 10);
                    StringBuilder out = new StringBuilder();
                    for (String str : wdMean)
                        out.append(str).append(";");
                    new android.app.AlertDialog.Builder(GameMode.this).setTitle("Dictionary")
                            .setMessage(s + " : " + out).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                    searchBox.setText("");
                                }
                            }).create().show();
                } catch (JSONException e) {
                    e.printStackTrace(System.out);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                //Log.d("myapp", "Something went wrong");
                Toast.makeText(GameMode.this, "Something went wrong", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap headers = new HashMap();
                headers.put("X-Api-Key", APIKeys.getAPI_NINJAS());
                return headers;
            }
        };
        requestQueue.add(jsonObjectRequest);
    }

    public void profile(View v) {
        //finish();
        Intent seeProfile = new Intent(GameMode.this, ProfileActivity.class);
        startActivity(seeProfile);
    }

    public int level(int points) {
        int lev = 1;
        if(points<200){
            return  1;
        }
        else if (points <= 2000) {
            lev = points / 200;
        } else if (points <= 12000) {
            lev = 10 + (points - 2000) / 500;
        } else
            lev = 30 + (points - 12000) / 1000;
        return lev;

    }
    private int getRandomAvatarResource(String gender) {
        Random random = new Random();
        int avatarIndex = random.nextInt(4) + 1;
        if (gender.equals("Male")) {
            switch (avatarIndex) {
                case 1: return R.drawable.man1;
                case 2: return R.drawable.man2;
                case 3: return R.drawable.man3;
                case 4: return R.drawable.man4;
            }
        } else if (gender.equals("Female")) {
            switch (avatarIndex) {
                case 1: return R.drawable.woman1;
                case 2: return R.drawable.woman2;
                case 3: return R.drawable.woman3;
                case 4: return R.drawable.woman4;
            }
        }
        return R.drawable.person;
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }
    private void uploadImageToFirebase(Uri imageUri, String imageType) {
        if (imageUri != null) {
            StorageReference storageRef= FirebaseStorage.getInstance().getReference();
            StorageReference fileReference = storageRef.child("uploads/" + System.currentTimeMillis() + ".png");
            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        saveImageUrlToFirestore(imageUrl, imageType);
                    }))
                    .addOnFailureListener(e -> Toast.makeText(GameMode.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
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

}