package im.vinci.server.search.domain.himalayas;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * 获取喜马拉雅category下的tag
 * Created by tim@vinci on 15/12/23.
 */
public class GetHimalayaCategoryTagsResponse extends HimalayaBaseResponse implements Serializable{
    @JsonProperty("category_id")
    private int categoryId;

    private List<HimalayaCategoryTag> tags;

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public List<HimalayaCategoryTag> getTags() {
        return tags;
    }

    public void setTags(List<HimalayaCategoryTag> tags) {
        this.tags = tags;
    }
}
