package root.iv.ivplayer.ui.activity;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import root.iv.ivplayer.R;
import root.iv.ivplayer.network.http.dto.server.UserEntityDTO;
import root.iv.ivplayer.ui.fragment.GameFragment;
import root.iv.ivplayer.ui.fragment.LoginFragment;
import root.iv.ivplayer.ui.fragment.RoomsFragment;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements
        GameFragment.Listener,
        LoginFragment.Listener
{
    public static final String CHANNEL_NAME = "ch:global";
    private static final String SHARED_LOGIN_KEY = "shared:login";

    private static final FragmentTag FRAGMENT_GAME = FragmentTag
            .builder()
            .fragment(GameFragment.getInstance())
            .tag("fragment:game")
            .build();

    private static final FragmentTag FRAGMENT_LOGIN = FragmentTag
            .builder()
            .fragment(LoginFragment.getInstance())
            .tag("fragment:login")
            .build();

    private static final FragmentTag FRAGMENT_ROOMS = FragmentTag
            .builder()
            .fragment(RoomsFragment.getInstance())
            .tag("fragment:rooms")
            .build();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        setFragment();
    }

    private void setFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainFrame, FRAGMENT_LOGIN.getFragment(), FRAGMENT_LOGIN.getTag())
                .commit();

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
    public void exitFromGameFragment() {

    }

    @Override
    public void authSuccessful(UserEntityDTO user) {
        Timber.i("Игрок успешно вошёл");
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainFrame, FRAGMENT_ROOMS.getFragment(), FRAGMENT_ROOMS.getTag())
                .commit();
    }

    // Удаление последнего отображенного фрагмента. Используется для ручного удаление игрового фрагмента
    private void removeLastFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        int countFragments = fragmentManager.getBackStackEntryCount();
        Fragment removedFragment = fragmentManager.getFragments().get(countFragments-1);
        fragmentManager
                .beginTransaction()
                .remove(removedFragment)
                .commit();
        fragmentManager.popBackStack();
    }
}
