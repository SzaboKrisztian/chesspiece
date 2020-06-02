package com.krisztianszabo.chesspiece;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.krisztianszabo.chesspiece.offline.OfflineActivity;
import com.krisztianszabo.chesspiece.online.OnlineActivity;

public class MainActivity extends AppCompatActivity {

    public static final String HOST_KEY = "Host address";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void playOffline(View view) {
        Intent intent = new Intent(this, OfflineActivity.class);
        startActivity(intent);
    }

    public void playOnline(View view) {
        Intent intent = new Intent(this, OnlineActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void setHost(MenuItem menuItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set host address");

        final EditText input = new EditText(this);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        input.setText(prefs.getString(MainActivity.HOST_KEY, ""));
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", ((dialog, which) -> {
            SharedPreferences.Editor editor = PreferenceManager.
                    getDefaultSharedPreferences(this).edit();
            editor.putString(HOST_KEY, input.getText().toString()).apply();
        }));
        builder.setNegativeButton("Cancel", ((dialog, which) -> dialog.cancel()));

        builder.show();
    }
}
