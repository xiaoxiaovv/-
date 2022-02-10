package com.istar.mediabroken.api

import com.istar.mediabroken.service.MonitorService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody

import static com.istar.mediabroken.api.ApiResult.apiResult

@Controller
@Slf4j
public class MonitorApiController {
    @Autowired
    MonitorService monitorSrv

    @RequestMapping(value = "/monitor/subjectStatus", method = RequestMethod.PUT)
    @ResponseBody
    public Map getSubjectStatus() {
        monitorSrv.createSubjectStatus()
        return apiResult()
    }

    @RequestMapping(value = "/monitor/subjectStatus", method = RequestMethod.GET)
    @ResponseBody
    public Map modifySubjectStatus() {
        def rep = monitorSrv.getSubjectStatus()
        return apiResult(rep)
    }

}
