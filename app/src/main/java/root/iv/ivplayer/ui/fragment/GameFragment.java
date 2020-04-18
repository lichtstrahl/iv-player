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

import butterknife.BindView;
import butterknife.ButterKnife;
import root.iv.ivplayer.R;
import root.iv.ivplayer.app.App;
import root.iv.ivplayer.game.TestScene;
import root.iv.ivplayer.game.controller.PlayerController;
import root.iv.ivplayer.game.object.ObjectGenerator;
import root.iv.ivplayer.game.object.Player;
import root.iv.ivplayer.game.view.GameView;
import root.iv.ivplayer.network.ws.pubnub.PNUtil;
import root.iv.ivplayer.network.ws.pubnub.PresenceEvent;
import root.iv.ivplayer.network.ws.pubnub.callback.PNSubscribePrecenseCallback;
import root.iv.ivplayer.network.ws.pubnub.dto.PlayerPositionDTO;
import root.iv.ivplayer.service.ChatService;
import root.iv.ivplayer.service.ChatServiceConnection;
import root.iv.ivplayer.ui.activity.MainActivity;
import timber.log.Timber;

public class GameFragment extends Fragment {

    @BindView(R.id.gameView)
    protected GameView gameView;

    private Listener listener;
    private ChatServiceConnection serviceConnection;
    private ObjectGenerator objectGenerator;
    private TestScene scene;
    private PlayerController playerController;

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

        scene = new TestScene();
        playerController = new PlayerController(this::processPosition);

        gameView.loadScene(scene);
        gameView.setOnClickListener(playerController);
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
        serviceConnection.unsubscribe(MainActivity.CHANNEL_NAME);
    }

    private Void processPNmsg(PubNub pn, PNMessageResult msg) {
        PlayerPositionDTO positionDTO = new Gson().fromJson(msg.getMessage().getAsString(), PlayerPositionDTO.class);
        Timber.tag(App.getTag()).i("Позиция %s изменилась", PNUtil.parseLogin(positionDTO.getUuid()));
        // Если такой игрок существует, то сдвигаем его, иначе создаём
        if (scene.findPlayer(positionDTO.getUuid())) {
            scene.movePlayer(
                    positionDTO.getUuid(),
                    Math.round(positionDTO.getX0()),
                    Math.round(positionDTO.getY0())
            );
        } else {
            Player newPlayer = new Player(objectGenerator.buildActor(
                    Math.round(positionDTO.getX0()),
                    Math.round(positionDTO.getY0())
            ), positionDTO.getUuid());
            scene.addDrawableObject(newPlayer);
        }
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
                    playerController.grabObject(newPlayer);
                }
                break;
        }
    }

    // Отправка информации о своих координатах в канал
    private void processPosition(PlayerPositionDTO positionDTO) {
        serviceConnection.publishMessageToChannel(
                new Gson().toJson(positionDTO), MainActivity.CHANNEL_NAME, null
        );
    }

    public interface Listener {
        void createGameFragment();
        void stopGameFragment();
    }
}
