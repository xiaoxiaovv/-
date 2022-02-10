package com.istar.mediabroken.repo.admin

import com.istar.mediabroken.entity.SystemSetting
import com.istar.mediabroken.utils.MongoHolder
import com.mongodb.QueryBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import static com.istar.mediabroken.utils.MongoHelper.toObj

/**
 * Author: Luda
 * Time: 2017/6/28
 */
@Repository
class SettingRepo {
    @Autowired
    MongoHolder mongo

    SystemSetting getSystemSetting(String type, String key) {
        def collection = mongo.getCollection("systemSetting")
        QueryBuilder queryBuilder = QueryBuilder.start();
        queryBuilder.put("type").is(type)
        queryBuilder.put("key").is(key)
        def setting = collection.findOne(queryBuilder.get())
        return setting ? new SystemSetting(setting) : null
    }
    SystemSetting getSystemSettingByDescription(String type, String description) {
        def collection = mongo.getCollection("systemSetting")
        QueryBuilder queryBuilder = QueryBuilder.start();
        queryBuilder.put("type").is(type)
        queryBuilder.put("description").is(description)
        def setting = collection.findOne(queryBuilder.get())
        return setting ? new SystemSetting(setting) : null
    }

    boolean modifySystemSetting(String[] content) {
        def collection = mongo.getCollection("systemSetting")
        collection.update(toObj([type: "news", key: "hotNews"]),
                toObj(['$set': [content: content]]),true,false)
        return true;
    }

    boolean isAdminAvailable(String adminUsername, String adminPassword) {
        boolean isAdminAvailable = false;
        SystemSetting adminSetting = getSystemSetting("admin", "user")
        if (adminSetting == null) {
            return [:]
        }
        List adminList = adminSetting.content
        adminList.each {
            if (adminUsername.equals(it.key) && adminPassword.equals(it.value)) {
                isAdminAvailable = true;
            }
        }
        return isAdminAvailable;
    }

}
