package temperatus.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Utility functions to manage dates... this is necessary because at the beginning of the project I didn't know
 * the new Java 8 date functions... so I used the old Date instead of the new LocalDate. Some JavaFX elements
 * need LocalDate objects so this utility functions transform dates between this two types.
 * <p>
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
        return date == null ? null : Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * Generate a LocalDateTime object from a Date
     *
     * @param date date to generate
     * @return LocalDateTime
     */
    public static LocalDateTime asLocalDateTime(Date date) {
        return date == null ? null : Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Generate a Date from a LocalDate object
     *
     * @param date LocalDate to generate
     * @return Date
     */
    public static Date asUtilDate(LocalDate date) {
        return date == null ? null : Date.from((date).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Generate a Date from a LocalDateTime object
     *
     * @param date LocalDateTime to generate
     * @return Date
     */
    public static Date asUtilDate(LocalDateTime date) {
        return date == null ? null : Date.from(date.atZone(ZoneId.systemDefault()).toInstant());
    }

}
