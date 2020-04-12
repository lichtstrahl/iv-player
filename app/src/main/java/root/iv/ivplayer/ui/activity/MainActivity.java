package root.iv.ivplayer.ui.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import root.iv.ivplayer.R;
import root.iv.ivplayer.app.App;
import root.iv.ivplayer.network.http.dto.UserEntityDTO;
import root.iv.ivplayer.ui.fragment.ChatFragment;
import root.iv.ivplayer.ui.fragment.GameFragment;
import root.iv.ivplayer.ui.fragment.RegisterFragment;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity
    implements
        RegisterFragment.Listener,
        GameFragment.Listener,
        ChatFragment.Listener
{
    private static final String SHARED_LOGIN_KEY = "shared:login";

    private int defaultFlags;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        setFragment();
        defaultFlags = getWindow().getAttributes().flags;
    }

    private void setFragment() {
        // Проверяем сохранился ли логин в преференсах. Если это первый вход, то необходимо зарегестрироваться
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        String currentLogin = sharedPreferences.getString(SHARED_LOGIN_KEY, "");

//        if (currentLogin != null && !currentLogin.isEmpty()) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mainFrame, ChatFragment.getInstance())
                    .commit();
//        } else {
//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.mainFrame, RegisterFragment.getInstance())
//                    .commit();
//        }
    }

    @Override
    public void registerSuccessful(UserEntityDTO dto) {
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SHARED_LOGIN_KEY, dto.getLogin());
        editor.apply();

        setFragment();
    }

    @Override
    public void createGameFragment() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
    }

    @Override
    public void stopGameFragment() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (getSupportActionBar() != null) getSupportActionBar().show();
    }

    @Override
    public void chatServiceStarted() {
        // Показываем игровой фрагмент
        Timber.tag(App.getTag()).i("Добавление нового фрагмента");
        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .add(R.id.mainFrame, GameFragment.getInstance())
                .commit();
    }
}
