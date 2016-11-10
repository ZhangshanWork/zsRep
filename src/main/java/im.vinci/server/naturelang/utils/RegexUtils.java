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
        }
        return year;
    }

    public static void main(String[] args) {
        System.out.println(regexYear("今年5月"));

        Calendar calendar = Calendar.getInstance();
        calendar.getTime().getYear();
        System.out.println( calendar.get(Calendar.YEAR) + " " + calendar.get(Calendar.MONTH) + " "+ calendar.get(Calendar.DATE)
                + " " + calendar.get(Calendar.HOUR_OF_DAY) + " "+ calendar.get(Calendar.MINUTE) + " "+ calendar.get(Calendar.SECOND) + " ");
        Date date = new Date();
        int year = date.getYear();
        System.out.println( calendar.getTime().toLocaleString());
    }
}
