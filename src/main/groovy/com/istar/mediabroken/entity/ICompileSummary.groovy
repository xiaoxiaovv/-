package com.istar.mediabroken.entity

import com.istar.mediabroken.api3rd.YqmsSession

class ICompileSummary {

    String summaryId   // _id
    long userId

    String yqmsTopicId
    long yqmsUserId
    Date startTime
    Date endTime

    YqmsSession session

    String title
    String time
    String place
    String person
    String event
    String ambiguous


    String data // 存放数据
    int template
    Date createTime
    Date updateTime

    // 计算最终的关键字组合
    String getKeywords() {
        def set = new HashSet()
        place.split(/\s+/).each {
            set << it
        }
        person.split(/\s+/).each {
            set << it
        }
        event.split(/\s+/).each {
            set << it
        }
        return set.toArray().join(' ')
    }
    boolean isEnoughInfo(){
        def set = new HashSet()
        place.split(/\s+/).each {
            set << it
        }
        person.split(/\s+/).each {
            set << it
        }
        event.split(/\s+/).each {
            set << it
        }
        set.remove("")
        return set.size() >= 3
    }
    boolean equals(ICompileSummary summary) {
        return (this.userId == summary.userId && this.title.equals(summary.title)
        && this.place.equals(summary.place)
        && this.event.equals(summary.event) && this.ambiguous.equals(summary.ambiguous)
        && this.startTime.equals(summary.startTime) && this.endTime.equals(summary.endTime)
        && this.person.equals(summary.person))
    }
}
