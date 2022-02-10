package com.istar.mediabroken.task

import com.istar.mediabroken.service.InformationStatistics.StatisticsNewsService
import com.istar.mediabroken.utils.DateUitl
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Created by zxj on   2018/3/16
 * 后台admin 的信息流入量统计
 */
@Component
@Slf4j
class StatisticsNewsTask implements Task {

    @Autowired
    StatisticsNewsService statisticsNewsService

    @Override
    void execute() {
        statisticsNewsService.getInformationDataInfluxes(DateUitl.getBeginDayOfYesterday(), DateUitl.getBeginDayOfParm())
        statisticsNewsService.getInformationDataOutflow(DateUitl.getBeginDayOfYesterday(), DateUitl.getBeginDayOfParm())
    }

}
