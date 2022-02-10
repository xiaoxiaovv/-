package com.istar.mediabroken.task

import com.istar.mediabroken.service.CaptureService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * 扫描数据库,给没有创建对应主题的站点创建主题
 */
@Component
@Slf4j
class AddSubjectTask implements Task {
    @Autowired
    CaptureService captureSrv

    @Override
    void execute() {
        captureSrv.addSubjectForSite()
    }
}
