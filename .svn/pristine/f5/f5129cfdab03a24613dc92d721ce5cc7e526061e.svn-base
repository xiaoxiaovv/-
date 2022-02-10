package com.istar.mediabroken.repo

import com.istar.mediabroken.utils.MongoHolder
import com.jsms.api.xpt.demo.ApiDemo4HttpClient
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import static com.istar.mediabroken.utils.MongoHelper.toObj

@Repository
@Slf4j
class VerifyCodeRepo {
    @Autowired
    MongoHolder mongo;

    Map getVerifyCode(String phoneNumber) {
        def collection = mongo.getCollection("verifyCode")
        def rep = collection.findOne(toObj([phoneNumber: phoneNumber]))
        if (rep) {
            return [
                    phoneNumber: rep.phoneNumber,
                    verifyCode: rep.verifyCode,
                    createTime: rep.createTime
            ]
        } else {
            null
        }
    }

    void addVerifyCode(String phoneNumber, String verifyCode) {
        def collection = mongo.getCollection("verifyCode")
        collection.update(toObj([
                phoneNumber: phoneNumber,
        ]), toObj([
                phoneNumber: phoneNumber,
                verifyCode: verifyCode,
                createTime: new Date(),
        ]), true, false)

    }

    void sendVerifyCode(String phoneNumber, String verifyCode) {
        log.debug("send sim message: {}, {}", phoneNumber, verifyCode)
        ApiDemo4HttpClient.sendSms(phoneNumber, "JSM41634-0001", '@1@=' + verifyCode)
    }

    void sendLoginMsg(String phoneNumber, String userName, String password) {
        log.debug("send loginMsg message: {}, {}, {}", phoneNumber, userName, password)
        ApiDemo4HttpClient.sendSms(phoneNumber, "JSM41634-0002", "@1@=编++,@2@=" + userName + ",@3@=" + password)
    }

    void removeVerifyCode(String id) {
        def collection = mongo.getCollection("verifyCode")
        collection.remove(toObj([phoneNumber: id]))
    }
}
