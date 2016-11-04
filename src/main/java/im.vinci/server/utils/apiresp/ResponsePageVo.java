package im.vinci.server.utils.apiresp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import java.io.Serializable;
import java.util.List;

/**
 * Created by tim@vinci on 15/11/27.
 * 用于翻页的返回结果, 在这里有两种表达方式,has more和当前page,哪种填了选哪种
 */
public class ResponsePageVo<T extends Serializable> implements Serializable{

    @JsonProperty("has_more")
    private Boolean hasMore;

    private Integer page;

    @JsonProperty("page_size")
    private int pageSize;

    @JsonProperty("total_number")
    private Integer totalCount;

    private List<T> data;

    public Boolean getHasMore() {
        return hasMore;
    }

    public ResponsePageVo<T> setHasMore(Boolean hasMore) {
        this.hasMore = hasMore;
        return this;
    }

    public Integer getPage() {
        return page;
    }

    public ResponsePageVo<T> setPage(int page) {
        this.page = page;
        return this;
    }

    public int getPageSize() {
        return pageSize;
    }

    public ResponsePageVo<T> setPageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public ResponsePageVo<T> setTotalCount(int totalCount) {
        this.totalCount = totalCount;
        return this;
    }

    public List<T> getData() {
        return data;
    }

    public ResponsePageVo<T> setData(List<T> data) {
        this.data = data;
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("hasMore", hasMore)
                .add("page", page)
                .add("pageSize", pageSize)
                .add("totalCount", totalCount)
                .add("data", data)
                .toString();
    }
}
