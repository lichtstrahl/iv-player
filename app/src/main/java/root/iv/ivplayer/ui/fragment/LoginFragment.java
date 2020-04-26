package root.iv.ivplayer.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import root.iv.ivplayer.R;

public class LoginFragment extends Fragment {
    private static final String SHARED_LOGIN_KEY = "shared:login";

    @BindView(R.id.inputLogin)
    protected TextInputEditText inputLogin;
    @BindView(R.id.buttonEnter)
    protected MaterialButton buttonEnter;

    public static LoginFragment getInstance() {
        return new LoginFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        ButterKnife.bind(this, view);

        Activity parentActivity = Objects.requireNonNull(this.getActivity());
        // Проверяем сохранился ли логин в преференсах. Если это первый вход, то необходимо зарегестрироваться
        SharedPreferences sharedPreferences = parentActivity.getPreferences(Context.MODE_PRIVATE);
        String currentLogin = sharedPreferences.getString(SHARED_LOGIN_KEY, "");

        return view;
    }
}
