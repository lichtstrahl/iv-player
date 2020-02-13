package root.iv.ivplayer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.disposables.CompositeDisposable;
import root.iv.ivplayer.R;
import root.iv.ivplayer.receiver.MsgReceiver;
import root.iv.ivplayer.service.ChatService;
import root.iv.ivplayer.service.ChatServiceConnection;

public class MainActivity extends AppCompatActivity implements MsgReceiver.Listener {
    private static final String TAG = "tag:ws";
    private static final String SAVE_VIEW = "save:view";
    private static final String SAVE_INPUT = "save:input";

    @BindView(R.id.view)
    protected TextView view;
    @BindView(R.id.input)
    protected EditText input;
    @BindView(R.id.statusService)
    protected Switch viewStatusService;

    private CompositeDisposable disposable;
    private MsgReceiver msgReceiver;
    private ChatServiceConnection serviceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


        disposable = new CompositeDisposable();
        msgReceiver = new MsgReceiver();
        msgReceiver.setListener(this);
        serviceConnection = new ChatServiceConnection();

        if (savedInstanceState != null)
            reload(savedInstanceState);


        viewStatusService.setOnCheckedChangeListener(this::changeStatus);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVE_VIEW, view.getText().toString());
        outState.putString(SAVE_INPUT, input.getText().toString());
    }

    @Override
    protected void onStart() {
        super.onStart();
        readMsgFromQueue();
        registerReceiver(msgReceiver, ChatService.getIntentFilter());
    }

    @Override
    protected void onStop() {
        super.onStop();
        disposable.dispose();
        unregisterReceiver(msgReceiver);
    }

    @Override
    protected void onDestroy() {
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

    private void appendMsg(String msg) {
        String str = view.getText().toString();
        str = str.concat(msg).concat("\n");
        view.setText(str);
    }

    private void reload(@NonNull Bundle bundle) {
        String saveMsg = bundle.getString(SAVE_VIEW);
        view.setText(saveMsg);
        String saveInput = bundle.getString(SAVE_INPUT);
        input.setText(saveInput);
    }

    private void changeStatus(View view, boolean status) {
        if (status)
            executeChatService();
        else
            unbindChatService();
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
        ChatService.start(this);
        ChatService.bind(this, serviceConnection);
    }

    // Отвязаться от сервиса. Нужно пометить флаг bind = false вручную, вызвав метов unbound
    private void unbindChatService() {
        if (serviceConnection.isBind()) {
            serviceConnection.unbound();
            ChatService.unbind(this, serviceConnection);
        }
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
                unbindChatService();
                viewStatusService.setChecked(false);
                break;

            case ChatService.ACTION_START:
                Log.i(TAG, "Activity: START");
                viewStatusService.setChecked(true);
                break;
            default:
        }
    }
}
