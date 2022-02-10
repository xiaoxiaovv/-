package com.istar.mediabroken.task

import com.istar.mediabroken.Const
import com.istar.mediabroken.service.CaptureService
import com.istar.mediabroken.service.CompetitionAnalysService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * Created by luda on 2017/5/11.
 */
@Component
@Slf4j
class LocalAutoTask {
    @Autowired
    CaptureService captureSrv
    @Autowired
    CompetitionAnalysService competitionAnalysSrv
    @Value('${env}')
    String env

//    @Scheduled(cron = "1 0/1 * * * ?")
    public void show(){
        System.out.println("Annotation：is show run" + (new Date()));
    }

//    @Scheduled(cron = "1 0/5 * * * ?")
    public void autoNewsPushTask(){
        if (Const.ENV_ONLINE == env || Const.ENV_TEST == env) {
            log.debug('{}', "AutoNewsPushTask start" + (new Date()))
            captureSrv.siteAutoPush()
            log.debug('{}', "AutoNewsPushTask end"  + (new Date()))
        }
    }

//    @Scheduled(cron = "1 0/5 * * * ?")
    public void copyrightMonitorTask(){
        if (Const.ENV_ONLINE == env || Const.ENV_TEST == env) {
            log.debug('{}', "CopyrightMonitorTask start" + (new Date()))
            competitionAnalysSrv.autoCopyrightMonitor()
            log.debug('{}', "CopyrightMonitorTask end" + (new Date()))
        }
    }

//    @Scheduled(cron = "1 0/5 * * * ?")
    public void captureStatTask(){
        if (Const.ENV_ONLINE == env || Const.ENV_TEST == env) {
            log.debug('{}', "captureStatTask start" + (new Date()))
            captureSrv.captureStatCount()
            log.debug('{}', "captureStatTask end" + (new Date()))
        }
    }

//    @Scheduled(cron = "1 0/5 * * * ?")
    public void focusCaptureTask(){
        if (Const.ENV_ONLINE == env || Const.ENV_TEST == env) {
            log.debug('{}', "captureStatTask start" + (new Date()))
            captureSrv.updateFocusCaptureCount()
            log.debug('{}', "captureStatTask end" + (new Date()))
        }
    }


}
