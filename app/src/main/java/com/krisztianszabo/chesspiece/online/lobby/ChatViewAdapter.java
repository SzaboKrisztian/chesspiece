package com.krisztianszabo.chesspiece.online.lobby;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.krisztianszabo.chesspiece.R;

import org.json.JSONObject;

import java.util.List;

public class ChatViewAdapter extends RecyclerView.Adapter<ChatViewHolder> {

    private List<JSONObject> messages;

    public void setMessages(List<JSONObject> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChatViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.lobby_chat_message, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        holder.setMessage(messages.get(position));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
}
