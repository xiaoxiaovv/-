package com.istar.mediabroken.repo.tags

import com.istar.mediabroken.utils.MongoHolder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import static com.istar.mediabroken.utils.MongoHelper.toObj

/**
 * @author: hexushuai
 * @date: 2019/2/28 10:38
 */
@Repository
class TagsManageRepo {

    @Autowired
    MongoHolder mongoHolder

    List findTagsByOrgId(String orgId) {
        def collection = mongoHolder.getCollection("tagsLibrary")
        def query = [orgId: orgId]
        def cursor = collection.find(toObj(query))
        def list = []
        while (cursor.hasNext()) {
            def obj = cursor.next()
            list << [
                    id        : obj._id,
                    tagName   : obj.tagName,
                    tagType : obj.tagType,
                    tagValue     : obj.tagValue,
                    orgId: obj.orgId,
                    createTime      : obj.createTime,
                    updateTime   : obj.updateTime
            ]
        }
        cursor.close()
        return list
    }

    void insert(Map map) {
        def collection = mongoHolder.getCollection("tagsLibrary")
        collection.insert(toObj(map))
    }

    int findByTagName(String orgId, String tagName) {
        def collection = mongoHolder.getCollection("tagsLibrary")
        def query = [orgId: orgId, tagName: tagName]
        int count = collection.find(toObj(query)).count()
        return count
    }

    Map findOne(String id) {
        Map map = new HashMap()
        def collection = mongoHolder.getCollection("tagsLibrary")
        def query = [_id: id]
        def cursor = collection.find(toObj(query)).limit(1)
        if (cursor.hasNext()) {
            def it = cursor.next()
            map.put("id", it._id)
            map.put("tagName", it.tagName)
            map.put("tagType", it.tagType)
            map.put("tagValue", it.tagValue)
            map.put("orgId", it.orgId)
            map.put("createTime", it.createTime)
        }
        return map
    }

    void update(String id, Map map) {
        def collection = mongoHolder.getCollection("tagsLibrary")
        collection.update(toObj([_id: id]),
                toObj(['$set': [tagName: map.get("tagName"),
                                tagValue: map.get("tagValue"),
                                tagType: map.get("tagType"),
                                updateTime: new Date()
                ]]),false, false)
    }

    void delete(String id) {
        def collection = mongoHolder.getCollection("tagsLibrary")
        collection.remove(toObj([_id: id]))
    }
}
