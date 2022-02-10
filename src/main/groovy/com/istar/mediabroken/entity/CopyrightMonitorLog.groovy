package com.istar.mediabroken.entity

class CopyrightMonitorLog {
    String monitorId
    String prevStartTime //上次的自动推送新闻开始时间（第一次推送系统时间-5分钟）
    String prevEndTime   //上次的自动推送新闻结束时间（最后一条新闻的时间）
    Date updateTime
    Date createTime
    String createId() {
        return monitorId
    }
}
