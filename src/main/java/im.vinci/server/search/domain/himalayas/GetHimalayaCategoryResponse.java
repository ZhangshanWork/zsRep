package im.vinci.server.search.domain.himalayas;

import java.io.Serializable;
import java.util.List;

/**
 * Created by tim@vinci on 15/12/23.
 */
public class GetHimalayaCategoryResponse extends HimalayaBaseResponse implements Serializable{
    private List<HimalayaCategory> categories;

    public List<HimalayaCategory> getCategories() {
        return categories;
    }

    public void setCategories(List<HimalayaCategory> categories) {
        this.categories = categories;
    }
}
