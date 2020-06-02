package com.krisztianszabo.chesspiece.online.games;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.krisztianszabo.chesspiece.R;

import org.json.JSONObject;

import java.util.List;

public class GamesListAdapter extends RecyclerView.Adapter<GamesListViewHolder> {

    private List<JSONObject> myGames;
    private String myName;

    public GamesListAdapter(List<JSONObject> list, String name) {
        myGames = list;
        myName = name;
    }

    @NonNull
    @Override
    public GamesListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GamesListViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.games_list_entry, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull GamesListViewHolder holder, int position) {
        holder.setViews(myGames.get(position), myName);
    }

    @Override
    public int getItemCount() {
        return myGames.size();
    }
}
