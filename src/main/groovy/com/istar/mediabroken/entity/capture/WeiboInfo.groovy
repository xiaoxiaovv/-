package com.istar.mediabroken.entity.capture

/**
 * Created by Zc on 2017/12/19.
 */
class WeiboInfo {
    long _id
    def id    //int64 	用户UID
    def idstr    //string 	字符串型的用户UID
    def screen_name    //string 	用户昵称
    def name    //string 	友好显示名称
    def province    //int 	用户所在省级ID
    def city    //int 	用户所在城市ID
    def location    //string 	用户所在地
    def description    //string 	用户个人描述
    def url    //string 	用户博客地址
    def profile_image_url    //string 	用户头像地址（中图），50×50像素
    def profile_url    //string 	用户的微博统一URL地址
    def domain    //string 	用户的个性化域名
    def weihao    //string 	用户的微号
    def gender    //string 	性别，m：男、f：女、n：未知
    def followers_count    //int 	粉丝数
    def friends_count    //int 	关注数
    def statuses_count    //int 	微博数
    def favourites_count    //int 	收藏数
    def created_at    //string 	用户创建（注册）时间
    def following    //boolean 	暂未支持
    def allow_all_act_msg    //boolean 	是否允许所有人给我发私信，true：是，false：否
    def geo_enabled    //boolean 	是否允许标识用户的地理位置，true：是，false：否
    def verified    //boolean 	是否是微博认证用户，即加V用户，true：是，false：否
    def verified_type    //int 	暂未支持
    def remark    //string 	用户备注信息，只有在查询用户关系时才返回此字段
    def status    //object 	用户的最近一条微博信息字段 详细
    def allow_all_comment    //boolean 	是否允许所有人对我的微博进行评论，true：是，false：否
    def avatar_large    //string 	用户头像地址（大图），180×180像素
    def avatar_hd    //string 	用户头像地址（高清），高清头像原图
    def verified_reason    //string 	认证原因
    def follow_me    //boolean 	该用户是否关注当前登录用户，true：是，false：否
    def online_status    //int 	用户的在线状态，0：不在线、1：在线
    def bi_followers_count    //int 	用户的互粉数
    def lang    //string 	用户当前的语言版本，zh-cn：简体中文，zh-tw：繁体中文，en：英语
    Date createTime
    Date updateTime

    Map<String, Object> toMap() {
        return [
                _id               : UUID.randomUUID().toString(),
                id                : id,
                idstr             : idstr ?: "",
                screen_name       : screen_name ?: "",
                name              : name,
                province          : province,
                city              : city,
                location          : location,
                description       : description,
                url               : url,
                profile_image_url : profile_image_url,
                profile_url       : profile_url,
                domain            : domain ?: "",
                weihao            : weihao ?: "",
                gender            : gender ?: "",
                followers_count   : followers_count,
                friends_count     : friends_count,
                statuses_count    : statuses_count,
                favourites_count  : favourites_count,
                created_at        : created_at,
                following         : following,
                allow_all_act_msg : allow_all_act_msg,
                geo_enabled       : geo_enabled,
                verified          : verified,
                verified_type     : verified_type,
                remark            : remark,
                status            : status,
                allow_all_comment : allow_all_comment,
                avatar_large      : avatar_large,
                avatar_hd         : avatar_hd,
                verified_reason   : verified_reason,
                follow_me         : follow_me,
                online_status     : online_status,
                online_status     : online_status,
                bi_followers_count: bi_followers_count,
                lang              : lang,
                createTime        : createTime,
                updateTime        : updateTime
        ]
    }

    WeiboInfo(Map map) {
        id = map.id
        idstr = map.idstr
        screen_name = map.screen_name
        name = map.name
        province = map.province
        city = map.city
        location = map.location
        description = map.description
        url = map.url
        profile_image_url = map.profile_image_url
        profile_url = map.profile_url
        domain = map.domain
        weihao = map.weihao
        gender = map.gender
        followers_count = map.followers_count
        friends_count = map.friends_count
        statuses_count = map.statuses_count
        favourites_count = map.favourites_count
        created_at = map.created_at
        following = map.following
        allow_all_act_msg = map.allow_all_act_msg
        geo_enabled = map.geo_enabled
        verified = map.verified
        verified_type = map.verified_type
        remark = map.remark
        status = map.status
        allow_all_comment = map.allow_all_comment
        avatar_large = map.avatar_large
        avatar_hd = map.avatar_hd
        verified_reason = map.verified_reason
        follow_me = map.follow_me
        online_status = map.online_status
        online_status = map.online_status
        bi_followers_count = map.bi_followers_count
        lang = map.lang
        createTime = map.createTime
        updateTime = map.updateTime
    }


    @Override
    public String toString() {
        return "WeiboInfo{" +
                "_id=" + _id +
                ", id=" + id +
                ", idstr=" + idstr +
                ", screen_name=" + screen_name +
                ", name=" + name +
                ", province=" + province +
                ", city=" + city +
                ", location=" + location +
                ", description=" + description +
                ", url=" + url +
                ", profile_image_url=" + profile_image_url +
                ", profile_url=" + profile_url +
                ", domain=" + domain +
                ", weihao=" + weihao +
                ", gender=" + gender +
                ", followers_count=" + followers_count +
                ", friends_count=" + friends_count +
                ", statuses_count=" + statuses_count +
                ", favourites_count=" + favourites_count +
                ", created_at=" + created_at +
                ", following=" + following +
                ", allow_all_act_msg=" + allow_all_act_msg +
                ", geo_enabled=" + geo_enabled +
                ", verified=" + verified +
                ", verified_type=" + verified_type +
                ", remark=" + remark +
                ", status=" + status +
                ", allow_all_comment=" + allow_all_comment +
                ", avatar_large=" + avatar_large +
                ", avatar_hd=" + avatar_hd +
                ", verified_reason=" + verified_reason +
                ", follow_me=" + follow_me +
                ", online_status=" + online_status +
                ", bi_followers_count=" + bi_followers_count +
                ", lang=" + lang +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
