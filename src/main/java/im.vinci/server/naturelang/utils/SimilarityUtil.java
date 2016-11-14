package im.vinci.server.naturelang.utils;

public class SimilarityUtil {
 /*   public static void main(String[] args) {
        //要比较的两个字符串
        String str1 = "刘德华冰雨";
        String str2 = "刘德华冰雨";
        levenshtein(str1,str2);
    }*/

    public static String trimString(String textContent) {
        textContent = textContent.trim();
        while (textContent.startsWith("　")) {
            textContent = textContent.substring(1, textContent.length()).trim();
        }
        while (textContent.endsWith("　")) {
            textContent = textContent.substring(0, textContent.length() - 1).trim();
        }
        return textContent;
    }

    /**
     * 　　DNA分析 　　拼字检查 　　语音辨识 　　抄袭侦测
     *
     * @createTime 2012-1-12
     */
    public static float levenshtein(String str1,String str2) {
        str1 = trimString(str1).toLowerCase();
        str2 = trimString(str2).toLowerCase();
        //计算两个字符串的长度。
        int len1 = str1.length();
        int len2 = str2.length();
        //建立上面说的数组，比字符长度大一个空间
        int[][] dif = new int[len1 + 1][len2 + 1];
        //赋初值，步骤B。
        for (int a = 0; a <= len1; a++) {
            dif[a][0] = a;
        }
        for (int a = 0; a <= len2; a++) {
            dif[0][a] = a;
        }
        //计算两个字符是否一样，计算左上的值
        int temp;
        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    temp = 0;
                } else {
                    temp = 1;
                }
                //取三个值中最小的
                dif[i][j] = min(dif[i - 1][j - 1] + temp, dif[i][j - 1] + 1,
                        dif[i - 1][j] + 1);
            }
        }
//        System.out.println("字符串\""+str1+"\"与\""+str2+"\"的比较");
        //取数组右下角的值，同样不同位置代表不同字符串的比较
//        System.out.println("差异步骤："+dif[len1][len2]);
        //计算相似度
        float similarity =1 - (float) dif[len1][len2] / Math.max(str1.length(), str2.length());
//        System.out.println("相似度："+similarity);
        return similarity;
    }

    //得到最小值
    private static int min(int... is) {
        int min = Integer.MAX_VALUE;
        for (int i : is) {
            if (min > i) {
                min = i;
            }
        }
        return min;
    }
    /**
     * @param str1
     * @param str2
     * @apiNote 求最长公共子串　　
     *
     * @since  2016-05-27
     *
     * @return string
     */
    public static String LCS(String str1, String str2)// str1为query，str2为field,实际上是求解两个字符串的最长公共子串
    {
        int len1, len2;
        len1 = str1.length();
        len2 = str2.length();
        int maxLen = len1 > len2 ? len1 : len2;

        int[] max = new int[maxLen];
        int[] maxIndex = new int[maxLen];
        int[] c = new int[maxLen];
        int i, j;
        for (i = 0; i < len2; i++)
            for (j = len1 - 1; j >= 0; j--) {
                if (str2.charAt(i) == str1.charAt(j)) {
                    if (i == 0 || j == 0)
                        c[j] = 1;
                    else
                        c[j] = c[j - 1] + 1;
                } else
                    c[j] = 0;
                if (c[j] > max[0]) {
                    max[0] = c[j];
                    maxIndex[0] = j;
                    for (int k = 1; k < maxLen; k++) {
                        max[k] = 0;
                        maxIndex[k] = 0;
                    }
                } else if (c[j] == max[0]) {
                    for (int k = 1; k < maxLen; k++) {
                        if (max[k] == 0) {
                            max[k] = c[j];
                            maxIndex[k] = j;
                            break; // 在后面加一个就要退出循环了
                        }
                    }
                }
            }
        String res = "";
        for (j = 0; j < maxLen; j++) {
            if (max[j] > 0) {
                for (i = maxIndex[j] - max[j] + 1; i <= maxIndex[j]; i++)
                    res += str1.charAt(i);
                break;
            }
        }
        return res;
    }

    /**
     * @param str
     * @return string
     * @apiNote 消除符号影响
     *
     * */
    public static  String filterStringByTokens(String str) {
        char[] array = {'\\', '、', '!', '！', '?', '？', ',', '，',
                '-', '_'};
        for (int i = 0; i < array.length; i++)
            str = str.replace(array[i], ' ');
		//str = str.replace(" ", "");
        str = str.trim();
        str = str.replaceAll("\\(...*\\)", "");
        str = str.replaceAll("\\（...*\\）", "");
        str = str.replace("(", "");
        str = str.replace(")", "");
        str = str.toLowerCase();
        return str;
    }


    /**
     * @param str
     * @return string
     * @apiNote 消除符号影响
     *
     * */
    public static  String filterStringByTokensNew(String str) {
        str = str.trim();
        str = str.replaceAll("\\(...*\\)", "");
        str = str.replaceAll("\\（...*\\）", "");
        str = str.replace("(", "");
        str = str.replace(")", "");
        String[] array = {"\\", "、", "!", "！", "?", "？", ",", "，","'",
                "-", "_"," "};
        for (int i = 0; i < array.length; i++)
            str = str.replace(array[i], "");
        str = str.toLowerCase();
        return str;
    }



}

