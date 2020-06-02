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
import com.krisztianszabo.chesspiece.online.games.GameViewFragment;
import com.krisztianszabo.chesspiece.online.lobby.GamesListFragment;
import com.krisztianszabo.chesspiece.online.lobby.ChatFragment;
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
    private final String GAME_VIEW_FRAGMENT = "Game view fragment";
    private String host;
    private LoginFragment loginFragment = new LoginFragment();
    private ChatFragment chatFragment = new ChatFragment();
    private UserListFragment userListFragment = new UserListFragment();
    private GamesListFragment gamesListFragment = new GamesListFragment();
    private GameViewFragment gameViewFragment = new GameViewFragment();
    private Socket socket;
    private String username;

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
        gameViewFragment.setParent(this);

        showLogin();
    }

    public String getUsername() {
        return username;
    }

    public String getHost() {
        return host;
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
                    .on("game", args -> showGameView((JSONObject)args[0]));
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
    }

    public void showChat(View view) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.theFrame, chatFragment, LOBBY_CHAT_FRAGMENT)
                .commit();
    }

    public void showUserList(View view) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.theFrame, userListFragment, USER_LIST_FRAGMENT)
                .commit();
    }

    public void showMyGames(View view) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.theFrame, gamesListFragment, GAME_LIST_FRAGMENT)
                .commit();
    }

    private void showGameView(JSONObject data) {
        try {
            Game game = new Game();
            game.initFromJSON(data.getJSONObject("data"));
            gameViewFragment.setGame(game);
            Log.d("GAME", "Ready to show fragment");
//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.theFrame, gameViewFragment, GAME_VIEW_FRAGMENT)
//                    .commit();
        } catch (JSONException e) {
            Toast.makeText(this, "Error parsing game data.", Toast.LENGTH_SHORT).show();
        }
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

    private boolean isFragmentVisible(String tag) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        return fragment != null && fragment.isVisible();
    }

    private void parseChallenge(JSONObject data) {
        Log.d("CHALLENGE", "parseChallenge: " + data.toString());
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
                            try {
                                Toast.makeText(this, "New game started with id: "
                                        + data.getString("gameId"), Toast.LENGTH_LONG).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
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
        getMenuInflater().inflate(R.menu.online_menu, menu);
        return super.onCreateOptionsMenu(menu);
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
}
