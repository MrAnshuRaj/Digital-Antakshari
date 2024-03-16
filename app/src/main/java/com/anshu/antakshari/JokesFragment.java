package com.anshu.antakshari;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class JokesFragment extends Fragment {

    TextView jokesTextView;
    ScrollView scrollView;
    StringBuilder jokes;
    ProgressBar pb;
    int jokesCount = 0;

    public JokesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_jokes, container, false);
        jokesTextView = root.findViewById(R.id.jokesTextView);
        scrollView = root.findViewById(R.id.jokes_ScrollView);
        pb = root.findViewById(R.id.loadJokesProgressBar);
        jokes = new StringBuilder();
        loadJokes();
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                View view = scrollView.getChildAt(scrollView.getChildCount() - 1);
                int bottomDetector = view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY());
                if (bottomDetector == 0) {
                    // Toast.makeText(getActivity(),"Loading more...",Toast.LENGTH_SHORT).show();
                    pb.setVisibility(View.VISIBLE);
                    loadJokes();
                }
            }
        });
        return root;
    }

    public void loadJokes() {
        RequestQueue requestQueue = Volley.newRequestQueue(requireActivity());
        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Request.Method.GET,
                "https://api.api-ninjas.com/v1/jokes?limit=30", null, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                try {
                    for (int i = 0; i < 30; i++) {
                        JSONObject obj = response.getJSONObject(i);
                        jokesCount++;
                        if (jokesCount == 1) {
                            jokes.append(jokesCount).append(". ").append(obj.getString("joke"));
                        } else {
                            jokes.append("\n\n").append(jokesCount).append(". ").append(obj.getString("joke"));
                        }
                        jokesTextView.setText(jokes);
                        pb.setVisibility(View.GONE);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                //Log.d("myapp", "Something went wrong");
                Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_LONG).show();
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
}