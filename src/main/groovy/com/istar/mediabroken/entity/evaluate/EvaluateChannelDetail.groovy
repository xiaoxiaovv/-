package com.istar.mediabroken.entity.evaluate

/**
 * @author zxj
 * @create 2018/6/22
 * @desc 存放加权后的四力和综合指数（可以作为报表的表）
 * EvaluateChannelDetail表存放是一周的信息 EvaluateChannelDaily存放每天的新闻
 */
class EvaluateChannelDetail {

    String id
    long userId
    String channelId
    long channelCount
    long contentCount
    String siteDomain
    int siteType
    String siteName
    String evaluateId
    String evaluateName
    Date startTime
    Date endTime
    Date time
    double psi
    double mii
    double tsi
    double bsi
    double multiple
    double psiRate
    double miiRate
    double tsiRate
    double bsiRate
    double multipleRate
    Date updateTime
    Date createTime
    //EvaluateChannelDaily存放的字段
    long publishCount
    long sumReprint
    double avgReprint
    long sumRead
    double avgRead
    long sumLike
    double avgLike

    EvaluateChannelDetail() {
        super
    }

    EvaluateChannelDetail(Map map) {
        id = map._id
        userId = map.userId ?: 0L
        channelId = map.channelId
        channelCount = map.channelCount ?: 0L
        contentCount = map.contentCount ?: 0L
        siteDomain = map.siteDomain
        siteType = map.siteType
        siteName = map.siteName
        evaluateId = map.evaluateId
        evaluateName = map.evaluateName
        startTime = map.startTime
        endTime = map.endTime
        time = map.time
        psi = map.psi ?: 0.00
        mii = map.mii ?: 0.00
        tsi = map.tsi ?: 0.00
        bsi = map.bsi ?: 0.00
        multiple = map.multiple ?: 0.00
        psiRate = map.psiRate ?: 0.00
        miiRate = map.miiRate ?: 0.00
        tsiRate = map.tsiRate ?: 0.00
        bsiRate = map.bsiRate ?: 0.00
        multipleRate = map.multipleRate ?: 0.00
        updateTime = map.updateTime
        createTime = map.createTime
        //
        publishCount = map.publishCount ?: 0L
        sumReprint = map.sumReprint ?: 0L
        avgReprint = map.avgReprint ?: 0.00
        sumRead = map.sumRead ?: 0L
        avgRead = map.avgRead ?: 0.00
        sumLike = map.sumLike ?: 0L
        avgLike = map.avgLike ?: 0.00
    }

    EvaluateChannelDetail(String id, long userId, String channelId, long channelCount, long contentCount, String siteDomain,
                          int siteType, String siteName, String evaluateId, String evaluateName, Date startTime,
                          Date endTime, Date time, double psi, double mii, double tsi, double bsi, double multiple,
                          double psiRate, double miiRate, double tsiRate, double bsiRate, double multipleRate,
                          Date updateTime, Date createTime) {
        this.id = id
        this.userId = userId
        this.channelId = channelId
        this.channelCount = channelCount
        this.contentCount = contentCount
        this.siteDomain = siteDomain
        this.siteType = siteType
        this.siteName = siteName
        this.evaluateId = evaluateId
        this.evaluateName = evaluateName
        this.startTime = startTime
        this.endTime = endTime
        this.time = time
        this.psi = psi
        this.mii = mii
        this.tsi = tsi
        this.bsi = bsi
        this.multiple = multiple
        this.psiRate = psiRate
        this.miiRate = miiRate
        this.tsiRate = tsiRate
        this.bsiRate = bsiRate
        this.multipleRate = multipleRate
        this.updateTime = updateTime
        this.createTime = createTime
    }
}
