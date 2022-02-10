package com.istar.mediabroken.service.weibo

import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.Unirest
import groovy.util.logging.Slf4j
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * Author: Luda
 * Time: 2017/8/19
 */
@Service
@Slf4j
class WeiboService {
    @Value('${xgsj.token}')
    String token
    @Value('${xgsj.weiboData.url}')
    String weiboInfoUrl


    public Map getWeiboInfo(List weiboUrl) {
        Map result = [:]
        try {
            StringBuffer weiboUrls = new StringBuffer("[")
            weiboUrl.each {
                weiboUrls.append("\"")
                weiboUrls.append(it.url)
                weiboUrls.append("\"")
                weiboUrls.append(",")
            }
            weiboUrls.deleteCharAt(weiboUrls.length() - 1)
            weiboUrls.append("]")
            //请求微博接口数据
            HttpResponse weiboResponse = Unirest.post(weiboInfoUrl)
                    .field("token", token)
                    .field("url", weiboUrls)
                    .asJson();
            if (weiboResponse.getStatus() != 200) {
                return
            }
            JSONObject weiboResult = weiboResponse.body.object
            //把微博数据和实际数据对应
            log.info("weiboResult:{}", weiboResult)
            if (weiboResult.has("status") && weiboResult.status == 0) {
                weiboResult.data.each {
                    result.put(it.url,["comments":it.comments,"reposts":it.reposts,"attitudes":it.attitudes])
                }
            }
        }
        catch (
                Exception e
                ) {
            log.error("更新微博信息错误:" + e.getMessage(), e)
            return null
        } finally {
            return result
        }
    }

}
