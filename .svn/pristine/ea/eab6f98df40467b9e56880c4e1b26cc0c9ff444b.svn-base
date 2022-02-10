package com.istar.mediabroken.repo

import com.alibaba.fastjson.JSONObject
import com.istar.mediabroken.entity.compile.ShareChannelResult
import com.istar.mediabroken.service.ShareChannelService
import com.istar.mediabroken.utils.MongoHolder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import static com.istar.mediabroken.utils.MongoHelper.toObj

/**
 * Author : YCSnail
 * Date   : 2017-03-30
 * Email  : liyancai1986@163.com
 */
@Repository
class ShareChannelRepo {

    @Autowired
    MongoHolder mongo


    def toChannel(res){
        def channel = [
                id          : res._id,
                channelType : res.channelType,
                name        : res.channelInfo.name,
                avatar      : res.channelInfo.avatar,
                uid         : res.channelInfo.uid,
                userId      : res.userId,
                createTime  : res.createTime
        ]
        if(res && res.channelType == ShareChannelService.CHANNEL_TYPE_WECHAT){
            channel.verifyType = res.accountInfo?.verify_type_info.id
        }
        return channel
    }


    List getShareChannels(Long userId) {
        def collection = mongo.getCollection("shareChannel")
        def query = [userId: userId]
        def cursor = collection.find(toObj(query)).sort(toObj([createTime: 1]))
        def channels = []
        while (cursor.hasNext()) {
            channels << toChannel(cursor.next())
        }
        cursor.close()

        return channels
    }

    List getShareChannels(Long userId, int channelType) {
        def collection = mongo.getCollection("shareChannel")
        def query = [userId: userId]
        if(channelType != 0) {
            query.channelType = channelType
        }
        def cursor = collection.find(toObj(query)).sort(toObj([createTime: 1]))
        def channels = []
        while (cursor.hasNext()) {
            channels << toChannel(cursor.next())
        }
        cursor.close()

        return channels
    }

    void addShareChannelResult(ShareChannelResult shareChannelResult){
        def collection = mongo.getCollection("shareChannelResult")
        collection.insert(toObj(shareChannelResult.toMap()))
    }

    List getShareChannelResultList(String timeStamp){
        def result = []
        def query = [timeStamp: timeStamp]
        def collection = mongo.getCollection("shareChannelResult")
        def cursor = collection.find(toObj(query))
        while (cursor.hasNext()) {
            def share = cursor.next()
            result << share.shareResult
        }
        cursor.close()

        return result
    }

    def getChannel(Long userId, String uid, int channelType){
        def collection = mongo.getCollection("shareChannel")
        return collection.findOne(toObj([userId: userId, 'channelInfo.uid': uid, channelType: channelType]))
    }

    def getChannelById(Long id){
        def collection = mongo.getCollection("shareChannel")
        return collection.findOne(toObj([_id: id]))
    }

    /**
     * @param userId
     * @param ids
     * @return
     */
    def getChannelsByIds(Long userId, List<Long> ids){
        def collection = mongo.getCollection("shareChannel")
        def query = [userId: userId]
        if(ids){
            query._id = [$in : ids]
        }else {
            return []
        }
        def channels = []
        def cursor = collection.find(toObj(query)).sort(toObj([createTime: 1]))
        while (cursor.hasNext()) {
            def res = cursor.next()
            channels << [
                    id          : res._id,
                    channelType : res.channelType,
                    accountInfo : res.accountInfo,
                    channelInfo : res.channelInfo
            ]
        }
        cursor.close()

        return channels
    }

    def add(Long userId, int channelType, String uid, String name, String avatar, def accountInfo) {
        def collection = mongo.getCollection("shareChannel")
        def channel = this.getChannel(userId, uid, channelType)

        if(channel){
            collection.update(
                    toObj([_id: channel._id]),
                    toObj(['$set': [accountInfo : accountInfo, 'channelInfo.name': name, 'channelInfo.avatar': avatar, updateTime: new Date()]])
            )
        }else {
            channel = [
                    _id         : System.currentTimeMillis(),
                    channelType : channelType,
                    userId      : userId,
                    channelInfo : [
                            name    : name,
                            uid     : uid,
                            avatar  : avatar
                    ],
                    accountInfo : accountInfo,
                    createTime  : new Date()
            ]
            collection.insert(toObj(channel))
        }

        return channel
    }

    def del(Long channelId){
        def collection = mongo.getCollection("shareChannel")
        collection.remove(toObj([_id : channelId]))
    }

    def delWechatChannel(String authorizerAppId) {
        def collection = mongo.getCollection("shareChannel")
        collection.remove(toObj(['channelInfo.uid' : authorizerAppId, channelType: ShareChannelService.CHANNEL_TYPE_WECHAT]))
    }
}
