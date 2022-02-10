package com.istar.mediabroken.task

import com.istar.mediabroken.service.compile.ReportService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Author: Luda
 * Time: 2017/8/15
 * 定时查看report
 */
@Component
@Slf4j
class ReportStatusTask implements Task {
    @Autowired
    ReportService reportService

    @Override
    void execute() {
        reportService.syncReportJobStatus()
        System.exit(-1)
    }
}
