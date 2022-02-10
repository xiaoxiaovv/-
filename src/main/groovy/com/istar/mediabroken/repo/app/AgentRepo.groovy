package com.istar.mediabroken.repo.app

import com.istar.mediabroken.entity.app.Agent
import com.istar.mediabroken.utils.MongoHolder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import static com.istar.mediabroken.utils.MongoHelper.toObj

/**
 * Author : YCSnail
 * Date   : 2018-01-19
 * Email  : liyancai1986@163.com
 */
@Component
class AgentRepo {

    @Autowired
    MongoHolder mongo

    def getAgentList(String domain) {
        def collection = mongo.getCollection("agent")
        def query = [ agentDomains : [$in: [domain]] ]
        def cursor = collection.find(toObj(query))
        def list = []
        while (cursor.hasNext()) {
            def it = cursor.next()
            list << Agent.toObject(it)
        }
        cursor.close()
        return list
    }
}
