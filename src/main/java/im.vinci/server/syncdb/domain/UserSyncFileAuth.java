package im.vinci.server.syncdb.domain;

import com.google.common.base.MoreObjects;
import im.vinci.server.utils.JsonUtils;
import im.vinci.server.utils.json.MaskField;

import java.io.Serializable;

/**
 * 用户空间授权
 * Created by tim@vinci on 16/10/20.
 */
public class UserSyncFileAuth implements Serializable{
    @MaskField
    private String accessKeyId;
    @MaskField
    private String accessKeySecret;
    @MaskField
    private String securityToken;
    private String expiration;
    private String endpoint;
    private String bucketName;
    private String pathPrefix;

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public UserSyncFileAuth setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
        return this;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public UserSyncFileAuth setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
        return this;
    }

    public String getSecurityToken() {
        return securityToken;
    }

    public UserSyncFileAuth setSecurityToken(String securityToken) {
        this.securityToken = securityToken;
        return this;
    }

    public String getExpiration() {
        return expiration;
    }

    public UserSyncFileAuth setExpiration(String expiration) {
        this.expiration = expiration;
        return this;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public UserSyncFileAuth setEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public String getBucketName() {
        return bucketName;
    }

    public UserSyncFileAuth setBucketName(String bucketName) {
        this.bucketName = bucketName;
        return this;
    }

    public String getPathPrefix() {
        return pathPrefix;
    }

    public UserSyncFileAuth setPathPrefix(String pathPrefix) {
        this.pathPrefix = pathPrefix;
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("accessKeyId", accessKeyId == null?"null":"*******")
                .add("accessKeySecret", accessKeySecret == null?"null":"*******")
                .add("securityToken", securityToken == null?"null":"*******")
                .add("expiration", expiration)
                .add("endpoint", endpoint)
                .add("bucketName", bucketName)
                .add("pathPrefix", pathPrefix)
                .toString();
    }

    public static void main(String[] args) {
        UserSyncFileAuth auth = new UserSyncFileAuth();
        auth.setAccessKeyId("abcd").setAccessKeySecret("bbbb").setSecurityToken("token").setExpiration("aaaaa");
        System.out.println(JsonUtils.encode(auth));
        System.out.println(JsonUtils.encodeWithMask(auth));
    }
}
