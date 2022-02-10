package com.istar.mediabroken.utils

import com.mongodb.BasicDBObject
import com.mongodb.DBObject

class MongoHelper {
    static DBObject toObj(Map params) {
        return new BasicDBObject(params)
    }

    static List<DBObject> toList(List params) {
        List<BasicDBObject> list = new ArrayList<>()
        for (int i = 0; i < params.size(); i++) {
            list.add(new BasicDBObject(params[i]))
        }
        return list
    }
}
