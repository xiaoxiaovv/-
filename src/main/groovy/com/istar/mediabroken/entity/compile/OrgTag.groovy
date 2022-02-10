package com.istar.mediabroken.entity.compile
/**
 * @author zxj
 * @create 2019/1/17
 * @desc orgTag
 */
class OrgTag {

    String id  // _id, mad(orgId + 一级分类名 +二级分类名 +。。。 )
    String parentId //文章标题
    String orgId //机构id
    int level
    boolean leaf
    String code //1000 开始
    String parentCode //父类的code
    String userTagName //用户分类名称
    String userTagId //用户的分类id
    Date updateTime
    Date createTime

    List subTagList //子类节点 （只是为了前端展示方便）
    boolean isHave //用户是否设置过这个栏目

    Map<String, Object> toMap() {
        return [
                _id        : id,
                parentId   : parentId,
                orgId      : orgId,
                level      : level,
                leaf       : leaf,
                code       : code,
                parentCode : parentCode,
                userTagName: userTagName,
                userTagId  : userTagId,
                updateTime : updateTime,
                createTime : createTime,
        ]
    }

    OrgTag() {
        super
    }

    OrgTag(Map map) {
        id = map._id ?: map.id ?: ""
        parentId = map.parentId ?: ""
        orgId = map.orgId ?: ""
        level = map.level ?: ""
        leaf = map.leaf ?: false
        code = map.code ?: ""
        parentCode = map.parentCode ?: ""
        userTagName = map.userTagName ?: ""
        userTagId = map.userTagId ?: ""
        createTime = map.createTime ?: new Date()
        updateTime = map.updateTime ?: new Date()
    }


    @Override
    public String toString() {
        return "OrgTag{" +
                "id='" + id + '\'' +
                ", parentId='" + parentId + '\'' +
                ", orgId='" + orgId + '\'' +
                ", level=" + level +
                ", leaf=" + leaf +
                ", code='" + code + '\'' +
                ", parentCode='" + parentCode + '\'' +
                ", userTagName='" + userTagName + '\'' +
                ", userTagId='" + userTagId + '\'' +
                ", subTagList=" + subTagList +
                ", isHave=" + isHave +
                ", updateTime=" + updateTime +
                ", createTime=" + createTime +
                '}';
    }
}
