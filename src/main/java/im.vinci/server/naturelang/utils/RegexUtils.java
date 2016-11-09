package im.vinci.server.naturelang.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mlc on 2016/11/9.
 */
public class RegexUtils {
    public static void main(String[] args) {
        System.out.println(filterNumer("播放录音160618"));
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

}
