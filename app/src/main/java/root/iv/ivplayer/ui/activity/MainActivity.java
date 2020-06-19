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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import lombok.AllArgsConstructor;
import root.iv.ivplayer.R;
import root.iv.ivplayer.game.GameType;
import root.iv.ivplayer.network.firebase.FBDataListener;
import root.iv.ivplayer.network.firebase.FBDatabaseAdapter;
import root.iv.ivplayer.network.firebase.dto.FBRoom;
import root.iv.ivplayer.ui.fragment.game.GameFragment;
import root.iv.ivplayer.ui.fragment.game.GameFragmentParams;
import root.iv.ivplayer.ui.fragment.game.ScreenParam;
import root.iv.ivplayer.ui.fragment.rooms.create.CreateRoomFragment;
import root.iv.ivplayer.ui.fragment.rooms.list.RoomsFragment;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements
        GameFragment.Listener,
        RoomsFragment.Listener
{
    private static final int RC_SIGN_IN = 101;
    private static final String ARG_REORIENTATION = "arg:reorientation";
    private static final String ARG_ROOM_NAME = "arg:room-name";
    private static final String ARG_GAME_TYPE = "arg:game-type";
    private static final String ARG_SCREEN_PARAM = "arg:screen-param";

    private boolean rotateScreen = false;
    private String roomName = "";
    private GameType gameType = null;
    private ScreenParam screenParam = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        // Если это первый запуск Activity или смена конфигурации не связанная с повторотом: auth
        if (savedInstanceState == null || !savedInstanceState.getBoolean(ARG_REORIENTATION)) {
            auth();
        }

        // Если это перезапуск Activity после смены ориентации экрана и тип игры задан, то старт GameFragment
        if (savedInstanceState != null && savedInstanceState.getBoolean(ARG_REORIENTATION) && savedInstanceState.getString(ARG_GAME_TYPE) != null){
            prepareScreen((ScreenParam) savedInstanceState.getSerializable(ARG_SCREEN_PARAM));
            GameType gType = GameType.valueOf(savedInstanceState.getString(ARG_GAME_TYPE));
            startGame(savedInstanceState.getString(ARG_ROOM_NAME), gType);
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
        outState.putString(ARG_ROOM_NAME, roomName);
        outState.putString(ARG_GAME_TYPE, (gameType != null) ? gameType.name() : null);
        outState.putSerializable(ARG_SCREEN_PARAM, screenParam);
    }

    @Override
    public void stopGameFragment() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (getSupportActionBar() != null) getSupportActionBar().show();
        rotateScreen = reorientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        gameType = null;
    }

    @Override
    public void clickRoom(String roomName, GameType gType) {
        ScreenParam sParam = GameFragmentParams.param(gType);

        // Готовим экран. Возможно был вызван поворот
        prepareScreen(sParam);

        // Если был запрошен поворот экрана, то передаём название комнаты и тип игры.
        // Если смены экрана не будет, то можно запустить игру прямо сейчас
        if (rotateScreen) {
            this.gameType = gType;
            this.roomName = roomName;
            this.screenParam = sParam;
        } else {
            startGame(roomName, gType);
        }
    }

    @Override
    public void clickCreateRoom() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.mainFrame, CreateRoomFragment.newInstance(), CreateRoomFragment.TAG)
                .addToBackStack(null)
                .commit();
    }

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

    private void startGame(String rName, GameType gType) {
        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.mainFrame, GameFragment.getInstance(rName, gType), GameFragment.TAG)
                .commit();
    }

    // Подготовка экрана, если это необходимо
    private void prepareScreen(ScreenParam screenParam) {
        if (screenParam.isFullScreen()) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        if (!screenParam.isVisibleActionBar() && getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        rotateScreen = reorientation(screenParam.getOrientation());
    }

    private boolean reorientation(int newOrientation) {
        int currentOrientation = getRequestedOrientation();
        if (newOrientation != currentOrientation) {
            setRequestedOrientation(newOrientation);
            return true;
        }

        return false;
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
