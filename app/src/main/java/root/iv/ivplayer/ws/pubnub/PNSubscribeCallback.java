package root.iv.ivplayer.ws.pubnub;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

import java.util.function.Consumer;

import kotlin.jvm.functions.Function2;

public class PNSubscribeCallback extends SubscribeCallback {
    @Nullable
    private Function2<PubNub, PNMessageResult, Void> receiverMsg;
    @Nullable
    private Function2<PubNub, PNStatus, Void> receiverStatus;
    @NonNull
    private Consumer<String> errorHandler;

    public PNSubscribeCallback(
            @Nullable Function2<PubNub, PNMessageResult, Void> msg,
            @Nullable Function2<PubNub, PNStatus, Void> status,
            @NonNull Consumer<String> errorHandler) {
        this.receiverMsg = msg;
        this.receiverStatus = status;
        this.errorHandler = errorHandler;
    }

    @Override
    public void status(PubNub pubnub, PNStatus status) {
        if (receiverStatus != null)
            receiverStatus.invoke(pubnub, status);
        else
            errorHandler.accept("Статус пришел, но обработчик не задан");
    }

    @Override
    public void message(PubNub pubnub, PNMessageResult message) {
        if (receiverMsg != null)
            receiverMsg.invoke(pubnub, message);
        else
            errorHandler.accept("Сообщение пришло, но обработчик не задан.");
    }

    @Override
    public void presence(PubNub pubnub, PNPresenceEventResult presence) {
        // Не работает, потому что
    }
}