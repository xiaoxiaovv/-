package com.istar.mediabroken.repo.compile

import com.istar.mediabroken.entity.compile.Style
import com.istar.mediabroken.utils.MongoHolder
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import static com.istar.mediabroken.utils.MongoHelper.toObj

/**
 * Author: zc
 * Time: 2017/8/3
 */
@Repository
@Slf4j
class StyleRepo {
    @Autowired
    MongoHolder mongo

    String addUserStyle(Style style) {
        def collection = mongo.getCollection("style")
        collection.insert(toObj(style.toMap()))
        return style.id
    }

    List<Style> getUserStyles(long userId) {
        def collection = mongo.getCollection("style")
        def cursor = collection.find(toObj([userId: userId])).sort(toObj([createTime: -1]))
        def result = []
        while (cursor.hasNext()) {
            def style = cursor.next()
            result << new Style(style)
        }
        cursor.close()
        return result
    }

    Style getUserStyleById(long userId, String styleId) {
        def collection = mongo.getCollection("style")
        def obj = collection.findOne(toObj([userId: userId,_id:styleId]))
        return obj
    }

    void removeStyle(long userId, String styleId) {
        def collection = mongo.getCollection("style")
        collection.remove(toObj([userId: userId, _id: styleId]))
    }
}
