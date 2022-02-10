package com.istar.mediabroken.task

import com.istar.mediabroken.service.CaptureService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Author: Luda
 * Time: 2017/5/24
 * 统计新闻的抓取数和推送以及分享数
 */
@Component
@Slf4j
class CaptureStatTask implements Task {
    @Autowired
    CaptureService captureSrv

    @Override
    void execute() {
        captureSrv.captureStatCount();
    }
}
