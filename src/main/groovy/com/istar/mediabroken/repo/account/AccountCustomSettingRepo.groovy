package com.istar.mediabroken.repo.account

import com.istar.mediabroken.entity.account.AccountCustomSetting
import com.istar.mediabroken.utils.MongoHolder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import static com.istar.mediabroken.utils.MongoHelper.toObj

@Repository
class AccountCustomSettingRepo {

    @Autowired
    MongoHolder mongo;


    def getOneAccountCustomSettingByKey(long userId, String key) {
        def collection = mongo.getCollection("accountCustomSetting")
        def obj = collection.findOne(toObj([userId: userId, key: key]))
        return obj != null ? AccountCustomSetting.toObject(obj) : null
    }

    def modifyAccountCustomSetting(long userId, String key, def content) {
        def collection = mongo.getCollection("accountCustomSetting")
        collection.update(
                toObj([userId: userId, key: key]),
                toObj(['$set': [
                        content   : content,
                        updateTime: new Date()
                ]]),
                true,
                false
        )
    }

    def addAccountCustomSetting(long userId, String key, def content, String orgId) {
        def collection = mongo.getCollection("accountCustomSetting")
        collection.save(toObj([
                userId    : userId,
                key       : key,
                content   : content,
                orgId     : orgId,
                createTime: new Date(),
                updateTime: new Date()
        ]))
    }
}
