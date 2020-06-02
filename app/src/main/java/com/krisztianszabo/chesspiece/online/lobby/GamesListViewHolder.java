package com.krisztianszabo.chesspiece.online.lobby;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.krisztianszabo.chesspiece.R;
import com.krisztianszabo.chesspiece.online.OnlineActivity;
import com.krisztianszabo.chesspiece.online.games.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class GamesListViewHolder extends RecyclerView.ViewHolder {

    private TextView players;
    private TextView status;
    private View layout;

    public GamesListViewHolder(@NonNull View itemView) {
        super(itemView);

        layout = itemView;
        players = itemView.findViewById(R.id.games_list_players);
        status = itemView.findViewById(R.id.games_list_status);
    }

    @SuppressLint("SetTextI18n")
    public void setViews(JSONObject data, OnlineActivity parent) {
        String myName = parent.getUsername();
        try {
            int gameId = data.getInt("id");
            String white = data.getString("whiteUser");
            String black = data.getString("blackUser");
            int statusCode = data.getInt("status");
            int numMoves = data.getInt("numMoves");

            players.setText(Utils.getPlayersText(white, black, myName));

            String statusText = Utils.getStatusText(statusCode, white, black, myName);
            if (statusCode == -2 || statusCode == -1) {
                statusText = Utils.addNumMoves(statusText, numMoves);
            }
            status.setText(statusText);

            layout.setOnClickListener(v -> parent.requestGame(gameId));
        } catch (JSONException e) {
            players.setText("ERROR");
            status.setText("Error parsing JSON data");
        }
    }
}
