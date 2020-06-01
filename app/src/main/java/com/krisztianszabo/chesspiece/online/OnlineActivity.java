package com.krisztianszabo.chesspiece.online;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.krisztianszabo.chesspiece.R;
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

    private final String HOST = "http://10.0.2.2:3310/";
    private ChatFragment chatFragment = new ChatFragment(this);
    private UserListFragment userListFragment = new UserListFragment(this);
    private Socket socket;
    private List<JSONObject> lobbyMessages = new ArrayList<>();
    private List<String> userList = new ArrayList<>();
    private List<JSONObject> myGames = new ArrayList<>();
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online);
        userListFragment.setUsers(userList);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.theFrame, new LoginFragment(this))
                .commit();
    }

    public List<JSONObject> getMessages() {
        return this.lobbyMessages;
    }

    public List<String> getUserList() {
        return this.userList;
    }

    public String getUsername() {
        return username;
    }

    public void isAuthenticated() {
        try {
            socket = IO.socket(HOST);
            socket.io().on(Manager.EVENT_TRANSPORT,
                    args -> SessionManager.getInstance().attachCookie(this, args));
            socket.on("lobby message", args -> addMessageAndNotify((JSONObject)args[0]))
                    .on("system message", args -> addMessageAndNotify((JSONObject)args[0]))
                    .on("userlist", args -> updateUserList((JSONObject)args[0]))
                    .on("challenge", args -> parseChallenge((JSONObject)args[0]))
                    .on("mygames", args -> parseMyGames((JSONObject)args[0]));
            socket.connect();
            username = SessionManager.getInstance().getUsername(this);
            Log.d("SOCKET.IO", "Socket.io connection successfully established to " + HOST);
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
        lobbyMessages.add(message);
        runOnUiThread(() -> chatFragment.updateChat());
    }

    private void updateUserList(JSONObject data) {
        try {
            JSONArray arr = data.getJSONArray("data");
            List<String> result = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                result.add(arr.getString(i));
            }
            this.userList.clear();
            this.userList.addAll(result);
            runOnUiThread(() -> userListFragment.update());
        } catch (JSONException e) {
            Log.e("USERLIST", "Error parsing json data: " + e);
        }
    }

    public void showChat(View view) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.theFrame, chatFragment)
                .commit();
    }

    public void showUserList(View view) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.theFrame, userListFragment)
                .commit();
    }

    public void showMyGames(View view) {
        socket.emit("mygames");
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
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseMyGames(JSONObject data) {
        Log.d("MYGAMES", data.toString());
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
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.theFrame, new LoginFragment(this))
                .commit();
        socket.disconnect();
    }
}
