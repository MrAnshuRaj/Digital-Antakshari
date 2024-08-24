package com.anshu.antakshari;

import static com.anshu.antakshari.R.drawable.medal;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class timeAttackMode extends AppCompatActivity {
    private static final int REQ_CODE = 50;
    FirebaseFirestore leaderBoard = FirebaseFirestore.getInstance();
    EditText inputField;
    TextView wordStatusOut,timer,points,highScore;
    ProgressBar timerProgressBar;
    int playerPoints=0, time, choice=1;
    ArrayList<String> words=new ArrayList<>();
    SharedPreferences getShared;
    SharedPreferences.Editor editor;
    String nameOfPlayer;
    DocumentReference index=leaderBoard.document("Leaderboard/Index");
    int[] times={30,60,120,180,300};
    int[] moderation={4,3,2,0,-2};
    int correct=0,wrong=0;
    int totalPoints;
    FirebaseUser currentUser;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_attack_mode);
        inputField=findViewById(R.id.INPUT2);
        wordStatusOut=findViewById(R.id.correctStatus);
        timer=findViewById(R.id.timer);
        timerProgressBar=findViewById(R.id.timerProgressBar);
        points=findViewById(R.id.pointsTimeAttackMode);
        highScore=findViewById(R.id.highScore);
        getShared=getSharedPreferences("digANT",MODE_PRIVATE);
        editor=getShared.edit();
        highScore.setText("High score="+getShared.getInt("High Score",0));
        nameOfPlayer = getShared.getString("Name", "Null");
        Intent intent=getIntent();
        choice=intent.getIntExtra("choiceIndex",1);
        timerProgressBar.setMax(times[choice]*100);
        time=times[choice];
        currentUser =   FirebaseAuth.getInstance().getCurrentUser();
        totalPoints= getShared.getInt("totalPoints", 0) ;
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            private String emailId = getShared.getString("email","default");

            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                if(time>=0)
                {
                    timer.setText(time+" s");
                    timerProgressBar.setProgress(time*100);
                    time--;
                    handler.postDelayed(this,1000);
                }
                else {
                    handler.removeCallbacks(this);
                    //dialog box to display result and end activity
                    new AlertDialog.Builder(timeAttackMode.this).setTitle("Game Over")
                            .setMessage("Congratulations! You have scored "+playerPoints+" points." +
                                    "\n\nCorrect Words: "+correct+"\nIncorrect Words: "+wrong)
                            .setCancelable(false).setIcon(medal)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    timeAttackMode.super.onBackPressed();
                                }
                            }).create().show();

                    //updating total points and high score into shared preferences

                     totalPoints+=playerPoints;
                     SharedPreferences getShared = getSharedPreferences("digANT", MODE_PRIVATE);
                     SharedPreferences.Editor editor = getShared.edit();
                     editor.putInt("totalPoints",totalPoints);
                     editor.apply();

                    if (getShared.getBoolean("firstPlay", true) ) { //if first time set data on an index
                        final int[] pts = {0};
                        rankListDataFirestore data = new rankListDataFirestore(nameOfPlayer, pts[0]); //index set to zero for all
                        leaderBoard.collection("Leaderboard").document("User: "+emailId).set(data)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        editor.putBoolean("firstPlay", false);
                                        editor.putInt("totalPoints",totalPoints);
                                        editor.apply();
                                    }
                                });

                    } else //if 2nd or further time then set data on the index for the play from shared pref
                    {
                        rankListDataFirestore dataUpdate = new rankListDataFirestore(nameOfPlayer, totalPoints);
                        leaderBoard.collection("Leaderboard").document("User: "+emailId).set(dataUpdate);
                    }
                    //setting high score in device storage
                    if(playerPoints > getShared.getInt("High Score",0)) {
                        editor.putInt("High Score",playerPoints);
                        Toast.makeText(timeAttackMode.this, "Congratulations! You have new High Score. New High Score is: "+playerPoints, Toast.LENGTH_LONG).show();
                    }
                    int wordsPlayed=words.size();
                    editor.putInt("totalWordsPlayed",(getShared.getInt("totalWordsPlayed",0)+wordsPlayed));
                    //updating tie spend data in shared preferences
                    int timeSpend=getShared.getInt("timeSpentPlaying",0)+(times[choice]*1000);
                    editor.putInt("timeSpentPlaying", timeSpend);
                    editor.apply();
                }
            }
        },0);

    }
    @SuppressLint("SetTextI18n")
    public void wordEntered(View v)
    {
        MediaPlayer media = MediaPlayer.create(timeAttackMode.this, R.raw.click);
        media.start();

        String inputWord = inputField.getText().toString();

        if(!repetition(inputWord)) {
            words.add(inputWord);
            new Thread(new Runnable() {
                @Override
                public void run() {
                        isValidWord(inputWord);
                }
            }).start();
        }
        else //repetition has occurred
            Toast.makeText(timeAttackMode.this, "Word already used! Use a different word", Toast.LENGTH_LONG).show();
        inputField.setText("");

    }
    public int pointsTimeAttack(String word)
    {
        int point;
        if(word.length()>2 && word.length()<=5)
            point=3;
        else if(word.length()>5 && word.length()<=10)
            point=7;
        else
            point=word.length();
        return point;
    }
    public void voice(View V) {

        Intent intent;
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        Objects.requireNonNull(intent).putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Need to speak");
        try {
            startActivityForResult(intent, REQ_CODE);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "Sorry your device not supported",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 50) {
            if (resultCode == RESULT_OK && null != data) {
                ArrayList<String> result = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                inputField.setText(result.get(0));
            }
        } else {
            throw new IllegalStateException("Unexpected value: " + requestCode);
        }
    }

    public void isValidWord(String str) {
        RequestQueue requestQueue;
        requestQueue = Volley.newRequestQueue(this);
        str = str.trim();
        String finalStr = str;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                    "https://api.api-ninjas.com/v1/dictionary?word=" + str, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        boolean valid=response.getBoolean("valid");
                        validWordEntered(valid, finalStr);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                   // Toast.makeText(timeAttackMode.this,"Something went wrong",Toast.LENGTH_LONG).show();
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap headers=new HashMap();
                    headers.put("X-Api-Key",APIKeys.getAPI_NINJAS());
                    return headers;
                }
            };
            requestQueue.add(jsonObjectRequest);
        }


    @SuppressLint("SetTextI18n")
    private void validWordEntered(boolean isValid,String str)
    {
        if(isValid) {
            playerPoints += pointsTimeAttack(str) + moderation[choice];
            wordStatusOut.setText(str+" ✔");
            correct++;
        }
        else {
            playerPoints--;
            wordStatusOut.setText(str+" ❌");
            wrong++;
        }
        new CountDownTimer(2000, 1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
             wordStatusOut.setText("");
            }
        }.start();
        points.setText(playerPoints + " points");
    }
    public boolean repetition(String s) {
        for (String word : words) {
            if (s.equalsIgnoreCase(word))
                return true;
        }
        return false;
    }
//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        startActivity(new Intent(timeAttackMode.this, GameMode.class));
//        finish();
//    }
}