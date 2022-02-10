package com.istar.mediabroken.task

import com.istar.mediabroken.service.statistics.RetainedDataStatisticsService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * @author hanhui
 * @date 2018/4/8 10:57
 * @desc 用户留存数据统计
 * */
@Component
@Slf4j
class RetainedDataStatisticsTask implements Task {
    @Autowired
    RetainedDataStatisticsService retainedDataStatisticsService

    @Override
    void execute() {
        retainedDataStatisticsService.addRetainedDataStatistics()
    }
}
