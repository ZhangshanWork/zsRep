package im.vinci.server.naturelang.utils;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by mlc on 2016/6/4.
 */
public class CommonUtils {

    private static Logger logger = LoggerFactory.getLogger(CommonUtils.class);

    /**
     * @param str1,str2
     * @apiNote 判定字符串是否相等
     * */
    public static boolean ifEqual(String str1, String str2) {
        boolean flag = false;
        str1 = SimilarityUtil.trimString(SimilarityUtil.filterStringByTokensNew(str1));
        str2 = SimilarityUtil.trimString(SimilarityUtil.filterStringByTokensNew(str2));
        if (str1.equalsIgnoreCase(str2)) {
            flag = true;
        }
        return flag;
    }


    /**
     * @param text
     * @apiNote 判定文本中是否包含地区信息
     * */
    public static boolean ifExitPlace(String text) {
        boolean flag = false;
        Segment segment = HanLP.newSegment().enablePlaceRecognize(true);
        List<Term> termList = segment.seg(text);
        for (Term term : termList) {
            System.out.println(term);
            if (term.toString().contains("ns")) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    /**
     *
     * @param IP
     * @return
     *
     * @apiNote  获取ip所在地
     */
    public static String GetAddressByIp(String IP){
        String resout = "北京";
        try{
            String str = getJsonContent("http://ip.taobao.com/service/getIpInfo.php?ip="+IP);

            //System.out.println(str);

            JSONObject obj = JSONObject.fromObject(str);
            JSONObject obj2 =  (JSONObject) obj.get("data");
            String code = String.valueOf(obj.get("code"));
            if(code.equals("0")){
                if(StringUtils.isNotBlank(obj2.get("city")+"")){
                    resout =  obj2.get("city") + "";
                }
                //resout =  obj2.get("country")+"--" +obj2.get("area")+"--" +obj2.get("city")+"--" +obj2.get("isp");
            }else{
                resout =  "IP地址有误";
            }
        }catch(Exception e){

            logger.error("用户ip " + IP + "  message:" + e.getMessage());
            resout = "北京";
        }
        return resout;

    }

    public static String getJsonContent(String urlStr)
    {
        try
        {// 获取HttpURLConnection连接对象
            URL url = new URL(urlStr);
            HttpURLConnection httpConn = (HttpURLConnection) url
                    .openConnection();
            // 设置连接属性
            httpConn.setConnectTimeout(3000);
            httpConn.setDoInput(true);
            httpConn.setRequestMethod("GET");
            // 获取相应码
            int respCode = httpConn.getResponseCode();
            if (respCode == 200)
            {
                return ConvertStream2Json(httpConn.getInputStream());
            }
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
            logger.error("message:" + e.getMessage());
        }
        catch (IOException e)
        {
            logger.error("message:" + e.getMessage());
        }
        return "";
    }


    private static String ConvertStream2Json(InputStream inputStream)
    {
        String jsonStr = "";
        // ByteArrayOutputStream相当于内存输出流
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        // 将输入流转移到内存输出流中
        try
        {
            while ((len = inputStream.read(buffer, 0, buffer.length)) != -1)
            {
                out.write(buffer, 0, len);
            }
            // 将内存流转换为字符串
            jsonStr = new String(out.toByteArray());
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            logger.error("message:" + e.getMessage());
        }
        return jsonStr;
    }
    //处理常见标点符号
    public static String filter(Object obj)
    {
        if(null == obj||StringUtils.isEmpty(obj.toString())) {
            return "";
        }
        String str = obj.toString();
        char[] array = {'\\', '、', '!', '！', '?', '？',  '（', '）', ',', '，',
                '-', '_','"','－'};
        str = str.replaceAll("\\(...*\\)", "");
        str = str.replaceAll("（...*）", "");
        str = str.replace("(", "");
        str = str.replace(")", "");
        for (int i = 0; i < array.length; i++)
            str = str.replace(array[i], ' ');
        str = str.replace("'", " ");
        str = SimilarityUtil.trimString(str).toLowerCase();
        str = HanLP.convertToSimplifiedChinese(str);
        return str;
    }
}
