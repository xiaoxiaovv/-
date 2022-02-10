package com.istar.mediabroken.entity.compile

/**
 * Author: Luda
 * Time: 2017/8/1
 */
class Material {

    String id  // _id, uuid
    String title //文章标题
    String author //编辑
    String content //内容
    String contentAbstract //摘要
    String keywords //关键词
    String classification //新闻分类
    String source //新闻来源
    String picUrl //图片
    int type // 1新闻素材，2 智能组稿素材，3自建素材
    int isPublished //  同步状态分为 1同步成功 ==true 、2未同步 ==false、4同步失败
    boolean isReleased //H5 发布
    String articleId
    long userId
    String orgId
    List tags
    List customTags
    def channelResult
    Date createTime
    Date updateTime


    Map<String, Object> toMap() {

        return [
                _id            : id ?: UUID.randomUUID().toString(),
                title          : title,
                author         : author,
                content        : content,
                contentAbstract: contentAbstract,
                keywords       : keywords,
                classification : classification,
                source         : source,
                picUrl         : picUrl,
                type           : type ?: 3,
                isPublished    : getPublishedStatus(isPublished),
                isReleased     : isReleased ?: false,
                articleId      : articleId,
                userId         : userId,
                orgId          : orgId,
                tags           : tags,
                customTags     : customTags,
                channelResult  : channelResult ?: [],
                updateTime     : updateTime,
                createTime     : createTime
        ]
    }

    //同步状态分为 1同步成功 ==true 、2未同步 ==false、4同步失败
    private int getPublishedStatus(def publish) {
        int isPublished = 2
        if (publish == true || publish == 1) {
            isPublished = 1;
        } else if (publish == null || publish == false || publish == 2) {
            isPublished = 2;
        } else if (publish == 4) {
            isPublished = 4;
        }
        return isPublished
    }
    Material() {
        super
    }

    Material(Map map) {
        id = map._id ?: map.id ?: ""
        title = map.title ?: ""
        author = map.author ?: ""
        content = map.content ?: ""
        contentAbstract = map.contentAbstract ?: ""
        keywords = map.keywords ?: map.keyWords ?: ""
        classification = map.classification ?: ""
        source = map.source ?: ""
        picUrl = map.picUrl ?: ""
        type = map.type ?: 0
        isPublished = getPublishedStatus(map.isPublished)
        isReleased = map.isReleased ?: false
        articleId = map.articleId ?: map.articleId ?: ""
        userId = map.userId ?: 0L
        orgId = map.orgId ?: "0"
        tags = map.tags ?: []
        customTags = map.customTags ?: []
        channelResult = map.channelResult ?: []
        createTime = map.createTime ?: new Date()
        updateTime = map.updateTime ?: new Date()

    }


    @Override
    public String toString() {
        return "Material{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", content='" + content + '\'' +
                ", contentAbstract='" + contentAbstract + '\'' +
                ", keywords='" + keywords + '\'' +
                ", classification='" + classification + '\'' +
                ", source='" + source + '\'' +
                ", picUrl='" + picUrl + '\'' +
                ", type=" + type +
                ", isPublished=" + isPublished +
                ", isReleased=" + isReleased +
                ", articleId='" + articleId + '\'' +
                ", userId=" + userId +
                ", orgId='" + orgId + '\'' +
                ", tags=" + tags +
                ", customTags=" + customTags +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}'
    }
}
