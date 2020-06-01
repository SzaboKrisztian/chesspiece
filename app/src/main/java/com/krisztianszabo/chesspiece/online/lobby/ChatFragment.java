package com.krisztianszabo.chesspiece.online.lobby;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.krisztianszabo.chesspiece.R;
import com.krisztianszabo.chesspiece.online.OnlineActivity;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;

public class ChatFragment extends Fragment {

    private OnlineActivity parent;
    private Socket socket;
    private TextView chatInput;
    private RecyclerView chatOutput;
    private ChatViewAdapter adapter;

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public ChatFragment(OnlineActivity parent) {
        this.parent = parent;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lobby, container, false);
        chatInput = view.findViewById(R.id.lobby_txt_input);
        chatOutput = view.findViewById(R.id.lobby_rec_chat);

        LinearLayoutManager lmgr = new LinearLayoutManager(this.getContext());
        lmgr.setStackFromEnd(true);

        chatOutput.setLayoutManager(lmgr);
        adapter = new ChatViewAdapter();
        adapter.setMessages(parent.getMessages());
        chatOutput.setAdapter(adapter);

        view.findViewById(R.id.lobby_btn_send).setOnClickListener(v -> {
            String msg = chatInput.getText().toString();
            if (!msg.isEmpty() && socket != null) {
                JSONObject res = new JSONObject();
                try {
                    res.put("content", msg);
                    socket.emit("lobby message", res);
                    chatInput.setText("");
                } catch (JSONException e) {
                    Log.e("JSON ERROR", e.toString());
                }
            }
        });
        return view;
    }

    public void updateChat() {
        if (adapter != null && chatOutput != null) {
            adapter.notifyDataSetChanged();
            chatOutput.smoothScrollToPosition(adapter.getItemCount() - 1);
        }
    }
}
