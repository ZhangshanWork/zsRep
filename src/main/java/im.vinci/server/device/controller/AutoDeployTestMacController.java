package im.vinci.server.device.controller;

import im.vinci.server.device.domain.OTATestMac;
import im.vinci.server.device.service.OTATestMacService;
import im.vinci.server.utils.apiresp.Result;
import im.vinci.server.utils.apiresp.ResultObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by ytl on 15/12/7.
 */
@Controller
@RequestMapping(value = "/vinci/device/otaconfig/", produces = "application/json;charset=UTF-8")
public class AutoDeployTestMacController {

    @Autowired
    private OTATestMacService otaTestMacService;

    @RequestMapping(value = "/addMac", method = RequestMethod.GET)
    public String addTestMac() {
        return "views/addmac";
    }

    // 添加测试机Mac地址
    @RequestMapping(value = "/addMac", method = RequestMethod.POST)
    @ResponseBody
    public Result addOTATestMac(@RequestParam("macAddr") String macAddr) throws Exception {
        OTATestMac otaTestMac = new OTATestMac();
        otaTestMac.setMac(macAddr);
        otaTestMacService.addOtaTestMac(otaTestMac);
        return new Result();
    }

    // 删除测试机Mac
    @RequestMapping(value = "/delMac", method = RequestMethod.POST)
    @ResponseBody
    public Result delTestMac(@RequestParam("macId") Long macId) throws Exception {
        otaTestMacService.deleteOtaTestMac(macId);
        return new Result();
    }

    // 更改测试机Mac
    @RequestMapping(value = "/updateMac", method = RequestMethod.POST)
    @ResponseBody
    public Result updateTestMac(@RequestBody OTATestMac otaTestMac) throws Exception {
        otaTestMacService.updateOtaTestMac(otaTestMac);
        return new Result();
    }

    // 获取测试机Mac列表
    @RequestMapping(value = "/getTestMacs", method = RequestMethod.GET)
    @ResponseBody
    public ResultObject<List<OTATestMac>> getTestMacs() throws Exception {
        return new ResultObject<>(otaTestMacService.getOtaTestMacs());
    }

}
