package com.istar.mediabroken.task

import com.istar.mediabroken.service.CaptureService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Author: Luda
 * Time: 2017/5/25
 */
@Component
@Slf4j
class FocusCaptureTask implements Task {
    @Autowired
    CaptureService captureSrv

    @Override
    void execute() {
        captureSrv.updateFocusCaptureCount()
    }
}
