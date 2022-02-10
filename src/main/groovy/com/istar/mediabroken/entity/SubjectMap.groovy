package com.istar.mediabroken.entity

class SubjectMap {
    String subjectId
    String yqmsSubjectId
    Long yqmsUserId

    // 以下几个用来定位专题
    int subjectType
    String yqmsSubjectName
    String websiteDomain
    String channelDomain
    String focusKeywords                 // 关注词,特殊关注需要用到
    String account

    Date createTime


}
