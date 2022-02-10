package com.istar.mediabroken.entity.evaluate

/**
 * @author zxj
 * @create 2018/6/20
 * @desc EvaluateChannel 存的是渠道信息 dailyFourPower每天四力  weeklyFourPower 每周四力
 */
class EvaluateChannel {
    String id
    long userId
    String siteName
    String siteDomain
    int siteType
    String evaluateTeamId
    Date updateTime
    Date createTime

    //channelForStat 表单独使用的字段
    int statFlag//1 需要全部统计 2 部分统计（除去收费字段）

    //关于四力
    Date time
    double psi//传播力
    double mii//影响力
    double tsi//公信力
    double bsi//引导力
    double multiple//综合指数
    //设置权重
    def psiWeight
    def miiWeight
    def tsiWeight
    def bsiWeight
    def multipleWeight
    //每天的参数
    long publishCount
    long sumReprint
    double avgReprint
    long sumRead
    double avgRead
    long sumLike
    double avgLike
    //词云
    List wordCloud
    EvaluateChannel(Map map) {
        id = map._id
        userId = map.userId ?: 0L
        siteName = map.siteName
        siteDomain = map.siteDomain
        siteType = map.siteType
        evaluateTeamId = map.evaluateTeamId
        updateTime = map.updateTime
        createTime = map.createTime
        time = map.time
        psi = map.psi ?: 0.00
        mii = map.mii ?: 0.00
        tsi = map.tsi ?: 0.00
        bsi = map.bsi ?: 0.00
        multiple = map.multiple ?: 0.00
        psiWeight = map.psiWeight
        miiWeight = map.miiWeight
        tsiWeight = map.tsiWeight
        bsiWeight = map.bsiWeight
        multipleWeight = map.multipleWeight
        //
        publishCount = map.publishCount ?: 0L
        sumReprint = map.sumReprint ?: 0L
        avgReprint = map.avgReprint ?: 0.00
        sumRead = map.sumRead ?: 0L
        avgRead = map.avgRead ?: 0.00
        sumLike = map.sumLike ?: 0L
        avgLike = map.avgLike ?: 0.00
        wordCloud = map.wordCloud ?:[]
    }


    EvaluateChannel() {
    }

    EvaluateChannel(String id, long userId, String siteName, String siteDomain, int siteType, String evaluateTeamId, Date updateTime, Date createTime) {
        this.id = id
        this.userId = userId ?: 0L
        this.siteName = siteName
        this.siteDomain = siteDomain
        this.siteType = siteType
        this.evaluateTeamId = evaluateTeamId
        this.updateTime = updateTime
        this.createTime = createTime
    }


}
