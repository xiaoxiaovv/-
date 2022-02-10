package com.istar.mediabroken.entity.account

/**
 * Author: Luda
 * Time: 2017/7/24
 */
class Menu {
    String id
    Integer level //1 2 3 级菜单
    String parentId
    Integer order
    String name
    String alias
    String url
    String icon
    Integer isLeaf
    Integer isVisible
    Date updateTime
    Date createTime
    Integer type//1 左侧菜单  2 个人设置菜单
    def subList

    Map<String, Object> toMap() {
        return [
                _id       : id,
                level     : level,
                parentId  : parentId,
                order     : order,
                name      : name,
                alias     : alias,
                url       : url,
                icon      : icon,
                isLeaf    : isLeaf,
                isVisible : isVisible,
                updateTime: updateTime,
                createTime: createTime,
                type      : type,
        ]
    }

    Menu() {
        super
    }

    Menu(Map map) {
        id = map._id
        level = map.level
        parentId = map.parentId
        order = map.order
        name = map.name
        alias = map.alias
        url = map.url
        icon = map.icon
        isLeaf = map.isLeaf
        isVisible = map.isVisible
        updateTime = map.updateTime
        createTime = map.createTime
        type       = map.type
    }


    @Override
    public String toString() {
        return "Menu{" +
                "id='" + id + '\'' +
                ", level=" + level +
                ", parentId='" + parentId + '\'' +
                ", order=" + order +
                ", name='" + name + '\'' +
                ", alias='" + alias + '\'' +
                ", url='" + url + '\'' +
                ", icon='" + icon + '\'' +
                ", isLeaf=" + isLeaf +
                ", isVisible=" + isVisible +
                ", updateTime=" + updateTime +
                ", createTime=" + createTime +
                ", type='" + type + '\'' +
                '}';
    }
}
