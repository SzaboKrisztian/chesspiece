package com.krisztianszabo.chesspiece.online;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.krisztianszabo.chesspiece.R;

public class LoginFragment extends Fragment implements AuthTest {

    private OnlineActivity parent;

    public LoginFragment(OnlineActivity parent) {
        this.parent = parent;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        TextView userField = view.findViewById(R.id.login_txt_user);
        TextView passField = view.findViewById(R.id.login_txt_pass);
        view.findViewById(R.id.login_btn_login).setOnClickListener(v -> {
            String username = userField.getText().toString();
            String password = passField.getText().toString();
            SessionManager.getInstance().login(parent, username, password, this);
        });
        SessionManager.getInstance().isAuthenticated(parent, this);
        return view;
    }


    @Override
    public void respond(boolean auth) {
        if (auth) {
            parent.isAuthenticated();
        }
    }
}
