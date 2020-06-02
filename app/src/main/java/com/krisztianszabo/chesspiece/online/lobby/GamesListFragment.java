package com.krisztianszabo.chesspiece.online.lobby;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.krisztianszabo.chesspiece.R;
import com.krisztianszabo.chesspiece.online.OnlineActivity;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GamesListFragment extends Fragment {

    private OnlineActivity parent;
    private List<JSONObject> myGames = new ArrayList<>();
    private RecyclerView.Adapter adapter;

    public void setGames(List<JSONObject> list) {
        myGames.clear();
        myGames.addAll(list);
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    public void setParent(OnlineActivity parent) {
        this.parent = parent;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_games, container, false);

        adapter = new GamesListAdapter(myGames, parent);
        RecyclerView gamesView = view.findViewById(R.id.games_rec_list);
        gamesView.setLayoutManager(new LinearLayoutManager(parent));
        gamesView.setAdapter(adapter);

        return view;
    }

    public void clearData() {
        myGames.clear();
        adapter.notifyDataSetChanged();
    }
}
