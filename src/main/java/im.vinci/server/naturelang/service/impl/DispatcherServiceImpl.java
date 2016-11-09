package im.vinci.server.naturelang.service.impl;

import im.vinci.server.naturelang.domain.ServiceRet;
import im.vinci.server.naturelang.listener.Context;
import im.vinci.server.naturelang.service.DispatcherService;
import im.vinci.server.naturelang.service.decision.ClockDecision;
import im.vinci.server.naturelang.service.decision.PmDecision;
import im.vinci.server.naturelang.service.decision.RecordDecision;
import im.vinci.server.naturelang.service.decision.WeatherDecision;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by mlc on 2016/7/27.
 */
@Service
public class DispatcherServiceImpl implements DispatcherService {
    @Autowired
    private XunFeiSearchService xunFeiSearchService;

    @Override
    public ServiceRet dispatch(String query) throws Exception {
        ServiceRet serviceRet = new ServiceRet();
        if (StringUtils.isBlank(query)) {   //若查询条件为空，则直接输出 null
            return null;
        }

        if(Context.IfMachineInstruct(query)){
            serviceRet.setService("machine");
            serviceRet.setOperation("instruct");
            serviceRet.setRc(0);
            return serviceRet;
        }

        //录音服务判定
        serviceRet = new RecordDecision().record_check(query);
        if(StringUtils.isNotBlank(serviceRet.getService())){
            return serviceRet;
        }
        //空气质量服务判定
        serviceRet = new PmDecision().pm_check(query);
        if(StringUtils.isNotBlank(serviceRet.getService())){
            return serviceRet;
        }
        //天气服务判定
        serviceRet = new WeatherDecision().weather_check(query);
        if(StringUtils.isNotBlank(serviceRet.getService())){
            return serviceRet;
        }
        //录音服务判定
        serviceRet = new ClockDecision().service_check(query);
        if(StringUtils.isNotBlank(serviceRet.getService())){
            return serviceRet;
        }
        //音乐判定
        serviceRet = ifMedia(query);
        if(StringUtils.isNotBlank(serviceRet.getService())){
            return serviceRet;
        }

        serviceRet = xunFeiSearchService.getXunFeiResultV2(query);

       /* serviceRet = Weather_check.weather_check(query);*/
        if(StringUtils.isNotBlank(serviceRet.getService())){
            return serviceRet;
        }

        return new ServiceRet();
    }

    //判定是否为音乐意向
    public ServiceRet ifMedia(String query) throws Exception {
        ServiceRet serviceRet = new ServiceRet();
        int flag = Context.ifMusicInstruct(query);
        if (flag == 1 || flag ==2 || Context.IfSinger(query)) {
            serviceRet.setRc(0);
            if (flag == 1 || Context.IfSinger(query)) {
                serviceRet.setService("music");
				serviceRet.setOperation("play");
            } else if (flag == 2) {
                serviceRet.setService("music_download");
            }
        }else {
            serviceRet.setRc(4);
        }
        return serviceRet;
    }



}
