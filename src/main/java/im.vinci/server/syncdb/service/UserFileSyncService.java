package im.vinci.server.syncdb.service;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.http.ProtocolType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.aliyuncs.sts.model.v20150401.AssumeRoleRequest;
import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse;
import com.google.common.collect.ImmutableMap;
import im.vinci.server.common.exceptions.VinciException;
import im.vinci.server.common.exceptions.error.ErrorCode;
import im.vinci.server.syncdb.domain.UserFileBusinessTypeEnum;
import im.vinci.server.syncdb.domain.UserSyncFileAuth;
import im.vinci.server.utils.BizTemplate;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * 用户进行文件操作的类,相当于一个个人的网盘
 * Created by tim@vinci on 16/10/19.
 */
@Service
public class UserFileSyncService {

    // 目前只有"cn-hangzhou"这个region可用, 不要使用填写其他region的值
    public static final String REGION_CN_HANGZHOU = "cn-hangzhou";
    // 当前 STS API 版本
    public static final String STS_API_VERSION = "2015-04-01";

    @Autowired
    private Environment env;

    @Value("${mobile.user.files.bucketName}")
    private String bucketName;

    @Value("${spring.profiles.active}")
    private String uploadDir;

    //这个是客户端上传下载文件的地址
    @Value("${mobile.user.files.client_endpoint}")
    private String clientEndpoint;

    //请求STS的client
    private DefaultAcsClient client;

    private String policyTemple;

    @PostConstruct
    private void init(){

        policyTemple = "{\n" +
                "    \"Version\": \"1\", \n" +
                "    \"Statement\": [\n" +
                "        {\n" +
                "            \"Action\": [\n" +
                "                \"oss:GetObject\", \n" +
                "                \"oss:PutObject\" \n" +
                "            ], \n" +
                "            \"Resource\": [\n" +
                "               \"acs:oss:*:*:"+bucketName+"/${user_path}/*\"\n" +
                "            ], \n" +
                "            \"Effect\": \"Allow\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        // 创建一个 Aliyun Acs Client, 用于发起 OpenAPI 请求
        IClientProfile profile = DefaultProfile.getProfile(REGION_CN_HANGZHOU,
                env.getProperty("mobile.upload_img.accessKeyId"), env.getProperty("mobile.upload_img.accessKeySecret"));
        client = new DefaultAcsClient(profile);
    }


    private AssumeRoleResponse assumeRole(String roleSessionName, String policy) throws ClientException {
        // 创建一个 AssumeRoleRequest 并设置请求参数
        final AssumeRoleRequest request = new AssumeRoleRequest();
        request.setDurationSeconds(900L);
        request.setVersion(STS_API_VERSION);
        request.setMethod(MethodType.POST);
        request.setProtocol(ProtocolType.HTTPS);
        String roleArn = "acs:ram::1847418972850959:role/aliyunosstokengeneratorrole";
        request.setRoleArn(roleArn);
        request.setRoleSessionName(roleSessionName);
        request.setPolicy(policy);
        // 发起请求，并得到response
        return client.getAcsResponse(request);
    }

    public UserSyncFileAuth auth(final long uid , final String businessType){
        return new BizTemplate<UserSyncFileAuth>("UserFileSyncService.auth") {

            @Override
            protected void checkParams() throws VinciException {
                if (UserFileBusinessTypeEnum.forName(businessType) == null) {
                    throw new IllegalArgumentException("不支持的b_type:"+businessType);
                }
            }

            @Override
            protected UserSyncFileAuth process() throws Exception {
                // RoleSessionName 是临时Token的会话名称，自己指定用于标识你的用户，主要用于审计，或者用于区分Token颁发给谁
                // 但是注意RoleSessionName的长度和规则，不要有空格，只能有'-' '_' 字母和数字等字符
                // 具体规则请参考API文档中的格式要求
                String roleSessionName = businessType + "-" + uid;
                String userPrefix = uploadDir + '/' + uid % 100 + '/' + (uid % 10000) / 100 + '/' + uid + '/' + businessType;
                ImmutableMap<String, String> map = ImmutableMap.<String,String>builder()
                        .put("user_path", userPrefix).build();
                String policy = new StrSubstitutor(map).replace(policyTemple);

                try {
                    final AssumeRoleResponse response = assumeRole(roleSessionName, policy);
                    UserSyncFileAuth auth = new UserSyncFileAuth();
                    auth.setAccessKeyId(response.getCredentials().getAccessKeyId());
                    auth.setAccessKeySecret(response.getCredentials().getAccessKeySecret());
                    auth.setSecurityToken(response.getCredentials().getSecurityToken());
                    auth.setExpiration(response.getCredentials().getExpiration());
                    auth.setBucketName(bucketName);
                    auth.setEndpoint("https://oss-cn-beijing.aliyuncs.com");
                    auth.setPathPrefix(userPrefix);
                    return auth;
                } catch (ClientException e) {
                    throw new VinciException(ErrorCode.USER_FILE_SPACE_AUTH_FAILED,e.getErrCode()+":"+e.getErrMsg(),"授权失败");
                }
            }
        }.execute();

    }

}
