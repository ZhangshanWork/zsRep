package im.vinci.server.user.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.google.common.collect.ImmutableMap;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import im.vinci.server.common.exceptions.VinciException;
import im.vinci.server.common.exceptions.error.ErrorCode;
import im.vinci.server.utils.BizTemplate;
import im.vinci.server.utils.JsonUtils;
import im.vinci.server.utils.cache.Cache;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Date;
import java.util.Random;

/**
 * 发短信的service
 * Created by tim@vinci on 16/3/22.
 */
@Service
public class SmsService {
    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private Environment env;

    @Autowired
    private Cache cache;

    private boolean isDebug;

    private final static String[] TEMPLATE = {
            "【${sign}】您的验证码是${code}",
            "【${sign}】您的订单${orderid}已发货，运单号${transportid}"
    };

    @PostConstruct
    protected void init() {
        Unirest.setTimeouts(10000, 20000);
        isDebug = "true".equalsIgnoreCase(env.getProperty("isDebug"));
    }

    @PreDestroy
    protected void destroy() throws IOException {
        Unirest.shutdown();
    }

    private final static String SIGN = "VINCI玩起智能头机";

    /**
     * 发送验证码
     *
     * @param phoneNum
     * @return
     */
    public boolean sendValidCode(final String phoneNum) {
        return new BizTemplate<Boolean>("sendValidCode") {
            @Override
            protected void checkParams() throws VinciException {
                if (StringUtils.isEmpty(phoneNum)) {
                    throw new VinciException(ErrorCode.API_USER_PHONENUM_NOT_CORRECT,"phonenum is empty","请输入正确的手机号");
                }
            }

            @Override
            protected Boolean process() throws Exception {
                long t = System.currentTimeMillis();

                String r = cache.get("phone_valid_code_" + phoneNum, String.class);
                if (StringUtils.hasText(r) && r.indexOf('_')>0) {
                    long lt = NumberUtils.toLong(r.substring(r.indexOf('_')+1),-1);
                    if (lt > 0 && (t - lt)>60000L) { //1分钟内不能向同一个手机号发送
                        throw new VinciException(ErrorCode.API_LOGIN_VALIDCODE_SEND_FREQUENCY,"{} 手机号发送验证码太频繁","不能太频繁发送验证码");
                    }
                }
                r = String.valueOf(new Random(t).nextInt(89999) + 10000);
                cache.put("phone_valid_code_" + phoneNum, r+"_"+t,new Date(1000 * 60));
                if (isDebug) {
                    logger.info("generate valid code for {} and that is {}",phoneNum,r);
                    return true;
                }

                return sendUserValidCode(phoneNum, r);
            }
        }.execute();
    }

    /**
     * 验证用户输入的验证码是否正确
     * @param phoneNum
     * @param code
     * @return
     */
    public boolean validCode(final String phoneNum , final String code) {
        return new BizTemplate<Boolean>("validCode") {

            @Override
            protected void checkParams() throws VinciException {
                if (StringUtils.isEmpty(phoneNum)) {
                    throw new VinciException(ErrorCode.API_USER_PHONENUM_NOT_CORRECT,"phonenum is empty","请输入正确的手机号");
                }
            }

            @Override
            protected Boolean process() throws Exception {
                if (isDebug) {
                    logger.info("valid code for {} and that is debug mode",phoneNum);
                    return true;
                }
                String r = cache.get("phone_valid_code_" + phoneNum, String.class);
                if (StringUtils.isEmpty(r)) {
                    throw new VinciException(ErrorCode.API_LOGIN_VALIDCODE_EXPIRED,"/api/login valid code is expire","验证码已过期");
                }
                if (!r.startsWith(code+"_")) {
                    throw new VinciException(ErrorCode.API_LOGIN_VALIDCODE_NOT_CORRECT,"/api/login valid code is not correct","验证码输入错误");
                }
                return true;
            }
        }.execute();

    }

