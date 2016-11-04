package im.vinci.server.utils;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;

/**
 * 判断一些字符串或者字符是否属于某一个范围
 * Created by tim@vinci on 16/9/12.
 */
public class StringContentUtils {

    /**
     * 英文字符算1个,中文 韩文等其他算2个,emoji也算两个
     */
    public static int countRealLength(CharSequence charSequence) {

        if (charSequence == null || charSequence.length() == 0 ) {
            return 0;
        }
        AtomicInteger length = new AtomicInteger();
        charSequence.codePoints().forEach(new IntConsumer() {
            @Override
            public void accept(int value) {
                if (value > 256) {
                    length.addAndGet(2);
                } else {
                    length.incrementAndGet();
                }
            }
        });
        return length.get();
    }

    public static boolean hasUnshowChar(CharSequence charSequence) {
        if (charSequence == null || charSequence.length() == 0 ) {
            return false;
        }
        for (int i=0; i<charSequence.length(); i++) {
            char c = charSequence.charAt(i);
            if (Character.isISOControl(c)) {
                return true;
            }
        }
        return false;
    }

    public static String trimToEmpty(String str) {
        return str == null ? "" : str.trim();
    }
    public static void main(String[] args) {
        StringBuilder a = new StringBuilder("jz_我");
        a.append(Character.toChars(127467));
        System.out.println(a);
        System.out.println(countRealLength(a.toString()));
        System.out.println(hasUnshowChar(a.toString()));
    }
}
