package im.vinci.server.device.controller;

import im.vinci.monitor.util.SystemTimer;
import im.vinci.server.common.exceptions.InvalidParameterException;
import im.vinci.server.common.exceptions.error.ErrorCode;
import im.vinci.server.device.domain.DeviceUserLog;
import im.vinci.server.device.domain.wrappers.requests.CrashLogUploading;
import im.vinci.server.device.service.DeviceModifyService;
import im.vinci.server.device.service.UserLogModifyService;
import im.vinci.server.utils.DateUtils;
import im.vinci.server.utils.FileUtils;
import im.vinci.server.utils.apiresp.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.File;
import java.text.ParseException;
import java.util.List;

/**
 * Created by tim@vinci on 15/11/16.
 * 收集用户log的controller
 */
@RestController
@RequestMapping(value = "/vinci/userlog_collector", produces = "application/json;charset=UTF-8")
public class UserLogCollectorController {

    private static Logger logger = LoggerFactory.getLogger(UserLogCollectorController.class);

    @Autowired
    Environment env;

    @Autowired
    private UserLogModifyService service;

    @Autowired
    private DeviceModifyService deviceModifyService;


    private String voice_record_dir = ".";

    @PostConstruct
    public void init() {
        voice_record_dir = env.getProperty("voice_record.dir",".");
    }


    @RequestMapping(value = "/push_nlp_voice", method = RequestMethod.POST , consumes = {"application/octet-stream"})
    public Result pushLog(@RequestParam("request_id")  String requestId,
                          HttpEntity<byte[]> requestEntity)
            throws Exception {
        if (StringUtils.isEmpty(requestId)) {
            return new Result();
        }
        try {
            if (requestEntity != null && requestEntity.getBody() != null && requestEntity.getBody().length >0) {
                File file = new File(voice_record_dir + "/" + SystemTimer.getTimeyyyyMMdd(), requestId + ".pcm");
                FileUtils.writeByteArrayToFile(file, requestEntity.getBody());
            }
        }catch (Exception e) {
            logger.warn("write record error:",e);
        }
        return new Result();
    }

    @RequestMapping(value = "/pushlogs", method = RequestMethod.POST)
    public Result pushLogs(@RequestBody List<DeviceUserLog> userLogs,
                           @RequestHeader("User-Agent") String userAgent,
                           @RequestHeader("localtime") String localtime)
            throws Exception {
        for (DeviceUserLog userLog : userLogs) {
            if (userLog == null || StringUtils.isEmpty(userLog.getName())) {
                logger.warn("用户log收集获取一个错误条目:{},{},{}", ErrorCode.BAD_REQUEST, "request userLog is null", "用户log数据为空");
                break;
            }
            if (userLog.getInfo() == null) {
                logger.warn("用户log收集获取一个错误条目:{},{},{}", ErrorCode.BAD_REQUEST, "request userLog's user is null", "用户log数据没有用户标识");
                break;
            }
            _setAgentForLog(userLog, userAgent);
            _reviseLogCreateTime(userLog, localtime);
            service.sendLogToONS(userLog);
        }
        return new Result();
    }

    @RequestMapping(value = "/upload_crash_log", method = RequestMethod.POST)
    public Result uploadCrashLog(@RequestParam(value = "imei",defaultValue = "")String imei,
                                 @RequestParam(value = "mac",defaultValue = "")String mac,
                                 @RequestParam(value = "appname",defaultValue = "unknown")String appName,
                                 @RequestParam(value = "crashtype",defaultValue = "unknown")String crashType,
                                 @RequestParam(value = "current_version",defaultValue = "unknown")String currentVersion,
                                 @RequestParam(value = "crash_count",defaultValue = "1")int crashCount,
                                 @RequestParam(value = "log")String log)
            throws Exception {
        try {
            CrashLogUploading crashLogUploading = new CrashLogUploading();
            crashLogUploading.setImei(imei);
            crashLogUploading.setMac(mac);
            crashLogUploading.setAppName(appName);
            crashLogUploading.setAppVersion(currentVersion);
            crashLogUploading.setCrashType(crashType);
            crashLogUploading.setCrashCount(crashCount);
            crashLogUploading.setLog(log);
            deviceModifyService.uploadCrashLog(crashLogUploading);
        } catch(Exception e) {
            logger.error("upload crash log got error : "+e.toString(),e);
            throw e;
        }

        return new Result();
    }

    /**
     * 可能经过两次校正:
     * 1,校正时区误差值
     * 2,校正前端的错误值
     *
     * @param userLog
     * @param headSetLocalTime
     * @throws ParseException
     */
    private void _reviseLogCreateTime(DeviceUserLog userLog, String headSetLocalTime) throws ParseException {
        long logCreatetime = userLog.getCreatetime();
        long now = System.currentTimeMillis();

        if (logCreatetime > now) {
            logCreatetime = now;
        } else {
            if (headSetLocalTime != null) {
                int diff = DateUtils.diffTimeZoneWithHourInMillion(headSetLocalTime);
                long revisedTimestamp = logCreatetime + diff;
                if (revisedTimestamp <= now) {//小于等于now才校正,大于的话就是前端已经校正了,1.6.0版本的rom里,前端对时区做了校正
                    logCreatetime = revisedTimestamp;
                }
            }

            //因为首次使用头机不联网校正时间,会产生一写时间戳不对的log,这种log在1.1.3之后的rom中得到校正,之前版本的rom产生的异常时间log靠后端这里校正
            //乘于7是因为前端log最多保持7天
            if (logCreatetime + (7 * 86400000) < now) {
                logCreatetime = now;
            }
        }

        userLog.setCreatetime(logCreatetime);
    }

    private void _setAgentForLog(DeviceUserLog userLog, String agent) throws InvalidParameterException {
        if (agent == null || agent.equals("vinci-headset")) {
            userLog.setAgent("headset");
        } else if (agent.equals("WeiXin")) {
            userLog.setAgent("WeiXin");
        } else if (agent.contains("Vinci-HeadSet")) {//形如:Dalvik/1.6.0 (Linux; U; Android 4.4.2; Vinci-HeadSet Build/KOT49H)
            userLog.setAgent("headset");
        } else {
            System.out.println("agent is:" + agent);
            throw new InvalidParameterException(ErrorCode.BAD_REQUEST, "request userLog's agent value of header is invalid", "header里的agent值不合法");
        }
    }
}
