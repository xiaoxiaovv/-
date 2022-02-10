package com.istar.mediabroken.task

import com.istar.mediabroken.service.CaptureService
import com.istar.mediabroken.service.system.MessageService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * 扫描用户表，将过期用户信息提示到消息列表
 */
@Component
@Slf4j
class AddMessageTask implements Task {
    @Autowired
    MessageService messageService

    @Override
    void execute() {
        messageService.addMessageForAccount()
    }
}
