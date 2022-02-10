package com.istar.mediabroken.task

import com.istar.mediabroken.service.CaptureService
import com.istar.mediabroken.service.capture.SiteService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@Slf4j
class AutoNewsPushTask implements Task {
    @Autowired
    SiteService siteService

    @Override
    void execute() {
        siteService.siteAutoPush()
    }
}
