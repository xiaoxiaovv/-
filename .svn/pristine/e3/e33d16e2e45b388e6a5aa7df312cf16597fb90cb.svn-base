import com.alibaba.fastjson.JSONObject

def setting = [
        _id     : UUID.randomUUID().toString(),
        type    : 'wechat',
        key     : 'massSendOrPreview',
        content : [
                massSend    : true,
                wechatName  : 'flame_Liyc'
        ],
        description : '微信公众号群发设置',
        "updateTime" : 'ISODate("2017-09-12T16:00:00.000Z")',
        "createTime" : 'ISODate("2017-09-12T16:00:00.000Z")'
]


println JSONObject.toJSONString(setting)