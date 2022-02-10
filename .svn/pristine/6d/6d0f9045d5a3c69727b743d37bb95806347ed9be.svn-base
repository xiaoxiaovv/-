package com.istar.mediabroken.entity.account

/**
 * Author: Luda
 * Time: 2017/7/24
 */
class Privilege {
    String id;
    Integer masterType; //1 User 2 Role...
    String masterValue; //userId/roleId
    Integer accessType; //1 Menu 2 Button 3 function...
    String accessValue; // menuId/buttonId
    Integer operation; //0 disabled 1 enabled
    Date updateTime;
    Date createTime;

    Map<String, Object> toMap() {
        return [
                _id        : id,
                masterType : masterType,
                masterValue: masterValue,
                accessType : accessType,
                accessValue: accessValue,
                operation  : operation,
                updateTime : updateTime,
                createTime : createTime,
        ]
    }

    Privilege() {
        super
    }

    Privilege(Map map) {
        id = map._id
        masterType = map.masterType
        masterValue = map.masterValue
        accessType = map.accessType
        accessValue = map.accessValue
        operation = map.operation
        updateTime = map.updateTime
        createTime = map.createTime
    }

    @Override
    public String toString() {
        return "Privilege{" +
                "id='" + id + '\'' +
                ", masterType=" + masterType +
                ", masterValue='" + masterValue + '\'' +
                ", accessType=" + accessType +
                ", accessValue='" + accessValue + '\'' +
                ", operation=" + operation +
                ", updateTime=" + updateTime +
                ", createTime=" + createTime +
                '}';
    }

    public static final String DISALLOW_VIEW = "0" //不允许访问
    public static final String DASHBOARD_VIEW = "1" //热点推荐浏览
    public static final String NEWS_VIEW = "2" //最新资讯浏览
    public static final String SUBJECT_VIEW = "3" //全网监控浏览
    public static final String MATERIAL_EDIT = "4" //文稿编写权限
    public static final String MATERIAL_VIEW = "5" //文稿列表浏览
    public static final String ANALYSIS_VIEW = "6" //传播测评浏览
    public static final String COPYRIGHT_VIEW = "7" //版权监控浏览
    public static final String PERSONAL_MANAGE = "8" //个人信息管理
    public static final String CHANGE_PASSWORD = "23" //密码修改
    public static final String WEBSITE_MANAGE = "9" //站点设置管理
    public static final String THEME_MANAGE = "10" //主题设置管理
    public static final String SHARE_CHANNEL = "11" //同步设置管理
    public static final String HOMEPAGE_MANAGE = "12" //首页设置管理
    public static final String ACCOUNT_MANAGE = "13"  //帐号管理
    public static final String PUSHNEWS_MANAGE = "14" //推送管理
    public static final String RUBBISHNEWS_MANAGE = "15" //垃圾新闻管理
    public static final String AUTOPUSH_MANAGE = "16"  //网站和公众号自动推送设置
    public static final String WEIBOKEYWORDS_MANAGE = "17"  //微博关键词设置
    public static final String DATAREPORT = "18" //数据报表
    public static final String MATERIAL_SHARE = "19" //文稿同步
    public static final String MATERIAL_DELETE = "20" //文稿删除
    public static final String TEAM_MANAGE = "21" //分组管理
    public static final String DATASTATISTICS = "22" //数据统计
    public static final String SUBSCRIPTION_SETTING = "24" //订阅列表
    public static final String SUBSCRIPTION_EXCLUSION = "25" //否定词设置
    public static final String INSTANTNEWS_VIEW = "26" //资讯快选

}

