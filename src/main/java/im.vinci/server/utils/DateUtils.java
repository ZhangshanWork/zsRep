package im.vinci.server.utils;

import org.apache.commons.lang3.time.FastDateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;

/**
 * Created by zhongzhengkai on 16/1/18.
 */
public class DateUtils {

    public static final int HOUR_IN_MILLON = 60 * 60 * 1000;

//    public static void main(String[] args) throws ParseException{
//        System.out.println(diffTimeZoneWithHourInMillion("2016-02-01T22:36:34.445+0900"));
//    }

    /**
     * 查看系统时间与传进来的时间相差多少个小时(即多少个时区),并返回其差值(单位:ms)
     * 如果差值是负数表示传进来的时间比系统时区大,反之是正数则小
     * @param UTCString 形如:2016-01-16T12:01:19.700-0700
     * @return
     */
    public static int diffTimeZoneWithHourInMillion(String UTCString) throws ParseException{
        Calendar cal = buildCalendarForUTCString(UTCString);
        int diffWithZeroTimeZone = cal.get(Calendar.ZONE_OFFSET);
        Calendar cal2 = Calendar.getInstance();
        int diffWithZeroTimeZone2 = cal2.get(Calendar.ZONE_OFFSET);
        return diffWithZeroTimeZone2 - diffWithZeroTimeZone;
    }

    public static Calendar buildCalendarForUTCString(String UTCString) throws ParseException{
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        format.parse(UTCString);
        return format.getCalendar();
    }

    /**
     * 查看系统时间与传进来的时间相差多少个小时,并返回其差值(单位:hour)
     * 如果差值是负数表示传进来的时间比系统时区大,反之是正数则小
     * @param UTCString 形如:2016-01-16T12:01:19.700-0700
     * @return
     */
    public static int diffTimeZoneWithHour(String UTCString) throws ParseException {
        return diffTimeZoneWithHourInMillion(UTCString) / HOUR_IN_MILLON;
    }


    /**
     * input: "2015-12-10 00:00:00" ---> return: 1449676800000
     * @param dateString
     * @return
     */
    public static long str2Timestamp(String dateString) {
        long timestamp = 0;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = sdf.parse(dateString);
            timestamp = date.getTime();
        } catch (ParseException e) {
            System.out.println(e.getMessage());
        }
        return timestamp;
    }


    /**
     * 获得当前时间往前第n天的零点零分零秒的时间戳
     * if date of now is "2015-12-17 **:**:**"
     * input: 0 ---> return : timestamp of "2015-12-17 00:00:00"
     * input: 1 ---> return : timestamp of "2015-12-16 00:00:00"
     * input: 2 ---> return : timestamp of "2015-12-15 00:00:00"
     * @param preCount 如果是0,就表示当天的零点零分零秒的时间戳
     * @return
     */
    public static long preDayZeroClockTimestamp(int preCount) {
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTimeInMillis(now.getTime() - preCount * 86400000L);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        Formatter fmt = new Formatter();
        return str2Timestamp(fmt.format("%d-%d-%d %d:%d:%d", year, month, day, 0, 0, 0).toString());
        //经过测试,calendar返回的时间戳有毫秒级的误差,所以这里弃用
//        cal.set(year,month,day,00,00,00);
//        return cal.getTime().getTime();
    }

    /**
     * 获得指定时间戳的上一个月的一号的零点时间戳
     * @param currentTime
     * @return
     */
    public static long preMonthFirstDayTimestamp(long currentTime) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(currentTime);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        Formatter fmt = new Formatter();
        if (month == 1) {
            return str2Timestamp(fmt.format("%d-%d-%d %d:%d:%d", year - 1, 12, 1, 0, 0, 0).toString());
        } else {
            return str2Timestamp(fmt.format("%d-%d-%d %d:%d:%d", year, month - 1, 1, 0, 0, 0).toString());
        }
    }

    public static long preMonthFirstDayTimestamp() {
        return preMonthFirstDayTimestamp(new Date().getTime());
    }

    /**
     * 获取当天的日期
     * @return
     */
    public static String today() {
        return _dayYMDStr(0);
    }

    /**
     * 获得向前n天的时间字符字符串
     * if date of now is "2015-12-17 **:**:**"
     * input: 0 ---> return : "2015-12-17"
     * input: 1 ---> return : "2015-12-16"
     * input: 2 ---> return : "2015-12-15"
     * @param preCount
     * @return
     */
    public static String preDayYMDStr(int preCount) {
        preCount = 0 - Math.abs(preCount);
        return _dayYMDStr(preCount);
    }

    /**
     * 获得向后n天的时间字符字符串
     * if date of now is "2015-12-17 **:**:**"
     * input: 0 ---> return : "2015-12-17"
     * input: 1 ---> return : "2015-12-18"
     * input: 2 ---> return : "2015-12-19"
     * @param nextCount
     * @return
     */
    public static String nextDayYMDStr(int nextCount) {
        nextCount = Math.abs(nextCount);
        return _dayYMDStr(nextCount);
    }

    /**
     * 获得n小时后的时间字符字符串
     * if date of now is "2015-12-17 12:**:**"
     * input: -1 ---> return : "2015-12-17 11:**:**"
     * input: 0  ---> return : "2015-12-17 12:**:**"
     * input: 1  ---> return : "2015-12-17 13:**:**"
     * input: 2  ---> return : "2015-12-17 14:**:**"
     * @param count
     * @return
     */
    public static String nextHourYMDHmsStr(int count) {
        return _dayYMDHmsStr(count);
    }


    private static String _dayYMDStr(int offset){
        Calendar cal = Calendar.getInstance();

//        Date now = new Date();
//        cal.setTimeInMillis(now.getTime() - preCount * 86400000L);
//        int year = cal.get(Calendar.YEAR);
//        int month = cal.get(Calendar.MONTH) + 1;
//        int day = cal.get(Calendar.DAY_OF_MONTH);

        cal.add(Calendar.DATE,offset);//正数向后推,负数向前推
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);

        String result = year + "-";
        if (String.valueOf(month).length() < 2) {
            result += "0" + month + "-";
        } else {
            result += month + "-";
        }

        if (String.valueOf(day).length() < 2) {
            result += "0" + day;
        } else {
            result += day;
        }
        return result;
    }

    private static String _dayYMDHmsStr(int offset){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR,offset);//正数向后推,负数向前推
        return FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").format(cal);
    }

    /**
     * 获取当前的时刻（小时）
     * @return
     */
    public static int getNowHours() {
        Date date = new Date();
        return date.getHours();
    }

}
