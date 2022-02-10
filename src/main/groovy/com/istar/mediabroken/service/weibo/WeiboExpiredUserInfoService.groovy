package com.istar.mediabroken.service.weibo

import com.alibaba.fastjson.JSONObject
import com.istar.mediabroken.entity.capture.WeiboInfo
import com.istar.mediabroken.repo.capture.WeiboInfoRepo
import com.istar.mediabroken.utils.DateUitl
import com.mashape.unirest.http.Unirest
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
/**
 * Author: HH
 * Time: 2018/1/4
 *
 */
@Slf4j
@Service
class WeiboExpiredUserInfoService {
    @Autowired
    WeiboInfoRepo weiboInfoRepo
    @Value('${weibo.syncUserInfo.url}')
    String weiboSyncUserInfoUrl
    @Value('${weibo.verified.url}')
    String weiboVerifiedUrl
    @Value('${weibo.syncUserInfo.timeInterval}')
    int syncTimeInterval
    @Value('${weibo.updateUserInfo.timeInterval}')
    int updateTimeInterval

    void syncUserInfo() {
        def pageNo = 1
        def date = DateUitl.getDayBegin()
        Calendar calendar = Calendar.getInstance()
        calendar.setTime(date)
        calendar.add(Calendar.DAY_OF_MONTH,syncTimeInterval)
        def flag = true
        while (flag){
            def userIds = weiboInfoRepo.getExpiredUserInfo(calendar.getTime(),pageNo,50)
            if (userIds) {
                def unirest = Unirest.post(weiboSyncUserInfoUrl)
                        .field("weibo_id_list", userIds)
                        .field("Content-Type", "application/x-www-form-urlencoded")
                        .asJson()
                def resultData = unirest.getBody().getObject()
                def userIdsArray = userIds.split(",")
                if(resultData){
                    ArrayList<String> resultIdsList = resultData.get("result_content").getAt("weibo_id_list").getAt("myArrayList")
                    def resultIdsArray = resultIdsList.toArray(String)
                    //如果返回信息中的用户ID 与 请求参数中的用户ID一致，则本次请求成功
                    if(Arrays.equals(userIdsArray,resultIdsArray)){
                        log.info("微博用户，同步用户信息成功：{}", resultIdsArray.toString())
                    } else {
                        log.info("微博用户，同步用户信息失败：用户ID不匹配")
                    }

                }else {
                    log.info("微博用户，同步用户信息失败，错误代码：{}，错误描述：{}", unirest.getStatus(),unirest.getStatusText())
                }
                if(userIdsArray.length<50){
                    flag = false
                }else {
                    pageNo++
                }
            }else {
                log.info("微博用户，暂无需要同步的微博用户")
                break
            }

        }
    }

    void updateUserInfo() {
        def pageNo = 1
        def date = DateUitl.getDayBegin()
        Calendar calendar = Calendar.getInstance()
        calendar.setTime(date)
        calendar.add(Calendar.DAY_OF_MONTH, updateTimeInterval)
        def flag = true
        while (flag){
            def userIds = weiboInfoRepo.getExpiredUserInfo(calendar.getTime(),pageNo,50)
            def userInfoList = this.getLatestUserInfoList(userIds)
            if(userInfoList)
                for (WeiboInfo user : userInfoList){
                     weiboInfoRepo.updateUserInfo(user)
                }
            def userIdsArray = userIds.split(",")
            if(userIdsArray.length<50){
                flag = false
            }else {
                pageNo++
            }
        }
    }

    List<WeiboInfo> getLatestUserInfoList(String userIds) {
        def userInfoList = new ArrayList()
        def userIdArray = userIds.split(",")
        userIdArray.each {userId->
            def unirest = Unirest.post(weiboVerifiedUrl)
                    .field("key", userId)
                    .field("Content-Type", "application/x-www-form-urlencoded")
                    .asJson()
            def res = unirest.body.object.map
            def userInfo = JSONObject.toJSON(res)
            WeiboInfo weiboInfo = new WeiboInfo(userInfo)
            if (weiboInfo) {
                userInfoList << weiboInfo
            }
        }
        return userInfoList
    }
}