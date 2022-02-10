package com.istar.mediabroken.repo.system

import com.istar.mediabroken.entity.system.OperationLog
import com.istar.mediabroken.utils.MongoHolder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller

import static com.istar.mediabroken.utils.MongoHelper.toObj

@Controller
class OperationLogRepo {
    @Autowired
    MongoHolder mongo;

    /**
     * 添加请求日志
     * @param operationLog
     */
    void addRequestLog(OperationLog operationLog){
        def collection = mongo.getCollection("operationLog_reprint")
        collection.insert(toObj(operationLog.toMap()))
    }

    void addRequestLogForCaptureTime(OperationLog operationLog){
        def collection = mongo.getCollection("operationLog_captureTime")
        collection.insert(toObj(operationLog.toMap()))
    }
}
