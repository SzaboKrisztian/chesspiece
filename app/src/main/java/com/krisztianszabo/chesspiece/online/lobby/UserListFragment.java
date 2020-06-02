package com.krisztianszabo.chesspiece.online.lobby;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.krisztianszabo.chesspiece.R;
import com.krisztianszabo.chesspiece.online.OnlineActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.socket.client.Socket;

public class UserListFragment extends Fragment {

    private OnlineActivity parent;
    private ArrayAdapter<String> adapter;
    private List<String> users = new ArrayList<>();
    private Socket socket;

    public void setParent(OnlineActivity parent) {
        this.parent = parent;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.lobby_userlist, container, false);
        ListView userList = view.findViewById(R.id.lobby_userlist);
        adapter = new ArrayAdapter<>(parent, android.R.layout.simple_list_item_1, users);
        userList.setAdapter(adapter);
        userList.setOnItemClickListener((parent1, view1, position, id) -> {
            String opponentName = users.get(position);
            if (!opponentName.equals(parent.getUsername())) {
                DialogInterface.OnClickListener clickListener = (dialog, which) -> {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            sendChallenge(opponentName);
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            dialog.dismiss();
                    }
                };
                AlertDialog.Builder bld = new AlertDialog.Builder(parent);
                bld.setMessage("Do you want to challenge " + opponentName + " to a game?")
                        .setPositiveButton("Yes", clickListener)
                        .setNegativeButton("No", clickListener)
                        .show();
            }
        });
        return view;
    }

    public void updateUsers(List<String> usersData) {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        this.users.clear();
        this.users.addAll(usersData);
    }

    private void sendChallenge(String player) {
        try {
            JSONObject data = new JSONObject();
            data.put("intent", "offer");
            data.put("target", player);
            socket.emit("challenge", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void clearData() {
        users.clear();
        adapter.notifyDataSetChanged();
    }
}
