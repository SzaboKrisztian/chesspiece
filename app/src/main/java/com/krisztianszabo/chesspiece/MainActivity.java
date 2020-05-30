package com.krisztianszabo.chesspiece;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.krisztianszabo.chesspiece.model.BoardState;

import org.json.JSONException;
import org.json.JSONObject;

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

    }
}
