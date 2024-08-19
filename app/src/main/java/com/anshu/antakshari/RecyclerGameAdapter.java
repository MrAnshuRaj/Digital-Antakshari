package com.anshu.antakshari;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class RecyclerGameAdapter extends RecyclerView.Adapter<RecyclerGameAdapter.ViewHolder> {
    Context context;
    ArrayList<gameplayModel> arrayList;
    private int selectedPosition = 0; // No item selected initially
    public RecyclerGameAdapter(Context context, ArrayList<gameplayModel> arrayList)
    {
        this.context=context;
        this.arrayList=arrayList;
    }
    public void setSelectedPosition(int position) {
        notifyItemChanged(selectedPosition);
        selectedPosition = position;
        notifyItemChanged(selectedPosition);
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

       // arrayList.get(i).profileUrl == null || arrayList.get(i).profileResource != 0
        if(arrayList.get(i).profileUrl != null && arrayList.get(i).profileUrl.matches("[0-9]+"))
        {
            int value = Integer.parseInt(arrayList.get(i).profileUrl);
            Uri avatarUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + value);
            viewHolder.profilePic.setImageURI(avatarUri);
        }
        else{
            Picasso.get().load(arrayList.get(i).profileUrl).into(viewHolder.profilePic);
        }
        if (i == selectedPosition) {
            viewHolder.mainLayout.setBackgroundResource(R.drawable.player_turn); // Set yellow for selected
        } else {
            viewHolder.mainLayout.setBackgroundResource(R.drawable.playercard); // Set purple for default
        }


    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name,points,typingStatus;
        ImageView profilePic;
        ConstraintLayout mainLayout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.nameGPCd);
            points=itemView.findViewById(R.id.pointsGPCd);
            typingStatus=itemView.findViewById(R.id.TypingStatus);
            profilePic = itemView.findViewById(R.id.profilePicCard);
            mainLayout=itemView.findViewById(R.id.columnItem);
        }
    }
}

