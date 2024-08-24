package com.anshu.antakshari;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class playWithComputer extends AppCompatActivity {
    ImageView userPic, done;
    String previousWord;
    TextToSpeech t2;
    char lastLetterOfPreviousWord;
    boolean firstTurn=true,wrongWord=false, vocabExhaust=false;
    int points=0,computerPoints=0,wordCounter=0,REQ_CODE=100;
    SharedPreferences getShared;
    SharedPreferences.Editor editor;
    FirebaseFirestore leaderBoard = FirebaseFirestore.getInstance();
    DocumentReference index=leaderBoard.document("Leaderboard/Index");
    TextView computer,nextLetter,computerPts,userPoints,thinking;
    EditText userInput;
    TextToSpeech tts;
    String[] words = new String[1000];
    String nameOfPlayer;
    TextView computerModeTimer;
    Button endButton,nextBtn;
    CountDownTimer timer;
        FirebaseUser currentUser;
    ProgressBar submitProgressBar;
    String[] a;
    private String emailId;

    {
        a = new String[]{"apple", "addition", "answer", "allotrope", "ample", "addition", "bet", "busy", "bamboo", "bend", "blend", "beast"
                , "cat", "cute", "catastrophic", "catalogue", "cease", "ceasefire", "dead", "driver", "drink", "end", "effect", "ester",
                "efflorescence", "effervescence", "elbow", "elephant", "eject", "exact", "fed", "feather", "fist", "fish", "find", "goat", "gold", "gateway", "gaze",
                "gallon", "hot", "hill", "haste", "habit", "hinder",
                "inquiry", "important", "incentive", "iteration", "joker", "joke", "jackal", "kite", "kangaroo", "kill", "kindness", "lie", "lean", "lest",
                "leap", "master", "mind", "maze", "nostalgia", "neanderthal", "oppressive", "over", "out", "past", "power", "pond",
                "pimple", "queen", "quest", "quarantine", "rose", "roll", "roast", "render", "ripple", "sober", "sweet", "sweat",
                "sour", "simple", "tower", "toe", "tattoo", "umbrella", "under", "vest", "vending", "veteran", "wide",
                "sweater", "wise", "wool", "xanthrophyll", "xmas", "yell", "yellow", "yeast",
                "yonder", "zebra", "zombie"};

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_withcomputer);
        computer= findViewById(R.id.computer);
        nextLetter= findViewById(R.id.nextletter);
        userInput= findViewById(R.id.inputText);
        computerPts=findViewById(R.id.textView8);
        userPoints=findViewById(R.id.textView9);
        thinking=findViewById(R.id.thinking);
        computerModeTimer=findViewById(R.id.timerComputerMode);
        endButton=findViewById(R.id.endButtonComputer);
        userPic=findViewById(R.id.userImage);
        done=findViewById(R.id.doneImageVIew);
        submitProgressBar=findViewById(R.id.submitProgressBar);

        t2 = new TextToSpeech(this, i -> {
            if (i != TextToSpeech.ERROR) {
                t2.setLanguage(Locale.ENGLISH);
            }
        });

        currentUser =   FirebaseAuth.getInstance().getCurrentUser();
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endGame();
            }
        });

        //next button clicked
        nextBtn=findViewById(R.id.nextBtn);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    nextBtn.setEnabled(false);
                    MediaPlayer media = MediaPlayer.create(playWithComputer.this, R.raw.click);
                    media.start();
                    submitProgressBar.setVisibility(View.VISIBLE);
                    nextLetter.clearAnimation();
                    computer.clearAnimation();
                    isValidWord(userInput.getText().toString());
                }
                catch(Exception e)
                {
                    Toast.makeText(playWithComputer.this,"No word entered!",Toast.LENGTH_SHORT).show();
                }
            }
        });

        tts = new TextToSpeech(this, i -> {
            if (i != TextToSpeech.ERROR)
                tts.setLanguage(Locale.ENGLISH);
        });

        getShared=getSharedPreferences("digANT",MODE_PRIVATE);
        editor=getShared.edit();
        emailId = getShared.getString("email","default");
        nameOfPlayer = getShared.getString("Name", "Null");
        String profilePicPath =getShared.getString("ProfilePath","");
        loadImageFromStorage(profilePicPath);

        timer = new CountDownTimer(120000, 1000) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long l) {
                int min=(int)l/60000;
                int sec=(int)(l-(min*60000))/1000;
                if(sec<10)
                    computerModeTimer.setText(min+":0"+sec);
                else
                    computerModeTimer.setText(min+":"+sec);
            }

            @Override
            public void onFinish() {
                endGame();
            }
        };
        timer.start();
    }
    private void loadImageFromStorage(String path)
    {

        try {
            File f=new File(path, "profile.jpg");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));

            userPic.setImageBitmap(b);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            Log.w("File","file load error");
        }

    }
    public void isValidWord(String wordToCheck)
    {
        WordChecker wordChecker = new WordChecker(this);
        boolean isValid = wordChecker.isValidWord(wordToCheck);
        submitProgressBar.setVisibility(View.GONE);
        if (isValid) {
            done.setImageResource(R.drawable.done);
            done.setVisibility(View.VISIBLE);
            nextButton();
        } else {
            done.setImageResource(R.drawable.cross);
            done.setVisibility(View.VISIBLE);
            nextBtn.setEnabled(true);
            tts.speak("Invalid English word! Try again.",TextToSpeech.QUEUE_FLUSH,null);
            Toast.makeText(playWithComputer.this,"Invalid English word! Try again. ",Toast.LENGTH_SHORT).show();
        }
        new CountDownTimer(1500, 500) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                done.setVisibility(View.GONE);
            }
        }.start();

    }
    public void isValidWord0(String str) {
        RequestQueue requestQueue;
        requestQueue = Volley.newRequestQueue(this);
        str = str.trim();
        String finalStr = str;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                "https://api.api-ninjas.com/v1/dictionary?word=" + str, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                submitProgressBar.setVisibility(View.GONE);

                try {
                    boolean valid=response.getBoolean("valid");
                    if(valid) {
                        done.setImageResource(R.drawable.done);
                        done.setVisibility(View.VISIBLE);
                        nextButton();
                    }
                    else {
                        done.setImageResource(R.drawable.cross);
                        done.setVisibility(View.VISIBLE);
                        nextBtn.setEnabled(true);
                        tts.speak("Invalid English word! Try again.",TextToSpeech.QUEUE_FLUSH,null);
                        Toast.makeText(playWithComputer.this,"Invalid English word! Try again. ",Toast.LENGTH_SHORT).show();
                    }
                    new CountDownTimer(1500, 500) {
                        @Override
                        public void onTick(long l) {

                        }

                        @Override
                        public void onFinish() {
                            done.setVisibility(View.GONE);
                        }
                    }.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                nextBtn.setEnabled(true);
                submitProgressBar.setVisibility(View.GONE);
                Toast.makeText(playWithComputer.this,"Internet not connected or Empty input",Toast.LENGTH_SHORT).show();
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
    public void nextButton() {
        nextLetter.clearAnimation();
        computer.clearAnimation();
        computer.setText("");

        String UserInput = userInput.getText().toString().trim();
        if (!UserInput.isEmpty()) {
            char firstCharacterOfUserInput = Character.toLowerCase(UserInput.charAt(0));

            if (firstTurn && firstCharacterOfUserInput == 'm') {
                points += scores(UserInput,false);
                userPoints.setText("Points=" + points);
                words[wordCounter] = UserInput;
                wordCounter++;
                previousWord = UserInput; //setting previous word for 2nd turn
                lastLetterOfPreviousWord = previousWord.charAt(previousWord.length() - 1);
                //setting last letter for 2nd turn
                firstTurn = false;
                userInput.setText("");

            } else { //if its 2nd turn or later
                if (firstCharacterOfUserInput == lastLetterOfPreviousWord && !repetition()) {
                    points += scores(UserInput,false);
                    words[wordCounter] = UserInput;
                    wordCounter++;
                    userPoints.setText("Points=" + points);
                    lastLetterOfPreviousWord = UserInput.charAt(UserInput.length() - 1);
                    userInput.setText("");
                } else if (repetition()) {
                    nextBtn.setEnabled(true);
                    tts.speak("Word already used",TextToSpeech.QUEUE_FLUSH,null);
                    Toast.makeText(getApplicationContext(), "Word Already Used", Toast.LENGTH_LONG).show();
                    userInput.setText("");
                    wrongWord = true;
                } else {
                    nextBtn.setEnabled(true);
                    tts.speak("Your word doesn't start with required letter",TextToSpeech.QUEUE_FLUSH,null);
                    Toast.makeText(getApplicationContext(), "Your word doesn't start with required letter!", Toast.LENGTH_LONG).show();
                    wrongWord = true;
                    userInput.setText("");
                }
            }
            if (!wrongWord) {
                int delay= (int) Math.floor(Math.random()*(3000-500+1)+500);
                thinking.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        thinking.setVisibility(View.INVISIBLE);
                        String computerAnswer = getComputerAnswer(lastLetterOfPreviousWord);
                        if (!computerAnswer.isEmpty()) {
                            Animation blink=AnimationUtils.loadAnimation(playWithComputer.this,R.anim.blink);
                            Animation bounce=AnimationUtils.loadAnimation(playWithComputer.this,R.anim.bounce);

                            nextBtn.setEnabled(true);
                            computer.setText("Computer : " + computerAnswer);
                            computer.startAnimation(bounce);
                            lastLetterOfPreviousWord = computerAnswer.trim().charAt(computerAnswer.length() - 1);
                            computerPoints += scores(computerAnswer,true);
                            words[wordCounter]=computerAnswer;
                            wordCounter++;
                            computerPts.setText("Points=" + computerPoints);
                            nextLetter.setText("Next letter : " + lastLetterOfPreviousWord);
                            tts.speak("Next letter is "+lastLetterOfPreviousWord,TextToSpeech.QUEUE_FLUSH,null);
                            nextLetter.startAnimation(blink);
                        }
                    }
                },delay);
            }
            //last letter of prv word from computer
            wrongWord = false;
        }
        else {
            nextBtn.setEnabled(true);
            Toast.makeText(getApplicationContext(), "Empty Input", Toast.LENGTH_LONG).show();
        }
    }

    private String getComputerAnswer(char letter) {
        for(int i = 0; i < a.length; i++)
        {
            if(a[i].charAt(0)==letter)
            {
                String out=a[i];
                a=delete(a,i);
                return out;
            }
        }
        vocabExhaust=true;
        endGame();
      return "";
    }

    public String[] delete(String[] arr,int index)
    {
        String[] arr2 =new String[arr.length-1];
        for(int j=0,k=0;j<arr.length;j++)
        {
            if(j==index)
                continue;
            arr2[k++]=arr[j];

        }
        return arr2;
    }

    public int scores(String w,boolean isComputer) {
        int pt;
         if (w.length() <= 3) {
            pt = 3;
            if(!isComputer)
                 tts.speak("Good",TextToSpeech.QUEUE_FLUSH,null);
        } else if (w.length() > 3 && w.length() <= 6) {
             if(!isComputer)
                tts.speak("Very Good",TextToSpeech.QUEUE_FLUSH,null);
             pt = 5;
        } else {
             if(!isComputer)
                tts.speak("Excellent",TextToSpeech.QUEUE_FLUSH,null);
            pt = w.length();
        }
        return pt;
    }
    public boolean repetition() {
        String s = userInput.getText().toString();
        for (String word : words) {
            if (s.equalsIgnoreCase(word))
                return true;
        }
        return false;
    }
    public void voiceSupport(View V)
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
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
    public void endGame() {
        //updating total points on device
        emailId = currentUser.getEmail();
        int totalPoints = getShared.getInt("totalPoints", 0) + points;
        editor.putInt("totalPoints", totalPoints);
        editor.apply();
        timer.cancel();

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
        //alertDialog code
        String title,message;
        if(vocabExhaust)
        {
            tts.speak("You won. Computer do not know more words",TextToSpeech.QUEUE_FLUSH,null);
            title="You Won";
            message="Computer do not know more words, you have won the game!\n\nYour points:" +points+"\n"+
                    "Computer points: "+computerPoints;
        }
        else if (points>=computerPoints) {
            tts.speak("You won. You have scored greater points than computer",TextToSpeech.QUEUE_FLUSH,null);
            title = "You Won";
            message="Congratulations! You have scored greater points than computer.\n\nYour points: "+points+"\n" +
                    "Computer points: "+computerPoints;
        }
        else {
            title="You lost";
            tts.speak("You lost. Better luck next time!",TextToSpeech.QUEUE_FLUSH,null);
            message="Better luck next time! Computer won.\n\nYour points: "+points+"\n" +
                    "Computer points: "+computerPoints;
        }
        new AlertDialog.Builder(playWithComputer.this).setTitle(title).setMessage(message).setCancelable(false).
                setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        startActivity(new Intent(playWithComputer.this,GameMode.class));
                        vocabExhaust=false;
                        points=computerPoints=wordCounter=0;
                        firstTurn=true;
                        wrongWord=false;
                        Arrays.fill(words,"");
                        finish();
                    }
                }).show();

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (resultCode == RESULT_OK && null != data) {
                ArrayList<String> result = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                userInput.setText(result.get(0));

            }
        } else {
            throw new IllegalStateException("Unexpected value: " + requestCode);
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        new AlertDialog.Builder(this).setTitle("Warning").setMessage("Are you sure you want to exit?").
                setPositiveButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                }).setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        vocabExhaust = false;
                        points = computerPoints = wordCounter = 0;
                        firstTurn = true;
                        wrongWord = false;
                        Arrays.fill(words, "");
                    }
                }).show();

    }
}