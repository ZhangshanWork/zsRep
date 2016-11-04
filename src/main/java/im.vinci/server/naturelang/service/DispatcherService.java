package im.vinci.server.naturelang.service;

import im.vinci.server.naturelang.domain.ServiceRet;

/**
 * Created by mlc on 2016/7/27.
 */
public interface DispatcherService {
    ServiceRet dispatch(String query) throws Exception;
}
