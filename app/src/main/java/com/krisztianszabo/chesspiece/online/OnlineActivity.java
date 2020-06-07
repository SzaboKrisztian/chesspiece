package com.krisztianszabo.chesspiece.online;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.krisztianszabo.chesspiece.MainActivity;
import com.krisztianszabo.chesspiece.R;
import com.krisztianszabo.chesspiece.model.Game;
import com.krisztianszabo.chesspiece.model.Player;
import com.krisztianszabo.chesspiece.online.games.GameViewFragment;
import com.krisztianszabo.chesspiece.online.games.OnlineGameManager;
import com.krisztianszabo.chesspiece.online.lobby.ChatFragment;
import com.krisztianszabo.chesspiece.online.lobby.GamesListFragment;
import com.krisztianszabo.chesspiece.online.lobby.UserListFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Manager;
import io.socket.client.Socket;

public class OnlineActivity extends AppCompatActivity {

    private final String LOGIN_FRAGMENT = "Login fragment";
    private final String LOBBY_CHAT_FRAGMENT = "Lobby chat fragment";
    private final String USER_LIST_FRAGMENT = "User list fragment";
    private final String GAME_LIST_FRAGMENT = "Game list fragment";
    private final String GAME_FRAGMENT = "Game fragment";
    private String host;
    private LoginFragment loginFragment = new LoginFragment();
    private ChatFragment chatFragment = new ChatFragment();
    private UserListFragment userListFragment = new UserListFragment();
    private GamesListFragment gamesListFragment = new GamesListFragment();
    private Socket socket;
    private String username;
    private Integer gameToDisplay;
    private boolean gameMenuVisible = false;
    private boolean showCoordinates;
    private boolean showLastMove;
    private boolean showLegalMoves;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online);
        // Get stored host name
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        host = prefs.getString(MainActivity.HOST_KEY, "");

        if (host.isEmpty()) {
            Toast.makeText(this, "Host address not configured.", Toast.LENGTH_LONG).show();
            finish();
        }

        loginFragment.setParent(this);
        userListFragment.setParent(this);
        gamesListFragment.setParent(this);

        setShowCoordinates(prefs.getBoolean("showCoordinates", false));
        setShowLastMove(prefs.getBoolean("showLastMove", false));
        setShowLegalMoves(prefs.getBoolean("showLegalMoves", true));

        showLogin();
    }

    public String getUsername() {
        return username;
    }

    public String getHost() {
        return host;
    }

    public void setGameToDisplay(Integer gameToDisplay) {
        this.gameToDisplay = gameToDisplay;
    }

    public void setShowCoordinates(boolean showCoordinates) {
        this.showCoordinates = showCoordinates;
        SharedPreferences.Editor editor = PreferenceManager.
                getDefaultSharedPreferences(this).edit();
        editor.putBoolean("showCoordinates", showCoordinates).apply();
        invalidateOptionsMenu();
    }

    public void setShowLastMove(boolean showLastMove) {
        this.showLastMove = showLastMove;
        SharedPreferences.Editor editor = PreferenceManager.
                getDefaultSharedPreferences(this).edit();
        editor.putBoolean("showLastMove", showLastMove).apply();
        invalidateOptionsMenu();
    }

    public void setShowLegalMoves(boolean showLegalMoves) {
        this.showLegalMoves = showLegalMoves;
        SharedPreferences.Editor editor = PreferenceManager.
                getDefaultSharedPreferences(this).edit();
        editor.putBoolean("showLegalMoves", showLegalMoves).apply();
        invalidateOptionsMenu();
    }

    public boolean isShowCoordinates() {
        return showCoordinates;
    }

    public boolean isShowLastMove() {
        return showLastMove;
    }

    public boolean isShowLegalMoves() {
        return showLegalMoves;
    }

    public void isAuthenticated() {
        try {
            socket = IO.socket(host);
            socket.io().on(Manager.EVENT_TRANSPORT,
                    args -> SessionManager.getInstance().attachCookie(this, args));
            socket.on("lobby message", args -> addMessageAndNotify((JSONObject)args[0]))
                    .on("system message", args -> addMessageAndNotify((JSONObject)args[0]))
                    .on("userlist", args -> updateUserList((JSONObject)args[0]))
                    .on("challenge", args -> parseChallenge((JSONObject)args[0]))
                    .on("mygames", args -> parseMyGames((JSONObject)args[0]))
                    .on("game", args -> parseGame((JSONObject)args[0]));
            socket.connect();
            username = SessionManager.getInstance().getUsername(this);
            socket.emit("mygames");
        } catch (URISyntaxException e) {
            Log.e("SOCKET.IO", "Error opening socket: " + e);
        }
        if (socket != null) {
            findViewById(R.id.online_llt_button_container).setVisibility(View.VISIBLE);
            chatFragment.setSocket(socket);
            userListFragment.setSocket(socket);
            showChat(null);
        }
    }

    @Override
    protected void onStop() {
        if (socket != null && socket.connected()) {
            socket.disconnect();
        }
        super.onStop();
    }

    private void addMessageAndNotify(JSONObject message) {
        runOnUiThread(() -> chatFragment.addMessageAndUpdate(message));
    }

    private void updateUserList(JSONObject data) {
        try {
            JSONArray arr = data.getJSONArray("data");
            List<String> result = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                result.add(arr.getString(i));
            }
            runOnUiThread(() -> userListFragment.updateUsers(result));
        } catch (JSONException e) {
            Log.e("USERLIST", "Error parsing json data: " + e);
        }
    }

    private void showLogin() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.theFrame, loginFragment, LOGIN_FRAGMENT)
                .commit();
        invalidateOptionsMenu();
    }

    public void showChat(View view) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.theFrame, chatFragment, LOBBY_CHAT_FRAGMENT)
                .commit();
        invalidateOptionsMenu();
    }

    public void showUserList(View view) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.theFrame, userListFragment, USER_LIST_FRAGMENT)
                .commit();
        invalidateOptionsMenu();
    }

    public void showMyGames(View view) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.theFrame, gamesListFragment, GAME_LIST_FRAGMENT)
                .commit();
        invalidateOptionsMenu();
    }

    public void showGameView(int gameId, Game game) {
        GameViewFragment gameFragment = new GameViewFragment();
        gameFragment.setParent(this);
        gameFragment.setGame(gameId, game);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.theFrame, gameFragment, GAME_FRAGMENT)
                .commit();
        invalidateOptionsMenu();
    }

    public void requestGame(int gameId) {
        try {
            JSONObject msg = new JSONObject();
            msg.put("gameId", gameId);
            msg.put("action", "get");
            socket.emit("game", msg);
        } catch (JSONException e) {
            Toast.makeText(this, "Error sending game request", Toast.LENGTH_SHORT).show();
        }
    }

    private Fragment getFragmentIfVisible(String tag) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        return (fragment != null && fragment.isVisible()) ? fragment : null;
    }

    private void parseChallenge(JSONObject data) {
        try {
            switch (data.getString("intent")) {
                case "offer":
                    runOnUiThread(() -> showChallengePopup(data));
                    break;
                case "reject":
                    String msg = "Player " + data.getString("target")
                            + " has rejected your challenge.";
                    runOnUiThread(() -> showInfoPopup(msg));
                    break;
                case "accepted":
                    runOnUiThread(() -> {
                            Toast.makeText(this, "New game started",
                                    Toast.LENGTH_LONG).show();
                        }
                    );
                    break;
                case "error":
                    runOnUiThread(() -> {
                                try {
                                    showInfoPopup(data.getString("message"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                    );
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseMyGames(JSONObject data) {
        try {
            JSONArray games = data.getJSONArray("data");
            List<JSONObject> result = new ArrayList<>();
            for (int i = 0; i < games.length(); i++) {
                result.add(games.getJSONObject(i));
            }
            runOnUiThread(() -> gamesListFragment.setGames(result));
        } catch (JSONException e) {
            Toast.makeText(this, "Error parsing JSON game data.", Toast.LENGTH_LONG)
                    .show();
        }
    }

    private void parseGame(JSONObject data) {
        try {
            int gameId;
            switch (data.getString("action")) {
                case "send":
                    JSONObject gameData = data.getJSONObject("data");
                    OnlineGameManager.getInstance().updateGame(gameData, this);
                    break;
                case "resign":
                    gameId = data.getInt("gameId");
                    runOnUiThread(() -> {
                        GameViewFragment gameFragment =
                                (GameViewFragment)getFragmentIfVisible(GAME_FRAGMENT);
                        if (gameFragment != null && gameFragment.getGameId() == gameId) {
                            showInfoPopup("Your opponent resigned.");
                        } else {
                            Toast.makeText(this, "An opponent resigned.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
                case "draw":
                    gameId = data.getInt("gameId");
                    runOnUiThread(() -> {
                        GameViewFragment gameFragment =
                                (GameViewFragment)getFragmentIfVisible(GAME_FRAGMENT);
                        if (gameFragment != null && gameFragment.getGameId() == gameId) {
                            gameFragment.opponentOfferedDraw();
                            showInfoPopup("Your opponent offered a draw.");
                            invalidateOptionsMenu();
                        }
                    });
                    break;
                case "get messages":
                    try {
                        gameId = data.getInt("gameId");
                        GameViewFragment fragment =
                                (GameViewFragment)getFragmentIfVisible(GAME_FRAGMENT);
                        if (fragment != null && fragment.getGameId() == gameId) {
                            List<JSONObject> result = new ArrayList<>();
                            JSONArray messages = data.getJSONArray("data");
                            for (int i = 0; i < messages.length(); i++) {
                                result.add(messages.getJSONObject(i));
                            }
                            runOnUiThread(() -> fragment.setMessages(result));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case "send message":
                    try {
                        gameId = data.getInt("gameId");
                        GameViewFragment fragment =
                                (GameViewFragment)getFragmentIfVisible(GAME_FRAGMENT);
                        if (fragment != null && fragment.getGameId() == gameId) {
                            JSONObject message = data.getJSONObject("message");
                            runOnUiThread(() -> fragment.addMessage(message));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void gameUpdated(int gameId) {
        GameViewFragment gameFragment = (GameViewFragment)getFragmentIfVisible(GAME_FRAGMENT);
        if (gameToDisplay != null && gameToDisplay == gameId) {
            gameToDisplay = null;
            runOnUiThread(() -> OnlineGameManager.getInstance().getGame(gameId, this));
        } else if (gameFragment != null && gameFragment.getGameId() == gameId) {
            runOnUiThread(() -> gameFragment.updateGame(gameId));
        } else {
            runOnUiThread(() -> Toast.makeText(this, "A game has been updated.",
                    Toast.LENGTH_SHORT).show());
        }
    }

    public void emitOnSocket(String event, JSONObject data) {
        if (socket != null) {
            socket.emit(event, data);
        }
    }

    private void showChallengePopup(JSONObject data) {
        try {
            AlertDialog.Builder bld = new AlertDialog.Builder(this);
            bld.setMessage("Player " + data.getString("source")
                    + " is challenging you to a game. Accept?")
                    .setPositiveButton("Yes", ((dialog, which) -> {
                            try {
                                data.put("intent", "accept");
                                socket.emit("challenge", data);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }))
                    .setNegativeButton("No", ((dialog, which) -> {
                        try {
                            data.put("intent", "reject");
                            socket.emit("challenge", data);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }))
                    .show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showInfoPopup(String message) {
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setMessage(message).setNeutralButton("OK", ((dialog, which) -> dialog.dismiss()))
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Fragment fragment = getFragmentIfVisible(GAME_FRAGMENT);
        if (fragment == null) {
            gameMenuVisible = false;
            getMenuInflater().inflate(R.menu.online_menu, menu);
        } else {
            gameMenuVisible = true;
            getMenuInflater().inflate(R.menu.online_menu_game, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        GameViewFragment gameFragment = (GameViewFragment)getFragmentIfVisible(GAME_FRAGMENT);
        if (gameMenuVisible) {
            if (gameFragment != null) {
                MenuItem resign = menu.findItem(R.id.game_menu_resign);
                MenuItem draw = menu.findItem(R.id.game_menu_draw);
                if (gameFragment.isGameOver()) {
                    resign.setEnabled(false);
                    draw.setTitle("Offer draw");
                    draw.setEnabled(false);
                } else {
                    resign.setEnabled(true);
                    if (gameFragment.canOfferDraw()) {
                        draw.setTitle("Offer draw");
                        draw.setEnabled(true);
                    } else if (gameFragment.canAcceptDraw()) {
                        draw.setTitle("Accept draw");
                        draw.setEnabled(true);
                    } else {
                        draw.setTitle("Draw offered");
                        draw.setEnabled(false);
                    }
                }
            }
        }
        MenuItem menuShowCoordinates = menu.findItem(R.id.game_menu_setting_coordinates);
        if (showCoordinates) {
            menuShowCoordinates.setTitle("Hide coordinates");
        } else {
            menuShowCoordinates.setTitle("Show coordinates");
        }
        menuShowCoordinates.setOnMenuItemClickListener(item -> {
            setShowCoordinates(!showCoordinates);
            if (gameFragment != null) {
                gameFragment.updateViews();
            }
            return true;
        });
        MenuItem menuShowLegalMoves = menu.findItem(R.id.game_menu_setting_legal_moves);
        if (showLegalMoves) {
            menuShowLegalMoves.setTitle("Don't show legal moves");
        } else {
            menuShowLegalMoves.setTitle("Show legal moves");
        }
        menuShowLegalMoves.setOnMenuItemClickListener(item -> {
            setShowLegalMoves(!showLegalMoves);
            if (gameFragment != null) {
                gameFragment.updateViews();
            }
            return true;
        });
        MenuItem menuShowLastMove = menu.findItem(R.id.game_menu_setting_last_move);
        if (showLastMove) {
            menuShowLastMove.setTitle("Don't highlight last move");
        } else {
            menuShowLastMove.setTitle("Highlight last move");
        }
        menuShowLastMove.setOnMenuItemClickListener(item -> {
            setShowLastMove(!showLastMove);
            if (gameFragment != null) {
                gameFragment.updateViews();
            }
            return true;
        });
        return super.onPrepareOptionsMenu(menu);
    }

    public void logout(MenuItem menuItem) {
        SessionManager.getInstance().logout(this);
        findViewById(R.id.online_llt_button_container).setVisibility(View.GONE);
        chatFragment.clearData();
        userListFragment.clearData();
        gamesListFragment.clearData();
        username = null;
        showLogin();
        socket.disconnect();
    }

    public void resign(MenuItem menuItem) {
        GameViewFragment gameFragment = (GameViewFragment)getFragmentIfVisible(GAME_FRAGMENT);
        if (gameFragment != null) {
            AlertDialog.Builder bld = new AlertDialog.Builder(this);
            bld.setMessage("Are you sure you want to resign?");
            bld.setPositiveButton("Yes", (dialog, which) -> {
                try {
                    JSONObject data = new JSONObject();
                    data.put("gameId", gameFragment.getGameId());
                    data.put("action", "resign");
                    socket.emit("game", data);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
            bld.setNegativeButton("No", (dialog, which) -> dialog.cancel());
            bld.show();
        }
    }

    public void draw(MenuItem menuItem) {
        GameViewFragment gameFragment = (GameViewFragment)getFragmentIfVisible(GAME_FRAGMENT);
        if (gameFragment != null) {
            if (gameFragment.canOfferDraw() || gameFragment.canAcceptDraw()) {
                try {
                    JSONObject data = new JSONObject();
                    data.put("gameId", gameFragment.getGameId());
                    data.put("action", "draw");
                    data.put("player", gameFragment.getMyColor() == Player.WHITE ? 0 : 1);
                    socket.emit("game", data);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                gameFragment.playerOfferedDraw();
            }
        }
        invalidateOptionsMenu();
    }
}
