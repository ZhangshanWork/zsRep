package im.vinci.server.elasticsearch.index;

import im.vinci.server.elasticsearch.domain.base.BasicDocModel;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.index.engine.DocumentMissingException;

import java.util.concurrent.ExecutionException;

/**
 * Created by zhongzhengkai on 16/2/24.
 */
public interface AccountExtraInfo {

    public IndexResponse save(String id, BasicDocModel model);

    /**
     * @param id 指的是用户的imei码(0.9.0版本及其之前的头机)
     * @param keyValueArgs
     * @return
     */
    public IndexResponse save(String id, Object... keyValueArgs);

    public UpdateResponse update(String id, BasicDocModel model);

    public UpdateResponse upsert(String id, BasicDocModel model) throws DocumentMissingException,InterruptedException,ExecutionException;

}
