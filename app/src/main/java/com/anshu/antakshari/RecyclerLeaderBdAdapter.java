package com.anshu.antakshari;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerLeaderBdAdapter extends RecyclerView.Adapter<RecyclerLeaderBdAdapter.ViewHolder> {
    Context context;
    ArrayList<LeaderBoardModel> arrayList;
    RecyclerLeaderBdAdapter(Context context,ArrayList<LeaderBoardModel> arrayList)
    {
        this.context=context;
        this.arrayList=arrayList;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v=LayoutInflater.from(context).inflate(R.layout.leaderboard_card_recyclerview,viewGroup,false);
        return new ViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
      if(arrayList.get(i).rank<10)
        viewHolder.rank.setText("  "+arrayList.get(i).rank);
      else
          viewHolder.rank.setText(""+arrayList.get(i).rank);
      viewHolder.name.setText(arrayList.get(i).name);
      viewHolder.points.setText("" + arrayList.get(i).points);
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView rank,name,points;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rank=itemView.findViewById(R.id.rank);
            name=itemView.findViewById(R.id.nameldbd);
            points=itemView.findViewById(R.id.leaderbdpoints);
        }
    }
}
