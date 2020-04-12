package root.iv.ivplayer.network.ws.pubnub.callback;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pubnub.api.PubNub;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import kotlin.jvm.functions.Function2;

// Надстройка над обычным callback, здесь допускается обработка presence-событий
// Здесь используется более удобный класс BiConsumer вместо Function2
// Не придётся возвращать значение
public class PNSubscribePrecenseCallback extends PNSubscribeCallback {
    @Nullable
    private BiConsumer<PubNub, PNPresenceEventResult> presenceHandler;

    public PNSubscribePrecenseCallback(
            @Nullable Function2<PubNub, PNMessageResult, Void> msg,
            @Nullable Function2<PubNub, PNStatus, Void> status,
            @Nullable BiConsumer<PubNub, PNPresenceEventResult> presenceHandler,
            @NonNull Consumer<String> errorHandler,
            boolean ignoreSelfMessage) {
        super(msg, status, errorHandler, ignoreSelfMessage);
        this.presenceHandler = presenceHandler;
    }

    @Override
    public void presence(PubNub pubnub, PNPresenceEventResult presence) {
        super.presence(pubnub, presence);

        if (presenceHandler != null)
            presenceHandler.accept(pubnub, presence);
        else
            errorHandler.accept("Presence-событие пришло, но обработчик не задан");
    }
}
