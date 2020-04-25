package root.iv.ivplayer.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.pubnub.api.PubNub;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.presence.PNHereNowChannelData;
import com.pubnub.api.models.consumer.presence.PNHereNowOccupantData;
import com.pubnub.api.models.consumer.presence.PNHereNowResult;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

import java.util.Objects;

import root.iv.ivplayer.R;
import root.iv.ivplayer.app.App;
import root.iv.ivplayer.network.http.dto.UserEntityDTO;
import root.iv.ivplayer.network.ws.pubnub.PNUtil;
import root.iv.ivplayer.network.ws.pubnub.PresenceEvent;
import root.iv.ivplayer.network.ws.pubnub.callback.PNHereNowCallback;
import root.iv.ivplayer.network.ws.pubnub.callback.PNSubscribePrecenseCallback;
import root.iv.ivplayer.service.ChatService;
import root.iv.ivplayer.service.ChatServiceConnection;
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
    public static final String CHANNEL_NAME = "ch:global";
    private static final String SHARED_LOGIN_KEY = "shared:login";

    private static final FragmentTag FRAGMENT_CHAT = FragmentTag
            .builder()
            .fragment(ChatFragment.getInstance())
            .tag("fragment:chat")
            .build();

    private static final FragmentTag FRAGMENT_GAME = FragmentTag
            .builder()
            .fragment(GameFragment.getInstance())
            .tag("fragment:game")
            .build();

    private ChatServiceConnection serviceConnection;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        setFragment();

        serviceConnection = new ChatServiceConnection();
    }

    private void setFragment() {
        // Проверяем сохранился ли логин в преференсах. Если это первый вход, то необходимо зарегестрироваться
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        String currentLogin = sharedPreferences.getString(SHARED_LOGIN_KEY, "");

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainFrame, FRAGMENT_CHAT.getFragment(), FRAGMENT_CHAT.getTag())
                .commit();

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
    public void exitFromGameFragment() {
        // При выходе из игрового фрагмента отправляем в ChatFragment событие "как будто закрылась нотификация"
        // Чтобы переключить в нормально положение switch
        ChatFragment fragment = (ChatFragment) getSupportFragmentManager()
                .findFragmentByTag(FRAGMENT_CHAT.getTag());
        Intent stopServiceIntent = new Intent(ChatService.ACTION_END);
        Objects.requireNonNull(fragment).receive(stopServiceIntent);
        // И останавливаем сервис
        finishChatService();
    }

    @Override
    public void chatServiceStarted() {
        // Показываем игровой фрагмент
        Timber.tag(App.getTag()).i("Добавление нового фрагмента");
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.mainFrame, FRAGMENT_GAME.getFragment(), FRAGMENT_GAME.getTag())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void serviceBind() {
        Timber.i("bind");
        ChatService.bind(this.getClass(), this, serviceConnection);



    }

    @Override
    public void executeChatService(String login) {
        Timber.i("start");
        ChatService.start(this, login);
        serviceBind();

        // PubNub: Подписываемся на канал. Добавляем callback
        PNSubscribePrecenseCallback callback = new PNSubscribePrecenseCallback(
                this::processPNmsg,
                this::processPNstatus,
                this::processPNpresence,
                App::logE,
                true
        );
        serviceConnection.addListener(callback);
        serviceConnection.subscribeToChannel(CHANNEL_NAME);

        PNHereNowCallback hereNowCallback = new PNHereNowCallback(
                this::processPNHereNow,
                App::logE
        );

        serviceConnection.hereNow(hereNowCallback, CHANNEL_NAME);
    }

    @Override
    public void publishMessage(String msg) {
        serviceConnection.publishMessageToChannel(msg, CHANNEL_NAME, null);
    }

    @Override
    public void switchToFalse() {
        // Посылать в ChatFragment уже не нужно ничего. Переключатель перешел в положение false
        // Просто остановить сервис
        finishChatService();
    }

    // Прекращение любого взаимодействия с сервисом. Отписка и отключение
    private void finishChatService() {
        serviceConnection.unsubscribe();

        if (serviceConnection.isBind()) {
            Timber.i("unbind");
            serviceConnection.unbound();
            ChatService.unbind(this.getClass(), this, serviceConnection);
        }
        Timber.i("stop");
        ChatService.stop(this);
    }

    private Void processPNmsg(PubNub pn, PNMessageResult pnMsg) {
        runOnUiThread(() -> {
                    String msg = pnMsg.getMessage().toString();
                    Timber.tag(App.getTag()).i(pnMsg.toString());
                });
        return null;
    }

    private void processPNHereNow(PNHereNowResult hereNowResult, PNStatus status) {
        PNHereNowChannelData channelData = hereNowResult.getChannels().get(CHANNEL_NAME);

        if (channelData != null) {
            String deviceLogin = serviceConnection.getLoginDevice();

            boolean loginFree = channelData
                    .getOccupants()
                    .stream()
                    .map(PNHereNowOccupantData::getUuid)
                    .map(PNUtil::parseLogin)
                    .noneMatch(login -> login.equals(deviceLogin));

            if (!loginFree) {
                this.onBackPressed();
                Toast.makeText(this, "Логин занят", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Void processPNstatus(PubNub pn, PNStatus status) {
        return null;
    }

    private void processPNpresence(PubNub pn, PNPresenceEventResult presenceEvent) {
        String event = presenceEvent.getEvent();
        Timber.tag(App.getTag()).i("Event: %s", event);

        if (event.equals(PresenceEvent.JOIN)) {
            String uuid = presenceEvent.getUuid();
            Timber.tag(App.getTag()).i("Join user %s", PNUtil.parseLogin(uuid));
        }

        if (event.equals(PresenceEvent.LEAVE)) {
            String login = PNUtil.parseLogin(presenceEvent.getUuid());
            Timber.tag(App.getTag()).i("Leave user %s", login);
        }
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
