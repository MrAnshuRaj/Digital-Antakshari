package com.anshu.antakshari;

import static com.anshu.antakshari.MainActivity.main_time_in_milli_sec;
import static com.anshu.antakshari.MainActivity.timeWord;
import static com.anshu.antakshari.MainSelection.currRoomNo;
import static com.anshu.antakshari.MainSelection.isHost;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;


public class game extends AppCompatActivity {
    FirebaseFirestore wo = FirebaseFirestore.getInstance();
    DocumentReference OnlineTimer = wo.document(currRoomNo + "/timer");
    DocumentReference isTyping = wo.document(currRoomNo + "/typingStatus");
    TextView nextLetter;
    TextView prv;
    TextView time;
    TextView n01;
    TextView time2;
    TextView nextPlayerTurn;
    WordChecker wordChecker;
    String[] name;
    String[] words = new String[1000];
    private FirebaseFirestore db;
    private DocumentReference userRef;
    private StorageReference storageRef;
    FirebaseAuth auth;
    ProgressBar gameProgressBar;
    int players;
    int countWords = 0, count = 0, REQ_CODE = 100, endgame = 0;
    long timeLeft, mainTimerLeft;
    char nextLet;
    int rounds = 0;
    String onlineWord, nameCurr;
    EditText input;
    Button nextBtn;
    int[] score;
    TextToSpeech t2;
    MediaPlayer sec3;
    String ne;
    String[] profileURLs;
    Map<String, String> wordServer, timerHashStart, timerHashStop, stopGameInit, stopGame, timerInit;
    Boolean pauseTimer = false, isOnline = false;
    SharedPreferences getShared;
    SharedPreferences.Editor editor;
    RecyclerView recyclerView;
    RecyclerGameAdapter adapter;
    ArrayList<gameplayModel> arrayList = new ArrayList<>();
    private CountDownTimer timer, mainTimer;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gameactivity);
        n01 = findViewById(R.id.n01);
        time = findViewById(R.id.time);
        nextPlayerTurn = findViewById(R.id.playerTurn);
        gameProgressBar = findViewById(R.id.progressBarGame);
        recyclerView = findViewById(R.id.gameRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        getShared = getSharedPreferences("digANT", MODE_PRIVATE);
        editor = getShared.edit();
        wordChecker = new WordChecker(this);
        nameCurr = getShared.getString("Name", "Null");
        wordServer = new HashMap<>();
        stopGame = new HashMap<>();
        stopGameInit = new HashMap<>();
        timerHashStart = new HashMap<>();
        timerHashStop = new HashMap<>();
        timerInit = new HashMap<>();
        stopGameInit.put("Status", "No");
        stopGame.put("Status", "Stop");
        timerHashStart.put("Status", "Start");
        timerHashStop.put("Status", "Stop");
        timerInit.put("Status", "None");
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        userRef = db.collection("users").document(auth.getCurrentUser().getUid());
        startMainTimer(main_time_in_milli_sec);
        nextBtn = findViewById(R.id.nextBtn);
        time2 = findViewById(R.id.time2);
        nextLetter = findViewById(R.id.nextletters);
        prv = findViewById(R.id.prev);
        input = findViewById(R.id.INPUT);



        Intent intent = getIntent();

        this.name = intent.getStringArrayExtra("nameArray");
        this.isOnline = intent.getBooleanExtra("isOnline", false);
        if(isOnline)
            this.profileURLs = intent.getStringArrayExtra("urlList");
        this.nextLet = intent.getCharExtra("nextL", 'k');
        score = new int[name.length];
        players = name.length;
        if(!isOnline)
         profileURLs=new String[name.length];
        final String[] currentUrl = new String[1];
        boolean isGoogleSignIn=getShared.getBoolean("GoogleSignIn",false);
        if (isGoogleSignIn) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null && user.getPhotoUrl() != null) {
                currentUrl[0] = String.valueOf(user.getPhotoUrl());
                initialize(currentUrl[0]);
            }
        } else {
            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists() && document.getString("ownerImage") != null) {
                        currentUrl[0] = document.getString("ownerImage");

                        initialize(currentUrl[0]);

                    }
                }
            });
        }


        nextPlayerTurn.setText(name[0] + " 's turn");
        blink(nextPlayerTurn);

        nextLetter.setText("Next letter : " + nextLet);
        sec3 = MediaPlayer.create(game.this, R.raw.countdown);

        t2 = new TextToSpeech(this, i -> {
            if (i != TextToSpeech.ERROR) {
                t2.setLanguage(Locale.ENGLISH);
            }
        });
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                next();
            }
        });
        input.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                // Trigger your function here
                next();
                return true;
            }
            return false;
        });
        timer = new CountDownTimer(timeWord, 1000) {
            @Override
            public void onTick(long l) {
                time.setText(l / 1000 + "s");
                timeLeft = l;
                if (l < 4000 && l > 3000) {

                    sec3.start();
                }

            }

            @Override
            public void onFinish() {
                pauseTimer = true;
                input.setText("0");
                MediaPlayer media = MediaPlayer.create(game.this, R.raw.timeout);
                media.start();
                next1();
            }
        }.start();
        wordServer.put("name", "cdsp");
        if (isOnline) {
            wo.collection(currRoomNo).document("word").set(wordServer).addOnSuccessListener(unused -> wo.collection(currRoomNo).document("word").addSnapshotListener((value, error) -> {
                assert value != null;
                if (value.exists()) {
                    onlineWord = value.getString("name");

                    assert onlineWord != null;
                    if (!Objects.equals(onlineWord, "cdsp")) {
                        pauseTimer = true;
                        input.setText(onlineWord);
                        next1();
                    }

                }
            }));
        }

        if (isOnline) {
            wo.collection(currRoomNo).document("End").set(stopGameInit);
            wo.collection(currRoomNo).document("End").addSnapshotListener((value, error) -> {

                assert value != null;
                if (Objects.requireNonNull(value.getString("Status")).equalsIgnoreCase("Stop")) {
                    if (isOnline)
                        OnlineTimer.set(timerHashStop);
                    else {
                        timer.cancel();
                        sec3.stop();
                    }
                    endgame = 1;
                    mainTimer.cancel();
                    countWords = count = 0;
                    isOnline = false;
                    int timeSpend = getShared.getInt("timeSpentPlaying", 0) + (int) (main_time_in_milli_sec - mainTimerLeft);
                    editor.putInt("timeSpentPlaying", timeSpend);
                    editor.apply();
                    Intent gameOver = new Intent(game.this, com.anshu.antakshari.gameOver.class);
                    gameOver.putExtra("nameArray", this.name);
                    gameOver.putExtra("scoreArray", this.score);
                    gameOver.putExtra("wordsList", words);
                    gameOver.putExtra("Rounds", rounds);
                    startActivity(gameOver);
                    finish();
                }
            });
        }
    }
    public void initialize(String currentUrl)
    {
        for (int i = 0; i < name.length; i++) {
            if(isOnline) {
                arrayList.add(new gameplayModel(name[i], score[i], "",profileURLs[i]));
            }
            else { //for getting random avatars when in offline mode
                if(name[i].equals(nameCurr))
                {
                    arrayList.add(new gameplayModel(name[i], score[i], "",currentUrl));
                    profileURLs[i]=currentUrl;
                }
                else {
                    int resource = getRandomAvatarResource();
                    arrayList.add(new gameplayModel(name[i], score[i], "", String.valueOf(resource)));
                    profileURLs[i]=String.valueOf(resource);
                }
            }
        }
        HashMap<String, String> typingYes = new HashMap<>();
        typingYes.put("status", "Typing..");
        HashMap<String, String> typingNo = new HashMap<>();
        typingNo.put("status", "");

        adapter = new RecyclerGameAdapter(game.this, arrayList);
        recyclerView.setLayoutManager(new LinearLayoutManager(game.this,LinearLayoutManager.HORIZONTAL,false));
        recyclerView.setAdapter(adapter);
        adapter.setSelectedPosition(0);
        gameProgressBar.setVisibility(View.GONE);

        if (isOnline) {
            OnlineTimer.addSnapshotListener((value, error) -> {
                assert value != null;
                if (value.exists()) {
                    String status = value.getString("Status");
                    assert status != null;
                    if (status.equalsIgnoreCase("Start"))
                        startTimer(timeLeft);
                    else if (status.equalsIgnoreCase("Stop"))
                        timer.cancel();
                }
            });
            isTyping.set(typingNo).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    isTyping.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                            String status = "";
                            if (documentSnapshot != null) {
                                status = documentSnapshot.getString("status");
                            }
                            arrayList.set(count, new gameplayModel(name[count], score[count], status, profileURLs[count]));
                            adapter.notifyItemChanged(count);
                        }
                    });
                }
            });
            //first creating the document then setting snapshot listener


        }
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!input.getText().toString().isEmpty()) {
                    if (isOnline && !pauseTimer) {
                        OnlineTimer.set(timerHashStop);
                    } else
                        timer.cancel();
                    sec3.stop();
                }
                //typing started
                if (isOnline && name[count].equalsIgnoreCase(nameCurr)) {
                    isTyping.set(typingYes);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!input.getText().toString().isEmpty()) {
                    if (isOnline && !pauseTimer) {
                        OnlineTimer.set(timerHashStop);
                    } else
                        timer.cancel();
                    sec3.stop();
                }
                //typing stopped
                if (isOnline && name[count].equalsIgnoreCase(nameCurr)) {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                        }
                    }, 1500);
                    isTyping.set(typingNo);
                }
            }
        });

    }
    public boolean repetition() {
        String s = input.getText().toString();
        for (String word : words) {
            if (s.equalsIgnoreCase(word))
                return true;

        }
        return false;
    }

    @SuppressLint("SetTextI18n")
    public int scores(String w) {
        int pt;
        sec3.stop();

        // Set the time limit based on mode
        long timeLimit = isOnline ? 30000 : timeWord;  // 30 seconds for online, variable for offline

        // Base score calculation based on word length
        if (w.equals("0")) {
            pt = -5;
            n01.setText("-5 points");
            t2.speak("Idiot", TextToSpeech.QUEUE_ADD, null);
        } else if (w.length() <= 3) {
            pt = 3;
            t2.speak("Good", TextToSpeech.QUEUE_FLUSH, null);
            n01.setText("+3 points");
        } else if (w.length() <= 6) {
            pt = 5;
            t2.speak("Very Good", TextToSpeech.QUEUE_FLUSH, null);
            n01.setText("+5 points");
        } else {
            pt = w.length();
            t2.speak("Excellent", TextToSpeech.QUEUE_FLUSH, null);
            n01.setText("+" + pt + " points");
        }

        // Bonus for fast response based on remaining time
        if (timeLeft > (timeLimit * 0.8)) {  // 80% or more time remaining
            pt += 3;  // High bonus for very fast response
            t2.speak("Blazing Fast!", TextToSpeech.QUEUE_ADD, null);
            n01.append(" +3 bonus!");
        } else if (timeLeft > (timeLimit * 0.5)) {  // 50% or more time remaining
            pt += 2;  // Medium bonus
            t2.speak("Quick Response!", TextToSpeech.QUEUE_ADD, null);
            n01.append(" +2 bonus!");
        } else if (timeLeft > (timeLimit * 0.2)) {  // 20% or more time remaining
            pt += 1;  // Small bonus
            t2.speak("Nice Timing!", TextToSpeech.QUEUE_ADD, null);
            n01.append(" +1 bonus!");
        }

        // Animation and cleanup
        Animation a1 = AnimationUtils.loadAnimation(this, R.anim.pointsanim);
        n01.startAnimation(a1);

        // Timer for displaying points
        new CountDownTimer(2000, 1000) {
            @Override
            public void onTick(long l) {}

            @Override
            public void onFinish() {
                n01.setText("");
            }
        }.start();

        return pt;
    }

    public void isValidWord(String str) {

        RequestQueue requestQueue;
        requestQueue = Volley.newRequestQueue(this);
        str = str.trim();
        if (!str.equals("0")) {
            Toast.makeText(game.this, "Submitting word...", Toast.LENGTH_SHORT).show();
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,

                    "https://api.api-ninjas.com/v1/dictionary?word=" + str, null, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {

                    try {
                        boolean valid = response.getBoolean("valid");
                        if (valid) {
                            next2();
                        } else {
                            t2.speak("Invalid English Word!", TextToSpeech.QUEUE_FLUSH, null);
                            Toast.makeText(getApplicationContext(), "Invalid English Word!", Toast.LENGTH_LONG).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace(System.out);
                    }

                }


            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    //Log.d("myapp", "Something went wrong");
                    Toast.makeText(game.this, "Something went wrong", Toast.LENGTH_LONG).show();
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
        } else {
            //input.setText("0");
            next2();
        }
    }

    public void next() {
        MediaPlayer media = MediaPlayer.create(game.this, R.raw.click);
        media.start();
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
        if (isOnline) {
            if (name[count].equalsIgnoreCase(nameCurr)) {
                online();

            } else
                Toast.makeText(game.this, "Its not your turn", Toast.LENGTH_SHORT).show();
        } else
            next1();
    }

    public void online() {
        wordServer.put("name", input.getText().toString());
        wo.collection(currRoomNo).document("word").set(wordServer).addOnSuccessListener(unused -> Toast.makeText(game.this, "Word submitted successfully", Toast.LENGTH_SHORT).show());
    }

    @SuppressLint("SetTextI18n")
    public void next2() {
        String inputWord = input.getText().toString();
        if (!(inputWord.isEmpty() || inputWord.trim().contains(" "))) {
            ne = input.getText().toString().trim();

            if ((countWords > 0) && repetition() && !(ne.equals("0"))) {
                if ((Character.toUpperCase(ne.charAt(0)) == Character.toUpperCase(nextLet))) {
                    t2.speak("Word already used", TextToSpeech.QUEUE_FLUSH, null);
                    Toast.makeText(getApplicationContext(), "Word already used", Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(getApplicationContext(), "Word doesn't start with required letter!", Toast.LENGTH_LONG).show();
                    t2.speak("Word doesn't start with required letter!", TextToSpeech.QUEUE_FLUSH, null);
                }
                input.setText("");
            } else if (!(repetition())) {
                String in = input.getText().toString().trim();
                if (!(in.equals("0"))) {
                    words[countWords] = in;
                    countWords++;
                }

                if ((Character.toUpperCase(ne.charAt(0)) != Character.toUpperCase(nextLet)) && !(ne.equals("0"))) {


                    Toast.makeText(getApplicationContext(), "Word doesn't start with required letter!", Toast.LENGTH_LONG).show();
                    t2.speak("Word doesn't start with required letter!", TextToSpeech.QUEUE_FLUSH, null);
                    input.setText("");
                    pauseTimer = false;
                } else //word starts from correct letter
                {
                    score[count] += scores(ne);
                    arrayList.set(count, new gameplayModel(name[count], score[count], "", profileURLs[count]));
                    adapter.notifyItemChanged(count);

                    if (count != (players - 1)) { // if not last player
                        recyclerView.scrollToPosition(count + 1);
                        adapter.setSelectedPosition(count+1);
                        nextPlayerTurn.setText(name[count + 1] + " 's turn");
                    }

                    //}checking fastResponse
                    if (nameCurr.equals(name[count])) {
                        int fastestResponseTime = ((int) (timeWord - timeLeft)) / 1000;
                        int getRespTimeShPf = getShared.getInt("fastestResponseTime", 30);
                        if (fastestResponseTime < getRespTimeShPf) {
                            editor.putInt("fastestResponseTime", fastestResponseTime);
                            editor.apply();
                        }
                    }

                    if (count == (players - 1)) { //if last player
                        count = -1;
                        adapter.setSelectedPosition(0);
                        recyclerView.scrollToPosition(0);
                        nextPlayerTurn.setText(name[0] + " 's turn");
                    }

                    count++;

                    if (!ne.equals("0")) {
                        prv.setText("Past Word : " + ne);
                        nextLet = Character.toUpperCase(ne.charAt(ne.length() - 1));
                        nextLetter.setText("Next Letter : " + nextLet);
                        t2.speak("Next letter is " + nextLet, TextToSpeech.QUEUE_ADD, null);
                    }
                    input.setText("");
                    timer.cancel();

                    startTimer(timeWord);

                    rounds++;

                    OnlineTimer.set(timerInit).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            pauseTimer = false;
                        }
                    });

                }
            }
        } else {
            if (input.getText().toString().trim().isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please enter a word before clicking the NEXT button!", Toast.LENGTH_LONG).show();
                t2.speak("Please enter a word before clicking the NEXT button!", TextToSpeech.QUEUE_FLUSH, null);
            } else if (input.getText().toString().trim().contains(" ")) {
                Toast.makeText(getApplicationContext(), "Only single word allowed. Please remove the whitespaces! ", Toast.LENGTH_LONG).show();
                t2.speak("Only single word allowed. Please remove the whitespaces! ", TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }
    public void isValidWord1(String wordToCheck)
    {
        if(wordToCheck.equals("0"))
            next2();
        boolean isValid = wordChecker.isValidWord(wordToCheck);
        if (isValid) {
            next2();
           // Toast.makeText(game.this, "Word is valid", Toast.LENGTH_SHORT).show();
        } else {
            t2.speak("Invalid English Word!", TextToSpeech.QUEUE_FLUSH, null);
            Toast.makeText(game.this, "Word is invalid", Toast.LENGTH_SHORT).show();
        }

    }
    public void next1() {
        String inputWord = input.getText().toString();
        isValidWord1(inputWord);
    }

    public void startTimer(long timeTimer) {//method to start timer for word
        if(timer!=null)
            timer.cancel();
        timer = new CountDownTimer(timeTimer, 1000) {

            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long l) {
                time.setText(l / 1000 + "s");
                timeLeft = l;
                if (l < 4000 && l > 3000) {
                    sec3.start();
                }
            }

            @Override
            public void onFinish() {
                pauseTimer = true;
                input.setText("0");
                MediaPlayer media = MediaPlayer.create(game.this, R.raw.timeout);
                media.start();
                next1();
            }
        }.start();
    }

    @Override
    public void onBackPressed() {
        sec3.stop();
        new AlertDialog.Builder(this).setTitle("Are you sure you want to exit?")
                .setMessage("Current game data will be lost in doing so")
                .setNegativeButton("Yes", (dialogInterface, i) -> {
                    timer.cancel();
                    isHost = false;
                    Arrays.fill(name, "");
                    isOnline = false;
                    int timeSpend = getShared.getInt("timeSpentPlaying", 0) + (int) (main_time_in_milli_sec - mainTimerLeft);
                    editor.putInt("timeSpentPlaying", timeSpend);
                    editor.apply();
                    count = countWords = 0;
                    startActivity(new Intent(game.this, GameMode.class));
                    finish();
                }).setPositiveButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        startTimer(timeLeft);
                    }
                }).create().show();
    }

    public void startMainTimer(long main_time) {//method to start timer for game

        final boolean[] flag = {true};

        AlertDialog.Builder builder = new AlertDialog.Builder(game.this).setTitle("Warning")
                .setView(getLayoutInflater().inflate(R.layout.gameover_dialog, null));
        AlertDialog alertDialog = builder.create();
        mainTimer = new CountDownTimer(main_time, 1000) {
            @SuppressLint({"SuspiciousIndentation", "SetTextI18n"})
            @Override
            public void onTick(long l) {
                int min = (int) l / 60000;
                int sec = (int) (l - (min * 60000)) / 1000;
                mainTimerLeft = l;
                if (sec < 10)
                    time2.setText(min + ":0" + sec);
                else
                    time2.setText(min + ":" + sec);
                if (l < 4000 && l > 3000) {
                    sec3.start();
                }
                if ((int) (l / 1000) < 5 && flag[0]) {
                    alertDialog.show();
                    flag[0] = false;
                }
            }

            @Override
            public void onFinish() {
                timer.cancel();
                endgame = 1;
                count = countWords = 0;
                alertDialog.dismiss();
                int timeSpend = getShared.getInt("timeSpentPlaying", 0) + (int) (main_time_in_milli_sec - mainTimerLeft);
                editor.putInt("timeSpentPlaying", timeSpend);
                editor.apply();
                Intent gameOver = new Intent(game.this, com.anshu.antakshari.gameOver.class);
                gameOver.putExtra("nameArray", name);
                gameOver.putExtra("scoreArray", score);
                gameOver.putExtra("wordsList", words);
                gameOver.putExtra("Rounds", rounds);
                startActivity(gameOver);
                isOnline = false;
                finish();
            }
        }.start();
    }

    public void voice(View V) {
        if (isOnline)
            OnlineTimer.set(timerHashStop);
        else {
            timer.cancel();
            sec3.stop();
        }

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (resultCode == RESULT_OK && null != data) {
                ArrayList<String> result = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                input.setText(result.get(0));
                if (isOnline)
                    OnlineTimer.set(timerHashStart);
                else {
                    startTimer(timeLeft);
                }
            }
        } else {
            throw new IllegalStateException("Unexpected value: " + requestCode);
        }
    }

    public void end(View view) {
        if (isOnline) {
            if (isHost) {
                wo.collection(currRoomNo).document("End").set(stopGame);

            } else
                Toast.makeText(game.this, "You don't have rights to end the game. The host can only end the game", Toast.LENGTH_LONG).show();
        } else {
            timer.cancel();
            sec3.stop();
            endgame = 1;
            count = countWords = 0;
            mainTimer.cancel();
            isOnline = false;
            int timeSpend = getShared.getInt("timeSpentPlaying", 0) + (int) (main_time_in_milli_sec - mainTimerLeft);
            editor.putInt("timeSpentPlaying", timeSpend);
            editor.apply();
            Intent gameOver = new Intent(game.this, com.anshu.antakshari.gameOver.class);
            gameOver.putExtra("nameArray", this.name);
            gameOver.putExtra("scoreArray", this.score);
            gameOver.putExtra("wordsList", words);
            gameOver.putExtra("Rounds", rounds);
            finish();
            startActivity(gameOver);
            Arrays.fill(score, 0);
            Arrays.fill(words, "");

        }
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
                    new android.app.AlertDialog.Builder(game.this).setTitle("Dictionary")
                            .setMessage(s + " : " + out).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            }).create().show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                //Log.d("myapp", "Something went wrong");
                Toast.makeText(game.this, "Something went wrong", Toast.LENGTH_LONG).show();
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

    public void dictionaryBtnClick(View v) {
        Toast.makeText(game.this, "Loading...", Toast.LENGTH_LONG).show();
        dictionary(ne);

    }//end of dictionaryBtnClick

    public void words(View v) {

        StringBuilder list = new StringBuilder();
        for (int i = 0; i < countWords; i++)
            list.append(words[i]).append(", ");
        new AlertDialog.Builder(this).setTitle("Words List")
                .setMessage(list.toString())
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                }).create().show();
    }

    public void blink(TextView txt) {
        AnimationDrawable animationDrawable = (AnimationDrawable) txt.getContext().getDrawable(R.drawable.animation_drawable);

        txt.setBackground(animationDrawable);
        txt.post(new Runnable() {
            @Override
            public void run() {
                animationDrawable.start();
            }
        });
        txt.postDelayed(new Runnable() {
            @Override
            public void run() {
                animationDrawable.stop();
                animationDrawable.start();
            }
        }, animationDrawable.getNumberOfFrames() * 500);
    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
    private int getRandomAvatarResource() {
        Random random = new Random();
        int avatarIndex = random.nextInt(5) + 1;
         switch (avatarIndex) {
             case 1:
                 return R.drawable.person;
             case 2:
                 return R.drawable.person2;
             case 3:
                 return R.drawable.person3;
             case 4:
                 return R.drawable.person4;
             case 5:
                 return R.drawable.person5;
             default:
                 return R.drawable.person;
         }

    }
}