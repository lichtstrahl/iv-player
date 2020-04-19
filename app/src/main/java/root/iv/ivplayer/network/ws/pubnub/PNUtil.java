package root.iv.ivplayer.network.ws.pubnub;

import androidx.annotation.Nullable;

import java.util.Locale;
import java.util.Random;

public class PNUtil {
    public static final String LOGIN_SEPARATOR = "_";

    // Генерируем UUID не только слоучайным числом, но и добавляем login
    // Пример: igor_01, sveta_02
    public static String randomUUID(String login) {
        return generateUUID(login, new Random().nextInt(10));
    }

    public static String constantUUID(String login) {
        return generateUUID(login, 0);
    }

    private static String generateUUID(String login, int random) {
        return String.format(
                Locale.ENGLISH,
                "%s%s%02d",
                login, LOGIN_SEPARATOR, random
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
