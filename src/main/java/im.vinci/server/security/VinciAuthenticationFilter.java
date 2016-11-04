package im.vinci.server.security;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import im.vinci.monitor.util.SystemTimer;
import im.vinci.server.common.exceptions.VinciAuthenticationException;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Created by henryhome on 2/15/15.
 */
@Component
@Aspect
@Order(Integer.MAX_VALUE)
public class VinciAuthenticationFilter {

    @Autowired
    private Environment env;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private String godSign = null;

    @PostConstruct
    private void initComplete() {
        String g = env.getProperty("auth.god_sign","").trim();
        if (StringUtils.hasText(g)) {
            godSign = g;
            logger.info("authentication god sign was settled that was : '{}'",godSign);
        }

    }

    @Before("@annotation(im.vinci.server.security.ApiSecurityLabel)")
    public void doFilter() {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String clientSignString = request.getHeader("sign");
        if (godSign != null && godSign.equals(clientSignString)) {
            logger.info("this request passed for god sign");
            return;
        }
        if (StringUtils.isEmpty(clientSignString)) {
            throw new VinciAuthenticationException("客户端没有传入sign");
        }
        String macAddress = request.getHeader("mac");
        String imei = request.getHeader("imei");
        long timestamp;
        try {
             timestamp = Long.parseLong(request.getHeader("timestamp"));
        }catch (Exception e) {
            throw new VinciAuthenticationException("timestamp没有传入正确的值");
        }

        //是否超过5分钟
        if (Math.abs(SystemTimer.currentTimeMillis() - timestamp) > 300000) {
//            throw new VinciAuthenticationException("timestamp传入的值大于5分钟了");
        }

        String[] secrets = {
                HmacSignatureGenerationUtil.genToken(macAddress, imei, 0),
                HmacSignatureGenerationUtil.genToken(macAddress, imei, -1),
                HmacSignatureGenerationUtil.genToken(macAddress, imei, -2)
        };

        boolean isOk = false;

        if (request.getContentType().startsWith("application/json;")) {

            StringBuilder builder = new StringBuilder();
            try {
                BufferedReader reader = request.getReader();
                char[] buffer = new char[1024];
                int len;
                while ((len = reader.read(buffer)) > 0) {
                    builder.append(buffer, 0, len);
                }
            } catch (IOException e) {
            }
            String content = builder.toString();
            for (String secret : secrets) {
                String sign = HmacSignatureGenerationUtil.generateSignature(timestamp + HmacSignatureGenerationUtil.generateSignature(content, secret), secret);
                if (logger.isDebugEnabled()) {
                    logger.debug("000000000-RequestBody check content is '" + content + "' , and server sign = " + sign);
                }
                if (sign.equals(clientSignString)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("000000000-RequestParam check ok, content is '" + content + "' , and sign = " + sign);
                    }
                    isOk = true;
                    break;
                }
            }
            if (!isOk) {
                if (logger.isDebugEnabled()) {
                    logger.debug("000000000-RequestBody check error, content is '" + content + "', and mac,imei:" + macAddress + "," + imei + " , and client sign = " + clientSignString);
                }
                throw new VinciAuthenticationException("接口认证失败");
            }
        } else if(request.getContentType().startsWith("application/octet-stream")){
            String md5 = "";
            try {
                InputStream is = request.getInputStream();
                md5 = getMD5(is);
            } catch (IOException e){

            } catch (NoSuchAlgorithmException e){

            }

            String content = md5;

            for (String secret : secrets) {
                String sign = HmacSignatureGenerationUtil.generateSignature(timestamp+HmacSignatureGenerationUtil.generateSignature(content, secret),secret);
                System.out.println(sign);

                if (logger.isDebugEnabled()) {
                    logger.debug("stream-000000-RequestBody check content is '"+content + "' , and server sign = "+sign + " , and secret:"+secret);
                }
                if (sign.equals(clientSignString)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("stream-000000000-RequestParam check ok, content is '"+content + "' , and sign = "+sign);
                    }
                    isOk = true;
                    break;
                }
            }
            if (!isOk) {
                if (logger.isDebugEnabled()) {
                    logger.debug("000000000-RequestBody check error, content is '" + content + "', and mac,imei:"+macAddress+","+imei+" , and client sign = " + clientSignString);
                }
                throw new VinciAuthenticationException("接口认证失败");
            }


        } else {
            Map<String, String[]> params = Maps.newHashMap(request.getParameterMap());
            if (request.getContentType().startsWith("multipart/form-data;")) {
                try {
                    Collection<Part> parts = request.getParts();
                    if (!CollectionUtils.isEmpty(parts)) {
                        for (Part part : parts) {
                            if (part == null) {
                                continue;
                            }
                            if (part.getSubmittedFileName() != null) {
                                String md5 = getMD5(part.getInputStream());
                                params.put(part.getName(), new String[]{md5});
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            String toSignString = getSignatureContent(params,timestamp);
            for (String secret : secrets) {
                String sign = HmacSignatureGenerationUtil.generateSignature(toSignString, secret);
                if (logger.isDebugEnabled()) {
                    logger.debug("000000000-RequestParam check temp error, content is '" + toSignString
                            + "', and mac,imei:"+macAddress+","+imei+" , and client sign = " + clientSignString
                            + " , and correct sign : "+sign + " , secret:"+secret);
                }
                if (sign.equals(clientSignString)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("000000000-RequestParam check ok, content is '"+toSignString + "' , and sign = "+sign);
                    }
                    isOk = true;
                    break;
                }
            }
            if (!isOk) {
                if (logger.isDebugEnabled()) {
                    logger.debug("000000000-RequestParam check error, content is '" + toSignString + "', and mac,imei:"+macAddress+","+imei+" , and client sign = " + clientSignString);
                }
                throw new VinciAuthenticationException("接口认证失败");
            }
        }

    }

    public static String getSignatureContent(Map<String, String[]> params, long timestamp) {
        Map<String, String[]> sortedParams = new TreeMap<>(params);

        StringBuilder content = new StringBuilder();
        List<String> keys = new ArrayList<String>(sortedParams.keySet());
        Collections.sort(keys);
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String[] value = sortedParams.get(key);
            if (value == null || value.length == 0 || value[0] == null) {
                content.append(i == 0 ? "" : "&").append(key).append("=");
            } else {
                content.append(i == 0 ? "" : "&").append(key).append("=").append(value[0]);
            }
        }
        if (content.length() != 0) {
            content.append("&");
        }
        content.append("timestamp=").append(timestamp);
        return content.toString();
    }

    /**
     * 获取该输入流的MD5值
     *
     * @param is
     * @return
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    private static String getMD5(InputStream is) throws NoSuchAlgorithmException, IOException {
        StringBuffer md5 = new StringBuffer();
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] dataBytes = new byte[1024];

        int nread = is.read(dataBytes);
        md.update(dataBytes, 0, nread);
        byte[] mdbytes = md.digest();
        // convert the byte to hex format
        for (int i = 0; i < mdbytes.length; i++) {
            md5.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        return md5.toString();
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        String macAddress = "e8e3f9cfc8ef42d9971dd20722d2e094";
        String imei = "b6384dd1145bfbbafe739a52600c2a63";
        String secret = "mQVvPkREK6O4VEejSxNbGA==";
        long timestamp = 1473059278837L;
        String content = "attention_uid=6&timestamp=1473059278837";
        String toSignString = getSignatureContent(ImmutableMap.of("attention_uid",new String[]{"6"}),timestamp);

        String[] secrets = {
                HmacSignatureGenerationUtil.genToken(macAddress, imei, 0),
                HmacSignatureGenerationUtil.genToken(macAddress, imei, -1),
                HmacSignatureGenerationUtil.genToken(macAddress, imei, -2)
        };
        System.out.println(toSignString);
        System.out.println(Arrays.toString(secrets));
        System.out.println(HmacSignatureGenerationUtil.generateSignature(toSignString, secrets[1]));

    }
}
