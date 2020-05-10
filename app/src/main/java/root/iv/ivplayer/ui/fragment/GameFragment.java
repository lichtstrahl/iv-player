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

import com.pubnub.api.PubNub;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import root.iv.ivplayer.R;
import root.iv.ivplayer.app.App;
import root.iv.ivplayer.game.TicTacTextures;
import root.iv.ivplayer.game.room.DuelRoom;
import root.iv.ivplayer.game.room.PlayerRoom;
import root.iv.ivplayer.game.room.RoomState;
import root.iv.ivplayer.game.view.GameView;
import timber.log.Timber;

public class GameFragment extends Fragment implements DuelRoom.Listener {

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

    private Listener listener;
    private PlayerRoom room;
    private Drawable square;
    private Drawable circle;
    private Drawable cross;

    public static GameFragment getInstance() {
        return new GameFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game, container, false);
        ButterKnife.bind(this, view);
        listener.createGameFragment();


        Resources resources = getResources();
        Context context = Objects.requireNonNull(getContext());

        square = resources.getDrawable(R.drawable.ic_square, context.getTheme());
        cross = resources.getDrawable(R.drawable.ic_cross, context.getTheme());
        circle = resources.getDrawable(R.drawable.ic_circle, context.getTheme());

        TicTacTextures textures = TicTacTextures
                .builder()
                .circle(circle)
                .cross(cross)
                .square(square)
                .background(Color.WHITE)
                .build();

        DuelRoom duelRoom = new DuelRoom(textures);
        duelRoom.addListener(this);
        this.room = duelRoom;

        gameView.loadScene(room.getScene());
        gameView.setOnClickListener(room.getScene().getMainController());
        gameView.setOnTouchListener(room.getScene().getMainController());

        labelRoomStatus.setText(room.getRoomState().getDescription());

        return view;
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
        Context context = Objects.requireNonNull(this.getContext());
        listener.exitFromGameFragment();
        listener = null;
    }

    @Override
    public void updatePlayers(@Nullable String login1, @Nullable String login2) {
        if (this.getActivity() != null)
            this.getActivity().runOnUiThread(() -> {
            viewLogin1.setText((login1 != null) ? login1 : "");

            viewLogin2.setText((login2 != null) ? login2 : "");
        });
    }

    @Override
    public void exit() {
        Objects.requireNonNull(this.getActivity())
                .runOnUiThread(() -> this.getActivity().onBackPressed());
    }

    @Override
    public void changeStatus(RoomState roomState) {
        if (this.getActivity() != null)
            this.getActivity()
                .runOnUiThread(() -> {
                    switchRoomState.setChecked(roomState == RoomState.GAME);
                    labelRoomStatus.setText(roomState.getDescription());
                });
    }

    @Override
    public void roomClosed() {

    }

    @Override
    public void win(String uuid) {
    }

    @Override
    public void end() {
        Objects.requireNonNull(this.getActivity())
                .runOnUiThread(() -> {
                    labelRoomStatus.setText("Игра окончена");
                    panelPlayer1.setBackgroundColor(Color.LTGRAY);
                    panelPlayer2.setBackgroundColor(Color.LTGRAY);
                });
    }

    public interface Listener {
        void createGameFragment();
        void stopGameFragment();
        void exitFromGameFragment();
    }
}
