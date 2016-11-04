package im.vinci.server.tests.integration.zzktmptest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by zhongzhengkai on 16/1/18.
 */
public class DateFormatTest {

    public static void main(String[] args) throws Exception{
//        Date date = formatStringAsDate("2016-01-16T12:01:19.700-0700");//2016-01-18T11:02:39.727+0800
//        Date date = formatStringAsDate("2016-01-18T11:02:39.727+0800");
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(date);
//        System.out.println(cal.get(Calendar.DST_OFFSET));
//        System.out.println(date.getTimezoneOffset());
//
//        System.out.println(formatDateAsString(date));
//
//        System.out.println(formatDateAsString(new Date()));
//        System.out.println(getSysTimeZone());

//          SimpleDateFormat format = new SimpleDateFormat("Z");

//        System.out.println(format.format(format.parse("2016-01-16T12:01:19.700-0700")));

//        System.out.println(formatDateAsString(new Date()));

//        System.out.println(getSysTimeZoneAsNumber());

        System.out.println(diffTimeZoneWithMillions("2016-01-16T12:01:19.700+0800"));

        String a="a";
        System.out.println(a instanceof String);
    }



    public static Date formatUTCStringAsDate(String str) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        try {
            format.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar cal = format.getCalendar();
        System.out.println(cal.get(Calendar.ZONE_OFFSET));
        return format.getCalendar().getTime();
    }

    public static String formatDateAsString(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("Z");
        return format.format(date);
    }

    public static int getSysTimeZone(Date date){
        Calendar cal = Calendar.getInstance();
        TimeZone zone = cal.getTimeZone();
        System.out.println(zone.getDisplayName());
        System.out.println(zone.getID());
//        return zone.getDisplayName(Locale.ENGLISH);
        return zone.getRawOffset();
    }



    /**
     *查看传进来的时间与系统时间相差多少个小时(即多少个时区),并返回其差值(单位:ms)
     * @param UTCString 形如:2016-01-16T12:01:19.700-0700
     * @return
     */
    public static int diffTimeZoneWithMillions(String UTCString) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        try {
            format.parse(UTCString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar cal = format.getCalendar();
        int diffWithZeroTimeZone = cal.get(Calendar.ZONE_OFFSET);
        Calendar cal2 = Calendar.getInstance();
        int diffWithZeroTimeZone2 = cal2.get(Calendar.ZONE_OFFSET);
        return diffWithZeroTimeZone2 - diffWithZeroTimeZone;
    }

    public static int getSysTimeZoneAsNumber(){
        Date now = new Date();
        SimpleDateFormat format = new SimpleDateFormat("Z");
        return Integer.parseInt(format.format(now));
    }


}
