package root.iv.ivplayer.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.pubnub.api.PubNub;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import root.iv.ivplayer.R;
import root.iv.ivplayer.app.App;
import root.iv.ivplayer.game.TestScene;
import root.iv.ivplayer.game.controller.MoveController;
import root.iv.ivplayer.game.object.Actor;
import root.iv.ivplayer.game.object.ObjectGenerator;
import root.iv.ivplayer.game.object.Player;
import root.iv.ivplayer.game.view.GameView;
import root.iv.ivplayer.network.ws.pubnub.PNUtilUUID;
import root.iv.ivplayer.network.ws.pubnub.PresenceEvent;
import root.iv.ivplayer.network.ws.pubnub.callback.PNSubscribePrecenseCallback;
import root.iv.ivplayer.service.ChatService;
import root.iv.ivplayer.service.ChatServiceConnection;
import timber.log.Timber;

public class GameFragment extends Fragment {

    @BindView(R.id.gameView)
    protected GameView gameView;

    private Listener listener;
    private ChatServiceConnection serviceConnection;
    private ObjectGenerator objectGenerator;
    private TestScene scene;
    private MoveController moveController;

    public static GameFragment getInstance() {
        return new GameFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game, container, false);
        ButterKnife.bind(this, view);
        listener.createGameFragment();


        objectGenerator = new ObjectGenerator();
        objectGenerator.setDrawable(this.getContext(), R.drawable.iv_yonatan_mid);
        objectGenerator.setFixSize(200, 200);

        scene = new TestScene(new ArrayList<>());
        moveController = new MoveController();

        gameView.loadScene(scene);
        gameView.setOnClickListener(moveController);
        serviceConnection = new ChatServiceConnection();
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Listener)
            listener = (Listener) context;
        else
            Timber.tag(App.getTag()).w("Не раализован нужный интерфейс слушателя");
    }

    @Override
    public void onStart() {
        super.onStart();
        ChatService.bind(this.getContext(), serviceConnection);

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
        listener = null;
    }

    private Void processPNmsg(PubNub pn, PNMessageResult msg) {
        Timber.tag(App.getTag()).i("GAME: msg");
        return null;
    }

    private Void processPNstatus(PubNub pn, PNStatus status) {
        Timber.tag(App.getTag()).i("GAME: status");
        return null;
    }

    private void processPNpresence(PubNub pn, PNPresenceEventResult presenceEvent) {
        String uuid = presenceEvent.getUuid();
        String login = PNUtilUUID.parseLogin(uuid);
        String event = presenceEvent.getEvent();
        Timber.tag(App.getTag()).i("GAME: event: %s user %s", event, login);

        // При входе нового игрока:
        // Если пришло сообщение о собственном входе, то оповещаем об этом остальных игроков.
        // И помещаем данного актера под своё управление
        switch (event) {
            case PresenceEvent.JOIN:
                String selfUUID = serviceConnection.getSelfUUID();
                String joinUUID = presenceEvent.getUuid();

                Player newPlayer = new Player(objectGenerator.buildActor(10, 100), joinUUID);
                scene.addDrawableObject(newPlayer);

                if (selfUUID.equalsIgnoreCase(joinUUID)) {
                    moveController.grabObject(newPlayer);
                }
                break;
        }
    }

    public interface Listener {
        void createGameFragment();
        void stopGameFragment();
    }
}
