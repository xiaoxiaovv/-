package com.istar.mediabroken.repo.account

import com.alibaba.fastjson.JSONObject
import com.istar.mediabroken.entity.account.Organization
import com.istar.mediabroken.utils.MongoHolder
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import static com.istar.mediabroken.utils.MongoHelper.toObj

/**
 * Author: Luda
 * Time: 2017/7/28
 */
@Repository
@Slf4j
class OrganizationRepo {
    @Autowired
    MongoHolder mongo;

    public static final String collName = "organization"

    Organization getOrganizationByAppId(String appId) {
        def collection = mongo.getCollection(collName)
        def result = collection.findOne(toObj([appId: appId]))
        if (result) {
            return new Organization(result)
        } else {
            return null
        }
    }

    Organization getOrgInfoByOrgIdAndAgentId(String orgId, String agentId){
        def collection = mongo.getCollection(collName)
        def result = collection.findOne(toObj([_id: orgId, agentId: agentId]))
        return result
    }

    List<String> getOrgIdsByAgentId(String agentId){
        def collection = mongo.getCollection(collName)
        def cursor = collection.find(toObj('agentId':agentId))
        def orgIds = []
        while (cursor.hasNext()){
            def organization = cursor.next()
            orgIds << organization._id
        }
        return orgIds
    }

    def save(def org) {
        def collection = mongo.getCollection(collName)

        log.info(['orgRepo', 'save', JSONObject.toJSONString(org)].join(':::') as String)

        collection.update(
                toObj([_id: org.id as String]),
                toObj(['$set': [
                        orgName     : org.orgName,
                        agentId     : org.agentId as String,
                        appId       : org.appId,
                        secret      : org.secret,
                        canPushNews : org.canPushNews,
                        maxUserCount: org.maxUserCount,
                        updateTime  : new Date(),
                        ecloudOrgInfo   : org.ecloudOrgInfo
                ]]), true, false
        )
    }

}
