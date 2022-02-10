package com.istar.mediabroken.entity


// 生成要闻摘要的配置信息
class AbstractSetting {

    public static final int THUMBNAIL_STRATEGY_HOT = 1
    public static final int THUMBNAIL_STRATEGY_NEW = 2

    int newsCount = 4
    int abstractLength
    boolean showThumbnail
    int thumbnailStrategy   // 默认缩略图选择策略 1. 最热, 2. 最新, 待讨论实现策略
    int thumbnailWidth   // 默认缩略图宽度
    int thumbnailHeight   // 默认缩略图高度
    int titleLength = 50
    String author
    long userId

    Map<String, Object> toMap(){
        return [
                newsCount           : this.newsCount,
                abstractLength      : this.abstractLength,
                showThumbnail       : this.showThumbnail,
                thumbnailStrategy   : this.thumbnailStrategy,
                thumbnailWidth      : this.thumbnailWidth,
                thumbnailHeight     : this.thumbnailHeight,
                titleLength         : this.titleLength,
                author              : this.author,
                userId              : this.userId
        ]
    }

    static AbstractSetting toObject(def map){
        return new AbstractSetting([
                newsCount           : map.newsCount as int,
                abstractLength      : map.abstractLength as int,
                showThumbnail       : map.showThumbnail as boolean,
                thumbnailStrategy   : map.thumbnailStrategy as int,
                thumbnailWidth      : map.thumbnailWidth as int,
                thumbnailHeight     : map.thumbnailHeight as int,
                titleLength         : map.titleLength as int,
                author              : map.author as String,
                userId              : map.userId as long
        ])
    }
}
