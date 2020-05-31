package com.krisztianszabo.chesspiece;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.krisztianszabo.chesspiece.model.Game;
import com.krisztianszabo.chesspiece.model.GameManager;
import com.krisztianszabo.chesspiece.offline.DisplaySettings;
import com.krisztianszabo.chesspiece.offline.SettingsManager;

public class OfflineActivity extends AppCompatActivity implements ChessActivity {

    private BoardView boardView;
    private TextView gameMsg;
    private Game game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline);
        boardView = findViewById(R.id.board);
        boardView.setParentActivity(this);
        gameMsg = findViewById(R.id.gameMsg);

        GameManager mgr = GameManager.getInstance();
        boolean success = mgr.loadOfflineGame(this);

        if (success) {
            this.game = mgr.getGame();
            updateViews();
        } else {
            startNewGame();
        }
        SettingsManager setMgr = SettingsManager.getInstance(this);
        boardView.setDisplaySettings(setMgr.getSettings());
    }


    @Override
    public void makeMove(int piecePosition, int move) {
        game.makeMove(piecePosition, move);
        GameManager.getInstance().saveOfflineGame(this);
        updateViews();
    }

    private void updateGameMessage() {
        String message = null;
        switch (game.getState()) {
            case WHITE_WINS:
                message = "Checkmate! White wins.";
                break;
            case BLACK_WINS:
                message = "Checkmate! Black wins.";
                break;
            case WHITE_MOVES:
                message = "It's white's move.";
                break;
            case BLACK_MOVES:
                message = "It's black's move.";
                break;
            case DRAW:
                message = "It's a draw.";
                break;
        }
        gameMsg.setText(message);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.offline_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void newGame(MenuItem menuItem) {
        final DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    startNewGame();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        };

        Game.State state = game.getState();
        if (state == Game.State.WHITE_MOVES || state == Game.State.BLACK_MOVES) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setMessage("Current game will be lost. Are you sure?")
                    .setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("Cancel", dialogClickListener)
                    .show();
        } else {
            startNewGame();
        }
    }

    public void undoMove(MenuItem menuItem) {
        game.undoMove();
        updateViews();
    }

    public void redoMove(MenuItem menuItem) {
        game.redoMove();
        updateViews();
    }

    public void toggleFlip(MenuItem menuItem) {
        SettingsManager mgr = SettingsManager.getInstance(this);
        mgr.toggleRotateBoard(this);
        boardView.setDisplaySettings(mgr.getSettings());
        updateViews();
    }

    public void toggleFlipOnMove(MenuItem menuItem) {
        SettingsManager mgr = SettingsManager.getInstance(this);
        mgr.toggleRotateBoardOnEachMove(this);
        boardView.setDisplaySettings(mgr.getSettings());
        updateViews();
    }

    public void toggleFlipTop(MenuItem menuItem) {
        SettingsManager mgr = SettingsManager.getInstance(this);
        mgr.toggleRotateTopPieces(this);
        boardView.setDisplaySettings(mgr.getSettings());
        updateViews();
    }

    public void toggleLegalMoves(MenuItem menuItem) {
        SettingsManager mgr = SettingsManager.getInstance(this);
        mgr.toggleShowLegalMoves(this);
        boardView.setDisplaySettings(mgr.getSettings());
        updateViews();
    }

    public void toggleCoordinates(MenuItem menuItem) {
        SettingsManager mgr = SettingsManager.getInstance(this);
        mgr.toggleShowCoordinates(this);
        boardView.setDisplaySettings(mgr.getSettings());
        updateViews();
    }

    public void toggleLastMove(MenuItem menuItem) {
        SettingsManager mgr = SettingsManager.getInstance(this);
        mgr.toggleShowLastMove(this);
        boardView.setDisplaySettings(mgr.getSettings());
        updateViews();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.undoMove).setEnabled(game.canUndo());
        menu.findItem(R.id.redoMove).setEnabled(game.canRedo());
        DisplaySettings settings = SettingsManager.getInstance(this).getSettings();
        if (settings.isRotateBoard()) {
            menu.findItem(R.id.flipBoard).setTitle("Don't flip board");
        } else {
            menu.findItem(R.id.flipBoard).setTitle("Flip board");
        }
        if (settings.isRotateBoardOneEachMove()) {
            menu.findItem(R.id.flipOnMove).setTitle("Don't flip board on each move");
        } else {
            menu.findItem(R.id.flipOnMove).setTitle("Flip board on each move");
        }
        if (settings.isRotateTopPieces()) {
            menu.findItem(R.id.flipTop).setTitle("Don't flip top pieces");
        } else {
            menu.findItem(R.id.flipTop).setTitle("Flip top pieces");
        }
        if (settings.isShowLegalMoves()) {
            menu.findItem(R.id.legalMoves).setTitle("Hide legal moves");
        } else {
            menu.findItem(R.id.legalMoves).setTitle("Show legal moves");
        }
        if (settings.isShowCoordinates()) {
            menu.findItem(R.id.coordinates).setTitle("Hide coordinates");
        } else {
            menu.findItem(R.id.coordinates).setTitle("Show coordinates");
        }
        if (settings.isShowLastMove()) {
            menu.findItem(R.id.lastmove).setTitle("Hide last move");
        } else {
            menu.findItem(R.id.lastmove).setTitle("Show last move");
        }
        return true;
    }

    private void startNewGame() {
        GameManager.getInstance().deleteSavedData(this);
        GameManager.getInstance().newOfflineGame();
        game = GameManager.getInstance().getGame();
        updateViews();
    }

    private void updateViews() {
        boardView.setBoardState(game.getBoard());
        updateGameMessage();
        this.invalidateOptionsMenu();
    }
}
