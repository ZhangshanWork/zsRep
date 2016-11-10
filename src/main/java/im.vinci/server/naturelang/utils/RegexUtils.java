package im.vinci.server.naturelang.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mlc on 2016/11/9.
 */
public class RegexUtils {
    public static void main(String[] args) {
        System.out.println(regexYear("2015年去年冬天"));
    }

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
     * 解析当前时间
     * @param date
     * @return
     */
    public static String regexYear(String date) {
        String result = "";
        String year = "今年|明年|去年|前年|后年|大后年|大前年|年底|\\d{2,4}年";
        Pattern p = Pattern.compile(year);
        Matcher m = p.matcher(date);
        while (m.find()) {
            result = m.group();
        }
        switch (result) {
            case "今年":
                break;
            


        }
        return result;
    }
}
