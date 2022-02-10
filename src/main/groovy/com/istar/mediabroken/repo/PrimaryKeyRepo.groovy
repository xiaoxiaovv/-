package com.istar.mediabroken.repo

import com.istar.mediabroken.utils.MongoHolder
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import static com.istar.mediabroken.utils.MongoHelper.toObj

/**
 * Author : YCSnail
 * Date   : 2018-01-24
 * Email  : liyancai1986@163.com
 */
@Repository
@Slf4j
class PrimaryKeyRepo {

    @Autowired
    MongoHolder mongo

    private String collectionName = "mongoPrimaryKey";

    String createId(String key) {

        def collection = mongo.getCollection(collectionName)

        def obj = collection.findAndModify(
                toObj(["_id" : key]),               //query
                toObj(["_id": 1, "value": 1]),      //fields
                null,
                false,
                toObj([$inc: [ "value" : 1]]),    //update
                true,
                true
        )

        return obj?.value.toString();
    }



}
