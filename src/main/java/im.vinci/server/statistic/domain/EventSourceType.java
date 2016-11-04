package im.vinci.server.statistic.domain;

/**
 * Created by zhongzhengkai on 15/12/12.
 */
public enum EventSourceType {

    SEARCH("search"),
    RECOMMEND("recommend"),
    MY_FAVORITE("myfavorite"),
    UPLOAD("upload"),
    OTHER("other");

    private String type;

    private EventSourceType(String type) {
        this.type = type;
    }

    public String value() {
        return this.type;
    }
}
