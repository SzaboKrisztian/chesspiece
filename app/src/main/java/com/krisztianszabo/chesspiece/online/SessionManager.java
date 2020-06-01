package com.krisztianszabo.chesspiece.online;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

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

    private final static String HOST = "http://10.0.2.2:3000/";
    private final static String SET_COOKIE_KEY = "Set-Cookie";
    private final static String COOKIE_KEY = "Cookie";
    private static SessionManager instance;

    private SessionManager() { }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void login(Activity activity, String username, String password, AuthTest callback) {
        StringRequest request = new StringRequest(Request.Method.POST, HOST + "auth/login",
                response -> callback.respond(true), error -> callback.respond(false)) {
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
                    editor.putString(COOKIE_KEY, res);
                    editor.apply();
                }
                return super.parseNetworkResponse(response);
            }
        };
        RequestQueueManager.getInstance(activity).addToRequestQueue(request);
    }

    public void isAuthenticated(Activity activity, AuthTest callback) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        String sessionId = prefs.getString(COOKIE_KEY, null);

        if (sessionId != null) {
            StringRequest request = new StringRequest(Request.Method.GET, HOST + "auth/test",
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
}
