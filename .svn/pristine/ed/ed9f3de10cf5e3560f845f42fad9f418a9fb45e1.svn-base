package com.istar.mediabroken.service.system

import com.istar.mediabroken.entity.account.Account
import com.istar.mediabroken.entity.system.OperationLog
import com.istar.mediabroken.repo.system.OperationLogRepo
import groovy.util.logging.Slf4j
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import static com.istar.mediabroken.api.ApiResult.apiResult

@Service("OperationLogService")
@Slf4j
class OperationLogService {
    @Autowired
    OperationLogRepo operationLogRepo

    def addOperationLog(Account userInfo, String operationSource, def siteType, def hot, def startTime, def endTime, def orientation, def hasPic, def order, def keyWords, def pageSize) {
        String queryCondition = "siteType:"+siteType+","+
                                "hot:"+hot+","+
                                "startTime:"+startTime+","+
                                "endTime:"+endTime+","+
                                "orientation:"+orientation+","+
                                "hasPic:"+hasPic+","+
                                "order:"+order+","+
                                "keyWords:"+keyWords+","+
                                "pageSize:"+pageSize

        OperationLog operationLog = new OperationLog(
                _id: UUID.randomUUID().toString(),
                userInfo: userInfo?userInfo.toMap():[],
                operationSource: operationSource,
                queryCondition: queryCondition,
                createTime: new Date()
        )
        if (order == 2){
            operationLogRepo.addRequestLog(operationLog)
        }else if (order == 1){
            operationLogRepo.addRequestLogForCaptureTime(operationLog)
        }
    }
}