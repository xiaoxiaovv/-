package com.istar.mediabroken.repo.favoriteGroup

import com.istar.mediabroken.entity.LoginUser
import com.istar.mediabroken.entity.capture.NewsOperation
import com.istar.mediabroken.entity.favoriteGroup.FavoriteGroup
import com.istar.mediabroken.utils.MongoHolder
import com.mongodb.DBCursor
import com.mongodb.WriteResult
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import static com.istar.mediabroken.utils.MongoHelper.toObj

/**
 * Author : YCSnail
 * Date   : 2017-11-09
 * Email  : liyancai1986@163.com
 */
@Repository
@Slf4j
class FavoriteGroupRepo {

    @Autowired
    MongoHolder mongo

    Map addGroup(long userId, String groupName) {
        def collection = mongo.getCollection("favoriteGroup")
        def groupId = UUID.randomUUID().toString()
        collection.insert(toObj([
                _id       : groupId,//文件id
                userId    : userId,
                groupName : groupName,
                groupType : FavoriteGroup.GROUP_TYPE_NORMAL,
                createTime: new Date(),
                updateTime: new Date()
        ]))
        def map = [:]
        map.put("groupId", groupId)
        map.put("groupName", groupName)
        map.put("groupType", FavoriteGroup.GROUP_TYPE_NORMAL)
        return map
    }

    Map updateGroup(long userId, String groupId, String groupName) {
        def collection = mongo.getCollection("favoriteGroup")
        collection.update(
                toObj([userId: userId, _id: groupId]),
                toObj(['$set': [
                        groupName : groupName,
                        updateTime: new Date()
                ]]),
                false,
                false
        )
        def map = [:]
        map.put("groupId", groupId)
        map.put("groupName", groupName)
        map.put("groupType", FavoriteGroup.GROUP_TYPE_NORMAL)
        return map
    }

    void delGroup(long userId, String groupId) {
        def collection = mongo.getCollection("favoriteGroup")
        collection.remove(toObj([_id: groupId, userId: userId]))
    }

    //获得类型为默认的所有列表
    def getGroupTypeIsNormalFavoriteGroupList(long userId) {
        def collection = mongo.getCollection("favoriteGroup")
        def obj = collection.find(toObj([userId: userId, groupType: FavoriteGroup.GROUP_TYPE_NORMAL]))
        def result = []
        while (obj.hasNext()) {
            def group = obj.next()
            result << new FavoriteGroup(group as Map)
        }
        obj.close()
        return result
    }
    //获得所有列表
    def getFavoriteGroupList(long userId) {
        def collection = mongo.getCollection("favoriteGroup")
        def obj = collection.find(toObj([userId: userId])).sort(toObj([groupType: 1]))
        def result = []
        while (obj.hasNext()) {
            def group = obj.next()
            result << new FavoriteGroup(group as Map)
        }
        obj.close()
        return result
    }

    Map newLoadGroup(long userId) {
        def collection = mongo.getCollection("favoriteGroup")
        def groupId = UUID.randomUUID().toString()
        collection.insert(toObj([
                _id       : groupId,//文件id
                userId    : userId,
                groupName : FavoriteGroup.GROUP_NAME_IMPORT,
                groupType : FavoriteGroup.GROUP_TYPE_IMPORT,
                createTime: new Date(),
                updateTime: new Date()
        ]))
        def map = [:]
        map.put("groupId", groupId)
        map.put("groupName", FavoriteGroup.GROUP_NAME_IMPORT)
        map.put("groupType", FavoriteGroup.GROUP_TYPE_IMPORT)
        return map
    }

    //根据id查询一条记录
    def getGroupById(String groupId) {
        def collection = mongo.getCollection("favoriteGroup")
        def obj = collection.findOne(toObj([_id: groupId]))
        return obj != null ? new FavoriteGroup(obj as Map) : null
    }

    def getFavoriteGroupImportList(long userId) {
        def collection = mongo.getCollection("favoriteGroup")
        def obj = collection.find(toObj([userId: userId, groupType: FavoriteGroup.GROUP_TYPE_IMPORT]))
        def result = []
        while (obj.hasNext()) {
            def group = obj.next()
            result << new FavoriteGroup(obj as Map)
        }
        obj.close()
        return result
    }

    def getImportGroupListByUserId(long userId) {
        def collection = mongo.getCollection("favoriteGroup")
        def obj = collection.findOne(toObj([userId: userId, groupType: FavoriteGroup.GROUP_TYPE_IMPORT]))
        return obj != null ? new FavoriteGroup(obj as Map) : null
    }

    def addGroupByTeam(LoginUser user, String teamId, String groupName) {
        def collection = mongo.getCollection("favoriteGroup")
        def groupId = UUID.randomUUID().toString()
        collection.insert(toObj([
                _id       : groupId,//文件id
                userId    : 0L,
                agentId   : user.agentId,
                orgId     : user.orgId,
                teamId    : teamId,
                groupName : groupName,
                groupType : FavoriteGroup.GROUP_TYPE_SHARED,
                createTime: new Date(),
                updateTime: new Date()
        ]))
    }

    List getGroupsByUserAndType(String teamId, String orgId, String agentId, int groupType) {
        def collection = mongo.getCollection("favoriteGroup")
        def obj = collection.find(toObj([teamId: teamId, orgId: orgId, agentId: agentId, groupType: groupType]))
        def result = []
        while (obj.hasNext()) {
            def group = obj.next()
            result << new FavoriteGroup(group as Map)
        }
        obj.close()
        return result
    }

    def delGroupByUserAndType(LoginUser user, teamId, int groupType) {
        def collection = mongo.getCollection("favoriteGroup")
        collection.remove(toObj([orgId: user.orgId, agentId: user.agentId, teamId: teamId, groupType: groupType]))
    }

}
