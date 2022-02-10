package com.istar.mediabroken.task

import com.istar.mediabroken.service.alioss.OssMaterialService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * @description: 阿里云oss视频转码更新任务
 * @author: hexushuai
 * @date: 2019/1/23 15:06
 */
@Component
@Slf4j
class AliOssTransCodeTask implements Task {

    @Autowired
    private OssMaterialService materialService

    @Override
    void execute() {
        while (true) {
            long startTime = System.currentTimeMillis()
            materialService.updateTransCode()
            long endTime = System.currentTimeMillis()
            log.info("oss trans run time:{}ms", (endTime - startTime))
            Thread.sleep(5000)
        }

    }
}
