package com.istar.mediabroken.service.toutiao

import com.istar.mediabroken.entity.app.Agent
import com.istar.mediabroken.service.shareChannel.ChannelOAuthService
import com.istar.mediabroken.utils.FileUtils
import com.istar.mediabroken.utils.StringUtils
import com.mashape.unirest.http.Unirest
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

import javax.servlet.http.HttpServletRequest

import static com.istar.mediabroken.api.ShareResult.*

/**
 * Author : YCSnail
 * Date   : 2017-05-31
 * Email  : liyancai1986@163.com
 */
@Service
@Slf4j
class ToutiaoShareService extends ChannelOAuthService<ToutiaoOAuthConfig> {

    @Value('${toutiao.open.client.key}')
    public String clientKey
    @Value('${toutiao.open.article.public}')
    public int articlePublic

    @Autowired
    private ToutiaoOAuthService toutiaoOAuthSrv

    public static final int TOUTIAO_TITLE_MAX_LENGTH = 30
    public static final int DIGEST_MAX_LENGTH = 140

    def createToutiao(HttpServletRequest request, String uid, def shareContent) {

        config = getOAuthConfig(request)

        String accessToken = toutiaoOAuthSrv.getAccessTokenFromMongo(request, uid)
        if(!accessToken) {
            return [
                    status  : 0,
                    msg     : 'token不存在，或是授权已过期'
            ]
        }

        try {
            String url = "https://mp.toutiao.com/open/new_article_post/?access_token=${accessToken}&client_key=${config.clientKey}"

            def params = [
                    title   : org.apache.commons.lang3.StringUtils.substring((shareContent.title?:'') as String, 0, TOUTIAO_TITLE_MAX_LENGTH),
                    content : StringUtils.removeAElement(shareContent.content as String),
                    abstract: org.apache.commons.lang3.StringUtils.substring((shareContent.digest?:'') as String, 0, DIGEST_MAX_LENGTH),
                    save    : articlePublic    //0-草稿 1-发布
            ]
            def res = Unirest.post(url).fields(params as Map<String, String>).asJson()

            def result = res.body.object
            return ('success' == result?.message) ? shareSuccess() : shareFailure(result?.isNull('data') ? '' : result.data, 0)
        } catch (Exception e) {
            e.printStackTrace()
            return shareFailure()
        }
    }

    def createToutiaoWithVideo(HttpServletRequest request, String uid, def shareContent) {

        config = getOAuthConfig(request)

        String accessToken = toutiaoOAuthSrv.getAccessTokenFromMongo(request, uid)
        if(!accessToken) {
            return [
                    status  : 0,
                    msg     : 'token不存在，或是授权已过期'
            ]
        }

        try {
            //获取上传视频信息
            String url = "https://mp.toutiao.com/open/video/get_upload_url/?access_token=${accessToken}&client_key=${config.clientKey}"
            def res = Unirest.get(url).asJson()

            def getInfoRes = res.body.object
            def data=[:]
            //{"code":0,"data":{"upload_id":"v020042e0000bh2ik7q0ifkj44n79lvg","upload_url":"http://i.snssdk.com/video/v2/upload/1/pgc/1548036639/29ba43e740f1535f2c2bd39cff43fdda/v020042e0000bh2ik7q0ifkj44n79lvg"},"message":"success"}
            if ('success' == getInfoRes?.message){
                data = getInfoRes.data
            }else {
                return getUploadInfoFailure()
            }

            //上传视频
            String upload_url = data.upload_url
            String videoUrl = shareContent.videoUrl
            def upload_res = Unirest.post(upload_url).field("video_file",FileUtils.getInputStream(videoUrl),"name").asJson()
//            def upload_res = Unirest.post(upload_url).field("video_file",new File("E://huajuan.mp4")).asJson()
            def upload_result = upload_res.body.object

            //发表视频文章
            String article_url = "https://mp.toutiao.com/open/new_article_post/?access_token=${accessToken}&client_key=${config.clientKey}"
            def article_params = [
                    video_id    : data.upload_id,
                    video_name  : "我上传的视频",//暂定
                    title       : org.apache.commons.lang3.StringUtils.substring((shareContent.title ?: '') as String, 0, TOUTIAO_TITLE_MAX_LENGTH),
                    article_type: 1,// 	视频文章该值必须传1
                    abstract    : org.apache.commons.lang3.StringUtils.substring((shareContent.digest ?: '') as String, 0, DIGEST_MAX_LENGTH),
                    save        : articlePublic    //0-草稿 1-发布
            ]
            def article_res = Unirest.post(article_url).fields(article_params as Map<String, String>).asJson()
            def article_result = article_res.body.object

            return ('success' == article_result?.message) ? shareSuccess() : shareFailure(article_result?.isNull('data') ? '' : article_result.data, 0)
        } catch (Exception e) {
            e.printStackTrace()
            return shareFailure()
        }
    }

    @Override
    ToutiaoOAuthConfig getOAuthConfig(HttpServletRequest request) {

        Agent agent = agentService.getAgent(request)

        config = configMap.get(agent.agentKey)

        if(!config) {
            def obj = getShareChannelOAuthConfig(agent)

            config = new ToutiaoOAuthConfig([
                    clientKey       : obj.toutiao.appKey,
                    clientSecret    : obj.toutiao.appSecret
            ])
            configMap.put(agent.agentKey, config)
        }
        return config
    }


}
