package im.vinci.server.device.service;

import im.vinci.server.common.exceptions.VinciException;
import im.vinci.server.common.exceptions.error.ErrorCode;
import im.vinci.server.config.RomOTAConfigConstants;
import im.vinci.server.device.domain.OTAHardwareCode;
import im.vinci.server.device.domain.OTARegionCode;
import im.vinci.server.device.domain.RomOTAConfig;
import im.vinci.server.device.domain.SystemVersion;
import im.vinci.server.device.persistence.RomOTAConfigMapper;
import im.vinci.server.device.persistence.SystemVersionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ytl on 15/12/3.
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class RomOTAConfigService {

    private static final Integer OTA_CONFIG_DEL    = 0;//删除
    private static final Integer OTA_TEST_CONFIG   = 1;//测试
    private static final Integer OTA_TEST_BEFORE   = 2;//测试过期
    private static final Integer OTA_DEPLOY_CONFIG = 3;//上线
    private static final Integer OTA_DEPLOY_BEFORE = 4;//上线过期

    @Autowired
    private SystemVersionMapper systemVersionMapper;
    @Autowired
    private RomOTAConfigMapper romOTAConfigMapper;


    // 添加测试配置
    // 当有新的测试配置时把原有测试配置设置为过期
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false, rollbackFor = Exception.class)
    
    public Long addRomOTATestConfig(OTARegionCode regionCode, OTAHardwareCode hardwareCode, RomOTAConfig romOTAConfig) {
        if (regionCode == null || hardwareCode == null) {
            throw new VinciException(ErrorCode.INVALID_OTA_UPDATE_REGION_OR_HARDWARE_CODE,"缺少region或hardware参数","没有地域或者硬件版本号参数");
        }
        romOTAConfig.setRegionCode(regionCode);
        romOTAConfig.setHardwareCode(hardwareCode);
        romOTAConfigMapper.updateRomOTAConfigStatus(regionCode,hardwareCode, RomOTAConfigConstants.OTA_TEST_CONFIG, RomOTAConfigConstants.OTA_TEST_EXPIRED);
        romOTAConfig.setStatus(RomOTAConfigConstants.OTA_TEST_CONFIG);
        Long insertRow = romOTAConfigMapper.addRomOTAConfig(romOTAConfig);
        addVersionPackages(romOTAConfig);
        return insertRow;
    }


    // 测试转正式发布
    // 原有发布配置过期,从测试配置拷贝插入为正式发布配置
    // 保留测试配置状态,当有新的测试配置时把原有测试配置设置为过期
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false, rollbackFor = Exception.class)
    public void updateConfigFromTestToDeploy(OTARegionCode regionCode, OTAHardwareCode hardwareCode, Long testConfigId) {
        RomOTAConfig romOTAConfig = romOTAConfigMapper.findRomOTAConfigById(testConfigId);
        if (romOTAConfig != null) {
            if (romOTAConfig.getHardwareCode() != hardwareCode || romOTAConfig.getRegionCode() != regionCode) {
                throw new VinciException(ErrorCode.UNPAIR_REGION_OR_HARDWARE_CODE,"configId("+testConfigId+") is not match :"+regionCode+","+hardwareCode,"传入的地域和硬件版本和要发布的配置不一致");
            }
            romOTAConfigMapper.updateRomOTAConfigStatus(regionCode,hardwareCode,RomOTAConfigConstants.OTA_DEPLOY_CONFIG, RomOTAConfigConstants.OTA_DEPLOY_EXPIRED);
            romOTAConfig.setStatus(RomOTAConfigConstants.OTA_DEPLOY_CONFIG);
            Map<String, SystemVersion> systemVersionMap = getVersionPackagesMap(romOTAConfig.getId());
            romOTAConfig.setVersionPackage(systemVersionMap);
            romOTAConfigMapper.addRomOTAConfig(romOTAConfig);
            addVersionPackages(romOTAConfig);
        }
    }

    private void addVersionPackages(RomOTAConfig romOTAConfig) {
        if (romOTAConfig != null && romOTAConfig.getVersionPackage() != null) {
            for(Map.Entry<String, SystemVersion> versionPackage : romOTAConfig.getVersionPackage().entrySet()) {
                SystemVersion systemVersion = versionPackage.getValue();
                systemVersion.setVersionName(versionPackage.getKey());
                systemVersion.setConfigId(romOTAConfig.getId());
                systemVersionMapper.addSystemVersion(systemVersion);
            }
        }
    }

    private Map<String, SystemVersion> getVersionPackagesMap(Long configId) {
        List<SystemVersion> systemVersionList = systemVersionMapper.getVersionPackagesByConfigId(configId);
        Map<String, SystemVersion> systemVersionMap = new HashMap<String, SystemVersion>();
        for(SystemVersion systemVersion : systemVersionList) {
            systemVersionMap.put(systemVersion.getVersionName(), systemVersion);
        }
        return systemVersionMap;
    }



    public RomOTAConfig getRomOTATestConfig(OTARegionCode regionCode, OTAHardwareCode hardwareCode) throws Exception {
        if (regionCode == null || hardwareCode == null) {
            throw new VinciException(ErrorCode.INVALID_OTA_UPDATE_REGION_OR_HARDWARE_CODE,"缺少region或hardware参数","没有地域或者硬件版本号参数");
        }

        RomOTAConfig romOTAConfig = romOTAConfigMapper.getRomOTAConfigByStatus(regionCode,hardwareCode,OTA_TEST_CONFIG);
        if (romOTAConfig != null) {
            Map<String, SystemVersion> versionPackages = getVersionPackagesMap(romOTAConfig);
            romOTAConfig.setVersionPackage(versionPackages);
        }
        return romOTAConfig;
    }

    public RomOTAConfig getRomOTADeployConfig(OTARegionCode regionCode, OTAHardwareCode hardwareCode) throws Exception {
        if (regionCode == null || hardwareCode == null) {
            throw new VinciException(ErrorCode.INVALID_OTA_UPDATE_REGION_OR_HARDWARE_CODE,"缺少region或hardware参数","没有地域或者硬件版本号参数");
        }

        RomOTAConfig romOTAConfig = romOTAConfigMapper.getRomOTAConfigByStatus(regionCode,hardwareCode,OTA_DEPLOY_CONFIG);
        if (romOTAConfig != null) {
            Map<String, SystemVersion> versionPackages = getVersionPackagesMap(romOTAConfig);
            romOTAConfig.setVersionPackage(versionPackages);
        }
        return romOTAConfig;
    }


    public List<RomOTAConfig> getAllRomOTAConfigs(OTARegionCode regionCode, OTAHardwareCode hardwareCode) throws Exception {
        if (regionCode == null || hardwareCode == null) {
            throw new VinciException(ErrorCode.INVALID_OTA_UPDATE_REGION_OR_HARDWARE_CODE,"缺少region或hardware参数","没有地域或者硬件版本号参数");
        }

        return romOTAConfigMapper.getAllConfigs(regionCode,hardwareCode);
    }

    public RomOTAConfig getRomOTAConfigById(Long id) throws Exception {
        RomOTAConfig romOTAConfig = romOTAConfigMapper.findRomOTAConfigById(id);
        if (romOTAConfig != null) {
            Map<String, SystemVersion> versionPackages = getVersionPackagesMap(romOTAConfig);
            romOTAConfig.setVersionPackage(versionPackages);
        }
        return romOTAConfig;
    }

    private Map<String, SystemVersion> getVersionPackagesMap(RomOTAConfig romOTAConfig) {
        Map<String, SystemVersion> versionPackages = new HashMap<String, SystemVersion>();
        if (romOTAConfig != null && romOTAConfig.getId() != null) {
            List<SystemVersion> systemVersions = systemVersionMapper.getVersionPackagesByConfigId(romOTAConfig.getId());
            for(SystemVersion systemVersion : systemVersions) {
                versionPackages.put(systemVersion.getVersionName(), systemVersion);
            }
        }
        return versionPackages;
    }
}
