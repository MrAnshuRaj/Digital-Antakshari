package com.anshu.antakshari;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.anshu.antakshari.R.drawable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LearnEnglish extends AppCompatActivity {
    TabLayout tabLayout;
    ViewPager viewPager;
    EditText dictInput;
    ImageButton search;
    FloatingActionButton fab;
    boolean flag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_english);
        tabLayout = findViewById(R.id.LearnEnglishTabLayout);
        viewPager = findViewById(R.id.viewPagerLearnEnglish);
        dictInput = findViewById(R.id.DictionaryEditTextPopup);
        search = findViewById(R.id.searchButton);
        fab = findViewById(R.id.fab);

        ViewPagerLearnEngAdapter adapter = new ViewPagerLearnEngAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.setOffscreenPageLimit(3);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dictionary(dictInput.getText().toString());
                dictInput.setText("");

            }
        });

    }

    public void dictionaryOfLearnEng(View v) {
        if (flag) {
            dictInput.setVisibility(View.VISIBLE);
            search.setVisibility(View.VISIBLE);
            fab.setImageDrawable(getResources().getDrawable(drawable.xbutton));
            flag = false;
        } else {
            dictInput.setVisibility(View.INVISIBLE);
            search.setVisibility(View.INVISIBLE);
            fab.setImageDrawable(getResources().getDrawable(drawable.dictionary));
            flag = true;
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
                    new android.app.AlertDialog.Builder(LearnEnglish.this).setTitle("Dictionary")
                            .setMessage(s + " : " + wordMeaning[0]).setPositiveButton("OK", new DialogInterface.OnClickListener() {
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
                Toast.makeText(LearnEnglish.this, "Something went wrong", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap headers = new HashMap();
                headers.put("X-Api-Key", "OPakK7lmBBhCx+Lakh1IGQ==14OypK9nRf0bFDPG");
                return headers;
            }
        };
        requestQueue.add(jsonObjectRequest);
    }
//    @Override
//    public void onBackPressed() {
//        startActivity(new Intent(LearnEnglish.this,GameMode.class));
//        finish();
//    }
}