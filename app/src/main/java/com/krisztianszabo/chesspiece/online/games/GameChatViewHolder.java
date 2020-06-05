package com.krisztianszabo.chesspiece.online.games;


import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.krisztianszabo.chesspiece.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

public class GameChatViewHolder extends RecyclerView.ViewHolder {

    private final static SimpleDateFormat TS_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'");
    private final static DateFormat OUTPUT_FORMAT =
            SimpleDateFormat.getDateTimeInstance();
    private TextView meta;
    private TextView content;

    public GameChatViewHolder(@NonNull View itemView) {
        super(itemView);

        meta = itemView.findViewById(R.id.lobby_chat_txt_meta);
        content = itemView.findViewById(R.id.lobby_chat_txt_content);
    }

    public void setMessage(JSONObject data) {
        try {
            Log.d("GAME CHAT", "timestamp: " + data.getString("createdAt"));
            String ts = "";
            try {
                Date dt = TS_FORMAT.parse(data.getString("createdAt"));
                ts = OUTPUT_FORMAT.format(dt);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String metaText = data.getString("author") + " said at " + ts;
            String contentText = URLDecoder.decode(data.getString("message"), "utf-8");
            meta.setText(metaText);
            content.setText(contentText);
        } catch (JSONException | UnsupportedEncodingException e) {
            content.setText("ERROR");
        }
    }
}
