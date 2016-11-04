package im.vinci.server.utils.apiresp;

import java.util.List;

/**
 * Created by henryhome on 5/10/16.
 */
public class RecommendResultList<T> extends ResultList<T> {

    private List<List<String>> tagList;

    public RecommendResultList() {
        super();
    }

    public RecommendResultList(List<T> contentList, List<List<String>> tagList) {
        super(contentList);
        this.tagList = tagList;

    }

    public RecommendResultList(Integer status, List<T> contentList, List<List<String>> tagList) {
        super(status, contentList);
        this.tagList = tagList;
    }

    public List<List<String>> getTagList() {
        return tagList;
    }

    public void setTagList(List<List<String>> tagList) {
        this.tagList = tagList;
    }
}
