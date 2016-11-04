package im.vinci.server.elasticsearch.impl;

import im.vinci.server.elasticsearch.domain.base.BasicDocModel;
import im.vinci.server.elasticsearch.esconst.Indices;
import im.vinci.server.elasticsearch.index.AccountExtraInfo;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.engine.DocumentMissingException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;

/**
 * Created by zhongzhengkai on 16/2/24.
 * 对应es服务器里 account_extra_info/account_extra_info 下的文档,通过下面链接查看其最新的mapping定义
 * http://101.200.159.42:9200/account_extra_info/account_extra_info/_mapping
 * keys: [sn,imei,mac,rom_version,create_time,agent]
 * 目前通过该文档可以统计日新增用户,用户版本分布数据
 */
@Service
public class AccountExtraInfoAccessor implements AccountExtraInfo {

    @Resource(name = "esClient")
    private Client esClient;

    public IndexResponse save(String id,Object... keyValueArgs){
        return esClient.prepareIndex(Indices.ACCOUNT_EXTRA_INFO.getIndexName(),"account_extra_info",id).setSource(keyValueArgs).execute().actionGet();
    }

    public IndexResponse save(String id, BasicDocModel model){
        return esClient.prepareIndex(Indices.ACCOUNT_EXTRA_INFO.getIndexName(),"account_extra_info",id).setSource(model.getXContentBuilder()).execute().actionGet();
    }

    public UpdateResponse update(String id,BasicDocModel model) throws DocumentMissingException{
        return esClient.prepareUpdate(Indices.ACCOUNT_EXTRA_INFO.getIndexName(),"account_extra_info",id).setDoc(model.getXContentBuilder()).get();
    }

    public UpdateResponse upsert(String id,BasicDocModel model) throws DocumentMissingException,InterruptedException,ExecutionException{
        IndexRequest indexRequest = new IndexRequest(Indices.ACCOUNT_EXTRA_INFO.getIndexName(), "account_extra_info", id)
                .source(model.getXContentBuilder());
        UpdateRequest updateRequest = new UpdateRequest(Indices.ACCOUNT_EXTRA_INFO.getIndexName(), "account_extra_info", id)
                .doc(model.getXContentBuilder()).upsert(indexRequest);
        return esClient.update(updateRequest).get();
    }
}
