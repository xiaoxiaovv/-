package com.istar.mediabroken.task

import com.istar.mediabroken.service.evaluate.EvaluateReportService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * @author zxj
 * @create 2018/7/2
 * @desc 自动生成 evaluateReport  一周一次(暂定周一凌晨1点)
 */
@Component
@Slf4j
class AddEvaluateReportTask implements Task{

    @Autowired
    EvaluateReportService evaluateReportService
    @Override
    void execute() {
        evaluateReportService.autoAddEvaluateReport()
    }
}
