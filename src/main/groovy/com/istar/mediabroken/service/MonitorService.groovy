package com.istar.mediabroken.service

import com.istar.mediabroken.api3rd.CaptureApi3rd
import com.istar.mediabroken.repo.account.AccountRepo
import com.istar.mediabroken.repo.CacheRepo
import com.istar.mediabroken.service.account.AccountService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

import java.text.SimpleDateFormat

@Service
@Slf4j
class MonitorService {
    @Value("env")
    String env

    @Autowired
    AccountService accountSrv

    @Autowired
    CaptureService captureSrv

    @Autowired
    VerifyCodeService verifyCodeSrv

    @Autowired
    CaptureApi3rd captureApi

    @Autowired
    CacheRepo cacheRepo

    @Autowired
    AccountRepo accountReop

    LinkedHashMap<String, Serializable> createSubjectStatus() {
        new Thread(new Runnable() {
            @Override
            void run() {
                log.debug('run')
                try {
                    def result = doCreateSubjectStatus()
                    cacheRepo.put("subjectStatus", result)
                } catch (Throwable e) {
                    log.error(e)
                }
            }
        }).start()
    }

    LinkedHashMap<String, Serializable> doCreateSubjectStatus() {
        def sdf = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss')
        def session = accountReop.getYqmsSession()

        def validSites = []
        def invalidSites = []
        def otherValidSubjects = []
        def otherInvalidSubjects = []
        def subjects = captureApi.getSubjectList(session.session, session.yqmsUserId)
        log.debug('{}', subjects)
        def total = 0
        subjects.list.each {
            if (it.subjectName.startsWith('st') || it.subjectName.startsWith('wc') || it.subjectName.startsWith('sname')) {
                def (hasData, latestTime) = hasData(session, it)
                if (hasData) {
                    validSites << [subjectId: it.subjectName, domain: it.domain, account: it.account, latestTime: sdf.format(latestTime)]
                } else {
                    invalidSites << [subjectId: it.subjectName, domain: it.domain, account: it.account]
                }
            } else {
                def (hasData, latestTime) = hasData(session, it)
                if (hasData) {
                    otherValidSubjects << [subjectId: it.subjectName, domain: it.domain, account: it.account, latestTime: sdf.format(latestTime)]
                } else {
                    otherInvalidSubjects << [subjectId: it.subjectName, domain: it.domain, account: it.account]
                }

            }
            total++
        }

        def yqmsSession = accountReop.getYqmsSession2(session.yqmsUserId)
        def totalToday = captureApi.getTotalToday(yqmsSession)

        def rep = [createTime             : sdf.format(new Date()), totalToday: totalToday, total: total, validSitesTotal: validSites.size(), invalidSitesTotal: invalidSites.size(),
                   otherValidSubjectsTotal: otherValidSubjects.size(), otherInvalidSubjectsTotal: otherInvalidSubjects.size(),
                   validSites             : validSites, invalidSites: invalidSites, otherValidSubjects: otherValidSubjects, otherInvalidSubjects: otherInvalidSubjects]
        rep
    }

    private List hasData(Map session , it) {
        def rep = captureApi.getNewsList(session.session, session.yqmsUserId as long, it.subjectId, 1, 1)
        if (rep.list)
            return [true, new Date(rep.list[0].time)]
        else
            return [false, null]

    }

    Map getSubjectStatus() {
        def value = cacheRepo.get("subjectStatus")
        return value ?: [:]
    }
}
