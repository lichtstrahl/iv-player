package root.iv.ivplayer.network.ws.pubnub;

import androidx.annotation.Nullable;

import java.util.Locale;
import java.util.Random;

public class PNUtil {
    public static final String LOGIN_SEPARATOR = "_";
    public static final String PN_EVENT_JOIN = "join";
    public static final String PN_EVENT_LEAVE = "leave";

    // Генерируем UUID не только слоучайным числом, но и добавляем login
    // Пример: igor_01, sveta_02
    public static String genereateUUID(String login) {
        return String.format(
                Locale.ENGLISH,
                "%s%s%02d",
                login, LOGIN_SEPARATOR, new Random().nextInt(10)
        );
    }

    @Nullable
    public static String parseLogin(String uuid) {
        return (uuid.contains(LOGIN_SEPARATOR))
                ? uuid.split(LOGIN_SEPARATOR)[0]
                : null;
    }

    public static boolean valid(String loginCandidate) {
        return !loginCandidate.isEmpty() && !loginCandidate.contains(LOGIN_SEPARATOR);
    }
}
