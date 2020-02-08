package root.iv.ivplayer.ws;

import root.iv.ivplayer.BuildConfig;

public abstract class WSUtil {

    // Составление URL по шаблону
    // wss://connect.websocket.in/v2/YOUR_CHANNEL_ID?token=YOUR_ACCESS_TOKEN
    public static String templateURL(String channelID) {
        return String.format("wss://connect.websocket.in/v2/%s?token=%s", channelID, BuildConfig.WS_IV_TOKEN);
    }

    public static String templateURL() {
        return templateURL(String.valueOf(BuildConfig.WS_IV_CHANNEL));
    }
}
