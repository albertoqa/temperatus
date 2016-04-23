package temperatus.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * Created by alberto on 23/4/16.
 */
public class DateUtils {

    /**
     * Generate a LocalDate object from a Date
     *
     * @param date date to generate
     * @return LocalDate
     */
    public static LocalDate asLocalDate(Date date) {
        if (date == null)
            return null;

        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * Generate a Date from a LocalDate object
     *
     * @param date LocalDate to generate
     * @return Date
     */
    public static Date asUtilDate(LocalDate date) {
        if (date == null)
            return null;

        return Date.from((date).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

}
