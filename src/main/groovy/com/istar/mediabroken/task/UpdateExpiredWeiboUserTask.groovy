package com.istar.mediabroken.task

import com.istar.mediabroken.service.weibo.WeiboExpiredUserInfoService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
/**
 * Author: hh
 * Time: 2018/1/4
 * 更新已经过期的微博用户数据
 */
@Component
@Slf4j
class UpdateExpiredWeiboUserTask implements Task {
    @Autowired
    WeiboExpiredUserInfoService expiredUserInfoService

    @Override
    void execute() {
        expiredUserInfoService.updateUserInfo()
        System.exit(-1)
    }
}
