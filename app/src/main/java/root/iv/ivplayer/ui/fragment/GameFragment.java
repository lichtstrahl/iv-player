package root.iv.ivplayer.ui.fragment;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import root.iv.ivplayer.R;
import root.iv.ivplayer.app.App;
import root.iv.ivplayer.game.TicTacTextures;
import root.iv.ivplayer.game.tictac.TicTacRoom;
import root.iv.ivplayer.game.room.Room;
import root.iv.ivplayer.game.room.RoomState;
import root.iv.ivplayer.game.scene.Scene;
import root.iv.ivplayer.game.view.GameView;
import timber.log.Timber;

public class GameFragment extends Fragment implements TicTacRoom.Listener {
    public static final String TAG = "fragment:game";
    private static final String ARG_ROOM_NAME = "arg:room-name";
    private static final String ARG_LOGIN = "arg:login";

    @BindView(R.id.gameView)
    protected GameView gameView;
    @BindView(R.id.switchRoomState)
    protected SwitchCompat switchRoomState;
    @BindView(R.id.labelRoomStatus)
    protected TextView labelRoomStatus;
    @BindView(R.id.bottomBlock)
    protected LinearLayout layout;
    @BindView(R.id.viewLoginPlayer1)
    protected TextView viewLogin1;
    @BindView(R.id.viewRolePlayer1)
    protected ImageView viewRole1;
    @BindView(R.id.viewLoginPlayer2)
    protected TextView viewLogin2;
    @BindView(R.id.viewRolePlayer2)
    protected ImageView viewRole2;
    @BindView(R.id.panelPlayer1)
    protected RelativeLayout panelPlayer1;
    @BindView(R.id.panelPlayer2)
    protected RelativeLayout panelPlayer2;
    @BindView(R.id.viewRoomName)
    protected TextView viewRoomName;

    private Listener listener;
    private Room room;
    private FirebaseAuth fbAuth;

    public static GameFragment getInstance(String roomName) {
        GameFragment fragment = new GameFragment();

        Bundle bundle = new Bundle();
        bundle.putString(ARG_ROOM_NAME, roomName);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game, container, false);
        ButterKnife.bind(this, view);
        listener.createGameFragment();

        fbAuth = FirebaseAuth.getInstance();

        Bundle args = Objects.requireNonNull(getArguments());
        String roomName = args.getString(ARG_ROOM_NAME, "<NO-NAME>");

        room = buildRoom(roomName);
        configGameView(room.getScene());
        room.addListener(this);

        viewRoomName.setText(roomName);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Timber.i("attach");
        if (context instanceof Listener) {
            listener = (Listener) context;
        } else
            Timber.tag(App.getTag()).w("Не раализован нужный интерфейс слушателя");
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        listener.stopGameFragment();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Timber.i("detach");
        room.exitFromRoom();
        listener = null;
    }

    @OnClick(R.id.viewRolePlayer1)
    protected void clickRole1() {
    }

    @Override
    public void updatePlayers(@Nullable String login1, @Nullable String login2) {
        viewLogin1.setText((login1 != null) ? login1 : "");
        viewLogin2.setText((login2 != null) ? login2 : "");
    }

    @Override
    public void changeStatus(RoomState roomState) {
        switchRoomState.setChecked(roomState == RoomState.GAME);
        labelRoomStatus.setText(roomState.getDescription());
    }

    @Override
    public void win(String email) {
        labelRoomStatus.setText("Игрок " + email + "выиграл");
        int color1 = viewLogin1.getText().toString().equals(email)
                ? Color.GREEN
                : Color.RED;

        int color2 = viewLogin2.getText().toString().equals(email)
                ? Color.GREEN
                : Color.RED;

        panelPlayer1.setBackgroundColor(color1);
        panelPlayer2.setBackgroundColor(color2);
    }

    @Override
    public void end() {
        labelRoomStatus.setText("Игра окончена");
        panelPlayer1.setBackgroundColor(Color.LTGRAY);
        panelPlayer2.setBackgroundColor(Color.LTGRAY);
    }

    private void configGameView(Scene scene) {
        gameView.loadScene(scene);
        gameView.setOnClickListener(scene.getMainController());
        gameView.setOnTouchListener(scene.getMainController());
    }

    private Room buildRoom(String name) {
        Resources resources = getResources();
        Context context = Objects.requireNonNull(getContext());

        Drawable square = resources.getDrawable(R.drawable.ic_square, context.getTheme());
        Drawable cross = resources.getDrawable(R.drawable.ic_cross, context.getTheme());
        Drawable circle = resources.getDrawable(R.drawable.ic_circle, context.getTheme());

        TicTacTextures textures = TicTacTextures
                .builder()
                .circle(circle)
                .cross(cross)
                .square(square)
                .background(Color.WHITE)
                .build();

        return new TicTacRoom(textures, name, fbAuth.getCurrentUser());
    }

    public interface Listener {
        void createGameFragment();
        void stopGameFragment();
    }
}
