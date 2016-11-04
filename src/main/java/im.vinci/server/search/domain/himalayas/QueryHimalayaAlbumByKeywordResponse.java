package im.vinci.server.search.domain.himalayas;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.io.Serializable;
import java.util.List;

/**
 * 通过关键词搜索喜马拉雅
 * Created by tim@vinci on 15/12/23.
 */
public class QueryHimalayaAlbumByKeywordResponse extends HimalayaBaseResponse implements Serializable{

    private int pageSize;

    private int page;

    @JsonProperty("total_count")
    private int totalCount;

    private List<HimalayaAlbum> albums;

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

    public List<HimalayaAlbum> getAlbums() {
        return albums;
    }

    public void setAlbums(List<HimalayaAlbum> albums) {
        this.albums = albums;
    }

}
