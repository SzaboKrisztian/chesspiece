package com.krisztianszabo.chesspiece.online.lobby;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.krisztianszabo.chesspiece.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ChatViewHolder extends RecyclerView.ViewHolder {

    private TextView meta;
    private TextView content;

    public ChatViewHolder(@NonNull View itemView) {
        super(itemView);

        meta = itemView.findViewById(R.id.lobby_chat_txt_meta);
        content = itemView.findViewById(R.id.lobby_chat_txt_content);
    }

    public void setMessage(JSONObject data) {
        try {
            String metaText;
            String contentText;
            if (data.has("author")) {
                content.setTextColor(Color.BLACK);
                metaText = data.getString("author") + " said at "
                        + fmt(data.getLong("timestamp")) + ":";
                contentText = URLDecoder.decode(data.getString("content"), "utf-8");
            } else {
                content.setTextColor(Color.GRAY);
                metaText = "System message at " + fmt(data.getLong("timestamp"));
                contentText = data.getString("message");
            }
            meta.setText(metaText);
            content.setText(contentText);
        } catch (JSONException | UnsupportedEncodingException e) {
            content.setText("ERROR");
        }
    }

    private String fmt(long timestamp) {
        Date date = new Date(timestamp);
        return new SimpleDateFormat("hh:mm:ss", Locale.US).format(date);
    }
}
