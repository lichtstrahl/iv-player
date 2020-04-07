package root.iv.ivplayer.network.ws.pubnub;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PubNubConnector {
    private static final PNPublishCallback defaultPublishCallback =
            new PNPublishCallback((result, status) -> { return null;});
    private PubNub pnConnect;
    private PNConfiguration pnConfiguration;


    public static PubNubConnector create(String pubKey, String subKey) {
        PubNubConnector connector = new PubNubConnector();
        connector.initConfig(pubKey, subKey);
        connector.loadConfig();

        return connector;
    }

    public void subscribe(String ... channels) {
        List<String> channelsList = new ArrayList<>();
        Collections.addAll(channelsList, channels);

        pnConnect
                .subscribe()
                .channels(channelsList)
                .execute();
    }

    public void addListener(PNSubscribeCallback callback) {
        pnConnect.addListener(callback);
    }

    public void sendMessage(String msg, String channel, PNPublishCallback callback) {
        pnConnect.publish()
                .message(msg)
                .channel(channel)
                .async(callback);
    }

    public void sendMessage(String msg, String channel) {
        pnConnect.publish()
                .message(msg)
                .channel(channel)
                .async(defaultPublishCallback);
    }

    private void initConfig(String pubKey, String subKey) {
        pnConfiguration = new PNConfiguration();
        pnConfiguration.setPublishKey(pubKey);
        pnConfiguration.setSubscribeKey(subKey);
        pnConfiguration.setSecure(true);
    }

    private void loadConfig() {
        pnConnect = new PubNub(pnConfiguration);
    }
}
