package im.vinci.server.naturelang.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mlc on 2016/11/9.
 */
public class RegexUtils {
    /**
     * 过滤录音指令中的文件ID
     * @param string
     * @return
     */
    public static String filterNumer(String string) {
        String result = "";
        Pattern p = Pattern.compile("\\d{7,9}");
        Matcher m = p.matcher(string);
        while(m.find()){
            result = m.group();
        }
        return result;
    }

    /**
     * 解析句子中的年信息
     * @param date
     * @return
     */
    public static int regexYear(String date) {
        Calendar calendar = Calendar.getInstance();
        int year = 0;
        String result = "";
        String yearReg = "每年|今年|明年|去年|前年|后年|大后年|大前年|年底|\\d{2,4}年";
        Pattern p = Pattern.compile(yearReg);
        Matcher m = p.matcher(date);
        while (m.find()) {
            result = m.group();
        }
        if (StringUtils.isNotBlank(result)) {
            switch (result) {
                case "每年":
                    year = calendar.get(Calendar.YEAR);
                    break;
                case "今年":
                    year = calendar.get(Calendar.YEAR);
                    break;
                case "明年":
                    year = calendar.get(Calendar.YEAR) + 1;
                    break;
                case "去年":
                    year = calendar.get(Calendar.YEAR) - 1;
                    break;
                case "前年":
                    year = calendar.get(Calendar.YEAR) - 2;
                    break;
                case "后年":
                    year = calendar.get(Calendar.YEAR) + 2;
                    break;
                case "大后年":
                    year = calendar.get(Calendar.YEAR) + 3;
                    break;
                case "大前年":
                    year = calendar.get(Calendar.YEAR) - 3;
                    break;
                default:
                    year = Integer.valueOf(result.replace("年", ""));
            }
        }else{
            year = calendar.get(Calendar.YEAR);
        }
        return year;
    }


    /**
     * 解析句子中的月份信息
     * @param date
     * @return
     */
    public static int regexMonth(String date) {
        Calendar calendar = Calendar.getInstance();
        int month = 0;
        String result = "";
        String yearReg = "每月|(一|二|三|四|五|六|七|八|九|十|十一|十二)月|(上个|上)月|(下个|下)月|本月|(这个|这)月|\\d{1,2}月";
        Pattern p = Pattern.compile(yearReg);
        Matcher m = p.matcher(date);
        while (m.find()) {
            result = m.group();
        }
        if (StringUtils.isNotBlank(result)) {
            switch (result) {
                case "每月":
                    month = calendar.get(Calendar.MONTH);
                    break;
                case "本月":
                    month = calendar.get(Calendar.MONTH);
                    break;
                case "这月":
                    month = calendar.get(Calendar.MONTH);
                    break;
                case "这个月":
                    month = calendar.get(Calendar.MONTH);
                    break;
                case "上个月":
                    month = calendar.get(Calendar.MONTH) - 1;
                    break;
                case "上月":
                    month = calendar.get(Calendar.MONTH) - 1;
                    break;
                case "下个月":
                    month = calendar.get(Calendar.MONTH) + 1;
                    break;
                case "下月":
                    month = calendar.get(Calendar.MONTH) + 1;
                    break;
                case "一月":
                    month = 0;
                    break;
                case "二月":
                    month = 1;
                    break;
                case "三月":
                    month = 2;
                    break;
                case "四月":
                    month = 3;
                    break;
                case "五月":
                    month = 4;
                    break;
                case "六月":
                    month = 5;
                    break;
                case "七月":
                    month = 6;
                    break;
                case "八月":
                    month = 7;
                    break;
                case "九月":
                    month = 8;
                    break;
                case "十月":
                    month = 9;
                    break;
                case "十一月":
                    month = 10;
                    break;
                case "十二月":
                    month = 11;
                    break;
                default:
                    month = Integer.valueOf(result.replace("月", "")) -1;
            }
        }else{
            month = calendar.get(Calendar.MONTH);
        }
        if ((month + 1) % 12 == 0) {
            return 12;
        }
        return (month + 1)%12;
    }


    /**
     * 解析句子中的日期信息
     * @param date
     * @return
     */
    public static int regexDay(String date) {
        Calendar calendar = Calendar.getInstance();
        int day = 0;
        String result = "";
        String dayReg = "每天|今天|明天|昨天|前天|后天|大后天|大前天|(星期|周)(一|二|三|四|五|六|天|日|末)|\\d{1,2}(号|日)";
        Pattern p = Pattern.compile(dayReg);
        Matcher m = p.matcher(date);
        while (m.find()) {
            result = m.group();
        }
        if (StringUtils.isNotBlank(result)) {
            switch (result) {
                case "周一":
                    day = calendar.get(Calendar.MONDAY);
                    break;
                case "周二":
                    day = calendar.get(Calendar.TUESDAY);
                    break;
                case "周三":
                    day = calendar.get(Calendar.WEDNESDAY);
                    break;
                case "周四":
                    day = calendar.get(Calendar.THURSDAY);
                    break;
                case "周五":
                    day = calendar.get(Calendar.FRIDAY);
                    break;
                case "周六":
                    day = calendar.get(Calendar.SATURDAY);
                    break;
                case "周日":
                    day = calendar.get(Calendar.SUNDAY);
                    break;
                case "周天":
                    day = calendar.get(Calendar.SUNDAY);
                    break;
                case "每天":
                    day = calendar.get(Calendar.DATE);
                    break;
                case "今天":
                    day = calendar.get(Calendar.DATE);
                    break;
                case "明天":
                    day = calendar.get(Calendar.DATE) + 1;
                    break;
                case "昨天":
                    day = calendar.get(Calendar.DATE) - 1;
                    break;
                case "前天":
                    day = calendar.get(Calendar.DATE) - 2;
                    break;
                case "后天":
                    day = calendar.get(Calendar.DATE) + 2;
                    break;
                case "大后天":
                    day = calendar.get(Calendar.DATE) + 3;
                    break;
                case "大前天":
                    day = calendar.get(Calendar.DATE) - 3;
                    break;
                default:
                    day = Integer.valueOf(result.replace("号", "").replace("日", ""));
            }
        }else{
            day = calendar.get(Calendar.DATE);
        }
        return day;
    }


    public static void main(String[] args) {
        String str = "周四";
        System.out.println(regexYear(str)+" "+regexMonth(str) +" "+regexDay(str));

        Calendar calendar = Calendar.getInstance();
        calendar.getTime().getYear();
        System.out.println( calendar.get(Calendar.YEAR) + " " + calendar.get(Calendar.MONTH) + " "+ " "+calendar.get(Calendar.DATE)
                + " " + calendar.get(Calendar.HOUR_OF_DAY) + " "+ calendar.get(Calendar.MINUTE) + " "+ calendar.get(Calendar.SECOND) + " ");
        Date date = new Date();
        int year = date.getYear();
        System.out.println( calendar.getTime().toLocaleString());
    }
}
