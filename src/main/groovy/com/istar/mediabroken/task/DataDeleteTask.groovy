package com.istar.mediabroken.task

import com.istar.mediabroken.service.statistics.DataDeleteService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * @author  hanhui
 * @date  2018/4/24 16:34
 * @desc  删除newsoperation表中的过期数据并备份
 **/
@Component
@Slf4j
class DataDeleteTask implements Task {
    @Autowired
    DataDeleteService dataDeleteService
    @Value('${task.delete.day}')
    int MAX_DAY

    @Override
    void execute() {
        dataDeleteService.dataDelete(MAX_DAY)
    }
}
