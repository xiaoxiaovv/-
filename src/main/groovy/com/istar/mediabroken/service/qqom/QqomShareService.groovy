package com.istar.mediabroken.service.qqom

import com.istar.mediabroken.api.QqomErrorEnum

import com.mashape.unirest.http.Unirest
import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.time.DateFormatUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

import javax.servlet.http.HttpServletRequest

import static com.istar.mediabroken.api.ShareResult.shareFailure
import static com.istar.mediabroken.api.ShareResult.shareSuccess

import org.apache.commons.codec.digest.*;
import org.apache.commons.io.IOUtils;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.params.HttpMethodParams;

/**
 * Author : YCSnail
 * Date   : 2017-09-11
 * Email  : liyancai1986@163.com
 */
@Service
@Slf4j
class QqomShareService {

    @Value('${image.upload.path}')
    public String videoPath

    @Autowired
    private QqomOAuthService qqomOAuthSrv

    public static final int QQOM_TITLE_MIN_LENGTH = 10
    public static final int QQOM_TITLE_MAX_LENGTH = 30

    def createQqShare(HttpServletRequest request, String uid, def shareContent) {

        String accessToken = qqomOAuthSrv.getAccessTokenFromMongo(request, uid)
        if(!accessToken) {
            return [
                    status  : 0,
                    msg     : 'token不存在，或是授权已过期！'
            ]
        }

        if (shareContent?.title?.length() < 6 || shareContent?.title?.length() > 30) {
            return shareFailure('标题长度应该在6-30字之间', 0)
        }

        // 发表图文后，生成一个事务Id, 最终的发布状态是查询该事务的状态
        def res = this.addQqomNews(accessToken, uid, shareContent)
        if(res.status == 1) {
            Thread.sleep(4000)
            def transactionRes = this.queryTransactionInfo(accessToken, uid, res.data.transaction_id as String)

            if(transactionRes.status == 1) {
                if(['成功', '处理中'].contains(transactionRes.data.transaction_status as String)){
                    return shareSuccess()
                } else {
                    return shareFailure()
                }
            } else {
                return shareFailure()
            }
        } else {
            return res
        }
    }

    def createQqShareWithVideo(HttpServletRequest request, String uid, def shareContent) {

        String accessToken = qqomOAuthSrv.getAccessTokenFromMongo(request, uid)
        if(!accessToken) {
            return [
                    status  : 0,
                    msg     : 'token不存在，或是授权已过期！'
            ]
        }

        if (shareContent?.title?.length() < 6 || shareContent?.title?.length() > 30) {
            return shareFailure('标题长度应该在6-30字之间', 0)
        }
        // 发表视频后，生成一个事务Id, 最终的发布状态是查询该事务的状态
        def res = this.addQqomNews(accessToken, uid, shareContent)
        if(res.status == 1) {
            Thread.sleep(4000)
            def transactionRes = this.queryTransactionInfo(accessToken, uid, res.data.transaction_id as String)

            if(transactionRes.status == 1) {
                if(['成功', '处理中'].contains(transactionRes.data.transaction_status as String)){
                    return shareSuccess()
                } else {
                    return shareFailure()
                }
            } else {
                return shareFailure()
            }
        } else {
            return res
        }

        def resVideo = this.addQqomVideo(accessToken, uid, shareContent)
        if(resVideo.status == 1) {
            Thread.sleep(4000)
            def transactionRes = this.queryTransactionInfo(accessToken, uid, resVideo.data.transaction_id as String)

            if(transactionRes.status == 1) {
                if(['成功', '处理中'].contains(transactionRes.data.transaction_status as String)){
                    return shareSuccess()
                }else {
                    return shareFailure()
                }
            } else {
                return shareFailure()
            }
        } else {
            return resVideo
        }

    }

