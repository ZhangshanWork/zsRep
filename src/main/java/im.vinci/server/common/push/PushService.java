package im.vinci.server.common.push;

import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jpush.api.JPushClient;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Message;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import im.vinci.server.common.domain.enums.BindDeviceType;
import im.vinci.server.common.exceptions.VinciException;
import im.vinci.server.utils.BizTemplate;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 对移动端消息推送的一个包装
 * Created by tim@vinci on 16/7/23.
 */
public class PushService {

    private final JPushClient headphonePushClient;

    private final JPushClient mobilePushClient;

    public PushService(String headPhoneAppKey, String headPhoneSecret,
                       String mobileAppKey, String mobileSecret) {
        headphonePushClient = new JPushClient(headPhoneSecret, headPhoneAppKey);
        mobilePushClient = new JPushClient(mobileSecret, mobileAppKey);
    }

    //TODO 需要实现真正的Async
    public void pushMessageAsync(long uid, BindDeviceType deviceType, String deviceId, Map<String, String> externalAttrs, String message) {
        pushMessage(String.valueOf(uid), deviceType, deviceId, externalAttrs, message);
    }

    public boolean pushMessageByUidAsync(long uid, Map<String, String> externalAttrs, String message) {
        return pushMessageByUid(String.valueOf(uid), externalAttrs, message);
    }

    /**
     * 向一个机器推送消息
     */
    public boolean pushMessage(String uid, BindDeviceType deviceType, String deviceId, Map<String, String> externalAttrs, String message) {
        return new BizTemplate<Boolean>("pushMessage") {

            @Override
            protected boolean onError(Throwable throwable) {
                if (throwable != null && (
                        throwable instanceof APIConnectionException
                                || throwable instanceof APIRequestException)) {
                    return false;
                }
                return true;
            }

            @Override
            protected Boolean defaultResult() {
                return false;
            }

            @Override
            protected void checkParams() throws VinciException {

            }

            @Override
            protected Boolean process() throws Exception {
                if (deviceType == null || StringUtils.isEmpty(deviceId)) {
                    return false;
                }
                Message msg = Message.newBuilder().addExtras(externalAttrs).setMsgContent(message).build();
                PushPayload.Builder builder = PushPayload.newBuilder()
                        .setPlatform(Platform.android())
                        .setAudience(Audience.alias(deviceId))
                        .setMessage(msg);
//                if (StringUtils.hasText(uid)) {
//                    builder.setAudience(
//                            Audience.newBuilder().addAudienceTarget(AudienceTarget.alias(deviceId))
//                                    .addAudienceTarget(AudienceTarget.tag(uid)).build()
//                    );
//                }
                JPushClient client = null;
                switch (deviceType) {
                    case headphone:
                        builder.setPlatform(Platform.android());
                        client = headphonePushClient;
                        break;
                    case mobile:
                        builder.setPlatform(Platform.android_ios());
                        client = mobilePushClient;
                        break;
                }

                PushResult result = client.sendPush(builder.build());
                return result != null && result.isResultOK();
            }
        }.execute();
    }

    /**
     * 同时向手机和headphone端推送消息
     */
    public Boolean pushMessageByUid(String uid, Map<String, String> externalAttrs, String message) {
        return new BizTemplate<Boolean>("pushMessageByUid") {

            @Override
            protected void checkParams() throws VinciException {

            }

            @Override
            protected Boolean process() throws Exception {
                Message msg = Message.newBuilder().addExtras(externalAttrs).setMsgContent(message).build();
                PushPayload.Builder builder = PushPayload.newBuilder()
                        .setPlatform(Platform.android())
                        .setAudience(Audience.tag(uid))
                        .setMessage(msg);
                try {
                    PushResult result = headphonePushClient.sendPush(builder.build());
                    if (!result.isResultOK()) {
                        return false;
                    }
                    builder.setPlatform(Platform.android_ios());
                    result = mobilePushClient.sendPush(builder.build());
                    if (!result.isResultOK()) {
                        return false;
                    }
                } catch (APIConnectionException | APIRequestException e) {
                    return false;
                }
                return true;
            }
        }.execute();


    }

}
