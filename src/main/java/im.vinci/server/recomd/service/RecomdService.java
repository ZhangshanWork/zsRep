package im.vinci.server.recomd.service;

import com.taobao.api.ApiException;
import im.vinci.server.recomd.domain.RecomdInput;

import java.util.List;

/**
 * Created by mlc on 2016/9/21.
 */
public interface RecomdService {
    public List<String> getRecomdList(RecomdInput recomdInput) throws ApiException;

    public List<String> getCollectionListStr(String deviceId);

    public List<String> getTopCollections(int size);

    public List<String> getSimilarMusic(String id) throws ApiException;
}
