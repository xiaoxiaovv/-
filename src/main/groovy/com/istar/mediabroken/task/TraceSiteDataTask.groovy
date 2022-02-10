package com.istar.mediabroken.task

import com.istar.mediabroken.service.capture.SiteDistinctService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * @author zxj
 * @create 2019/1/3
 * @desc 追溯用户添加微博数据并且添加到星光的微博主题里面(10分钟每次)
 */
@Component
@Slf4j
class TraceSiteDataTask implements Task{

    @Autowired
    SiteDistinctService siteDistinctService
    @Override
    void execute() {
        siteDistinctService.traceNewsForWeiboTask()
    }
}
