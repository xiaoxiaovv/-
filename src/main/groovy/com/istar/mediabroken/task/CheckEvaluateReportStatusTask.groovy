package com.istar.mediabroken.task

import com.istar.mediabroken.service.evaluate.EvaluateReportService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * @author zxj
 * @create 2018/6/26
 * @desc 检查报表数据状态 如果有数据了更新到表中(每半小时一次)
 */
@Component
@Slf4j
class CheckEvaluateReportStatusTask implements Task {
    @Autowired
    EvaluateReportService evaluateReportService

    @Override
    void execute() {
        evaluateReportService.checkEvaluateReportStatus()

    }
}
