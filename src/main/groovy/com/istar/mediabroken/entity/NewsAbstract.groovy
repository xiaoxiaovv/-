package com.istar.mediabroken.entity

import com.istar.mediabroken.utils.Md5Util
import org.apache.commons.lang3.time.DateFormatUtils;


// 要闻摘要
public class NewsAbstract {
    String abstractId  // _id, uuid

    // 新闻内容
    String title //文章标题
    String author //编辑
    String picUrl //图片
    String content //内容
    List newsDetail //原文信息
    List imgs //原文图片
    String contentAbstract //摘要
    String keyWords //关键词
    String classification //新闻分类
    String source //新闻来源

    String originalAuthor //原作者
    String firstPublishSite //首发网站
    Date firstPublishTime //首发时间
    String originalUrl //原文链接
    int type // 1新闻素材，2 智能组稿素材，3自建素材

    long userId
    String orgId

    Date createTime
    Date updateTime

    NewsAbstract(){
        super
    }

    NewsAbstract(long userId, String orgId) {
        this.userId = userId
        this.orgId = orgId
    }

    public String createId() {
        return Md5Util.md5((userId as String).concat(orgId).concat(System.currentTimeMillis().toString()))
    }

    Map<String, Object> toMap(){
        return [
                title               : this.title,
                author              : this.author,
                picUrl              : this.picUrl,
                content             : this.content,
                newsDetail          : this.newsDetail,
                imgs                : this.imgs,
                contentAbstract     : this.contentAbstract ? this.contentAbstract : "",
                keyWords            : this.keyWords ? this.keyWords : "",
                classification      : this.classification ? this.classification : "",
                source              : this.source ? this.source : "",
                originalAuthor      : this.originalAuthor ? this.originalAuthor : "",
                firstPublishSite    : this.firstPublishSite ? this.firstPublishSite : "",
                firstPublishTime    : this.firstPublishTime ? this.firstPublishTime : null,
                originalUrl         : this.originalUrl ? this.originalUrl : "",
                type                : this.type ? this.type : 2,
                orgId               : this.orgId,
                userId              : this.userId,
                updateTime          : this.updateTime,
                createTime          : this.createTime
        ]
    }

    static NewsAbstract toObject(def map){
        return new NewsAbstract([
                abstractId  : map._id,
                title       : map.title,
                author      : map.author,
                picUrl      : map.picUrl,
                content     : map.content,
                newsDetail  : map.newsDetail,
                imgs        : map.imgs,
                contentAbstract     : map.containsKey("contentAbstract") ? map.contentAbstract : "",
                keyWords            : map.containsKey("keyWords") ? map.keyWords : "",
                classification      : map.containsKey("classification") ? map.classification : "",
                source              : map.containsKey("source") ? map.source : "",
                originalAuthor      : map.containsKey("originalAuthor") ? map.originalAuthor : "",
                firstPublishSite    : map.containsKey("firstPublishSite") ? map.firstPublishSite : "",
                firstPublishTime    : map.containsKey("firstPublishTime") ? map.firstPublishTime : null,
                originalUrl         : map.containsKey("originalUrl") ? map.originalUrl : "",
                type                : map.containsKey("type") ? map.type : 2,
                orgId       : map.orgId,
                userId      : map.userId,
                updateTime  : map.updateTime,
                createTime  : map.createTime
        ])
    }
}
