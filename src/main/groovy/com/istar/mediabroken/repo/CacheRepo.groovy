package com.istar.mediabroken.repo

import com.istar.mediabroken.utils.MongoHolder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller

import static com.istar.mediabroken.utils.MongoHelper.toObj

@Controller
class CacheRepo {
    @Autowired
    MongoHolder mongo;

    void put(String key, Map data) {
        def collection = mongo.getCollection("cache")
        collection.update(toObj([_id: key]), toObj([data: data, updateTime: new Date(), type: 'map']), true, false)
    }

    Map get(String key) {
        def collection = mongo.getCollection("cache")
        def rep = collection.findOne(toObj([_id: key]))
        return rep ? rep.data : null
    }
}
