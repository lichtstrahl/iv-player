package root.iv.ivplayer.ui.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import lombok.AllArgsConstructor;
import root.iv.ivplayer.R;
import root.iv.ivplayer.network.firebase.FBDataListener;
import root.iv.ivplayer.network.firebase.FBDatabaseAdapter;
import root.iv.ivplayer.network.firebase.dto.FBRoom;
import root.iv.ivplayer.ui.fragment.game.GameFragment;
import root.iv.ivplayer.ui.fragment.LoginFragment;
import root.iv.ivplayer.ui.fragment.game.ScreenParam;
import root.iv.ivplayer.ui.fragment.rooms.RoomsFragment;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements
        GameFragment.Listener,
        LoginFragment.Listener,
        RoomsFragment.Listener
{
    private static final int RC_SIGN_IN = 101;
    private static final String ARG_REORIENTATION = "arg:reorientation";

    private boolean rotateScreen = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        // Если это первый запуск Activity или смена конфигурации не связанная с повторотом: auth
        if (savedInstanceState == null || !savedInstanceState.getBoolean(ARG_REORIENTATION)) {
            auth();
        }

    }

    private void auth() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        Intent intent = AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).build();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                authSuccessful(FirebaseAuth.getInstance().getCurrentUser());
            } else {
                Toast.makeText(this, "Неудачный вход", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(ARG_REORIENTATION, rotateScreen);
    }

    @Override
    public void createGameFragment(ScreenParam screenParam) {
        if (screenParam.isFullScreen()) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        if (!screenParam.isVisibleActionBar() && getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        int currentOrientation = getRequestedOrientation();
        if (screenParam.getOrientation() != currentOrientation) {
            rotateScreen = true;
            setRequestedOrientation(screenParam.getOrientation());
        }
    }

    @Override
    public void stopGameFragment() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (getSupportActionBar() != null) getSupportActionBar().show();
        rotateScreen = true;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void clickRoom(String roomName) {
        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.mainFrame, GameFragment.getInstance(roomName, 2), GameFragment.TAG)
                .commit();
    }

    @Override
    public void authSuccessful(FirebaseUser user) {
        Timber.i("Игрок успешно вошёл");

        // Перед запуском удаляем существующие комнаты со своим именем
        FBDatabaseAdapter.getRooms()
                .addListenerForSingleValueEvent(new RoomsFBRemoveDead(user));

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainFrame, RoomsFragment.getInstance(), RoomsFragment.TAG)
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

    @AllArgsConstructor
    private class RoomsFBRemoveDead extends FBDataListener {
        private FirebaseUser user;

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            for (DataSnapshot room : dataSnapshot.getChildren()) {
                String roomName = room.getKey();
                FBRoom fbRoom = Objects.requireNonNull(room.getValue(FBRoom.class));
                if (fbRoom.isPresent(user.getUid()))
                    FBDatabaseAdapter.getRoom(roomName).removeValue();
            }
        }
    }
}
