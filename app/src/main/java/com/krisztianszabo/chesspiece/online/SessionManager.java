package com.krisztianszabo.chesspiece.online;

import android.app.Activity;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.socket.engineio.client.Transport;

public class SessionManager {

    private final static String SET_COOKIE_KEY = "Set-Cookie";
    private final static String COOKIE_KEY = "Cookie";
    private final static String USERNAME_KEY = "Username";
    private static SessionManager instance;

    private SessionManager() { }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void login(OnlineActivity activity, String username, String password, AuthTest callback) {
        String host = activity.getHost();
        StringRequest request = new StringRequest(Request.Method.POST, host + "auth/login",
                response -> callback.respond(true), error -> {
            Toast.makeText(activity, "Could not connect to host", Toast.LENGTH_LONG).show();
            callback.respond(false);
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("password", password);
                return params;
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String cookie = response.headers.get(SET_COOKIE_KEY);
                if (cookie != null) {
                    String res = cookie.substring(cookie.indexOf('=') + 1, cookie.indexOf(';'));
                    SharedPreferences.Editor editor = PreferenceManager.
                            getDefaultSharedPreferences(activity).edit();
                    editor.putString(USERNAME_KEY, username);
                    editor.putString(COOKIE_KEY, res);
                    editor.apply();
                }
                return super.parseNetworkResponse(response);
            }
        };
        RequestQueueManager.getInstance(activity).addToRequestQueue(request);
    }

    public void isAuthenticated(OnlineActivity activity, AuthTest callback) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        String sessionId = prefs.getString(COOKIE_KEY, null);
        String host = activity.getHost();

        if (sessionId != null) {
            StringRequest request = new StringRequest(Request.Method.GET, host + "auth/test",
                    response -> callback.respond(true),
                    error -> callback.respond(false)) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = super.getHeaders();

                    if (headers == null || headers.isEmpty()) {
                        headers = new HashMap<>();
                    }

                    addSessionCookie(headers, sessionId);

                    return headers;
                }
            };
            RequestQueueManager.getInstance(activity).addToRequestQueue(request);
        } else {
            callback.respond(false);
        }
    }

    public String getUsername(Activity activity) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        return prefs.getString(USERNAME_KEY, null);
    }

    public void addSessionCookie(Map<String, String> headers, String sessionId) {
        StringBuilder sb = new StringBuilder();
        sb.append("connect.sid=").append(sessionId);
        if (headers.containsKey(COOKIE_KEY)) {
            sb.append("; ").append(headers.get(COOKIE_KEY));
        }
        headers.put(COOKIE_KEY, sb.toString());
    }

    public void attachCookie(Activity activity, Object... args) {
        Transport transport = (Transport) args[0];

        transport.on(Transport.EVENT_REQUEST_HEADERS, args1 -> {
            @SuppressWarnings("unchecked")
            Map<String, List<String>> headers = (Map<String, List<String>>) args1[0];
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
            String sessionId = prefs.getString(COOKIE_KEY, null);
            headers.put(COOKIE_KEY, Arrays.asList("connect.sid=" + sessionId));
        });
    }

    public void logout(Activity activity) {
        SharedPreferences.Editor editor = PreferenceManager.
                getDefaultSharedPreferences(activity).edit();
        editor.remove(USERNAME_KEY).remove(COOKIE_KEY).apply();
    }
}
