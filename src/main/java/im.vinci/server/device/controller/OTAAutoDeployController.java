package im.vinci.server.device.controller;

import im.vinci.server.common.exceptions.InvalidParameterException;
import im.vinci.server.common.exceptions.error.ErrorCode;
import im.vinci.server.device.domain.OTAHardwareCode;
import im.vinci.server.device.domain.OTARegionCode;
import im.vinci.server.device.domain.RomOTAConfig;
import im.vinci.server.device.domain.wrappers.requests.OTAConfigUser;
import im.vinci.server.device.service.RomOTAConfigService;
import im.vinci.server.device.service.SystemVersionModifyService;
import im.vinci.server.utils.apiresp.Result;
import im.vinci.server.utils.apiresp.ResultObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Created by ytl on 15/12/3.
 */
@Controller
@RequestMapping(value = "/vinci/device/otaconfig/", produces = "application/json;charset=UTF-8")
public class OTAAutoDeployController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private SystemVersionModifyService systemVersionModifyService;
    @Autowired
    private RomOTAConfigService romOTAConfigService;
    @Autowired
    private Environment env;

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(@ModelAttribute OTAConfigUser otaConfigUser) {
        return "views/login";
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String otaConfigLogin(OTAConfigUser otaConfigUser, RedirectAttributes redirectAttributes) throws Exception {
        String userName = otaConfigUser.getUsername();
        String passWord = otaConfigUser.getPassword();
        if (null != userName && userName.trim().equals(env.getProperty("otauser")) && null != passWord && passWord.trim().equals(env.getProperty("password"))) {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            requestAttributes.getRequest().getSession(true).setAttribute("cur_user", otaConfigUser);
            return "redirect:/vinci/device/otaconfig/index";
        } else {
            redirectAttributes.addFlashAttribute("message", "userName or password is wrong");
            redirectAttributes.addFlashAttribute("user", otaConfigUser);
            return "redirect:/vinci/device/otaconfig/login";
        }

    }

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public ModelAndView index(
            @RequestParam(value = "region_code",defaultValue = "CN")OTARegionCode regionCode,
            @RequestParam(value = "hardware_code",defaultValue = "_01")OTAHardwareCode hardwareCode
    ) {
        ModelAndView model = new ModelAndView("views/index");
        model.addObject("region_code", regionCode);
        model.addObject("hardware_code", hardwareCode);
        model.addObject("all_region_code",OTARegionCode.values());
        model.addObject("all_hardware_code",OTAHardwareCode.values());
        return model;
    }

    //更改测试配置(主版本 + version package)
    @RequestMapping(value = "/addOrUpdateOTATestConfig", method = RequestMethod.POST)
    @ResponseBody
    public Result updateOTATestConfig(@RequestBody RomOTAConfig romOTAConfig) throws Exception {
        // 版本号不允许为空
        if (null == romOTAConfig.getSysVersionName() || StringUtils.isEmpty(romOTAConfig.getSysVersionName())) {
            throw new InvalidParameterException(ErrorCode.BAD_REQUEST, "request rom system version name is null", "目标版本号不能为空");
        }
        if (null == romOTAConfig.getRegionCode() || null == romOTAConfig.getHardwareCode()) {
            throw new InvalidParameterException(ErrorCode.BAD_REQUEST, "request region or hardware is null", "地域和硬件代码不能为空");
        }
        romOTAConfigService.addRomOTATestConfig(romOTAConfig.getRegionCode(),romOTAConfig.getHardwareCode(),romOTAConfig);
        return new Result();
    }

    // 删除差分包配置
    @RequestMapping(value = "/delVersionPackage", method = RequestMethod.POST)
    @ResponseBody
    public Result delVersionPackage(@RequestParam Long id) throws Exception {
        systemVersionModifyService.delSystemVersion(id);
        return new Result();
    }

    // 获取测试环境配置
    @RequestMapping(value = "/getOTATestConfig", method = RequestMethod.GET)
    @ResponseBody
    public ResultObject<RomOTAConfig> getOTATestConfig(
            @RequestParam("region_code")OTARegionCode regionCode,
            @RequestParam("hardware_code")OTAHardwareCode hardwareCode
    ) throws Exception {
            RomOTAConfig romOTAConfig = romOTAConfigService.getRomOTATestConfig(regionCode,hardwareCode);
        return new ResultObject<>(romOTAConfig);
    }

    // 测试环境配置转升级环境配置
    @RequestMapping(value = "/testConfigToDeploy", method = RequestMethod.POST)
    @ResponseBody
    public Result testConfigToDeploy(
            @RequestParam("configId") Long configId,
            @RequestParam("region_code")OTARegionCode regionCode,
            @RequestParam("hardware_code")OTAHardwareCode hardwareCode

    ) throws Exception {
        romOTAConfigService.updateConfigFromTestToDeploy(regionCode,hardwareCode,configId);
        return new Result();
    }

    // 获取rom历史配置
    @RequestMapping(value = "/configHistory", method = RequestMethod.GET)
    public ModelAndView getRomOTADeployConfigPage(
            @RequestParam(value = "region_code",defaultValue = "US")OTARegionCode regionCode,
            @RequestParam(value = "hardware_code",defaultValue = "_01")OTAHardwareCode hardwareCode
    ) throws Exception {
        ModelAndView model = new ModelAndView("views/history");
        model.addObject("region_code", regionCode);
        model.addObject("hardware_code", hardwareCode);
        return model;
    }

    // 获取所有版本信息
    @RequestMapping(value = "/getAllConfigs")
    @ResponseBody
    public ResultObject<List<RomOTAConfig>> getAllConfigs(
            @RequestParam("region_code")OTARegionCode regionCode,
            @RequestParam("hardware_code")OTAHardwareCode hardwareCode
    ) throws Exception {
        return new ResultObject<>(romOTAConfigService.getAllRomOTAConfigs(regionCode, hardwareCode));
    }

    @RequestMapping(value = "/getConfigDetail/{id}")
    @ResponseBody
    public ResultObject<RomOTAConfig> getConfigDetail(@PathVariable(value = "id") Long id) throws Exception {
        RomOTAConfig romOTAConfig = romOTAConfigService.getRomOTAConfigById(id);
        return new ResultObject<>(romOTAConfig);
    }

    @RequestMapping(value = "/isForbidden", method = RequestMethod.GET)
    public String isAuthorizedIp() {
        return "views/forbidden";
    }

}
