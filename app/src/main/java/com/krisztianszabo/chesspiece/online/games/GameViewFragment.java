package com.krisztianszabo.chesspiece.online.games;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.krisztianszabo.chesspiece.BoardView;
import com.krisztianszabo.chesspiece.ChessMoveReceiver;
import com.krisztianszabo.chesspiece.R;
import com.krisztianszabo.chesspiece.model.BoardState;
import com.krisztianszabo.chesspiece.model.Game;
import com.krisztianszabo.chesspiece.model.Player;
import com.krisztianszabo.chesspiece.online.OnlineActivity;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;

public class GameViewFragment extends Fragment implements ChessMoveReceiver {

    private OnlineActivity parent;
    private Socket socket;
    private int gameId;
    private Game game;
    private TextView gameMsg;
    private TextView whoAmI;
    private BoardView board;
    private Player myColor;

    public void setParent(OnlineActivity parent) {
        this.parent = parent;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setGame(int gameId, Game game) {
        this.gameId = gameId;
        this.game = game;
    }

    public int getGameId() {
        return gameId;
    }

    public void updateGameView(int gameId) {
        if (this.gameId == gameId) {
            OnlineGameManager.getInstance().getGame(gameId, parent);
            updateBoard();
        } else {
            Toast.makeText(parent, "GAME UPDATED ID: " + gameId, Toast.LENGTH_SHORT).show();
        }
    }

    private Player getMyColor() {
        String myName = parent.getUsername();
        return myName.equals(game.getWhite()) ? Player.WHITE : Player.BLACK;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game_view, container, false);

        //view.findViewById(R.id.game_btn_chat).setOnClickListener(v -> showGameChat());
        whoAmI = view.findViewById(R.id.game_view_who_am_i);
        gameMsg = view.findViewById(R.id.game_view_msg);
        board = view.findViewById(R.id.game_view_board);
        board.setMoveReceiver(this);

        myColor = getMyColor();
        gameMsg.setText(generateGameStatusText(game.getState(), myColor));
        String whoAmIText = "You play " + (myColor == Player.WHITE ? "white." : "black.");
        whoAmI.setText(whoAmIText);

        updateBoard();

        return view;
    }

    private void updateBoard() {
        BoardState boardState = game.getBoard();
        board.setBoardState(boardState);
        board.setAllowTouch(boardState.getCurrentPlayer() == myColor);
    }

    @Override
    public void makeMove(int piecePosition, int move) {
        try {
            JSONObject data = new JSONObject();
            data.put("action", "move");
            data.put("gameId", gameId);
            data.put("piecePosition", piecePosition);
            data.put("move", move);
            parent.emitGameEvent(data);
            Log.d("GAME EMIT", "makeMove: " + data);
        } catch (JSONException e) {
            Log.e("MOVE", "Failed to compose move message");
        }
    }

    private void showLatestBoard() {
        BoardState state = game.getBoard();
        board.setBoardState(state);
        board.setAllowTouch(state.getCurrentPlayer() == myColor);
    }

    private void showFromHistory(int i) {
        board.setBoardState(game.getBoard(i));
        board.setAllowTouch(false);
    }

    private String generateGameStatusText(Game.State gameState, Player whoAmI) {
        switch (gameState) {
            case WHITE_MOVES:
                return whoAmI == Player.WHITE ? "Your move." : "Opponent's move.";
            case BLACK_MOVES:
                return whoAmI == Player.BLACK ? "Your move." : "Opponent's move.";
            case WHITE_WINS_CHECKMATE:
                return whoAmI == Player.WHITE ? "You won by checkmate!" : "Opponent won by checkmate.";
            case BLACK_WINS_CHECKMATE:
                return whoAmI == Player.BLACK ? "You won by checkmate!" : "Opponent won by checkmate.";
            case WHITE_WINS_RESIGNATION:
                return whoAmI == Player.WHITE ? "You won by resignation." : "Opponent won by resignation.";
            case BLACK_WINS_RESIGNATION:
                return whoAmI == Player.BLACK ? "You won by resignation." : "Opponent won by resignation.";
            case DRAW_AGREEMENT:
                return "Draw by agreement.";
            case DRAW_FIFTY:
                return "Draw by fifty moves rule.";
            case DRAW_MATERIAL:
                return "Draw by insufficient material.";
            case DRAW_STALEMATE:
                return "Draw by stalemate.";
        }
        return null;
    }
}
