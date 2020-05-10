package root.iv.ivplayer.util;

import android.icu.util.Calendar;

public class DateTimeUtil {
    public static String stringDateTime(long milliseconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        return stringDateTime(calendar);
    }

    public static String stringDateTime(Calendar calendar) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int h = calendar.get(Calendar.HOUR_OF_DAY);
        int m = calendar.get(Calendar.MINUTE);
        int s = calendar.get(Calendar.SECOND);
        return String.format("%s-%s-%s %s:%s:%s", year, month, day, h, m, s);
    }
}
