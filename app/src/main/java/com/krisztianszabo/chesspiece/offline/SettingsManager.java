package com.krisztianszabo.chesspiece.offline;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SettingsManager {
    private DisplaySettings settings;
    private static SettingsManager instance;

    public static SettingsManager getInstance(Context context) {
        if (instance == null) {
            instance = new SettingsManager();
            boolean success = instance.loadSettings(context);
            if (!success) {
                instance.settings = new DisplaySettings();
                instance.saveSettings(context);
            }
        }
        return instance;
    }

    public DisplaySettings getSettings() {
        return this.settings;
    }

    public void toggleRotateBoard(Context context) {
        settings.setRotateBoard(!settings.isRotateBoard());
        saveSettings(context);
    }

    public void toggleRotateBoardOnEachMove(Context context) {
        settings.setRotateBoardOneEachMove(!settings.isRotateBoardOneEachMove());
        saveSettings(context);
    }

    public void toggleRotateTopPieces(Context context) {
        settings.setRotateTopPieces(!settings.isRotateTopPieces());
        saveSettings(context);
    }

    public void toggleShowLegalMoves(Context context) {
        settings.setShowLegalMoves(!settings.isShowLegalMoves());
        saveSettings(context);
    }

    public void toggleShowCoordinates(Context context) {
        settings.setShowCoordinates(!settings.isShowCoordinates());
        saveSettings(context);
    }

    public void toggleShowLastMove(Context context) {
        settings.setShowLastMove(!settings.isShowLastMove());
        saveSettings(context);
    }

    public void saveSettings(Context context) {
        try {
            File outFile = new File(context.getCacheDir(), "settings.dat");
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outFile));
            out.writeObject(this.settings);
            out.close();
        } catch (IOException e) {
            Log.e("SETTINGS", "save: " + e.toString());
        }
    }

    public boolean loadSettings(Context context) {
        ObjectInput in;
        File inFile = new File(context.getCacheDir(), "settings.dat");
        try {
            in = new ObjectInputStream(new FileInputStream(inFile));
            DisplaySettings settings = (DisplaySettings) in.readObject();
            in.close();
            this.settings = settings;
            return true;
        } catch (ClassNotFoundException | IOException e) {
            Log.e("SETTINGS", "load: " + e.toString());
            return false;
        }
    }
}
