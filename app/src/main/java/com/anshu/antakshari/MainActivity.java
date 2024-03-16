package com.anshu.antakshari;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {
    static int timeWord, main_time_in_milli_sec;
    EditText names;
    TextToSpeech t1;
    Spinner toughness, mainTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        names = findViewById(R.id.namesin);
        toughness = findViewById(R.id.spinner);
        mainTime = findViewById(R.id.spinner2);
        Objects.requireNonNull(getSupportActionBar()).hide();
        List<String> categories = new ArrayList<>();
        categories.add("30 seconds");
        categories.add("25 seconds");
        categories.add("20 seconds");
        categories.add("15 seconds");
        categories.add("10 seconds");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toughness.setAdapter(dataAdapter);

        toughness.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String item = adapterView.getItemAtPosition(i).toString();
                if (item.equalsIgnoreCase("25 seconds"))
                    timeWord = 25000;
                else if (item.equalsIgnoreCase("20 seconds"))
                    timeWord = 20000;
                else if (item.equalsIgnoreCase("15 seconds"))
                    timeWord = 15000;
                else if (item.equalsIgnoreCase("10 seconds"))
                    timeWord = 10000;
                else if (item.equalsIgnoreCase("30 seconds"))
                    timeWord = 30000;

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        List<String> cat2 = new ArrayList<>();
        cat2.add("15 minutes");
        cat2.add("10 minutes");
        cat2.add("5 minutes");
        cat2.add("2 minutes");
        cat2.add("1 minute");
        ArrayAdapter<String> dataAdapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cat2);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mainTime.setAdapter(dataAdapter2);
        mainTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String item = adapterView.getItemAtPosition(i).toString();
                //main timer selection
                if (item.equalsIgnoreCase("15 minutes"))
                    main_time_in_milli_sec = 900000;
                else if (item.equalsIgnoreCase(("10 minutes")))
                    main_time_in_milli_sec = 600000;
                else if (item.equalsIgnoreCase("5 minutes"))
                    main_time_in_milli_sec = 300000;
                else if (item.equalsIgnoreCase("2 minutes"))
                    main_time_in_milli_sec = 120000;
                else if (item.equalsIgnoreCase("1 minute"))
                    main_time_in_milli_sec = 60000;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        t1 = new TextToSpeech(this, i -> {
            if (i != TextToSpeech.ERROR)
                t1.setLanguage(Locale.ENGLISH);
        });

        new CountDownTimer(2000, 1000) {
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {

                t1.speak("Welcome", TextToSpeech.QUEUE_FLUSH, null);

            }
        }.start();
    }

    public void play(View v) {
        //called when play button is clicked for offline mode
        MediaPlayer clicked = MediaPlayer.create(getApplicationContext(), R.raw.click);
        clicked.start();
        if ((names.getText().toString().contains(","))) {
            String nameString = names.getText().toString();
            String[] nameArray = nameString.split(",");
            char nextLetter = (char) ((Math.random() * ((90 - 65) + 1)) + 65);
            t1.speak("Next letter is " + nextLetter, TextToSpeech.QUEUE_ADD, null);

            Intent game = new Intent(MainActivity.this, game.class);
            game.putExtra("nameArray", nameArray);
            game.putExtra("isOnline", false);
            game.putExtra("nextL", nextLetter);

            startActivity(game);
            finish();

        } else {
            new AlertDialog.Builder(this).setTitle("Warning!")
                    .setMessage("Please separate the names by comma or enter names of at least 2 players to continue")
                    .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.cancel()).create().show();
        }
    }
//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        startActivity(new Intent(MainActivity.this, GameMode.class));
//        finish();
//    }

}