    private boolean sendUserValidCode(String phoneNum, String code) throws UnirestException {
        if (StringUtils.isEmpty(phoneNum) || StringUtils.isEmpty(code)) {
            throw new VinciException(ErrorCode.API_USER_PHONENUM_NOT_CORRECT, "phonenum or code is empty", "请输入正确的手机号");
        }
        ImmutableMap<String, String> map = ImmutableMap.<String, String>builder().put("sign", SIGN).put("code", code).build();
        String msg = new StrSubstitutor(map).replace(TEMPLATE[0]);
        if (phoneNum.startsWith("86")) {
            return YunpianSendMsg(phoneNum.substring(2), msg);
        } else {
            return YunpianSendMsg(phoneNum, msg);
        }

    }

    public static void main(String[] args) throws UnirestException {
        new SmsService().sendUserValidCode("8618601998564", "12345");
    }

    // 国都
    private boolean GuoduSendMsg(String phonenum, String msg) throws UnirestException {
        HttpResponse<String> response = Unirest.get("http://221.179.180.158:9007/QxtSms/QxtFirewall").queryString("OperID", "bjjrdl").queryString("OperPass", "jrdl666")
                .queryString("SendTime", "").queryString("ValidTime", "").queryString("AppendID", "").queryString("DesMobile", phonenum)
                .queryString("Content", msg, "GBK").queryString("ContentType", "8").asString();
        if (response.getStatus() != 200) {
            logger.warn("send goudu sms failed:{},{}", response.getStatus(), response.getStatusText());
            return false;
        }
        // success return : <?xml version="1.0" encoding="gbk" ?><response><code>03</code><message><desmobile>8618601998564</desmobile><msgid>E0354016032217083900</msgid></message></response>
        // failed return  : <?xml version="1.0" encoding="gbk" ?><response><code>05</code></response>
        if (response.getBody() != null && response.getBody().contains("<response><code>03</code>")) {
            return true;
        }
        logger.warn("send goudu sms failed:{}", response.getBody());
        return false;
    }

    //云片
    private boolean YunpianSendMsg(String phonenum, String msg) throws UnirestException {
        ImmutableMap<String, Object> map = ImmutableMap.<String, Object>builder().put("apikey", "29fd611ffa792e9694fae1c6d439ca06")
                .put("mobile", phonenum).put("text", msg).build();
        HttpResponse<String> response = Unirest.post("http://yunpian.com/v1/sms/send.json").fields(map).asString();
        if (response.getStatus() != 200) {
            logger.warn("send yunpian sms failed:{},{}", response.getStatus(), response.getStatusText());
            return false;
        }
        // success: {"code":0,"msg":"OK","result":{"count":1,"fee":1,"sid":5468025326}}
        // failed : {"code":2,"msg":"请求参数格式错误","detail":"参数 mobile 格式不正确，mobile手机号格式不正确"}
        JsonNode root = JsonUtils.decode(response.getBody(), JsonNode.class);
        if (root == null || root.findValue("code") == null ||
                root.findValue("code").getNodeType() != JsonNodeType.NUMBER ||
                root.findValue("code").asInt() != 0) {
            logger.warn("send yunpian sms failed:{}", response.getBody());
            return false;
        }
        return true;
    }

    //据说可以发国外短信
    private boolean NexmoSendMsg(String phonenum, String msg) throws UnirestException {
        HttpResponse<String> response = Unirest.get("https://rest.nexmo.com/sms/json").queryString("api_key", "dca94860").queryString("api_secret", "6f830969")
                .queryString("from", "VINCI").queryString("to", phonenum).queryString("type", "unicode").queryString("text", msg).asString();
        if (response.getStatus() != 200) {
            logger.warn("send Nexmo sms failed:{},{}", response.getStatus(), response.getStatusText());
            return false;
        }
        // success:
        /*

        {
    "message-count": "1",
    "messages": [{
        "to": "8618601998564",
        "message-id": "03000000E01FC585",
        "status": "0",
        "remaining-balance": "11.39950000",
        "message-price": "0.02800000",
        "network": "46001"
    }]
    }
         */

        JsonNode root = JsonUtils.decode(response.getBody(), JsonNode.class);
        if (root == null || root.findValue("status") == null ||
                root.findValue("status").getNodeType() != JsonNodeType.STRING ||
                !"0".equals(root.findValue("status").asText())) {
            logger.warn("send Nexmo sms failed:{}", response.getBody());
            return false;
        }
        return true;
    }
}
