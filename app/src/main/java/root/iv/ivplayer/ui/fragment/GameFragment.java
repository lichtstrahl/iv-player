package root.iv.ivplayer.ui.fragment;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
import root.iv.ivplayer.game.view.GameView;
import root.iv.ivplayer.network.ws.pubnub.PNUtil;
import root.iv.ivplayer.network.ws.pubnub.PresenceEvent;
import root.iv.ivplayer.network.ws.pubnub.callback.PNSubscribePrecenseCallback;
import root.iv.ivplayer.service.ChatService;
import root.iv.ivplayer.service.ChatServiceConnection;
import timber.log.Timber;

public class GameFragment extends Fragment {

    @BindView(R.id.gameView)
    protected GameView gameView;
    @BindView(R.id.switchRoomState)
    protected SwitchCompat switchRoomState;
    @BindView(R.id.labelRoomStatus)
    protected TextView labelRoomStatus;
    @BindView(R.id.bottomBlock)
    protected LinearLayout layout;

    private Listener listener;
    private ChatServiceConnection serviceConnection;
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

        DuelRoom duelRoom = new DuelRoom(serviceConnection, textures);
        duelRoom.addChangeStatusListener(this::changeSwitchRoomStatus);
        duelRoom.addWinListener(this::win);
        this.room = duelRoom;

        gameView.loadScene(room.getScene());
        gameView.setOnClickListener(room.getScene().getMainController());
        gameView.setOnTouchListener(room.getScene().getMainController());

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Timber.i("attach");
        if (context instanceof Listener) {
            listener = (Listener) context;
            serviceConnection = new ChatServiceConnection();
            ChatService.bind(this.getClass(), this.getContext(), serviceConnection);
        } else
            Timber.tag(App.getTag()).w("Не раализован нужный интерфейс слушателя");
    }

    @Override
    public void onStart() {
        super.onStart();

        // PubNub: Подписываемся на канал. Добавляем callback
        PNSubscribePrecenseCallback callback = new PNSubscribePrecenseCallback(
                this::processPNmsg,
                this::processPNstatus,
                this::processPNpresence,
                App::logE,
                true
        );
        serviceConnection.addListener(callback);

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
        ChatService.unbind(this.getClass(), context, serviceConnection);
        listener.exitFromGameFragment();
        listener = null;
    }

    private Void processPNmsg(PubNub pn, PNMessageResult msg) {
        room.receiveMsg(msg);
        return null;
    }

    private Void processPNstatus(PubNub pn, PNStatus status) {
        Timber.tag(App.getTag()).i("GAME: status");
        return null;
    }

    private void processPNpresence(PubNub pn, PNPresenceEventResult presenceEvent) {
        String uuid = presenceEvent.getUuid();
        String login = PNUtil.parseLogin(uuid);
        String event = presenceEvent.getEvent();
        Timber.tag(App.getTag()).i("GAME: event: %s user %s", event, login);

        // Сообщаем об изменении количества игроков сцене
        switch (event) {
            case PresenceEvent.JOIN:
                room.joinPlayer(presenceEvent.getUuid());
                break;
            case PresenceEvent.LEAVE:
                room.leavePlayer(presenceEvent.getUuid());
                break;
        }
    }

    private void changeSwitchRoomStatus(Boolean active) {
        Objects.requireNonNull(this.getActivity())
                .runOnUiThread(() -> switchRoomState.setChecked(active));
    }

    private void win(String uuid) {
        Objects.requireNonNull(this.getActivity())
                .runOnUiThread(() -> {
                    String winMsg = String.format(Locale.ENGLISH, "Игрок %s победил!", PNUtil.parseLogin(uuid));
                    labelRoomStatus.setText(winMsg);

                    int color = (serviceConnection.getSelfUUID().equals(uuid))
                            ? Color.GREEN
                            : Color.RED;
                    layout.setBackgroundColor(color);
                });
    }

    public interface Listener {
        void createGameFragment();
        void stopGameFragment();
        void exitFromGameFragment();
    }
}
