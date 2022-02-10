package com.istar.mediabroken.entity.evaluate

/**
 * @author hanhui
 * @date 2018/6/21 14:23
 * @desc 测评概览相关基础信息
 * */
class ChannelSummary {
    String id
    String evaluateId
    String evaluateName
    String channelsName
    Long channelCount
    Long articleCount
    Double multiple
    Double psi
    Double mii
    Double bsi
    Double tsi
    String multipleRate //综合指数周增长率
    String psiRate
    String miiRate
    String bsiRate
    String tsiRate
    Date startTime
    Date endTime
    Date updateTime
    Date createTime

    ChannelSummary(){
        super
    }
    Map<String, Object> toMap() {
        return [
                _id         : id,
                evaluateId  : evaluateId,
                evaluateName: evaluateName,
                channelsName: channelsName,
                channelCount: channelCount,
                articleCount: articleCount,
                multiple    : multiple,
                psi         : psi,
                mii         : mii,
                bsi         : bsi,
                tsi         : tsi,
                multipleRate: multipleRate,
                psiRate     : psiRate,
                miiRate     : miiRate,
                bsiRate     : bsiRate,
                tsiRate     : tsiRate,
                startTime   : startTime,
                endTime     : endTime,
                updateTime  : updateTime,
                createTime  : createTime
        ]
    }

    ChannelSummary(Map map) {
        id = map._id
        evaluateId = map.evaluateId
        evaluateName = map.evaluateName
        channelsName = map.channelsName
        channelCount = map.channelCount
        articleCount = map.articleCount
        multiple = map.multiple
        psi = map.psi
        mii = map.mii
        bsi = map.bsi
        tsi = map.tsi
        multipleRate = map.multipleRate
        psiRate = map.psiRate
        miiRate = map.miiRate
        bsiRate = map.bsiRate
        tsiRate = map.tsiRate
        startTime = map.startTime
        endTime = map.endTime
        createTime = map.createTime
        updateTime = map.updateTime
    }

    ChannelSummary(String id, String evaluateId, String evaluateName, String channelsName, Long channelCount,
                   Long articleCount, Double multiple, Double psi, Double mii, Double bsi, Double tsi,
                   String multipleRate, String psiRate, String miiRate, String bsiRate, String tsiRate,
                   Date startTime, Date endTime, Date updateTime, Date createTime) {
        this.id = id
        this.evaluateId = evaluateId
        this.evaluateName = evaluateName
        this.channelsName = channelsName
        this.channelCount = channelCount
        this.articleCount = articleCount
        this.multiple = multiple
        this.psi = psi
        this.mii = mii
        this.bsi = bsi
        this.tsi = tsi
        this.multipleRate = multipleRate
        this.psiRate = psiRate
        this.miiRate = miiRate
        this.bsiRate = bsiRate
        this.tsiRate = tsiRate
        this.startTime = startTime
        this.endTime = endTime
        this.updateTime = updateTime
        this.createTime = createTime
    }

}
