package com.istar.mediabroken.entity

import com.istar.mediabroken.utils.Md5Util

/**
 * Created by luda on 2017/5/10.
 */
class CaptureStat {
    String _id
    Long userId
    String siteId
    String siteName
    Integer siteType
    Long captureCount
    Long pushCount
    Long shareCount
    Date date
    Date updateTime

    public String createId() {
        return Md5Util.md5((userId as String).concat(siteId).concat(date.toString()))
    }
    Map<String, Object> toMap(){
        return [
                _id              : this._id,
                userId           : this.userId,
                siteId           : this.siteId,
                siteName         : this.siteName,
                siteType         : this.siteType,
                captureCount    : this.captureCount,
                pushCount        : this.pushCount,
                shareCount       : this.shareCount,
                date              : this.date,
                updateTime       : this.updateTime,
        ]
    }

    @Override
    public String toString() {
        return "CaptureStat{" +
                "_id='" + _id + '\'' +
                ", userId=" + userId +
                ", siteId='" + siteId + '\'' +
                ", siteName='" + siteName + '\'' +
                ", siteType=" + siteType +
                ", captureCount=" + captureCount +
                ", pushCount=" + pushCount +
                ", shareCount=" + shareCount +
                ", date=" + date +
                ", updateTime=" + updateTime +
                '}';
    }
}
