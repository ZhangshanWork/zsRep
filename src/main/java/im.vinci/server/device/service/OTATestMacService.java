package im.vinci.server.device.service;

import im.vinci.server.device.domain.OTATestMac;
import im.vinci.server.device.persistence.OTATestMacMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by ytl on 15/12/7.
 */
@Service
public class OTATestMacService {

    @Autowired
    private OTATestMacMapper otaTestMacMapper;

    public List<OTATestMac> getOtaTestMacs() {
        return otaTestMacMapper.getOtaTestMacs();
    }

    public Long getOtaTestMacCount(String mac) {
        return otaTestMacMapper.getOtaTestMacByMac(mac);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false, rollbackFor = Exception.class)
    public void addOtaTestMac(OTATestMac otaTestMac) throws Exception {
        otaTestMacMapper.addOtaTestMac(otaTestMac);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false, rollbackFor = Exception.class)
    public void updateOtaTestMac(OTATestMac otaTestMac) throws Exception {
        otaTestMacMapper.updateOtaTestMac(otaTestMac);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false, rollbackFor = Exception.class)
    public void deleteOtaTestMac(Long id) throws Exception {
        otaTestMacMapper.deleteOtaTestMac(id);
    }
}
