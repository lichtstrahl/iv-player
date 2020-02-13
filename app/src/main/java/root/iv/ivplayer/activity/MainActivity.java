package root.iv.ivplayer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import io.reactivex.disposables.CompositeDisposable;
import root.iv.ivplayer.R;
import root.iv.ivplayer.receiver.MsgReceiver;
import root.iv.ivplayer.service.ChatService;

public class MainActivity extends AppCompatActivity implements MsgReceiver.Listener {
    private static final String TAG = "tag:ws";
    private static final String SAVE_VIEW = "save:view";
    private static final String SAVE_INPUT = "save:input";

    private Button button;
    private TextView view;
    private EditText input;
    private CompositeDisposable disposable;
    private MsgReceiver msgReceiver;
    private Switch viewStatusService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = this.findViewById(R.id.button);
        view = this.findViewById(R.id.view);
        input = this.findViewById(R.id.input);
        viewStatusService = this.findViewById(R.id.statusService);

        disposable = new CompositeDisposable();
        msgReceiver = new MsgReceiver();
        msgReceiver.setListener(this);

        if (savedInstanceState != null)
            reload(savedInstanceState);



        button.setOnClickListener(this::click);
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
        ChatService.start(this);
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

    private void click(View view) {
        String text = input.getText().toString();
        if (!text.isEmpty()) {
            appendMsg("Я: " + text);
            input.getText().clear();
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
            ChatService.start(this);
        else
            ChatService.stop(this);
    }

    @Override
    public void receive(Intent intent) {
        String action = intent.getAction();

        switch (action) {
            case ChatService.ACTION_MSG:
                String msg = ChatService.getMessage(intent);
                appendMsg("Некто: " + msg);
                break;

            case ChatService.ACTION_END:
                Log.i(TAG, "END");
                ChatService.stop(this);
                viewStatusService.setChecked(false);
                break;

            case ChatService.ACTION_START:
                Log.i(TAG, "START");
                viewStatusService.setChecked(true);
                break;
            default:

        }

    }
}
