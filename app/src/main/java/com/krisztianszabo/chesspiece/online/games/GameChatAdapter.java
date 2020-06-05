package com.krisztianszabo.chesspiece.online.games;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.krisztianszabo.chesspiece.R;

import org.json.JSONObject;

import java.util.List;

public class GameChatAdapter extends RecyclerView.Adapter<GameChatViewHolder> {

    private List<JSONObject> messages;

    public GameChatAdapter(List<JSONObject> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public GameChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GameChatViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_message, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull GameChatViewHolder holder, int position) {
        holder.setMessage(messages.get(position));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
}
