package com.krisztianszabo.chesspiece.online.games;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.krisztianszabo.chesspiece.R;

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
    public void setViews(JSONObject data, String myName) {
        try {
            int gameId = data.getInt("id");
            String white = data.getString("whiteUser");
            white = white.equals(myName) ? "You" : white;
            String black = data.getString("blackUser");
            black = black.equals(myName) ? "You" : black;
            int statusCode = data.getInt("status");
            int numMoves = data.getInt("numMoves");
            String statusText;

            players.setText("White: " + white + " vs. Black: " + black);
            String movesText = " " + (numMoves / 2) + (numMoves % 2 != 0 ? "½ " : " ")
                    + "moves played.";
            switch (statusCode) {
                case -2:
                    statusText = (white.equals("You") ? "Your turn." : "Opponent's turn.")
                            + movesText;
                    break;
                case -1:
                    statusText = (black.equals("You") ? "Your turn." : "Opponent's turn.")
                            + movesText;
                    break;
                case 0:
                    statusText = (white.equals("You") ? "You win" : "Opponent wins")
                            + " by checkmate.";
                    break;
                case 1:
                    statusText = (black.equals("You") ? "You win" : "Opponent wins")
                            + " by checkmate.";
                    break;
                case 2:
                    statusText = (white.equals("You") ? "You win" : "Opponent wins")
                            + " by resignation.";
                    break;
                case 3:
                    statusText = (black.equals("You") ? "You win" : "Opponent wins")
                            + " by resignation.";
                    break;
                case 4:
                    statusText = "Draw by insufficient material.";
                    break;
                case 5:
                    statusText = "Draw by stalemate.";
                    break;
                case 6:
                    statusText = "Draw by fifty moves rule.";
                    break;
                case 7:
                    statusText = "Draw by agreement.";
                    break;
                default:
                    throw new JSONException("Unexpected status value: " + statusCode);
            }
            status.setText(statusText);

            layout.setOnClickListener(v -> Log.d("GAME", "Show game with id = " + gameId));
        } catch (JSONException e) {
            players.setText("ERROR");
            status.setText("Error parsing JSON data");
        }
    }
}
