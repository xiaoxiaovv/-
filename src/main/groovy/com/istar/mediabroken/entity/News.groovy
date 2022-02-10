package com.istar.mediabroken.entity

class News {
    // 新闻内容
    String title
    String source
    String author
    String keyword
    String url
    String newsId
    String content
    String contentAbstract
    Date createTime
    String site
    int newsType
    String simhash
    int reprintCount
    String imgUrl
    String orientation
    String keywords
    def viewpoint

    int getHeat() {
        return caculateHeat(reprintCount)
    }

    static News toObject(def map) {
        return new News([
                title          : map.title,
                source         : map.source,
                author         : map.author,
                keyword        : map.keyword,
                url            : map.url,
                newsId         : map.newsId,
                content        : map.content,
                contentAbstract: map.abstract,
                site           : map.site,
                createTime     : map.createTime,
                viewpoint      : map.viewpoint
        ])
    }

    static int caculateHeat(int v) {
        if (v < 11) {
            return 2
        } else if (10 < v && v < 21) {
            return 4
        } else if (20 < v && v < 31) {
            return 6
        } else if (30 < v && v < 41) {
            return 8
        } else {
            return 10
        }
    }


    @Override
    public String toString() {
        return "News{" +
                "title='" + title + '\'' +
                ", source='" + source + '\'' +
                ", author='" + author + '\'' +
                ", keyword='" + keyword + '\'' +
                ", url='" + url + '\'' +
                ", newsId='" + newsId + '\'' +
                ", content='" + content + '\'' +
                ", contentAbstract='" + contentAbstract + '\'' +
                ", createTime=" + createTime +
                ", site='" + site + '\'' +
                ", newsType=" + newsType +
                ", simhash='" + simhash + '\'' +
                ", reprintCount=" + reprintCount +
                ", imgUrl='" + imgUrl + '\'' +
                ", orientation='" + orientation + '\'' +
                ", keywords='" + keywords + '\'' +
                '}';
    }
}
