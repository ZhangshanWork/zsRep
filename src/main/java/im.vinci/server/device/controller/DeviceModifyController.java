package im.vinci.server.device.controller;

import im.vinci.server.device.domain.Device;
import im.vinci.server.device.domain.wrappers.requests.OTAInfo;
import im.vinci.server.device.domain.wrappers.responses.DeviceUpdateInfo;
import im.vinci.server.device.service.DeviceModifyService;
import im.vinci.server.utils.apiresp.Result;
import im.vinci.server.utils.apiresp.ResultObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by henryhome on 9/14/15.
 */
@RestController
@RequestMapping(value = "/vinci/device", produces = "application/json;charset=UTF-8")
public class DeviceModifyController {

    @Autowired
    private DeviceModifyService deviceModifyService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping(value = "/ota_update", method = RequestMethod.POST)
    public ResultObject<DeviceUpdateInfo> updateViaOTA(@RequestBody OTAInfo otaInfo)
        throws Exception {

        DeviceUpdateInfo updateInfo = deviceModifyService.updateViaOTA(otaInfo);

        return new ResultObject<>(updateInfo);
    }



    @RequestMapping(method = RequestMethod.POST)
    public ResultObject<Integer> addDevice(@RequestBody Device device) throws Exception {
        Integer device_id = deviceModifyService.addDevice(device);

        return new ResultObject<>(device_id);
    }

    @RequestMapping(value = "/{device_id}", method = RequestMethod.PUT)
    public Result updateDevice(@PathVariable("device_id") Integer deviceId,
                               @RequestBody Device device) throws Exception {
        device.setId(deviceId);
        deviceModifyService.updateDevice(device);

        return new Result();
    }

    @RequestMapping(value = "/{device_id}", method = RequestMethod.DELETE)
    public Result deleteDevice(@PathVariable("device_id") Integer deviceId) throws Exception {

        deviceModifyService.deleteDevice(deviceId);

        return new Result();
    }

}
