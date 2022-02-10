package com.istar.mediabroken.task

import com.istar.mediabroken.service.statistics.DataStatisticsService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
/**
 * @author  hanhui
 * @date  2018/4/24 16:35
 * @desc  用户及机构推送、同步数据统计任务
 **/
@Component
@Slf4j
class DataStatisticsTask implements Task {
    @Autowired
    DataStatisticsService dataStatisticsService

    @Override
    void execute() {
        dataStatisticsService.addDataStatistics()
    }
}
