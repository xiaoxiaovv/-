package com.istar.mediabroken.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.time.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DateUitl {

    //获取当天的开始时间
    public static java.util.Date getDayBegin() {
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }


    //获取明天的开始时间
    public static Date getBeginDayOfTomorrow() {
        Calendar cal = new GregorianCalendar();
        cal.setTime(getDayBegin());
        cal.add(Calendar.DAY_OF_MONTH, 1);

        return cal.getTime();
    }

    //获取参数的开始时间
    public static Date getBeginDayOfParm(Date date) {
        if (date == null) {
            date = new Date();
        }
        Calendar cal = Calendar.getInstance(); //得到日历
        cal.setTime(date);//把当前时间赋给日历
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    //获取昨天的开始时间
    public static Date getBeginDayOfYesterday() {
        Calendar cal = new GregorianCalendar();
        cal.setTime(getDayBegin());
        cal.add(Calendar.DAY_OF_MONTH, -1);

        return cal.getTime();
    }


    //在昨天开始时间的基础上加一分钟
    public static Date getBeginDayOfYesterdayAddOne() {
        Calendar cal = new GregorianCalendar();
        cal.setTime(getDayBegin());
        cal.set(Calendar.MINUTE, 1);
        cal.add(Calendar.DAY_OF_MONTH, -1);

        return cal.getTime();
    }

    public static List<String> getOneWeek() {
        Date end = getDayBegin();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        List<String> week = new ArrayList<String>();
        for (int i = 7; i > 0; i--) {
            week.add(simpleDateFormat.format(DateUitl.addDay(end, -i)));
        }
        return week;
    }

    public static Date convertEsDate(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        if (dateStr.indexOf("T") == -1) {
            try {
                Long dateTime = Long.valueOf(dateStr);
                return new Date(dateTime);
            } catch (Exception e) {
                return new Date();
            }
        } else {
            try {
                return sdf.parse(dateStr);
            } catch (Exception e) {
                return new Date();
            }
        }
    }

    public static String convertFormatDate(Date date, String format) {
        String formatStr = "";
        if (null != date && !"".equals(format)) {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            formatStr = sdf.format(date);
        }
        return formatStr;
    }

    public static String convertFormatDate(long date, String format) {
        String formatStr = "";
        if (0 != date && !"".equals(format)) {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            formatStr = sdf.format(date);
        }
        return formatStr;
    }

    public static Date convertFormatDate(String dateStr, String format) {
        Date date = null;
        if (null != dateStr && !"".equals(dateStr) && !"".equals(format)) {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            try {
                date = sdf.parse(dateStr);
            } catch (ParseException e) {
                return null;
            }
        }
        return date;
    }

    public static Date convertEsDate(Date date) {
        return date;
    }

    public static Date convertEsDate(Long dateTime) {
        return new Date(dateTime);
    }

    public static Date convertAutoPushDate(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            return sdf.parse(dateStr);
        } catch (Exception e) {
            return new Date();
        }
    }

    public static String convertStrDate(Date date) {
        if (date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                String str = sdf.format(date);
                return str;
            } catch (Exception e) {
                return "";
            }
        } else {
            return "";
        }

    }

    //给天数增加指定天
    public static Date addDay(Date time, int days) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(time);
        cal.add(Calendar.DATE, days);
        return cal.getTime();
    }

    public static Date addHour(Date time, int hours) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(time);
        cal.add(Calendar.HOUR, hours);
        return cal.getTime();
    }

    public static Date addMins(Date time, int mins) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(time);
        cal.add(Calendar.MINUTE, mins);
        return cal.getTime();
    }
    public static Date addSeconds(Date time, int second) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(time);
        cal.add(Calendar.SECOND, second);
        return cal.getTime();
    }

    //获取两个时间的差值
    public static int getDistance(Date small, Date big) {
        GregorianCalendar gc1 = new GregorianCalendar();
        GregorianCalendar gc2 = new GregorianCalendar();
        gc1.setTime(small);
        gc2.setTime(big);
        int distance = getDays(gc1, gc2);
        return distance;
    }
    //获取两个时间的小时差值
    public static int getHourDistance(Date small,Date big){
        Long difference = big.getTime() - small.getTime();
        int hour = (int) (difference/1000/60/60);
        return hour;
    }

    public static int getDays(GregorianCalendar g1, GregorianCalendar g2) {
        int elapsed = 0;
        GregorianCalendar gc1, gc2;
        if (g2.after(g1)) {
            gc2 = (GregorianCalendar) g2.clone();
            gc1 = (GregorianCalendar) g1.clone();
        } else {
            gc2 = (GregorianCalendar) g1.clone();
            gc1 = (GregorianCalendar) g2.clone();
        }
        gc1.clear(Calendar.MILLISECOND);
        gc1.clear(Calendar.SECOND);
        gc1.clear(Calendar.MINUTE);
        gc1.clear(Calendar.HOUR_OF_DAY);
        gc2.clear(Calendar.MILLISECOND);
        gc2.clear(Calendar.SECOND);
        gc2.clear(Calendar.MINUTE);
        gc2.clear(Calendar.HOUR_OF_DAY);
        while (gc1.before(gc2)) {
            gc1.add(Calendar.DATE, 1);
            elapsed++;
        }
        return elapsed;
    }


    public static List<String> getMonthFirstAndLastDay() {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();

        int now = Calendar.MONTH;

        calendar.setTime(new Date());
        calendar.add(Calendar.YEAR, -1);

        calendar.add(Calendar.MONTH, -now + 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date firstDay = calendar.getTime();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date lastDay = calendar.getTime();

        List<String> result = new ArrayList<String>();
        JSONObject json = new JSONObject();
        json.put("first", sdf.format(firstDay));
        json.put("last", sdf.format(lastDay));

        result.add(json.toJSONString());

        for (int i = 1; i < 12; i++) {
            calendar.add(Calendar.MONTH, 1);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            firstDay = calendar.getTime();
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            lastDay = calendar.getTime();

            JSONObject jsons = new JSONObject();
            jsons.put("first", sdf.format(firstDay));
            jsons.put("last", sdf.format(lastDay));

            result.add(jsons.toJSONString());
        }

        return result;
    }

    public static Long getTimes(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date time = null;
        try {
            time = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return time.getTime();
    }
    public static boolean isMonth(String startDate, String endDate) {
        boolean b = strTimeLtEEndTime(startDate, endDate);
        if (!b){
            return false;
        }
        if (dayLog(startDate, endDate) > 32) {
            return false;
        }
        return true;
    }

    //天数差
    private static int dayLog(String startDate, String endDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date dStar = sdf.parse(startDate);
            Date dEnd = sdf.parse(endDate);
            int distance = getDistance(dStar, dEnd);
            return distance;
        } catch (ParseException e) {
            return -1;
        }
    }

    public static boolean strTimeLtEEndTime(String startDate, String endDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date dStar = sdf.parse(startDate);
            Date dEnd = sdf.parse(endDate);
            long time = dEnd.getTime() - dStar.getTime();
            if (time >= 0) {
                return true;
            } else {
                return false;
            }
        } catch (ParseException e) {
            return false;
        }
    }
    public static boolean isEquals(Date date1,Date date2){
        return DateUtils.isSameDay(date1,date2);
    }

    public static Date getThisWeekMonday(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        // 获得当前日期是一个星期的第几天
        int dayWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (1 == dayWeek) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }
        // 设置一个星期的第一天，按中国的习惯一个星期的第一天是星期一
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        // 获得当前日期是一个星期的第几天
        int day = cal.get(Calendar.DAY_OF_WEEK);
        // 根据日历的规则，给当前日期减去星期几与一个星期第一天的差值
        cal.add(Calendar.DATE, cal.getFirstDayOfWeek() - day);
        return cal.getTime();
    }

    public static Date getThisWeekMondayBegin(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getThisWeekMonday(date));
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
    public static Date getLastWeekMondayBegin(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getThisWeekMonday(date));
        cal.add(Calendar.DATE, -7);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
    public static Date getLastWeekSundayEnd(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getThisWeekMonday(date));
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.SECOND,-1);
        return cal.getTime();
    }
    public static void main(String[] args) {
       /* SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.YEAR, -1);

        calendar.add(Calendar.MONTH,-1);

        calendar.set(Calendar.DAY_OF_MONTH,1);

        Date firstDay =calendar.getTime();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        System.out.println (sdf.format(firstDay));

        Date lastDay = calendar.getTime();
        System.out.println (sdf.format(lastDay));*/

        System.out.println(getLastWeekSundayEnd(new Date())+"========="+getThisWeekMondayBegin(new Date()));
    }
}
