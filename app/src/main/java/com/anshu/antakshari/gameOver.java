package com.anshu.antakshari;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;


public class gameOver extends AppCompatActivity {
    TextView cong;
    String[] name;
    int[] score;
    int rounds;
    FirebaseFirestore leaderBoard = FirebaseFirestore.getInstance();
    DocumentReference index = leaderBoard.document("Leaderboard/Index");
    StringBuilder list;
    String nameOfPlayer;
    SharedPreferences getShared;
    SharedPreferences.Editor editor;
    RecyclerView recyclerView;
    ArrayList<LeaderBoardModel> arrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_over);

        cong = findViewById(R.id.textView3);
        recyclerView = findViewById(R.id.gameOverLBD);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        getShared = getSharedPreferences("digANT", MODE_PRIVATE);
        editor = getShared.edit();

        nameOfPlayer = getShared.getString("Name", "Null");

        Intent intent = getIntent();
        name = intent.getStringArrayExtra("nameArray");
        score = intent.getIntArrayExtra("scoreArray");
        rounds = intent.getIntExtra("Rounds", 1);
        //creating words list
        list = new StringBuilder();
        String[] word = intent.getStringArrayExtra("wordsList");
        assert word != null;
        for (String s : word) {
            if (s == null)
                break;
            list.append(s).append(", ");
        }
        MediaPlayer p = MediaPlayer.create(this, R.raw.win);
        p.start();
        Animation a1 = AnimationUtils.loadAnimation(this, R.anim.anim2);
        cong.startAnimation(a1);
        sort();

    }

    @SuppressLint("SetTextI18n")
    public void sort() {
        int n = score.length;
        int temp = 0;
        String tem = "";
        for (int i = 0; i < n; i++) {
            for (int j = 1; j < (n - i); j++) {
                if (score[j - 1] < score[j]) {
                    //swap elements
                    temp = score[j - 1];
                    tem = name[j - 1];
                    score[j - 1] = score[j];
                    name[j - 1] = name[j];
                    score[j] = temp;
                    name[j] = tem;
                }

            }
        }
        for (int i = 0; i < score.length; i++) {
            arrayList.add(new LeaderBoardModel((i + 1), name[i], score[i]));
        }
        RecyclerLeaderBdAdapter adapter = new RecyclerLeaderBdAdapter(this, arrayList);
        recyclerView.setAdapter(adapter);

        //for player profile
        if (nameOfPlayer.equals(name[0])) {
            editor.putInt("totalWins", getShared.getInt("totalWins", 0) + 1);
            editor.apply();
        }
        for (int i = 0; i < n; i++) {
            if (nameOfPlayer.equals(name[i])) {
                //totalPoints
                int totalPoints = getShared.getInt("totalPoints", 0) + score[i];
                editor.putInt("totalPoints", totalPoints);
                boolean googleSignIn = getShared.getBoolean("GoogleSignIn", false);
                if (getShared.getBoolean("firstPlay", true) && !googleSignIn) { //if first time set data on an index
                    index.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            Long ind = documentSnapshot.getLong("index");

                            editor.putLong("IndexLBD", ind);
                            editor.putBoolean("firstPlay", false);
                            editor.apply(); //setting index of player from database to device

                            rankListDataFirestore data = new rankListDataFirestore(nameOfPlayer, Integer.parseInt(String.valueOf(ind)), totalPoints);
                            leaderBoard.collection("Leaderboard").document("Index" + Objects.requireNonNull(ind)).set(data);


                            HashMap<String, Integer> indexUpdate = new HashMap<>();
                            indexUpdate.put("index", (int) (ind + 1));
                            index.set(indexUpdate);//index++ on database

                        }
                    });
                } else if (getShared.getBoolean("firstPlay", true) && googleSignIn) {
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                    currentUser.getIdToken(true).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
                        @Override
                        public void onSuccess(GetTokenResult getTokenResult) {
                            String token = getTokenResult.getToken();
                            editor.putString("googleToken", token);
                            editor.putBoolean("firstPlay", false);
                            editor.apply();
                            rankListDataFirestore data = new rankListDataFirestore(nameOfPlayer, Integer.parseInt(String.valueOf(0)), totalPoints); //index set to zero for all
                            leaderBoard.collection("Leaderboard").document("UID:" + token).set(data);

                        }
                    });
                } else //if 2nd or further time then set data on the index for the play from shared pref
                {
                    if (googleSignIn) {
                        String token = getShared.getString("googleToken", "");
                        rankListDataFirestore dataUpdate = new rankListDataFirestore(nameOfPlayer, Math.toIntExact(getShared.getLong("IndexLBD", 0L)), totalPoints);
                        leaderBoard.collection("Leaderboard").document("UID:" + token).set(dataUpdate);
                    } else {
                        rankListDataFirestore dataUpdate = new rankListDataFirestore(nameOfPlayer, Math.toIntExact(getShared.getLong("IndexLBD", -1L)), totalPoints);
                        leaderBoard.collection("Leaderboard").document("Index" + getShared.getLong("IndexLBD", -1L)).set(dataUpdate);
                    }
                }
                //highestScore //inside name equality if block
                if (score[i] > getShared.getInt("highestScore", 0))
                    editor.putInt("highestScore", score[i]);
                //wordsPlayed
                int wordsPlayed = rounds / name.length;
                editor.putInt("totalWordsPlayed", (getShared.getInt("totalWordsPlayed", 0) + wordsPlayed));


                editor.apply();
            }
        }


    }

    public void playAgain(View v) {
        gameOver.super.onBackPressed();
        rounds = 0;
        Arrays.fill(score, 0);
        list = new StringBuilder();
        finish();

    }

    public void wordsUsed(View v) {
        new AlertDialog.Builder(this).setTitle("Words List")
                .setMessage(this.list)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                }).create().show();
    }
//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        startActivity(new Intent(gameOver.this, GameMode.class));
//        finish();
//    }
}