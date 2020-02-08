package root.iv.ivplayer;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import root.iv.ivplayer.ws.EchoWSListener;
import root.iv.ivplayer.ws.WSHolder;
import root.iv.ivplayer.ws.WSUtil;

public class MainActivity extends AppCompatActivity {

    private Button button;
    private TextView view;
    private WSHolder wsHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = this.findViewById(R.id.button);
        view = this.findViewById(R.id.view);
        wsHolder = new WSHolder(WSUtil.templateURL(), new EchoWSListener());

        button.setOnClickListener(this::click);
    }

    @Override
    protected void onStart() {
        super.onStart();
        wsHolder.open();
    }

    @Override
    protected void onStop() {
        super.onStop();
        wsHolder.close();
    }

    private void click(View view) {
        wsHolder.send("hello");
    }
}
