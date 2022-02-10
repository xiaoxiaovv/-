package com.istar.mediabroken.entity.account

/**
 * Author: zc
 * Time: 2017/9/6
 */
class AccountApply {
    String id
    String name
    String company
    String position
    String mobile
    String email
    String qq
    String message
    long applyTime
    Date createTime

    Map<String, Object> toMap() {
        return [
                _id          : id ?: "",
                name     : name ?: "",
                company  : company ?: "",
                position     : position ?: "",
                mobile       : mobile ?: "",
                email     : email ?: "",
                qq      : qq ?: "",
                message   : message?:"",
                applyTime:applyTime?:0L,
                createTime   : createTime
        ]
    }

    AccountApply() {
        super
    }

    AccountApply(Map map) {
        id = map._id ?: map.id ?: 0
        name = map.name ?: ""
        company = map.company ?: ""
        position = map.position ?: ""
        mobile = map.mobile ?: ""
        email = map.email ?: ""
        qq = map.qq?:""
        message = map.message ?: ""
        applyTime = map.applyTime
        createTime = map.createTime
    }


    @Override
    public String toString() {
        return "AccountApply{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", company='" + company + '\'' +
                ", position='" + position + '\'' +
                ", mobile='" + mobile + '\'' +
                ", email='" + email + '\'' +
                ", qq='" + qq + '\'' +
                ", message='" + message + '\'' +
                ", applyTime=" + applyTime +
                ", createTime=" + createTime +
                '}';
    }
}
