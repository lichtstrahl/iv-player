package root.iv.ivplayer.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.pubnub.api.PubNub;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import root.iv.ivplayer.R;
import root.iv.ivplayer.app.App;
import root.iv.ivplayer.game.object.ObjectGenerator;
import root.iv.ivplayer.game.scene.MPScene;
import root.iv.ivplayer.game.scene.Scene;
import root.iv.ivplayer.game.view.GameView;
import root.iv.ivplayer.network.ws.pubnub.PNUtil;
import root.iv.ivplayer.network.ws.pubnub.PresenceEvent;
import root.iv.ivplayer.network.ws.pubnub.callback.PNSubscribePrecenseCallback;
import root.iv.ivplayer.network.ws.pubnub.dto.PlayerPositionDTO;
import root.iv.ivplayer.service.ChatService;
import root.iv.ivplayer.service.ChatServiceConnection;
import timber.log.Timber;

public class GameFragment extends Fragment {

    @BindView(R.id.gameView)
    protected GameView gameView;

    private Listener listener;
    private ChatServiceConnection serviceConnection;
    private Scene scene;

    public static GameFragment getInstance() {
        return new GameFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game, container, false);
        ButterKnife.bind(this, view);
        listener.createGameFragment();


        ObjectGenerator objectGenerator = new ObjectGenerator();
        objectGenerator.setDrawable(this.getContext(), R.drawable.iv_yonatan_mid);
        objectGenerator.setFixSize(200, 200);

        scene = new MPScene(objectGenerator, serviceConnection);

        gameView.loadScene(scene);
        gameView.setOnClickListener(scene.getMainController());

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
        PlayerPositionDTO positionDTO = new Gson().fromJson(msg.getMessage().getAsString(), PlayerPositionDTO.class);
        Timber.tag(App.getTag()).i("Позиция %s изменилась", PNUtil.parseLogin(positionDTO.getUuid()));
        scene.processPlayerPositionDTO(positionDTO);
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
                scene.joinPlayer(presenceEvent.getUuid(), 10, 100);
                break;
            case PresenceEvent.LEAVE:
                scene.leavePlayer(presenceEvent.getUuid());
                break;
        }
    }

    public interface Listener {
        void createGameFragment();
        void stopGameFragment();
        void exitFromGameFragment();
    }
}