    /**
     * 发表图文
     * @param accessToken
     * @param openId
     * @param shareContent
     * @return
     */
    def addQqomNews(String accessToken, String openId, def shareContent) {

        try {
//            String url = "https://api.om.qq.com/articlev2/authpubpic?access_token=${accessToken}&openid=${openId}"
            String url = "https://api.om.qq.com/article/authpubpic?access_token=${accessToken}&openid=${openId}"

            def params = [
                    title       : StringUtils.substring((shareContent.title?:'') as String, 0, QQOM_TITLE_MAX_LENGTH),
                    content     : shareContent.content as String,
                    cover_pic   : shareContent.thumbUrl as String
            ]
            def res = Unirest.post(url).fields(params as Map<String, String>).asJson()
            def result = res.body.object
            log.info(['qqom', '新增图文接口', result].join(':::') as String)

//    {
//        "code":"0",
//        "msg": "success",
//        "data": {
//        "transaction_id":TRANSACTION_ID
//    }
            if((result.code as int) == 0) {
                return [
                        status  : 1,
                        msg     : result.msg,
                        data    : result.data
                ]
            } else {
                return shareFailure(QqomErrorEnum.getErrorMsg(result.code as int), result.code as int)
            }
        } catch (Exception e) {

            log.error(['qqom', '新增图文接口', e.message].join(':::') as String)
            log.error('qqom:::', e)
            return shareFailure('接口请求异常！', 0)
        }
    }

    def addQqomVideo(String accessToken, String openId, def shareContent) {

        String videoDownloadPath = "";

        String title = StringUtils.substring((shareContent.title ?: '') as String, 0, QQOM_TITLE_MAX_LENGTH)
        String tags = "其他"
        String cat = "202"//自拍
        String desc = shareContent.digest

        try {
            String videoUrl = shareContent.videoUrl
            InputStream inputStream = com.istar.mediabroken.utils.FileUtils.getInputStream(videoUrl)

            videoDownloadPath = videoPath + "/download/" + DateFormatUtils.format(new Date(), "/yyyyMMdd/") + title+".mp4"
            File file = new File(videoDownloadPath)
            FileUtils.copyInputStreamToFile(inputStream, file);
            String md5 =DigestUtils.md5Hex(IOUtils.toByteArray(new FileInputStream(file)));

            String url = "https://api.om.qq.com/articlev2/authpubvid?access_token=${accessToken}&openid=${openId}&title=${title}&tags=${tags}&cat=${cat}&md5=${md5}&desc=${desc}"//&media=${media}"
            def res = Unirest.post(url).field("media", file).asJson()

            def result = res.body.object
            log.info(['qqom', '新增视频接口', result].join(':::') as String)
            if((result.code as int) == 0) {
                return [
                        status  : 1,
                        msg     : result.msg,
                        data    : result.data
                ]
            } else {
                return shareFailure(QqomErrorEnum.getErrorMsg(result.code as int), result.code as int)
            }
        } catch (Exception e) {
            log.error(['qqom', '新增视频接口', e.message].join(':::') as String)
            log.error('qqom:::', e)
            return shareFailure('接口请求异常！', 0)
        } finally{
            if (videoDownloadPath){//删掉下载到本应用后台服务器的视频
                File file = new File(videoDownloadPath);
                if (file){
                    file.delete()
                }
            }
        }
    }

    /**
     * 查询媒体状态
     * @param accessToken
     * @param openId
     * @param transactionId
     * @return
     */
    def queryTransactionInfo(String accessToken, String openId, String transactionId) {

        try {
            String url = "https://api.om.qq.com/transaction/infoauth?access_token=${accessToken}&openid=${openId}"

            def res = Unirest.get(url).queryString('transaction_id', transactionId).asJson()
            def result = res.body.object
            log.info(['qqom', '查询事物状态接口', result].join(':::') as String)


//            "msg":"SUCCESS",
//            "code":0,
//            "data":{
//                "transaction_id":"7265967822210302510",
//                "transaction_status":"成功",
//                "transaction_ctime":"2017-09-12 09:56:09",
//                "transaction_type":"文章",
//                "article_info":Object{...}
//            }

            if((result.code as int) == 0) {
                return [
                        status  : 1,
                        msg     : '',
                        data    : result.data
                ]
            }else {
                return [
                        status  : 0
                ]
            }
        } catch (Exception e) {

            log.error(['qqom', '查询事物状态接口', e.message].join(':::') as String)
            log.error('qqom:::', e)
            return shareFailure('接口请求异常！', 0)
        }
    }

}
