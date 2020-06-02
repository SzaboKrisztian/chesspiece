package com.krisztianszabo.chesspiece.online.games;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.krisztianszabo.chesspiece.model.Game;
import com.krisztianszabo.chesspiece.online.OnlineActivity;

public class GameViewFragment extends Fragment {

    private OnlineActivity parent;
    private Game game;

    public void setParent(OnlineActivity parent) {
        this.parent = parent;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
