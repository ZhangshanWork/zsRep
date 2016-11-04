package im.vinci.server.other.domain.wrappers.requests.user;

/**
 * Created by henryhome on 10/17/15.
 */
public class FirstMusicGet {

    private String deviceId;
    private String currentMood;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getCurrentMood() {
        return currentMood;
    }

    public void setCurrentMood(String currentMood) {
        this.currentMood = currentMood;
    }

    public String toString() {
        return "{\"device_id\":\"" + deviceId + "\",\"current_mood\":\"" + currentMood + "\"}";
    }
}
