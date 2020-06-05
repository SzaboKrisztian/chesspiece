package com.krisztianszabo.chesspiece.online.games;

import android.util.Log;

import com.krisztianszabo.chesspiece.model.Game;
import com.krisztianszabo.chesspiece.online.OnlineActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OnlineGameManager {

    private static OnlineGameManager instance;
    private Map<Integer, Game> games = new ConcurrentHashMap<>();

    private OnlineGameManager() {}

    public static OnlineGameManager getInstance() {
        if (instance == null) {
            instance = new OnlineGameManager();
        }
        return instance;
    }

    public void getGame(int gameId, OnlineActivity activity) {
        if (games.containsKey(gameId)) {
            activity.showGameView(gameId, games.get(gameId));
        } else {
            activity.setGameToDisplay(gameId);
            activity.requestGame(gameId);
        }
    }

    public void updateGame(JSONObject data, OnlineActivity activity) {
        Game game = new Game();
        try {
            game.initFromJSON(data);
            int id = data.getJSONObject("meta").getInt("id");
            games.put(id, game);
            activity.gameUpdated(id);
        } catch (JSONException e) {
            Log.e("GAME", "Error parsing JSON game data.");
            e.printStackTrace();
        }
    }
}
