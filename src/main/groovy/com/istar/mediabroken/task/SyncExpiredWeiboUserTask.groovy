package com.istar.mediabroken.task

import com.istar.mediabroken.service.weibo.WeiboExpiredUserInfoService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
/**
 * Author: hh
 * Time: 2018/1/4
 *  向星光数据平台发出同步微博用户数据请求
 */
@Component
@Slf4j
class SyncExpiredWeiboUserTask implements Task {
    @Autowired
    WeiboExpiredUserInfoService expiredUserInfoService

    @Override
    void execute() {
        expiredUserInfoService.syncUserInfo()
        System.exit(-1)
    }
}
