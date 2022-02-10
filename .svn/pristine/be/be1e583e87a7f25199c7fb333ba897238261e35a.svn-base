package com.istar.mediabroken.task

import com.istar.mediabroken.service.capture.InstantNewsService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * @author  zc
 * @date  2018/5/10
 * @desc  删除标记新闻任务 每天零点  删除二十四小时之前的标记的新闻
 **/
@Component
@Slf4j
class DeleteInstantNewsTask implements Task{
    @Autowired
    InstantNewsService instantNewsService
    @Override
    void execute() {
        instantNewsService.deleteInstantNewsOver24Hours()
    }
}
