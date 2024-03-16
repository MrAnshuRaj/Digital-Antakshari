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

public class RecyclerGameAdapter extends RecyclerView.Adapter<RecyclerGameAdapter.ViewHolder> {
    Context context;
    ArrayList<gameplayModel> arrayList;
    public RecyclerGameAdapter(Context context, ArrayList<gameplayModel> arrayList)
    {
        this.context=context;
        this.arrayList=arrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v= LayoutInflater.from(context).inflate(R.layout.game_play_card,viewGroup,false);
        return new ViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.name.setText(arrayList.get(i).name);
        viewHolder.points.setText(""+arrayList.get(i).points);
        viewHolder.typingStatus.setText(arrayList.get(i).typingStatusStr);

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name,points,typingStatus;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.nameGPCd);
            points=itemView.findViewById(R.id.pointsGPCd);
            typingStatus=itemView.findViewById(R.id.TypingStatus);
        }
    }
}

