package com.istar.mediabroken.entity.evaluate

/**
 * @author hanhui
 * @date 2018/6/21 14:23
 * @desc 渠道周数据详情
 * */
class SiteInfoWeekly {
    String id
    Date startTime //起始统计日期
    Date endTime //截至统计日期
    String siteDomain //站点域名
    Long siteType //站点类型
    Long publishCount //本周累计发稿发稿量
    Long sumRead //本周累计发稿阅读数
    Double avgRead //本周发稿单篇平均阅读
    Long sumReprint //本周发稿转载总量
    Double avgReprint //平均转载
    Long sumComm //本周发稿评论总数
    Double avgComm //本周发稿单篇平均阅读
    Long sumLike //本周发稿点赞总数
    Long avgLike //本周发稿平均点赞数
    Date recentPublishTime //最后发文时间
    Integer status //渠道状态  1 ：活跃    2：不活跃  3：异常
    Date updateTime
    Date createTime


    SiteInfoWeekly() {
        super
    }

    Map<String, Object> toMap() {
        return [
                _id              : id,
                startTime        : startTime,
                endTime          : endTime,
                siteDomain       : siteDomain,
                siteType         : siteType,
                publishCount     : publishCount,
                sumRead          : sumRead,
                avgRead          : avgRead,
                sumReprint       : sumReprint,
                avgReprint       : avgReprint,
                sumComm          : sumComm,
                avgComm          : avgComm,
                sumLike          : sumLike,
                avgLike          : avgLike,
                recentPublishTime: recentPublishTime,
                status           : status,
                updateTime       : updateTime,
                createTime       : createTime
        ]
    }

    SiteInfoWeekly(Map map) {
        id = map._id
        startTime = map.startTime
        endTime = map.endTime
        siteDomain = map.siteDomain
        siteType = map.siteType
        publishCount = map.publishCount
        sumRead = map.sumRead
        avgRead = map.avgRead
        sumReprint = map.sumReprint
        avgReprint = map.avgReprint
        sumComm = map.sumComm
        avgComm = map.avgComm
        sumLike = map.sumLike
        avgLike = map.avgLike
        recentPublishTime = map.recentPublishTime
        status = map.status
        updateTime = map.updateTime
        createTime = map.createTime
    }
}
/**
 * @author zxj
 * @create 2018/7/9
 */
enum UserStatusEnum {
    active(1, '活跃中'), //status 1
    negative(2, '不活跃'), //status 2
    unusual(3, '异常'), //status 3
    private Integer key
    private String desc

    UserStatusEnum(Integer key, String desc) {
        this.key = key
        this.desc = desc
    }

    Integer getKey() {
        return key
    }

    String getDesc() {
        return desc
    }

    public Map toMap() {
        Map map = new HashMap();
        map.put("status", key);
        map.put("desc", desc);
        return map;
    }
}