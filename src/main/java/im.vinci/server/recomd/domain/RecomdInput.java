package im.vinci.server.recomd.domain;

/**
 * Created by mlc on 2016/9/21.
 */
public class RecomdInput {
    private int heartbeat_baseline;
    private int heartheat_current;
    private String topic;
    private String device_id;
    private int size;

    public int getHeartbeat_baseline() {
        return heartbeat_baseline;
    }

    public void setHeartbeat_baseline(int heartbeat_baseline) {
        this.heartbeat_baseline = heartbeat_baseline;
    }

    public int getHeartheat_current() {
        return heartheat_current;
    }

    public void setHeartheat_current(int heartheat_current) {
        this.heartheat_current = heartheat_current;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public int getSize() {
        if (size == 0) {
            size = 10;
        }
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
