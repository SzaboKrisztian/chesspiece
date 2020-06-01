package com.krisztianszabo.chesspiece;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.krisztianszabo.chesspiece.offline.OfflineActivity;
import com.krisztianszabo.chesspiece.online.OnlineActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        StringRequest boardRequest = new StringRequest(Request.Method.GET,
//                "http://10.0.2.2:3000/test", response -> {
//                    BoardState boardState = new BoardState();
//                    try {
//                        final JSONObject res = new JSONObject(response);
//                        boardState.loadState(res);
//                    } catch (JSONException e) {
//                        boardState.initStandardChess();
//                    } finally {
//                        boardView.setBoardState(boardState);
//                    }
//                }, error -> Log.e("JSON ERROR", "Request failed " + error.getMessage()));
//        RequestQueueManager.getInstance(MainActivity.this).addToRequestQueue(boardRequest);
    }

    public void playOffline(View view) {
        Intent intent = new Intent(this, OfflineActivity.class);
        startActivity(intent);
    }

    public void playOnline(View view) {
        Intent intent = new Intent(this, OnlineActivity.class);
        startActivity(intent);
    }
}
