package im.vinci.server.statistic.controller;

import im.vinci.server.statistic.service.UserLogAutoStatisticService;
import im.vinci.server.utils.apiresp.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by tim@vinci on 16/4/18.
 */
@Controller
public class StatsApiController {

    @Autowired
    private UserLogAutoStatisticService userLogAutoStatisticService;

    @RequestMapping("/api/stats/callStatsDaily")
    @ResponseBody
    public Result callStatsDaily() {
        userLogAutoStatisticService.dailyStat();
        return new Result();
    }
}
