package im.vinci.server.naturelang.wrapper;

import im.vinci.server.naturelang.domain.UnderstandModel;

import java.io.Serializable;

public class UnderStandResult implements Serializable {

    private UnderstandModel underStandModel;
    private String requestId = null;
    private String flag = "OFF";

    public UnderstandModel getUnderStandModel() {
        return underStandModel;
    }

    public void setUnderStandModel(UnderstandModel underStandModel) {
        this.underStandModel = underStandModel;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getRequestId() {
        return requestId;
    }

    public UnderStandResult setRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }
}
