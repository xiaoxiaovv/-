package com.istar.mediabroken.task

import com.istar.mediabroken.service.statistics.DataStatisticsService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@Slf4j
class TestTask implements Task {
    @Autowired
    DataStatisticsService dataStatisticsService
    @Override
    void execute() {
        def hours = dataStatisticsService.addDataStatistics()
    }
}
