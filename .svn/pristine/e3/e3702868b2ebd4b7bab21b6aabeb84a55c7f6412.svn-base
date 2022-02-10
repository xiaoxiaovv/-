package com.istar.mediabroken.repo.compile

import com.istar.mediabroken.entity.compile.AccountTagHistory
import com.istar.mediabroken.entity.compile.OrgTag
import com.istar.mediabroken.utils.MongoHolder
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import static com.istar.mediabroken.utils.MongoHelper.toObj

/**
 * @author zxj
 * @create 2019/1/17
 */
@Repository
@Slf4j
class TagRepo {
    @Autowired
    MongoHolder mongo

    List getTagListByIds(List ids) {
        def collection = mongo.getCollection("orgTag")
        def cursor = collection.find(toObj(["_id": [$in: ids]]))
        def result = []
        while (cursor.hasNext()) {
            def style = cursor.next()
            OrgTag tag = new OrgTag(
                    [id         : style.get("_id"),
                     parentId   : style.get("parentId"),
                     orgId      : style.get("orgId"),
                     level      : style.get("level"),
                     leaf       : style.get("leaf"),
                     code       : style.get("code"),
                     parentCode : style.get("parentCode"),
                     userTagName: style.get("userTagName"),
                     userTagId  : style.get("userTagId"),
                     subTagList : [],
                     isHave     : false
                    ]
            )
            result << tag
        }
        cursor.close()
        return result
    }

    List getTagHistoryTagIds(long userId) {
        def collection = mongo.getCollection("accountTagHistory")
        def cursor = collection.find(toObj([userId: userId])).sort(toObj([createTime: -1]))
        def result = []
        while (cursor.hasNext()) {
            def style = cursor.next()
            result << style.get("orgTagIds")
        }
        cursor.close()
        return result
    }

    def saveAccountTagHistory(AccountTagHistory history) {
        def collection = mongo.getCollection("accountTagHistory")
        collection.save(toObj(
                [
                        "_id"       : history.getId(),
                        "userId"    : history.userId,
                        "orgTagIds" : history.orgTagIds,
                        "createTime": history.createTime
                ]
        ))
    }

    def saveOrgTag(OrgTag orgTag) {
        def collection = mongo.getCollection("orgTag")
        collection.save(toObj(
                [
                        "_id"        : orgTag.getId(),
                        "parentId"   : orgTag.getParentId(),
                        "orgId"      : orgTag.getOrgId(),
                        "level"      : orgTag.getLevel(),
                        "leaf"       : orgTag.getLeaf(),
                        "code"       : orgTag.getCode(),
                        "parentCode" : orgTag.getParentCode(),
                        "userTagName": orgTag.getUserTagName(),
                        "userTagId"  : orgTag.getUserTagId()
                ]
        ))
    }

    List getMaxCodeIsLeaf(String orgId) {

        def collection = mongo.getCollection("orgTag")
        def cursor = collection.find(toObj([orgId: orgId, leaf: true])).sort(toObj([code: -1]))
        def result = []
        while (cursor.hasNext()) {
            def style = cursor.next()
            result << style.get("code")
        }
        cursor.close()
        return result

    }

    def getOrgTagById(String id) {
        def collection = mongo.getCollection("orgTag")
        def cursor = collection.find(toObj([_id: id]))
        def result = null
        if (cursor.hasNext()) {
            def style = cursor.next()
            result = style
        }
        cursor.close()
        return result
    }

    List getMaxCodeNotLeaf(String orgId) {
        def collection = mongo.getCollection("orgTag")
        def cursor = collection.find(toObj([orgId: orgId, leaf: false])).sort(toObj([code: -1]))
        def result = []
        while (cursor.hasNext()) {
            def style = cursor.next()
            result << style.get("code")
        }
        cursor.close()
        return result
    }

    List<OrgTag> getOrgTagList(String orgId) {
        def collection = mongo.getCollection("orgTag")
        def cursor = collection.find(toObj([orgId: orgId]))
        def result = []
        while (cursor.hasNext()) {
            def style = cursor.next()
            OrgTag tag = new OrgTag(style)
            result << tag
        }
        cursor.close()
        return result
    }

    void deleteOrgTag(String orgId) {
        def collection = mongo.getCollection("orgTag")
        collection.remove(toObj([orgId: orgId]))
    }

    Map getOrgTag(String id) {
        def collection = mongo.getCollection("orgTag")
        def result = collection.findOne(toObj(["_id": id]))
        if (result) {
            return new OrgTag(result.toMap()).toMap()
        } else {
            return [:]
        }

    }

    void addOrgTag(Map orgTagMap) {
        def collection = mongo.getCollection("orgTag")
        collection.insert(toObj(orgTagMap))
    }

    int getMaxCode(String orgId, String parentCode) {
        def collection = mongo.getCollection("orgTag")
        def orgTag = collection.findOne(toObj([orgId: orgId, parentCode: parentCode]), toObj([code: 1]), toObj([code: -1]))
        if (orgTag) {
            String code = orgTag.get("code")
            return Integer.parseInt(code.substring(code.length() - 4))
        } else {
            return 1000
        }
    }

    void saveOrgTagHistory(List<OrgTag> orgTagList) {
        def collection = mongo.getCollection("orgTagHistory")
        if (!(orgTagList && orgTagList.size() > 0)) {
            return
        }
        List dbObjectList = []
        orgTagList.each {
            dbObjectList << toObj([_id:UUID.randomUUID().toString(),orgTag:it.toMap(),createTime: new Date()])
        }
        collection.insert(dbObjectList)
    }
}
