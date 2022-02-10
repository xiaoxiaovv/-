package com.istar.mediabroken.entity.rubbish

/**
 * Author : YCSnail
 * Date   : 2017-11-10
 * Email  : liyancai1986@163.com
 */
class RubbishNews {

    public static final String RUBBISH_NEWS_APPROVED_NO = "N";//不认同为垃圾新闻
    public static final String RUBBISH_NEWS_APPROVED_YES = "Y";//确认为垃圾新闻
    public static final String RUBBISH_NEWS_WAITING_DEAL = "W";//待处理

    String id
    String newsId
    String simhash
    def submitter
    String approved
    Date createTime
    Date updateTime

    RubbishNews() {
        super
    }

    static RubbishNews toObject(def map){
        return new RubbishNews(
                id          : map.id ?: map._id,
                newsId      : map.newsId as String,
                simhash     : map.simhash as String,
                submitter   : map.submitter,
                approved    : map.approved,
                createTime  : map.createTime,
                updateTime  : map.updateTime
        )
    }

}
