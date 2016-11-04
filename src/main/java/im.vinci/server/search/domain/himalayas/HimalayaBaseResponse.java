package im.vinci.server.search.domain.himalayas;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

/**
 * 喜马拉雅基础response
 * Created by tim@vinci on 15/12/23.
 */
public abstract class HimalayaBaseResponse implements Serializable{
    private int ret;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String errmsg;

    public int getRet() {
        return ret;
    }

    public void setRet(int ret) {
        this.ret = ret;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }
}
