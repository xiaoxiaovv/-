package com.istar.mediabroken.service.statistics

import com.istar.mediabroken.entity.account.Account
import com.istar.mediabroken.entity.statistics.AccountDataStatistics
import com.istar.mediabroken.repo.account.AccountRepo
import com.istar.mediabroken.repo.statistics.AccountDataStatisticsRepo
import com.istar.mediabroken.service.account.AccountService
import com.istar.mediabroken.utils.DateUitl
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * @author hanhui
 * @date 2018/4/2 15:39
 * */
@Service
@Slf4j
class AccountDataStatisticsService {
    @Autowired
    AccountService accountService
    @Autowired
    AccountRepo accountRepo
    @Autowired
    AccountDataStatisticsRepo accountDataStatisticsRepo

    void addAccountDataStatistics() {
        AccountDataStatistics dataStatistics = getAccountStatistic()
        getNewAccountStatistic(dataStatistics)
        dataStatistics.setTotalCount(dataStatistics.getRegisteredCount() + dataStatistics.getTrialCount()
                + dataStatistics.getOfficalCount())
        dataStatistics.setNewCount(dataStatistics.getNewRegisteredCount() + dataStatistics.getNewTrialCount()
                + dataStatistics.getNewOfficalCount())
        dataStatistics.setStatisticTime(DateUitl.getBeginDayOfYesterday())
        dataStatistics.setCreateTime(new Date())
        accountDataStatisticsRepo.addAccountDataStatistics(dataStatistics)
    }

    private AccountDataStatistics getAccountStatistic() {
        AccountDataStatistics accountDataStatistics = new AccountDataStatistics()
        accountDataStatistics.setTotalCount(0L)
        accountDataStatistics.setNewCount(0L)
        accountDataStatistics.setOfficalCount(0L)
        accountDataStatistics.setTrialCount(0L)
        accountDataStatistics.setNewOfficalCount(0L)
        accountDataStatistics.setNewTrialCount(0L)
        Long registrationCount = 0L
        Long officalCount = 0L
        Long trialCount = 0L
        def dataStatistics = accountRepo.getAccountStatistic(null, null)
        dataStatistics.each { data ->
            String userType = data.get("_id").getAt("userType")
            Integer openType = data.get("_id").getAt("openType")
            Long count = data.get("count")
            String openWay = accountService.getOpenType(openType)
            if (userType && openType)
                if (Account.USERTYPE_OFFICIAL.equals(userType) && Account.OPENTYPE_ADMIN.equals(openWay)) {
                    officalCount = officalCount + count
                } else if (Account.USERTYPE_TRIAL.equals(userType) && Account.OPENTYPE_ADMIN.equals(openWay)) {
                    trialCount = trialCount + count
                } else if (Account.OPENTYPE_REGIST.equals(openWay)) {
                    registrationCount = registrationCount + count
                }
        }
        accountDataStatistics.setOfficalCount(officalCount)
        accountDataStatistics.setTrialCount(trialCount)
        accountDataStatistics.setRegisteredCount(registrationCount)
        return accountDataStatistics
    }

    private void getNewAccountStatistic(AccountDataStatistics dataStatistics) {
        Long officalCount = 0L
        Long trialCount = 0L
        Long newRegistrationCount = 0L
        def newDataStatistics = accountRepo.getAccountStatistic(DateUitl.getBeginDayOfYesterday(),
                DateUitl.getBeginDayOfParm())
        newDataStatistics.each { newData ->
            String userType = newData.get("_id").getAt("userType")
            Integer openType = newData.get("_id").getAt("openType")
            Long count = newData.get("count")
            String openWay = accountService.getOpenType(openType)
            if (userType && openType)
                if (Account.USERTYPE_OFFICIAL.equals(userType) && Account.OPENTYPE_ADMIN.equals(openWay)) {
                    officalCount = officalCount + count
                } else if (Account.USERTYPE_TRIAL.equals(userType) && Account.OPENTYPE_ADMIN.equals(openWay)) {
                    trialCount = trialCount + count
                } else if (Account.OPENTYPE_REGIST.equals(openWay)) {
                    newRegistrationCount = newRegistrationCount + count
                }
        }
        dataStatistics.setNewOfficalCount(officalCount)
        dataStatistics.setNewTrialCount(trialCount)
        dataStatistics.setNewRegisteredCount(newRegistrationCount)
    }
}
