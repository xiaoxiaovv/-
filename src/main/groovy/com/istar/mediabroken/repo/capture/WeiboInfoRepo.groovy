package com.istar.mediabroken.repo.capture

import com.istar.mediabroken.entity.capture.WeiboInfo
import com.istar.mediabroken.utils.MongoHolder
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import static com.istar.mediabroken.utils.MongoHelper.toObj

/**
 * Created by Zc on 2017/12/19.
 */
@Repository
@Slf4j
class WeiboInfoRepo {
    @Autowired
    MongoHolder mongo

    Map getVerifiedById(long id) {
        def collection = mongo.getCollection("weiboInfo")
        def query = ["id": id]
        def weiboInfo = collection.findOne(toObj(query))
        if (weiboInfo) {
            return [verified: weiboInfo.verified, verified_type: weiboInfo.verified_type]
        }
        return [:]
    }

    String addWeiboInfo(WeiboInfo weiboInfo) {
        def collection = mongo.getCollection("weiboInfo")
        collection.insert(toObj(weiboInfo.toMap()))
        return weiboInfo.id
    }

    String getExpiredUserInfo(Date date,int pageNo,int size) {
        def result = new StringBuffer("")
        def collection = mongo.getCollection("weiboInfo")
        def query = ["updateTime": ["\$lt": date],"id":["\$ne":null]]
        def cursor = collection.find(toObj(query)).limit(size).skip((pageNo - 1) * size).sort(toObj([id: 1]))
        while (cursor.hasNext()) {
            def weiboInfo = cursor.next()
            def info = new WeiboInfo(weiboInfo)
            if (info.getId() != null)
                result.append(info.getId()+",")
        }
        cursor.close()
        if(!"".equals(result.toString())){
            return result.substring(0,result.length()-1)
        }else {
            return null
        }

    }

    void updateUserInfo(WeiboInfo user) {
        def colleciton = mongo.getCollection("weiboInfo")
            colleciton.update(toObj([id: user.getId()]),
                    toObj(['$set': [
                            idstr:user.getIdstr(),
                            screen_name:user.getScreen_name(),
                            name:user.getName(),
                            province:user.getProvince(),
                            city:user.getCity(),
                            location:user.getLocation(),
                            description:user.getDescription(),
                            url:user.getUrl(),
                            profile_image_url:user.getProfile_image_url(),
                            profile_url:user.getProfile_url(),
                            domain:user.getDomain(),
                            weihao:user.getWeihao(),
                            gender:user.getGender(),
                            followers_count:user.getFollowers_count(),
                            friends_count:user.getFriends_count(),
                            statuses_count:user.getStatuses_count(),
                            favourites_count:user.getFavourites_count(),
                            created_at:user.getCreated_at(),
                            following:user.getFollowing(),
                            allow_all_act_msg:user.getAllow_all_act_msg(),
                            geo_enabled:user.getGeo_enabled(),
                            verified:user.getVerified(),
                            verified_type:user.getVerified_type(),
                            remark:user.getRemark(),
                            status:user.getStatus(),
                            allow_all_comment:user.getAllow_all_comment(),
                            avatar_large:user.getAvatar_large(),
                            avatar_hd:user.getAvatar_hd(),
                            verified_reason:user.getVerified_reason(),
                            follow_me:user.getFollow_me(),
                            online_status:user.getOnline_status(),
                            bi_followers_count:user.getBi_followers_count(),
                            lang:user.getLang(),
                            updateTime:new Date()
                    ]]))
    }
}
