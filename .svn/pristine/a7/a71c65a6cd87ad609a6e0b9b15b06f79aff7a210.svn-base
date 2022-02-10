package com.istar.mediabroken.task

import com.istar.mediabroken.service.CompetitionAnalysService
import com.istar.mediabroken.service.copyright.MonitorService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@Slf4j
class CopyrightMonitorTask implements Task {
    @Autowired
    MonitorService monitorService

    @Override
    void execute() {
        monitorService.autoCopyrightMonitor()
        System.exit(-1)
    }
}
