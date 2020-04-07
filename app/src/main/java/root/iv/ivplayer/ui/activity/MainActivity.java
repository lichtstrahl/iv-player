package root.iv.ivplayer.ui.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import root.iv.ivplayer.R;
import root.iv.ivplayer.network.http.dto.UserEntityDTO;
import root.iv.ivplayer.ui.fragment.ChatFragment;
import root.iv.ivplayer.ui.fragment.RegisterFragment;

public class MainActivity extends AppCompatActivity
    implements RegisterFragment.Listener
{
    private static final String SHARED_LOGIN_KEY = "shared:login";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setFragment();
    }

    private void setFragment() {
        // Проверяем сохранился ли логин в преференсах. Если это первый вход, то необходимо зарегестрироваться
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        String currentLogin = sharedPreferences.getString(SHARED_LOGIN_KEY, "");

        if (currentLogin != null && !currentLogin.isEmpty()) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mainFrame, ChatFragment.getInstance())
                    .commit();
        } else {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mainFrame, RegisterFragment.getInstance())
                    .commit();
        }
    }

    @Override
    public void registerSuccessful(UserEntityDTO dto) {
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SHARED_LOGIN_KEY, dto.getLogin());
        editor.apply();

        setFragment();
    }
}
