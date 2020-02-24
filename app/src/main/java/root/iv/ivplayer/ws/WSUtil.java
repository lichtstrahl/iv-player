package root.iv.ivplayer.ws;

import root.iv.ivplayer.BuildConfig;

public abstract class WSUtil {
    private static final String WS = "ws";
    private static final String WSS = "wss";

    // Составление URL по шаблону
    // wss://connect.websocket.in/v2/YOUR_CHANNEL_ID?token=YOUR_ACCESS_TOKEN
    public static String websocketInURL(String channelID) {

        return String.format("%s/v2/%s?token=%s", url("wss", BuildConfig.URL_WS_IN, null), channelID, BuildConfig.WS_IV_TOKEN);

    }

    public static String websocketInURL() {
        return websocketInURL(String.valueOf(BuildConfig.WS_IV_CHANNEL));
    }

    public static String springWSURL() {
        String url = url(WS, BuildConfig.URL_WS_SPRING, "9010");
        return String.format("%s/ws-handler", url);
    }

    private static String url(String protocol, String baseURL, String port) {
        return String.format(
                port != null && !port.isEmpty()
                        ? "%s://%s:%s"
                        : "%s://%s",
                protocol, baseURL, port
        );
    }
}
