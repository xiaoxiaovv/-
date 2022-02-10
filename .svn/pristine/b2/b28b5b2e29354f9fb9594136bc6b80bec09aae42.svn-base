package com.istar.mediabroken.entity.capture


class NewsOperation {
    String _id;
    String newsId;
    def news;
    int pushType;
    int status;
    String siteId;
    String orgId;
    Long userId;
    Date updateTime;
    Date createTime;
    int operationType; // 1 新闻推送 2 新闻同步 3 新闻收藏 4 word导入

    def shareChannel;    //分享的渠道信息
    def shareContent;

    String agentId
    String groupId  // 用户收藏文稿时的分组ID
    String teamId //用户所属的组ID
    Boolean isAutoPush //用户手动推送false 自动推送true

    def shareResult
    String timeStamp
    int shareChannelCount
    int weiboChannel
    int wechatChannel
    int toutiaoChannel
    int qqomChannel

    String teamName
    String publisher

    NewsOperation() {
        super
    }

    NewsOperation(long userId, String orgId, String newsId) {
        this.userId = userId
        this.orgId = orgId
        this.newsId = newsId
    }

    String createId() {
        return UUID.randomUUID().toString()
    }

    static NewsOperation toObject(def map) {
        return new NewsOperation(
                _id: map._id,
                newsId: map.newsId,
                news: map.news,
                pushType: map.pushType,
                status: map.status,
                siteId: map.siteId,
                agentId: map.agentId,
                orgId: map.orgId,
                userId: map.userId,
                updateTime: map.updateTime,
                createTime: map.createTime,
                operationType: map.operationType,
                shareChannel: map.shareChannel,
                shareContent: map.shareContent,
                groupId: map.groupId,
                teamId: map.teamId,
                isAutoPush: map.isAutoPush,
                shareResult: map.shareResult,
                timeStamp: map.timeStamp,
                shareChannelCount: map.shareChannelCount,
                weiboChannel: map.weiboChannel,
                wechatChannel: map.wechatChannel,
                toutiaoChannel: map.toutiaoChannel,
                qqomChannel: map.qqomChannel,
                teamName: map.teamName,
                publisher: map.publisher
        )
    }

    Map<String, Object> toMap() {
        return [
                _id              : _id,
                newsId           : newsId,
                news             : news,
                pushType         : pushType,
                status           : status,
                siteId           : siteId,
                agentId          : agentId,
                orgId            : orgId,
                userId           : userId,
                updateTime       : updateTime,
                createTime       : createTime,
                operationType    : operationType,
                shareChannel     : shareChannel,
                shareContent     : shareContent,
                groupId          : groupId,
                teamId           : teamId,
                isAutoPush       : isAutoPush,
                shareResult      : shareResult,
                timeStamp        : timeStamp,
                shareChannelCount: shareChannelCount,
                weiboChannel     : weiboChannel,
                wechatChannel    : wechatChannel,
                toutiaoChannel   : toutiaoChannel,
                qqomChannel      : qqomChannel,
                teamName: teamName,
                publisher: publisher
        ]
    }

    NewsOperation(Map map) {
        _id = map._id
        newsId = map.newsId
        news = map.news
        pushType = map.pushType ?: 0
        status = map.status ?: 0
        siteId = map.siteId
        agentId = map.agentId
        orgId = map.orgId
        userId = map.userId
        updateTime = map.updateTime
        createTime = map.createTime
        operationType = map.operationType ?: 0
        shareContent = map.shareContent
        groupId = map.groupId
        teamId = map.teamId
        isAutoPush = map.isAutoPush
        shareResult = map.shareResult
        timeStamp = map.timeStamp
        shareChannelCount = map.shareChannelCount ?: 1
        weiboChannel = map.weiboChannel ?: 0
        wechatChannel = map.wechatChannel ?: 0
        toutiaoChannel = map.toutiaoChannel ?: 0
        qqomChannel = map.qqomChannel ?: 0
        teamName=map.teamName
        publisher=map.publisher
    }

    @Override
    public String toString() {
        return "NewsOperation{" +
                "_id='" + _id + '\'' +
                ", newsId='" + newsId + '\'' +
                ", news=" + news +
                ", pushType=" + pushType +
                ", status=" + status +
                ", siteId='" + siteId + '\'' +
                ", orgId='" + orgId + '\'' +
                ", userId=" + userId +
                ", updateTime=" + updateTime +
                ", createTime=" + createTime +
                ", operationType=" + operationType +
                ", shareChannel=" + shareChannel +
                ", shareContent=" + shareContent +
                ", agentId='" + agentId + '\'' +
                ", groupId='" + groupId + '\'' +
                ", teamId='" + teamId + '\'' +
                ", isAutoPush=" + isAutoPush +
                ", shareResult=" + shareResult +
                ", timeStamp='" + timeStamp + '\'' +
                ", shareChannelCount=" + shareChannelCount +
                ", weiboChannel=" + weiboChannel +
                ", wechatChannel=" + wechatChannel +
                ", toutiaoChannel=" + toutiaoChannel +
                ", qqomChannel=" + qqomChannel +
                ", teamName='" + teamName + '\'' +
                ", publisher='" + publisher + '\'' +
                '}';
    }
}
