package com.istar.mediabroken.service.ecloud

/**
 * Author : YCSnail
 * Date   : 2018-04-25
 * Email  : liyancai1986@163.com
 */
enum EcloudErrorCodeEnum {

    ERROR(500, '服务方未定义异常'),
    SIGN_ERROR(501, 'sign的值错误'),
    PRODUCTCODE_NOT_EXIST(502, 'productcode不存在'),
    CODE_NOT_EXIST(503, '业务编码不存在'),
    PRODUCTPARAS_ERROR(504, '产品参数错误'),
    SERVICEPARAS_ERROR(505, '业务参数错误'),
    ECORDERCODE_NOT_EXIST(506, '企业应用订购编码不存在（变更时）'),
    CUST_NOT_EXIST(507, '客户不存在（变更时）')

    private int code
    private String msg

    EcloudErrorCodeEnum(int code, String msg) {
        this.code = code
        this.msg = msg
    }

    int getCode() {
        return code
    }

    String getMsg() {
        return msg
    }

    Map toMap(){
        return [
                code    : this.code,
                msg     : this.msg
        ]
    }

}