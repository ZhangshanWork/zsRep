package im.vinci.server.naturelang.wrapper;

import com.google.common.base.MoreObjects;

import java.io.Serializable;
import java.util.Map;

/**
 * 结果应答
 * Created by mlc on 2016/7/26.
 */
public class ResponseResult implements Serializable{
    private String session_id;
    private String session_val;
    private String query;     //原始query
    private String speech;    //播报文本
    private String asked;     //服务问答，asked和speech不能同时存在
    private String service;   //响应服务
    private String operation;  //操作类型
    private Map<String,Object> semantic;  //语义理解信息
    private Object data;      //应答结果

    public String getAsked() {
        return asked;
    }

    public void setAsked(String asked) {
        this.asked = asked;
    }

    public String getSession_id() {
        return session_id;
    }

    public void setSession_id(String session_id) {
        this.session_id = session_id;
    }

    public String getSession_val() {
        return session_val;
    }

    public void setSession_val(String session_val) {
        this.session_val = session_val;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getSpeech() {
        return speech;
    }

    public void setSpeech(String speech) {
        this.speech = speech;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public Map<String, Object> getSemantic() {
        return semantic;
    }

    public void setSemantic(Map<String, Object> semantic) {
        this.semantic = semantic;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("session_id", session_id)
                .add("session_val", session_val)
                .add("query", query)
                .add("speech", speech)
                .add("asked", asked)
                .add("service", service)
                .add("operation", operation)
                .add("semantic", semantic)
                .add("data", data)
                .toString();
    }
}
