package com.istar.mediabroken.repo.evaluate

import com.istar.mediabroken.entity.evaluate.EvaluateTeam
import com.istar.mediabroken.utils.MongoHolder
import com.mongodb.DBObject
import com.mongodb.WriteResult
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import static com.istar.mediabroken.utils.MongoHelper.toObj

/**
 * @author zxj
 * @create 2018/6/19
 */
@Repository
@Slf4j
class EvaluateTeamRepo {
    @Autowired
    MongoHolder mongo

    void addEvaluateTeam(long userId, String teamName) {
        def collection = mongo.getCollection("evaluateTeam")
        def teamId = UUID.randomUUID().toString()
        def insert = collection.insert(toObj([
                _id       : teamId,//文件id
                userId    : userId,
                teamName  : teamName,
                createTime: new Date(),
                updateTime: new Date()
        ]))
    }

    List getEvaluateTeams(long userId) {
        def collection = mongo.getCollection("evaluateTeam")
        def sort = collection.find(toObj([userId: userId])).sort(toObj([createTime: 1]))
        def result = []
        while (sort.hasNext()) {
            def team = sort.next()
            result << new EvaluateTeam(team as Map)
        }
        sort.close()
        return result
    }

    Integer removeEvaluateTeamById(long userId, String id) {
        def collection = mongo.getCollection("evaluateTeam")
        def remove = collection.remove(toObj([_id: id, userId: userId]))
        return remove.getN()
    }

    void modifyEvaluateTeamById(long userId, String id, String teamName) {
        def collection = mongo.getCollection("evaluateTeam")
        def update = collection.update(toObj([_id: id, userId: userId]), toObj(['$set': [teamName: teamName, updateTime: new Date()]]))
    }

    EvaluateTeam getEvaluateTeamById(long userId, String id){
        def collection = mongo.getCollection("evaluateTeam")
        def one = collection.findOne(toObj(_id: id, userId: userId))
        return one
    }


}
