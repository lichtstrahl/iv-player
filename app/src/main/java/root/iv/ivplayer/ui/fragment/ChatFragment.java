package root.iv.ivplayer.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.pubnub.api.PubNub;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.disposables.CompositeDisposable;
import root.iv.ivplayer.R;
import root.iv.ivplayer.app.App;
import root.iv.ivplayer.network.ws.pubnub.PNUtilUUID;
import root.iv.ivplayer.network.ws.pubnub.callback.PNSubscribePrecenseCallback;
import root.iv.ivplayer.receiver.MsgReceiver;
import root.iv.ivplayer.service.ChatService;
import root.iv.ivplayer.service.ChatServiceConnection;
import timber.log.Timber;

public class ChatFragment extends Fragment implements MsgReceiver.Listener {
    private static final String TAG = "tag:ws";
    private static final String SAVE_VIEW = "save:viewMsg";
    private static final String SAVE_INPUT = "save:input";
    private static final String CHANNEL_NAME = "ch:global";

    @BindView(R.id.view)
    protected TextView viewMsg;
    @BindView(R.id.input)
    protected EditText input;
    @BindView(R.id.statusService)
    protected Switch viewStatusService;

    private CompositeDisposable disposable;
    private MsgReceiver msgReceiver;
    private ChatServiceConnection serviceConnection;

    public static ChatFragment getInstance() {
        return new ChatFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        ButterKnife.bind(this, view);


        disposable = new CompositeDisposable();
        msgReceiver = new MsgReceiver();
        msgReceiver.setListener(this);
        serviceConnection = new ChatServiceConnection();

        if (savedInstanceState != null)
            reload(savedInstanceState);


        viewStatusService.setOnCheckedChangeListener(this::changeStatus);

        if (ChatService.fromNotification(this.getActivity().getIntent())) {
            changeSwitch(true);
            ChatService.bind(this.getContext(), serviceConnection);
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVE_VIEW, viewMsg.getText().toString());
        outState.putString(SAVE_INPUT, input.getText().toString());
    }

    @Override
    public void onStart() {
        super.onStart();
        readMsgFromQueue();
        this.getContext().registerReceiver(msgReceiver, ChatService.getIntentFilter());
    }

    @Override
    public void onStop() {
        super.onStop();
        disposable.dispose();
        this.getContext().unregisterReceiver(msgReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        msgReceiver.removeListener();
    }

    @OnClick(R.id.buttonSend)
    protected void click(View view) {
        String text = input.getText().toString();
        if (!text.isEmpty()) {
            appendMsg("Я: " + text);
            input.getText().clear();

            if (serviceConnection.isBind())
                serviceConnection.send(text);
        }
    }

    @OnClick(R.id.buttonClear)
    protected void clickClear() {
        viewMsg.setText("");
    }

    @OnClick(R.id.buttonTest)
    protected void clickTest() {
        String msg = input.getText().toString();
        serviceConnection.publishMessageToChannel(msg, CHANNEL_NAME, null);
    }

    private void appendMsg(String msg) {
        String str = viewMsg.getText().toString();
        str = str.concat(msg).concat("\n");
        viewMsg.setText(str);
    }

    private void reload(@NonNull Bundle bundle) {
        String saveMsg = bundle.getString(SAVE_VIEW);
        viewMsg.setText(saveMsg);
        String saveInput = bundle.getString(SAVE_INPUT);
        input.setText(saveInput);
    }

    private void changeStatus(View view, boolean status) {
        if (status) {
            // Если мы подключаемся, то необходимо проверить введенное имя в поле
            String login = input.getText().toString();
            if (PNUtilUUID.valid(login)) {
                executeChatService(login);
            } else {
                Toast.makeText(
                                Objects.requireNonNull(this.getActivity()),
                                "Некорректное имя",
                                Toast.LENGTH_LONG)
                        .show();
                changeSwitch(false);
            }
        }
        else
            stopChatService();
    }

    // Чтение сообщений из очереди сервиса
    private void readMsgFromQueue() {
        if (serviceConnection.isBind()) {
            while (serviceConnection.countMsgInQueue() != 0) {
                String msg = serviceConnection.read();
                appendMsg("Некто: " + msg);
            }
        }
    }

    // Запуск сервиса и привязка к нему
    private void executeChatService(@NonNull String login) {
        ChatService.start(this.getContext(), login);
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
        serviceConnection.subscribeToChannel(CHANNEL_NAME);
    }

    private Void processPNmsg(PubNub pn, PNMessageResult pnMsg) {
        Objects.requireNonNull(this.getActivity())
                .runOnUiThread(() -> {
            String msg = pnMsg.getMessage().toString();
            Timber.tag(App.getTag()).i(pnMsg.toString());
            Toast.makeText(this.getContext(), msg, Toast.LENGTH_SHORT).show();
        });
        return null;
    }

    private Void processPNstatus(PubNub pn, PNStatus status) {
        Objects.requireNonNull(this.getActivity())
                .runOnUiThread(() -> {
            String msg = String.format(Locale.ENGLISH, "Status: %d from %s",
                    status.getStatusCode(), status.getUuid());
            Toast.makeText(this.getContext(), msg, Toast.LENGTH_SHORT).show();
        });
        return null;
    }

    private void processPNpresence(PubNub pn, PNPresenceEventResult presenceEvent) {
        String event = presenceEvent.getEvent();
        Timber.tag(App.getTag()).i("Event: %s", event);

        if (event.equals("join")) {
            String uuid = presenceEvent.getUuid();
            Timber.tag(App.getTag()).i("Join user %s", PNUtilUUID.parseLogin(uuid));
        }
    }

    // Отвязаться от сервиса. Нужно пометить флаг bind = false вручную, вызвав метов unbound
    private void unbindChatService() {
        if (serviceConnection.isBind()) {
            serviceConnection.unbound();
            ChatService.unbind(Objects.requireNonNull(this.getContext()), serviceConnection);
        }
    }

    // Отвязаться и остановить сервис
    private void stopChatService() {
        unbindChatService();
        ChatService.stop(this.getContext());
    }

    private void changeSwitch(boolean value) {
        viewStatusService.setOnCheckedChangeListener(null);
        viewStatusService.setChecked(value);
        viewStatusService.setOnCheckedChangeListener(this::changeStatus);
    }

    // interface Listener (MsgReceiver)
    @Override
    public void receive(Intent intent) {
        String action = Objects.requireNonNull(intent.getAction());

        switch (action) {
            case ChatService.ACTION_MSG:
                String msg = ChatService.getMessage(intent);
                serviceConnection.read();
                appendMsg("Некто: " + msg);
                break;

            case ChatService.ACTION_END:
                Timber.tag(App.getTag()).i("Activity: END");
                stopChatService();
                changeSwitch(false);
                break;

            case ChatService.ACTION_START:
                Timber.tag(App.getTag()).i("Activity: START");
                changeSwitch(true);
                break;
            default:
        }
    }
}
