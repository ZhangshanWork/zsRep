package im.vinci.server.search.domain.himalayas;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.io.Serializable;

/**
 * 获取专辑detail和其中的文件
 * Created by tim@vinci on 15/12/23.
 */
public class GetHimalayaAlbumDetailResponse extends HimalayaBaseResponse implements Serializable{

    private int pageSize;

    private int page;

    @JsonProperty("total_count")
    private int totalCount;

    private HimalayaAlbum album;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    @JsonGetter("page_size")
    public int getPageSize() {
        return pageSize;
    }

    @JsonSetter("per_page")
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public HimalayaAlbum getAlbum() {
        return album;
    }

    public void setAlbum(HimalayaAlbum album) {
        this.album = album;
    }
}
