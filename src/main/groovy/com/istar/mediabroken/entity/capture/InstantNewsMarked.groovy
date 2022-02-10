package com.istar.mediabroken.entity.capture

class InstantNewsMarked {

    String id
    Long userId                     //用户ID
    String newsId                   //新闻ID
    String url                      //原文链接
    int newsType                   //新闻类型
    String siteName                //发布网站
    String title                   //新闻标题
    String contentAbstract        //摘要
    String content                 //文章内容  （有带格式的文章则为带格式 没有则为文章内容）
    def imgUrls                    //图片
    boolean hasImg                 //是否有图
    Date captureTime               //采集时间
    Date updateTime                //更新时间
    Date createTime                //创建时间

    Map<String, Object> toMap() {
        return [
                _id            : id ?: UUID.randomUUID().toString(),
                userId         : userId,
                newsId         : newsId,
                url            : url,
                newsType       : newsType,
                siteName       : siteName,
                title          : title,
                contentAbstract: contentAbstract,
                content        : content,
                imgUrls        : imgUrls,
                hasImg         : hasImg,
                captureTime    : captureTime,
                updateTime     : updateTime,
                createTime     : createTime
        ]
    }

    InstantNewsMarked() {
        super
    }

    InstantNewsMarked(Map map) {
        id = map._id
        userId = map.userId
        newsId = map.newsId
        url = map.url
        newsType = map.newsType
        siteName = map.siteName
        title = map.title
        contentAbstract = map.contentAbstract
        content = map.content
        imgUrls = map.imgUrls
        hasImg = map.hasImg
        captureTime = map.captureTime
        updateTime = map.updateTime
        createTime = map.createTime
    }

    @Override
    public String toString() {
        return "InstantNewsMarked{" +
                "id='" + id + '\'' +
                ", userId=" + userId +
                ", newsId='" + newsId + '\'' +
                ", url='" + url + '\'' +
                ", newsType=" + newsType +
                ", siteName='" + siteName + '\'' +
                ", title='" + title + '\'' +
                ", contentAbstract='" + contentAbstract + '\'' +
                ", content='" + content + '\'' +
                ", imgUrls=" + imgUrls +
                ", hasImg=" + hasImg +
                ", captureTime=" + captureTime +
                ", updateTime=" + updateTime +
                ", createTime=" + createTime +
                '}';
    }
}

