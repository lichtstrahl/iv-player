package root.iv.ivplayer.network.ws.pubnub.callback;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.presence.PNHereNowResult;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import root.iv.ivplayer.app.App;
import timber.log.Timber;

public class PNHereNowCallback extends PNCallback<PNHereNowResult> {
    @Nullable
    private BiConsumer<PNHereNowResult, PNStatus> responseHandler;
    @NonNull
    private Consumer<String> errorHandler;

    public PNHereNowCallback(
            @Nullable BiConsumer<PNHereNowResult, PNStatus> responseHandler,
            @NonNull Consumer<String> errorHandler
    ){
        this.responseHandler = responseHandler;
        this.errorHandler = errorHandler;
    }

    @Override
    public void onResponse(PNHereNowResult result, PNStatus status) {
        if (status.isError()) {
            errorHandler.accept("Ошибка." + status.getErrorData().getInformation());

        } else {
            if (responseHandler != null) {
                responseHandler.accept(result, status);
            } else {
                Timber.tag(App.getTag()).i("Ответ hereNow пришел, но обработчик не задан");
            }
        }
    }
}
