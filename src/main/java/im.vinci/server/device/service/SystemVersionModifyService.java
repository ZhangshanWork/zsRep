package im.vinci.server.device.service;

import im.vinci.server.device.domain.SystemVersion;
import im.vinci.server.device.persistence.SystemVersionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by ytl on 15/12/4.
 */
@Service
public class SystemVersionModifyService  {

    @Autowired
    private SystemVersionMapper systemVersionMapper;

    public Integer addSystemVersion(SystemVersion systemVersion) {
        return systemVersionMapper.addSystemVersion(systemVersion);
    }

    public void delSystemVersion(Long id) {
        systemVersionMapper.delSystemVersionById(id);
    }
}
