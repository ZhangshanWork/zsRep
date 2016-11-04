package im.vinci.server.other.domain.wrappers.requests.music;

/**
 * Created by henryhome on 10/2/15.
 */
public class MusicRecommendation {

    private Integer heartbeatBaseline;
    private Integer heartbeatCurrent;
    private String topic;
    private String userId;
    private Integer size;

    public Integer getHeartbeatBaseline() {
        return heartbeatBaseline;
    }

    public void setHeartbeatBaseline(Integer heartbeatBaseline) {
        this.heartbeatBaseline = heartbeatBaseline;
    }

    public Integer getHeartbeatCurrent() {
        return heartbeatCurrent;
    }

    public void setHeartbeatCurrent(Integer heartbeatCurrent) {
        this.heartbeatCurrent = heartbeatCurrent;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}
