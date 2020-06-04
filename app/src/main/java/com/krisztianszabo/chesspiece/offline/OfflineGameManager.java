package com.krisztianszabo.chesspiece.offline;

import android.content.Context;
import android.util.Log;

import com.krisztianszabo.chesspiece.model.Game;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class OfflineGameManager {
    private Game theGame;
    private static OfflineGameManager instance = new OfflineGameManager();

    private OfflineGameManager() {}

    static OfflineGameManager getInstance() {
        return instance;
    }

    public Game getGame() {
        return theGame;
    }

    void saveOfflineGame(Context context) {
        try {
            File outFile = new File(context.getCacheDir(), "game.dat");
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outFile));
            out.writeObject(theGame);
            out.close();
        } catch (IOException e) {
            Log.e("SAVE GAME", "Error saving game state to disk: " + e.toString());
        }
    }

    boolean loadOfflineGame(Context context) {
        ObjectInput in;
        File inFile = new File(context.getCacheDir(), "game.dat");
        try {
            in = new ObjectInputStream(new FileInputStream(inFile));
            Game game = (Game) in.readObject();
            in.close();
            this.theGame = game;
            return true;
        } catch (ClassNotFoundException | IOException e) {
            Log.e("LOAD GAME", "loadOfflineGame: " + e.toString());
            return false;
        }
    }

    void deleteSavedData(Context context) {
        File saveData = new File(context.getCacheDir(), "game.dat");
        if (saveData.exists()) {
            //noinspection ResultOfMethodCallIgnored
            saveData.delete();
        }
    }

    void newOfflineGame() {
        theGame = new Game();
        theGame.initOffline();
    }
}
