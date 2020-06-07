package com.krisztianszabo.chesspiece.online.games;

import android.app.AlertDialog;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.krisztianszabo.chesspiece.BoardView;
import com.krisztianszabo.chesspiece.ChessMoveReceiver;
import com.krisztianszabo.chesspiece.R;
import com.krisztianszabo.chesspiece.model.BoardState;
import com.krisztianszabo.chesspiece.model.Game;
import com.krisztianszabo.chesspiece.model.Player;
import com.krisztianszabo.chesspiece.online.OnlineActivity;
import com.krisztianszabo.chesspiece.online.lobby.ChatViewAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GameViewFragment extends Fragment implements ChessMoveReceiver {

    private OnlineActivity parent;
    private int gameId;
    private Game game;
    private RecyclerView chatOutput;
    private List<JSONObject> messages = new ArrayList<>();
    private GameChatAdapter adapter;
    private TextView chatInput;
    private TextView gameMsg;
    private TextView whoAmI;
    private BoardView board;
    private Player myColor;

    public void setParent(OnlineActivity parent) {
        this.parent = parent;
    }

    public void setGame(int gameId, Game game) {
        this.gameId = gameId;
        this.game = game;
        this.myColor = game.getWhite().equals(parent.getUsername()) ? Player.WHITE : Player.BLACK;
    }

    public void setMessages(List<JSONObject> messages) {
        this.messages.clear();
        this.messages.addAll(messages);
        adapter.notifyDataSetChanged();
        if (messages.size() > 0) {
            chatOutput.smoothScrollToPosition(messages.size() - 1);
        }
    }

    public void addMessage(JSONObject message) {
        this.messages.add(message);
        adapter.notifyDataSetChanged();
        if (messages.size() > 0) {
            chatOutput.smoothScrollToPosition(messages.size() - 1);
        }
    }

    public int getGameId() {
        return gameId;
    }

    public Player getMyColor() {
        return myColor;
    }

    public void updateGame(int gameId) {
        if (this.gameId == gameId) {
            OnlineGameManager.getInstance().getGame(gameId, parent);
            updateViews();
        } else {
            Toast.makeText(parent, "A game has been updated." + gameId, Toast.LENGTH_SHORT).show();
        }
    }

    public boolean canOfferDraw() {
        return this.game.getDrawOffered() == null;
    }

    public boolean canAcceptDraw() {
        return this.game.getDrawOffered() == (myColor == Player.WHITE ? Player.BLACK : Player.WHITE);
    }

    public boolean isGameOver() {
        return  this.game.getState() != Game.State.WHITE_MOVES &&
                this.game.getState() != Game.State.BLACK_MOVES;
    }

    public void opponentOfferedDraw() {
        game.setDrawOffered(myColor == Player.WHITE ? Player.BLACK : Player.WHITE);
    }

    public void playerOfferedDraw() {
        game.setDrawOffered(myColor);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game_view, container, false);

        View chatLayout = view.findViewById(R.id.game_chat_layout);
        View boardLayout = view.findViewById(R.id.game_view_layout);
        chatLayout.setVisibility(View.GONE);
        boardLayout.setVisibility(View.VISIBLE);

        chatOutput = view.findViewById(R.id.game_chat_output);
        chatInput = view.findViewById(R.id.game_chat_input);
        view.findViewById(R.id.game_chat_btn_send).setOnClickListener(v -> {
            String message = chatInput.getText().toString();
            if (message.length() > 0) {
                try {
                    JSONObject data = new JSONObject();
                    data.put("gameId", gameId);
                    data.put("action", "send message");
                    data.put("message", message);
                    parent.emitOnSocket("game", data);
                    chatInput.setText("");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        LinearLayoutManager lmgr = new LinearLayoutManager(this.getContext());
        lmgr.setStackFromEnd(true);

        chatOutput.setLayoutManager(lmgr);
        adapter = new GameChatAdapter(messages);
        chatOutput.setAdapter(adapter);

        try {
            JSONObject data = new JSONObject();
            data.put("gameId", gameId);
            data.put("action", "get messages");
            parent.emitOnSocket("game", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        view.findViewById(R.id.game_btn_chat).setOnClickListener(v -> {
            boardLayout.setVisibility(View.GONE);
            chatLayout.setVisibility(View.VISIBLE);
        });
        view.findViewById(R.id.game_btn_board).setOnClickListener(v -> {
            chatLayout.setVisibility(View.GONE);
            boardLayout.setVisibility(View.VISIBLE);
        });
        whoAmI = view.findViewById(R.id.game_view_who_am_i);
        gameMsg = view.findViewById(R.id.game_view_msg);
        board = view.findViewById(R.id.game_view_board);
        board.setMoveReceiver(this);

        String whoAmIText = "You play " + (myColor == Player.WHITE ? "white." : "black.");
        whoAmI.setText(whoAmIText);

        updateViews();

        if (!isGameOver() && game.getDrawOffered() ==
                (myColor == Player.WHITE ? Player.BLACK : Player.WHITE)) {
            AlertDialog.Builder bld = new AlertDialog.Builder(parent);
            bld.setMessage("Your opponent offered a draw.");
            bld.setNeutralButton("Ok", (dialog, which) -> dialog.dismiss());
            bld.show();
        }

        return view;
    }

    public void updateViews() {

        gameMsg.setText(generateGameStatusText(game.getState(), myColor));

        BoardState boardState = game.getBoard();
        board.setBoardState(boardState);
        board.setAllowTouch(boardState.getCurrentPlayer() == myColor);
        board.setShowCoordinates(parent.isShowCoordinates());
        board.setShowLegalMoves(parent.isShowLegalMoves());
        board.setShowLastMove(parent.isShowLastMove());
        if (myColor == Player.BLACK) {
            board.setRotateBoard(true);
        }
        board.invalidate();
    }

    @Override
    public void makeMove(int piecePosition, int move) {
        try {
            JSONObject data = new JSONObject();
            data.put("action", "move");
            data.put("gameId", gameId);
            data.put("piecePosition", piecePosition);
            data.put("move", move);
            parent.emitOnSocket("game", data);
        } catch (JSONException e) {
            Log.e("MOVE", "Failed to compose move message");
        }
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
