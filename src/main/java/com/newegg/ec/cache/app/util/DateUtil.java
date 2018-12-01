package com.newegg.ec.cache.app.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by gl49 on 2018/4/22.
 */
public class DateUtil {
    public static String getCurrentDetailHour() {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        return df.format(new Date());
    }

    public static String getCurrentDate() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df.format(new Date());
    }

    public static String getFormatDate(long timeStamp) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return df.format(new Date(timeStamp));
    }

    public static int getTime() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    public static int getDay() {
        Date dNow = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String currentDay = sdf.format(dNow); //格式化当前时间
        return Integer.valueOf(currentDay);
    }

    public static int getHour() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    public static int getMinute() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.MINUTE);
    }

    public static int getTimeStamp(String dateStr, String format) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        int timeStemp = 0;
        Date date = null;
        date = simpleDateFormat.parse(dateStr);
        if (null == date) {
            return 0;
        }
        long tmp = date.getTime();
        timeStemp = (int) (tmp / 1000);
        return timeStemp;
    }

    /**
     * 获取当前某个小时的时间戳
     *
     * @param ihour
     * @return
     */
    public static long getBeforeHourTime(int ihour) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) - ihour);
        return calendar.getTimeInMillis();
    }

    /**
     * 获取当前某分钟的时间戳
     *
     * @param num
     * @return
     */
    public static long getBeforeMinutesTime(int num) {
        Calendar beforeTime = Calendar.getInstance();
        beforeTime.add(Calendar.MINUTE, -num);
        Date beforeD = beforeTime.getTime();
        return beforeD.getTime()/1000;
    }
}
