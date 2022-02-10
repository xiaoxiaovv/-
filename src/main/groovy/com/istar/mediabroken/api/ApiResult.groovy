package com.istar.mediabroken.api

import com.alibaba.fastjson.JSONObject
import org.apache.http.HttpStatus

class ApiResult {

    static Map apiResult(Map result) {
        def newResult = [status: HttpStatus.SC_OK]
        if (result) {
            newResult.putAll(result)
        }
        return newResult
    }

    static Map apiResult() {
        return [status: HttpStatus.SC_OK]
    }

    static Map apiResult(int status, String message) {
        return [status: status, msg: message]
    }

    static Map apiResult(int status, String message, String errorId) {
        return [status: status, msg: message, errorId: errorId]
    }

    static Map apiResult(int status, String message, String errorId, String errorMsg) {
        return [status: status, msg: message, errorId: errorId, errorMsg: errorMsg]
    }

    static Map apiResult(def obj) {
        def rep = JSONObject.toJSON(obj)
        rep.status = HttpStatus.SC_OK
        return rep
    }
    static Map apiResult(int status, def obj) {
        def rep = JSONObject.toJSON(obj)
        rep.status = status
        return rep
    }

}
