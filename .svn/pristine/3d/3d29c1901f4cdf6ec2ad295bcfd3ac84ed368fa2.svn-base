package com.istar.mediabroken.repo.account

import com.istar.mediabroken.entity.LoginUser
import com.istar.mediabroken.entity.account.Team
import com.istar.mediabroken.entity.favoriteGroup.FavoriteGroup
import com.istar.mediabroken.repo.PrimaryKeyRepo
import com.istar.mediabroken.utils.MongoHolder
import com.mongodb.DBObject
import com.mongodb.WriteResult
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import static com.istar.mediabroken.utils.MongoHelper.toObj

/**
 * Created by zxj on   2018/1/23
 */
@Repository
@Slf4j
class TeamRepo {

    @Autowired
    MongoHolder mongo
    @Autowired
    PrimaryKeyRepo primaryKeyRepo

    Map addTeam(String orgId, String agentId, String teamName) {
        def collection = mongo.getCollection("team")
        def teamId = primaryKeyRepo.createId('team')
        def result = collection.insert(toObj([
                _id       : teamId,//组id
                orgId     : orgId,
                agentId   : agentId,
                teamName  : teamName,
                createTime: new Date(),
                updateTime: new Date()
        ]))
        def map = [:]
        map.put("teamId", teamId)
        map.put("orgId", orgId)
        map.put("agentId", agentId)
        map.put("teamName",teamName)
        return map
    }

    List getTeamList(String orgId) {
        def collection = mongo.getCollection("team")
        def obj = collection.find(toObj([orgId: orgId])).sort(toObj([createTime: 1]))
        def result = []
        while (obj.hasNext()) {
            def team = obj.next()
            result << new Team(team as Map)
        }
        obj.close()
        return result
    }

    def modifyTeam(String orgId, String teamId, String teamName){
        def collection = mongo.getCollection("team")
        def result = collection.update(
                toObj([orgId: orgId, _id: teamId]),
                toObj(['$set': [
                        teamName  : teamName,
                        updateTime: new Date()
                ]]),
                false,
                false
        )
        return result
    }

    def delTeam(String teamId){
        def collection = mongo.getCollection("team")
        collection.remove(toObj([_id: teamId]))
    }

    Team getTeam(String teamId){
        def collection = mongo.getCollection("team")
        def obj = collection.findOne(toObj([_id: teamId]))
        return obj
    }

    List getTeamListByOrgIdAndAnegtId(String agentId, String orgId) {
        def collection = mongo.getCollection("team")
        def obj = collection.find(toObj([orgId: orgId, agentId: agentId]))
        def result = []
        while (obj.hasNext()) {
            def team = obj.next()
            result << new Team(team as Map)
        }
        obj.close()
        return result
    }
}
