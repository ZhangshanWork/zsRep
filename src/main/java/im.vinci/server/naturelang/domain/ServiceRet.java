package im.vinci.server.naturelang.domain;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by mlc on 2016/7/27.
 * 服务检测结果返回类
 */
public class ServiceRet {
    private int rc;
    private String service;
    private String operation;
    private JSONObject body;

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public int getRc() {
        return rc;
    }

    public void setRc(int rc) {
        this.rc = rc;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public JSONObject getBody() {
        return body;
    }

    public void setBody(JSONObject body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "ServiceRet{" +
                "rc=" + rc +
                ", service='" + service + '\'' +
                ", operation='" + operation + '\'' +
                ", body=" + body +
                '}';
    }
}
