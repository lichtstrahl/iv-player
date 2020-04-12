package root.iv.ivplayer.network.ws.pubnub;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import root.iv.ivplayer.network.ws.pubnub.callback.PNPublishCallback;
import root.iv.ivplayer.network.ws.pubnub.callback.PNSubscribeCallback;

public class PubNubConnector {
    private static final PNPublishCallback defaultPublishCallback =
            new PNPublishCallback((result, status) -> { return null;});
    private PubNub pnConnect;
    private PNConfiguration pnConfiguration;


    @Deprecated
    public static PubNubConnector create(String pubKey, String subKey) {
        PubNubConnector connector = new PubNubConnector();
        connector.initConfig(pubKey, subKey);
        connector.loadConfig();

        return connector;
    }

    public static PubNubConnector create(String pubKey, String subKey, String login) {
        PubNubConnector connector = new PubNubConnector();
        connector.initConfig(pubKey, subKey, login);
        connector.loadConfig();

        return connector;
    }

    public void subscribe(String ... channels) {
        List<String> channelsList = new ArrayList<>();
        Collections.addAll(channelsList, channels);

        pnConnect
                .subscribe()
                .channels(channelsList)
                .withPresence()
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

    /** @deprecated Потому что не передаётся login */
    @Deprecated
    private void initConfig(String pubKey, String subKey) {
        pnConfiguration = new PNConfiguration();
        pnConfiguration.setPublishKey(pubKey);
        pnConfiguration.setSubscribeKey(subKey);
        pnConfiguration.setUuid(UUID.randomUUID().toString());
    }

    private void initConfig(String pubKey, String subKey, String login) {
        pnConfiguration = new PNConfiguration();
        pnConfiguration.setPublishKey(pubKey);
        pnConfiguration.setSubscribeKey(subKey);
        pnConfiguration.setUuid(PNUtilUUID.genereateUUID(login));
    }

    private void loadConfig() {
        pnConnect = new PubNub(pnConfiguration);
    }
}
