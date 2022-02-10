package com.istar.mediabroken.task

import com.istar.mediabroken.service.monitor.NewsMonitorService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * @author  hanhui
 * @date  2018/4/24 16:34
 * @desc  基础站点数据监控任务
 **/
@Component
@Slf4j
class NewsMonitorTask implements Task {
    @Autowired
    NewsMonitorService newsMonitorService

    @Override
    void execute() {
        newsMonitorService.newsMonitor()
    }
}
