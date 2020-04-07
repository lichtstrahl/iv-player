package root.iv.ivplayer.network.ws.pubnub;

import androidx.annotation.NonNull;

import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;

import kotlin.jvm.functions.Function2;

public class PNPublishCallback extends PNCallback<PNPublishResult> {
    @NonNull
    private Function2<PNPublishResult, PNStatus, Void> handler;

    public PNPublishCallback(@NonNull Function2<PNPublishResult, PNStatus, Void> handler) {
        this.handler = handler;
    }

    @Override
    public void onResponse(PNPublishResult result, PNStatus status) {
        handler.invoke(result, status);
    }
}
