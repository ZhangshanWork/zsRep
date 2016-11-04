package im.vinci.server.naturelang.domain;

/**
 *  请求参数类
 *  Created by mlc on 2016/7/26.
 */

public class Parameter {
    private String query;
    private Location location;
    private String timezone;
    private String lang;
    private String session_id;
    private String session_val;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
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
}
