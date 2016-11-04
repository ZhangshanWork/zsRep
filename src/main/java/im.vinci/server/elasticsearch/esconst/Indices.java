package im.vinci.server.elasticsearch.esconst;

/**
 * Created by zhongzhengkai on 16/1/14.
 */
public enum Indices {

    XIAMI_MUSIC("xiami_music"), USER_LOGS("user_logs"), ACCOUNT_EXTRA_INFO("account_extra_info");

    private Indices(String indexName) {
        this.indexName = indexName;
    }

    private String indexName;

    public String getIndexName() {
        return this.indexName;
    }
}
