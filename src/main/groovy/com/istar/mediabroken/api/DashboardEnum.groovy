package com.istar.mediabroken.api

/**
 * Author : YCSnail
 * Date   : 2017-12-07
 * Email  : liyancai1986@163.com
 */
enum DashboardEnum {

    highlightNews('highlightNews', '首页重点新闻'),
    hotNews('hotNews', '热点新闻'),
    subjectNews('subjectNews', '全网监控主题新闻'),
    weiboNews('weiboNews', '微博'),
    wechatNews('wechatNews', '微信'),
    riseRateMonitor('riseRateMonitor', '上升率监控'),
    reprintMediaMonitor('reprintMediaMonitor', '转载媒体监控'),
    weiboKeyWords('weiboKeyWords', '微博查询关键词')


    private String key
    private String desc

    DashboardEnum(String key, String desc) {
        this.key = key
        this.desc = desc
    }

    String getKey() {
        return key
    }

    String getDesc() {
        return desc
    }
}