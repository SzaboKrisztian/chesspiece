package com.krisztianszabo.chesspiece.online;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.krisztianszabo.chesspiece.R;
import com.krisztianszabo.chesspiece.online.lobby.ChatViewAdapter;
import com.krisztianszabo.chesspiece.online.lobby.LobbyFragment;

import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Manager;
import io.socket.client.Socket;

public class OnlineActivity extends AppCompatActivity {

    private LobbyFragment lobbyFragment = new LobbyFragment(this);
    private Socket socket;
    private List<JSONObject> lobbyMessages = new ArrayList<>();
    private ChatViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.theFrame, new LoginFragment(this))
                .commit();
    }

    public void isAuthenticated() {
        try {
            socket = IO.socket("http://10.0.2.2:3310/");
            socket.io().on(Manager.EVENT_TRANSPORT,
                    args -> SessionManager.getInstance().attachCookie(this, args));
            socket.on("lobby message", args -> addMessageAndNotify((JSONObject)args[0]))
                    .on("system message", args -> addMessageAndNotify((JSONObject)args[0]));
            socket.connect();
            Log.d("SOCKET.IO", "Socket.io connection successfully established to http://10.0.2.2:3310/");
        } catch (URISyntaxException e) {
            Log.e("SOCKET.IO", "Error opening socket: " + e);
        }
        if (socket != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.theFrame, lobbyFragment)
                    .commit();
            lobbyFragment.setSocket(socket);
        }
    }

    @Override
    protected void onStop() {
        if (socket != null && socket.connected()) {
            socket.disconnect();
        }
        super.onStop();
    }

    public void setAdapter(ChatViewAdapter adapter) {
        if (adapter != null) {
            adapter.setMessages(lobbyMessages);
        }
        this.adapter = adapter;
    }

    private void addMessageAndNotify(JSONObject message) {
        lobbyMessages.add(message);
        runOnUiThread(() -> {
            lobbyFragment.updateChat();
        });
    }
}
