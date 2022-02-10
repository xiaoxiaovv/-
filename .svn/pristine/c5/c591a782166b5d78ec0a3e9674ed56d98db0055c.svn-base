package com.istar.mediabroken.service.statistics

import com.istar.mediabroken.entity.account.Account
import com.istar.mediabroken.entity.statistics.RetainedDataStatistics
import com.istar.mediabroken.repo.account.AccountRepo
import com.istar.mediabroken.repo.statistics.RetainedDataStatisticsRepo
import com.istar.mediabroken.utils.DateUitl
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.text.NumberFormat

/**
 * @author hanhui
 * @date 2018/4/2 15:39
 * */
@Service
@Slf4j
class RetainedDataStatisticsService {
    @Autowired
    AccountRepo accountRepo
    @Autowired
    RetainedDataStatisticsRepo retainedDataStatisticsRepo

    void addRetainedDataStatistics() {
        RetainedDataStatistics dataStatistics = getRetainedDataStatistics()
        retainedDataStatisticsRepo.addRetainedDataStatistics(dataStatistics)
    }

    private RetainedDataStatistics getRetainedDataStatistics() {
        NumberFormat numberFormat = NumberFormat.getInstance()
        numberFormat.setMaximumFractionDigits(2)
        RetainedDataStatistics dataStatistics = new RetainedDataStatistics()
        Date startTime = DateUitl.beginDayOfYesterday
        Date endTime = DateUitl.getBeginDayOfParm()
        Long totalCount = accountRepo.getAccountCountByType(null, null, null)
        Long retainedCount = accountRepo.getAccountCountByType(Account.USERTYPE_OFFICIAL, endTime, null)
        String retainedPercent = numberFormat.format(retainedCount / totalCount * 100)+"%"
        Long lossCount = accountRepo.getAccountCountByType(null, null, endTime)
        String lossPercent = numberFormat.format(lossCount / totalCount * 100)+"%"
        List<Account> accountList = accountRepo.getAccountListByExpDate(startTime, endTime)
        Long newLossCount = accountList.size()
        StringBuffer userIds = new StringBuffer("")
        accountList.each { account ->
            userIds.append(account.getId() + ",")
        }
        String newLossIds = userIds ?: ""
        dataStatistics.setNewLossIds(newLossIds)
        dataStatistics.setRetainedCount(retainedCount)
        dataStatistics.setRetainedPercent(retainedPercent)
        dataStatistics.setLossCount(lossCount)
        dataStatistics.setLossPercent(lossPercent)
        dataStatistics.setNewLossCount(newLossCount)
        dataStatistics.setStatisticTime(DateUitl.getBeginDayOfYesterday())
        dataStatistics.setCreateTime(new Date())
        return dataStatistics
    }
}
