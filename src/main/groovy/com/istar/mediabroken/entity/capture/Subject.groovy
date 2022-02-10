package com.istar.mediabroken.entity.capture

class Subject {
    public static final int SUBJECT_MAX_COUNT = 20  //最大主题数

    Long userId
    String subjectId
    String subjectName  //主题名称
    Integer keywordsScope  //关键词搜索范围
    String titleKeywords //标题关键词
    String keyWords  //关键词
    String areaWords //地域词
    String excludeWords //排除词
    String pinYinPrefix //拼音
    Date updateTime
    Date createTime
    Date resetTime       //用户查看监控主题的时间，用于记录下次监控主题进入数据的开始时间
    Date countTime      // 系统统计监控主题新闻数的时间
    int count           // 系统统计的用户自上次浏览过该监控主题后，监控主题采集到的新闻数量，如果没有resetTime

    Map<String, Object> toMap() {
        return [
                _id         : subjectId ?: UUID.randomUUID().toString(),
                userId      : userId,
                subjectName : subjectName,
                keywordsScope: keywordsScope ?: KeywordsScopeEnum.contentScope.key,
                titleKeywords: titleKeywords,
                keyWords    : keyWords,
                areaWords   : areaWords,
                excludeWords: excludeWords,
                updateTime  : updateTime,
                createTime  : createTime,
                resetTime   : resetTime,
                countTime   : countTime,
                count       : count ?: 0,
                pinYinPrefix: pinYinPrefix
        ]
    }

    Subject() {
        super
    }

    Subject(Map map) {
        subjectId = map._id
        userId = map.userId
        subjectName = map.subjectName
        keywordsScope = map.keywordsScope ?: KeywordsScopeEnum.contentScope.key
        titleKeywords = map.titleKeywords
        keyWords = map.keyWords
        areaWords = map.areaWords
        excludeWords = map.excludeWords
        pinYinPrefix = map.pinYinPrefix
        updateTime = map.updateTime
        createTime = map.createTime
        resetTime = map.resetTime
        countTime = map.countTime
        count = map.count ?: 0
    }


    @Override
    public String toString() {
        return "Subject{" +
                "userId=" + userId +
                ", subjectId='" + subjectId + '\'' +
                ", subjectName='" + subjectName + '\'' +
                ", keywordsScope=" + keywordsScope +
                ", titleKeywords='" + titleKeywords + '\'' +
                ", keyWords='" + keyWords + '\'' +
                ", areaWords='" + areaWords + '\'' +
                ", excludeWords='" + excludeWords + '\'' +
                ", pinYinPrefix='" + pinYinPrefix + '\'' +
                ", updateTime=" + updateTime +
                ", createTime=" + createTime +
                ", resetTime=" + resetTime +
                ", countTime=" + countTime +
                ", count=" + count +
                '}';
    }
}
enum KeywordsScopeEnum {
    contentScope(1, '正文'), //关键词搜索范围，正文
    titleScope(2, '标题') //关键词搜索范围，标题

    private Integer key
    private String desc

    KeywordsScopeEnum (Integer key, String desc) {
        this.key = key
        this.desc = desc
    }

    Integer getKey() {
        return key
    }

    String getDesc() {
        return desc
    }
}