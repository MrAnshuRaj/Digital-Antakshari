package com.anshu.antakshari;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class GlobalLeaderBoardActivity extends AppCompatActivity {
    ArrayList<LeaderBoardModel> leaderboardItems=new ArrayList<>();
    FirebaseFirestore leaderBoard = FirebaseFirestore.getInstance();
    DocumentReference rankList=leaderBoard.document("LeaderBoard/rankList");
    TextView[] names=new TextView[3],points=new TextView[3];
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global_leader_board);

        names[0]=findViewById(R.id.firstRank);
        names[1]=findViewById(R.id.secondRank);
        names[2]=findViewById(R.id.thirdRank);

        points[0]=findViewById(R.id.points1st);
        points[1]=findViewById(R.id.points2nd);
        points[2]=findViewById(R.id.points3rd);

        progressBar=findViewById(R.id.lbdLoadingProgressBar);

        RecyclerView recyclerView=findViewById(R.id.leaderBoardRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        leaderBoard.collection("Leaderboard").orderBy("points", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    int i=0;
                    progressBar.setVisibility(View.GONE);
                    for(QueryDocumentSnapshot documentSnapshot: Objects.requireNonNull(task.getResult()))
                    {
                        if(i<3) {
                            names[i].setText(documentSnapshot.getString("name"));
                            points[i].setText(documentSnapshot.getLong("points")+" points");
                        }
                        else
                            leaderboardItems.add(new LeaderBoardModel((i+1),documentSnapshot.getString("name"), Math.toIntExact(documentSnapshot.getLong("points"))));
                        i++;
                    }
                    RecyclerLeaderBdAdapter adapter=new RecyclerLeaderBdAdapter(GlobalLeaderBoardActivity.this,leaderboardItems);
                    recyclerView.setAdapter(adapter);
                }
            }
        });


    }


}