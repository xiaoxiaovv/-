package com.istar.mediabroken.utils

import com.alibaba.fastjson.JSONObject

class VarUtil {
    static Object getValue(Object value, Object defaultValue) {
        return value == null ? defaultValue : value
    }

    static Map toMap(Object obj) {
        return JSONObject.toJSON(obj)
    }
}
