package com.istar.mediabroken.service.evaluate

import com.istar.mediabroken.entity.evaluate.EvaluateTeam
import com.istar.mediabroken.repo.evaluate.EvaluateChannelRepo
import com.istar.mediabroken.repo.evaluate.EvaluateTeamRepo
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * @author zxj
 * @create 2018/6/19
 */
@Service
@Slf4j
class EvaluateTeamService {


    @Autowired
    EvaluateTeamRepo evaluateTeamRepo
    @Autowired
    EvaluateChannelRepo evaluateChannelRepo

    void addEvaluateTeam(long userId, String teamName){
        evaluateTeamRepo.addEvaluateTeam(userId, teamName)
    }

    List getEvaluateTeams(long userId){
        return evaluateTeamRepo.getEvaluateTeams(userId)
    }

    Integer removeEvaluateTeamById(long userId, String id) {
        return evaluateTeamRepo.removeEvaluateTeamById(userId, id)
    }

    def delTeam(long userId, String id) {
        def channel = evaluateChannelRepo.findByTeamId(userId, id)
        evaluateTeamRepo.removeEvaluateTeamById(userId, id)
        def ids = []
        channel.each { elem ->
            ids.add(elem.id)
        }
        evaluateChannelRepo.modifyChannelTeamId(userId, "0", ids)
    }

    void modifyTeamById(long userId, String id, String teamName){
        evaluateTeamRepo.modifyEvaluateTeamById(userId, id, teamName)
    }

    EvaluateTeam findOneBydId(long userId, String id){
        evaluateTeamRepo.getEvaluateTeamById(userId, id)
    }
}
