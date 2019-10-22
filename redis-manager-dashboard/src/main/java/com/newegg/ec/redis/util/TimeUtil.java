package com.newegg.ec.redis.util;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

import static javax.management.timer.Timer.ONE_MINUTE;
import static javax.management.timer.Timer.ONE_SECOND;

/**
 * @author Jay.H.Zou
 * @date 8/3/2019
 */
public class TimeUtil {

    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static final long FIVE_SECONDS = 5 * ONE_SECOND;

    public static final long TEN_SECONDS = 10 * ONE_SECOND;

    public static final long FIVE_MINUTES = 5 * ONE_MINUTE;

    public static final long TEN_MINUTES = 2 * FIVE_MINUTES;

    public static final long HALF_HOUR = 3 * TEN_MINUTES;

    /**
     * Default duration
     */
    public static final long ONE_HOUR = 2 * HALF_HOUR;

    public static final long TWO_HOURS = 2 * ONE_HOUR;

    public static final long THREE_HOURS = 3 * ONE_HOUR;

    public static final long SIX_HOURS = 6 * ONE_HOUR;

    public static final long TWELVE_HOURS = 12 * ONE_HOUR;

    public static final long ONE_DAY = 24 * ONE_HOUR;

    public static final long TWO_DAYS = 2 * ONE_DAY;

    public static final long THREE_DAYS = 3 * ONE_DAY;

    public static final long SEVEN_DAYS = 7 * ONE_DAY;

    public static final long FIFTEEN_DAYS = 15 * ONE_DAY;

    private TimeUtil() {
    }

    public static Timestamp getCurrentTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }


    public static Timestamp getTime(long duration) {
        return new Timestamp(System.currentTimeMillis() - duration);
    }

    public static Timestamp getLastHourTimestamp() {
        return getTime(ONE_HOUR);
    }

    public static boolean moreThanTwoDays(Timestamp startTime, Timestamp endTime) {
        return endTime.getTime() - startTime.getTime() > TWO_DAYS;
    }

}
