package com.istar.mediabroken.service.capture

import com.alibaba.fastjson.JSONObject
import com.istar.mediabroken.entity.capture.WeiboInfo
import com.istar.mediabroken.repo.capture.WeiboInfoRepo
import com.istar.mediabroken.utils.UrlUtils
import com.mashape.unirest.http.Unirest
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * Created by Zc on 2017/12/19.
 */
@Service
@Slf4j
class WeiboInfoService {
    @Autowired
    WeiboInfoRepo weiboInfoRepo

    @Value('${weibo.verified.url}')
    String weiboVerifiedUrl

    Map getVerifiedById(String url){
        def vflag = [verified: false, vflag: ""]
        try{
            long uid = Long.valueOf(UrlUtils.getWeiboUidFromUrl(url))
            Map map = weiboInfoRepo.getVerifiedById(uid)//从mongo库中查该微博用户信息
            if (!map){//从mongo库中不能查到该微博用户信息，需要调用微博接口查询信息，再存储到mongo库中
                def unirest = Unirest.post(weiboVerifiedUrl)
                        .field("key",uid)
                        .field("Content-Type","application/x-www-form-urlencoded")
                        .asJson()
                def res = unirest.body.object.map
                map = JSONObject.toJSON(res.user?res.user.map:res)
                WeiboInfo weiboInfo = new WeiboInfo(map)
                if (weiboInfo){
                    weiboInfo.createTime = new Date()
                    weiboInfo.updateTime = new Date()
                    String weiboId = weiboInfoRepo.addWeiboInfo(weiboInfo)
                }
            }
            if (map){//从mongo库中可以查到该微博用户信息
                if (map.verified && map.verified_type ==0){
                    vflag = [verified: map.verified, vflag: "yellow"]
                }else if (map.verified && map.verified_type !=0){
                    vflag = [verified: map.verified, vflag: "blue"]
                }
            }
        }catch (Exception e){
            return vflag
        }
        return vflag
    }
}
