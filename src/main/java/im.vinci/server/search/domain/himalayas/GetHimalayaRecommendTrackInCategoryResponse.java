package im.vinci.server.search.domain.himalayas;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.io.Serializable;
import java.util.List;

/**
 * 通过关键词搜索喜马拉雅
 * Created by tim@vinci on 15/12/23.
 */
public class GetHimalayaRecommendTrackInCategoryResponse extends HimalayaBaseResponse implements Serializable{

    private int pageSize;

    private int page;

    @JsonProperty("total_count")
    private int totalCount;

    @JsonProperty("category_id")
    private int categoryId;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String tag;

    private List<HimalayaTrack> tracks;

    @JsonGetter("page_size")
    public int getPageSize() {
        return pageSize;
    }

    @JsonSetter("per_page")
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public List<HimalayaTrack> getTracks() {
        return tracks;
    }

    public void setTracks(List<HimalayaTrack> tracks) {
        this.tracks = tracks;
    }
}
