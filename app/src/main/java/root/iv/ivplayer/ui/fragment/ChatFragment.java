package root.iv.ivplayer.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.disposables.CompositeDisposable;
import root.iv.ivplayer.R;
import root.iv.ivplayer.receiver.MsgReceiver;
import root.iv.ivplayer.service.ChatService;
import root.iv.ivplayer.service.ChatServiceConnection;

public class ChatFragment extends Fragment implements MsgReceiver.Listener {
    private static final String TAG = "tag:ws";
    private static final String SAVE_VIEW = "save:viewMsg";
    private static final String SAVE_INPUT = "save:input";

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
        if (status)
            executeChatService();
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
    private void executeChatService() {
        ChatService.start(this.getContext());
        ChatService.bind(this.getContext(), serviceConnection);
    }

    // Отвязаться от сервиса. Нужно пометить флаг bind = false вручную, вызвав метов unbound
    private void unbindChatService() {
        if (serviceConnection.isBind()) {
            serviceConnection.unbound();
            ChatService.unbind(this.getContext(), serviceConnection);
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
        String action = intent.getAction();

        switch (action) {
            case ChatService.ACTION_MSG:
                String msg = ChatService.getMessage(intent);
                serviceConnection.read();
                appendMsg("Некто: " + msg);
                break;

            case ChatService.ACTION_END:
                Log.i(TAG, "Activity: END");
                stopChatService();
                changeSwitch(false);
                break;

            case ChatService.ACTION_START:
                Log.i(TAG, "Activity: START");
                changeSwitch(true);
                break;
            default:
        }
    }
}
