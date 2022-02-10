package com.istar.mediabroken.openapi

import com.istar.mediabroken.service.CompetitionAnalysService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import static org.springframework.web.bind.annotation.RequestMethod.POST

/**
 * Author: Luda
 * Time: 2017/6/13
 */
@RequestMapping(value = "/openapi/copyRight")
@RestController
class CopyRightOApiController {

    @Autowired
    CompetitionAnalysService competitionAnalysSrv

    @RequestMapping(value = "/monitors", method = POST)
    Map getMonitors(
            @RequestParam("appId") String appId,
            @RequestParam("data") String data,
            @RequestParam(value = "orderType" , required = false, defaultValue = '0') int orderType
    ){

        def result = competitionAnalysSrv.getCopyrightMonitorNews(appId,data,orderType)
        return result;
    }
}
