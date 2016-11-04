package im.vinci.server.utils.apiresp;

import java.io.Serializable;

/**
 * Created by henryhome on 2/27/15.
 */
public class Result implements Serializable{

    protected Integer status;
//    protected Map<String, String> extendedProperties;
//    protected String callback;

    public Result() {
        this(200);
    }

    public Result(Integer status) {
        this.status = status;
    }

//    public Map<String, String> getExtendedProperties() {
//        return extendedProperties;
//    }
//
//    public void setExtendedProperties(Map<String, String> extendedProperties) {
//        this.extendedProperties = extendedProperties;
//    }
//
//    public String getCallback() {
//        return callback;
//    }
//
//    public void setCallback(String callback) {
//        this.callback = callback;
//    }

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
}




