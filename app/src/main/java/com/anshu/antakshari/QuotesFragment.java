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

public class QuotesFragment extends Fragment {

    TextView quotesTextView;
    ScrollView scrollView;
    StringBuilder quotes;
    ProgressBar pb;
    int quotesCount=0;

    public QuotesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root=inflater.inflate(R.layout.fragment_quotes, container, false);
        quotesTextView= root.findViewById(R.id.quotesTextView);
        scrollView=root.findViewById(R.id.jokes_ScrollView);
        pb=root.findViewById(R.id.loadQuotesProgressBar);
        quotes= new StringBuilder();
        for(int i=1;i<=5;i++) {
            loadQuotes();
        }
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                View view = scrollView.getChildAt(scrollView.getChildCount() - 1);
                int bottomDetector = view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY());
                if(bottomDetector == 0 ){
                    //Toast.makeText(getActivity(),"Loading more...",Toast.LENGTH_SHORT).show();
                    pb.setVisibility(View.VISIBLE);
                    for(int i=1;i<=5;i++) {
                        loadQuotes();
                    }
                }
            }
        });
        return root;
    }
    public void  loadQuotes()
    {
        RequestQueue requestQueue = Volley.newRequestQueue(requireActivity());
        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Request.Method.GET,
                "https://api.api-ninjas.com/v1/quotes", null, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                try {
                        JSONObject obj = response.getJSONObject(0);
                        quotesCount++;
                        if(quotesCount==1) {
                            quotes.append(quotesCount).append(". ").append(obj.getString("quote")).append("\n(").append(obj.getString("category")).append(")\n--By ").append(obj.getString("author"));
                        } else {
                            quotes.append("\n\n\n").append(quotesCount).append(". ").append(obj.getString("quote")).append("\n(").append(obj.getString("category")).append(")\n--By ").append(obj.getString("author"));
                        }
                        quotesTextView.setText(quotes);
                        pb.setVisibility(View.GONE);

                } catch (JSONException e) {
                    e.printStackTrace(System.out);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                //Log.d("myapp", "Something went wrong");
                Toast.makeText(getActivity(),"Something went wrong",Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String> headers=new HashMap<>();
                headers.put("X-Api-Key",APIKeys.getAPI_NINJAS());
                return headers;
            }
        };
        requestQueue.add(jsonObjectRequest);
    }
}