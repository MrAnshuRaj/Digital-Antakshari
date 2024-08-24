package com.anshu.antakshari;

import android.content.Context;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class WordChecker {

    private Set<String> wordSet;

    public WordChecker(Context context) {
        wordSet = new HashSet<>();
        loadWordsFromFile(context);
    }

    private void loadWordsFromFile(Context context) {
        try {
            InputStream is = context.getAssets().open("words_alpha.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                wordSet.add(line.trim());
            }
            reader.close();
        } catch (IOException e) {
            Toast.makeText(context, "Error loading words from file", Toast.LENGTH_SHORT).show();
            e.printStackTrace(System.out);
        }
    }

    public boolean isValidWord(String word) {
        return wordSet.contains(word.trim().toLowerCase());
    }
}
