package com.krisztianszabo.chesspiece.model;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class GameManager {
    private Game theGame;
    private static GameManager instance = new GameManager();

    private GameManager() {}

    public static GameManager getInstance() {
        return instance;
    }

    public Game getGame() {
        return theGame;
    }

    public void saveOfflineGame(Context context) {
        try {
            File outFile = new File(context.getCacheDir(), "game.dat");
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outFile));
            out.writeObject(theGame);
            out.close();
        } catch (IOException e) {
            Log.e("SAVE GAME", "Error saving game state to disk: " + e.toString());
        }
    }

    public boolean loadOfflineGame(Context context) {
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

    public void deleteSavedData(Context context) {
        File saveData = new File(context.getCacheDir(), "game.dat");
        if (saveData.exists()) {
            saveData.delete();
        }
    }

    public void newOfflineGame() {
        theGame = new Game();
        theGame.initOffline();
    }

    public void newOnlineGame(String opponent) {

    }

    public void sendInvite(String email) {

    }

    public void resign() {

    }

    public void offerDraw() {

    }
}
