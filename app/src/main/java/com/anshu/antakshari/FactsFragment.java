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
import java.util.Objects;


public class FactsFragment extends Fragment {


    TextView factsTextView;
    ScrollView scrollView;
    StringBuilder facts;
    ProgressBar pb;
    int factsCount = 0;

    public FactsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_facts, container, false);
        factsTextView = root.findViewById(R.id.factsTextView);
        scrollView = root.findViewById(R.id.factsScrollView);
        pb = root.findViewById(R.id.loadFactsProgressBar);
        facts = new StringBuilder();
        for(int i=1;i<=10;i++)
            loadFacts();
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                View view = scrollView.getChildAt(scrollView.getChildCount() - 1);
                int bottomDetector = view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY());
                if (bottomDetector == 0) {
                    //Toast.makeText(getActivity(),"Loading more...",Toast.LENGTH_SHORT).show();
                    pb.setVisibility(View.VISIBLE);
                    for(int i=1;i<=10;i++)
                        loadFacts();
                }
            }
        });

        // Inflate the layout for this fragment
        return root;
    }

    public void loadFacts() {
        RequestQueue requestQueue = Volley.newRequestQueue(requireActivity());
        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Request.Method.GET,
                "https://api.api-ninjas.com/v1/facts", null, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                try {
                    for (int i = 0; i < 30; i++) {
                        JSONObject obj = response.getJSONObject(i);
                        factsCount++;
                        if (factsCount == 1) {
                            facts.append(factsCount).append(". ").append(obj.getString("fact"));
                        } else {
                            facts.append("\n\n\n\n").append(factsCount).append(". ").append(obj.getString("fact"));
                        }
                        pb.setVisibility(View.GONE);
                        factsTextView.setText(facts);


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
                headers.put("X-Api-Key", APIKeys.getAPI_NINJAS());
                return headers;
            }
        };
        requestQueue.add(jsonObjectRequest);
    }
}