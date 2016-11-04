package im.vinci.server.other.domain.wrappers.responses.account;

/**
 * Created by henryhome on 2/27/15.
 */
public class VinciCredentials {
    
    private String accessId;
    private String accessKey;

    public VinciCredentials() {
    }
    
    public VinciCredentials(String accessId, String accessKey) {
        this.accessId = accessId;
        this.accessKey = accessKey;
    }
    
    public String getAccessId() {
        return accessId;
    }

    public void setAccessId(String accessId) {
        this.accessId = accessId;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }
}
