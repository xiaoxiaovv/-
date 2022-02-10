package com.istar.mediabroken.task

import com.istar.mediabroken.service.statistics.AccountDataStatisticsService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
/**
 * @author  hanhui
 * @date  2018/4/8 10:58
 * @desc 每天新增以及已有账户数统计任务
 **/
@Component
@Slf4j
class AccountDataStatisticsTask implements Task {
    @Autowired
    AccountDataStatisticsService accountDataStatisticsService
    @Override
    void execute() {
        accountDataStatisticsService.addAccountDataStatistics()
    }
}
