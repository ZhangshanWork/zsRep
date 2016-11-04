package im.vinci.server.security;

import com.google.common.collect.ImmutableMap;
import im.vinci.monitor.util.SystemTimer;
import im.vinci.server.common.exceptions.VinciAuthenticationException;
import im.vinci.server.utils.apiresp.ResultObject;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * create by TimWang at 2016.7.15
 */
@RestController
@RequestMapping(value = "/vinci/security", produces = "application/json;charset=UTF-8")
public class VinciAuthenticationController {

    /**
     * 为客户端生成token
     */
    @RequestMapping(value = "/gen_auth", method = RequestMethod.POST)
    @ResponseBody
    public ResultObject<Map<String, String>> generateAuthInfo(
            @RequestHeader("mac") String macAddress,
            @RequestHeader("imei") String imei,
            @RequestParam("timestamp") long timestamp,
            @RequestParam("sign") String signString
    ) {

        //TODO 这里要验证IMEI和mac地址是否是我们的机器, 同时还要对调用次数做限制

        long currentTime = SystemTimer.currentTimeMillis();
        if (Math.abs(currentTime - timestamp) > 300000L) {
            throw new VinciAuthenticationException("访问时限超过5分钟");
        }
        String toBeSignString = macAddress + imei + timestamp + "fnJoFt3NM#3;";
        String secretString = timestamp % 1000000 + "KM8+/pNG3UEY";
        String correctSignString = HmacSignatureGenerationUtil.generateSignature(toBeSignString, secretString);

        if (!correctSignString.equals(signString)) {
            throw new VinciAuthenticationException("获取auth token签名失败,correct is " + correctSignString);
        }
        String token = HmacSignatureGenerationUtil.genToken(macAddress,imei,0);
        return new ResultObject<>(ImmutableMap.<String, String>builder().put("token", token).build());
    }

//    @RequestMapping(value = "/test", method = RequestMethod.POST )
//    @ResponseBody
//    @ApiSecurityLabel
//    public Result test(@RequestBody JsonNode details) {
////        System.out.println(VinciUtils.getStringFromException(new Exception()));
////        throw new VinciAuthenticationException("abcd");
//        System.err.println(JsonUtils.encode(details));
//        return new Result();
//    }
//
//    @RequestMapping(value = "/test2", method = RequestMethod.GET)
//    @ResponseBody
//    @ApiSecurityLabel
//    public Result test(@RequestParam("a1")String a1, @RequestParam("a2")String a2) {
////        System.out.println(VinciUtils.getStringFromException(new Exception()));
////        throw new VinciAuthenticationException("abcd");
//        return new Result();
//    }
//
//    @RequestMapping(value = "/test2", method = RequestMethod.POST )
//    @ResponseBody
//    @ApiSecurityLabel
//    public Result testPost(@RequestParam("a1")String a1, @RequestParam("a2")String a2) {
////        System.out.println(VinciUtils.getStringFromException(new Exception()));
////        throw new VinciAuthenticationException("abcd");
//        return new Result();
//    }
//
//    public static void main(String[] args) {
//        String macAddress = "12345";
//        String imei = "234567";
//        long timestamp = 1468658200000L;
//        String token = HmacSignatureGenerationUtil.genToken(macAddress,imei,0);
//        System.out.println(token);
//    }
}



