package com.istar.mediabroken.service.weibo

import com.alibaba.fastjson.JSONObject
import com.istar.mediabroken.api.WeiboErrorEnum
import com.istar.mediabroken.service.ShareChannelService
import com.mashape.unirest.http.Unirest
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.StringUtils
import org.apache.http.protocol.HTTP
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import weibo4j.Timeline
import weibo4j.http.ImageItem
import weibo4j.model.Status
import weibo4j.model.WeiboException

import javax.servlet.http.HttpServletRequest
import java.util.regex.Matcher
import java.util.regex.Pattern

import static com.istar.mediabroken.api.ShareResult.*

/**
 * Author : YCSnail
 * Date   : 2017-03-28
 * Email  : liyancai1986@163.com
 */
@Service
@Slf4j
class WeiboShareService {

    @Value('${weibo.api.url}')
    public String apiUrl

    @Autowired
    private ShareChannelService shareChannelSrv
    @Autowired
    private WeiboOAuthService weiboOAuthSrv

    public static final int CONTENT_MAX_LENGTH = 140

    def shareWeibo(String accessToken, String content) {

        String url = apiUrl + '/2/statuses/share.json'

        def params = [
                access_token    : accessToken,
                status          : content,       //要发布的微博文本内容，必须做URLencode，内容不超过140个汉字。
        ]

        def res = Unirest.post(url).fields(params as Map<String, Object>).asJson()

        def result = res.body.object
        return res.statusCode == 200 ? shareSuccess() : shareFailure(WeiboErrorEnum.getErrorMsg(result.error_code as int), result.error_code as int)
    }

    /**
     * 发布一条纯文本微博
     */
    def updateWeibo(String accessToken, String content){
        String url = apiUrl + '/2/statuses/update.json'

        def params = [
                access_token    : accessToken,
                status          : content,       //要发布的微博文本内容，必须做URLencode，内容不超过140个汉字。
                visible         : 0,        //微博的可见性，0：所有人能看，1：仅自己可见，2：密友可见，3：指定分组可见，默认为0。
                annotations     : JSONObject.toJSONString([])  //元数据，主要是为了方便第三方应用记录一些适合于自己使用的信息，每条微博可以包含一个或者多个元数据，必须以json字串的形式提交，字串长度不超过512个字符，具体内容可以自定。
        ]

        def res = Unirest.post(url).fields(params as Map<String, Object>).asJson()

        def result = res.body.object
        return res.statusCode == 200 ? shareSuccess() : shareFailure(WeiboErrorEnum.getErrorMsg(result.error_code as int), result.error_code as int)
    }

    /**
     * 上传图片，返回图片picid,urls(3个url)
     */
    def uploadPic(String accessToken, String pic){

        String url = apiUrl + '/2/statuses/upload_pic.json'

        def params = [
                access_token    : accessToken,
                pic             : pic
        ]

        def res = Unirest.post(url).fields(params as Map<String, Object>).asJson()

        def result = res.body.object
        return res.statusCode == 200 ? [
                status  : 1,
                msg     : '发布成功'
        ] : [
                status  : 0,
                msg     : result.error,
                code    : result.error_code
        ]
    }

    /**
     * 高级接口，需要申请
     * 指定一个图片URL地址抓取后上传并同时发布一条新微博
     * 如果只有一张图片，传url然后发布微博
     * 如果有多张图片（最多9张） 需要先将图片上传，获取pic_id再做发布微博
     */
    def uploadUrlTextWeibo(String accessToken, String content, Collection<String> picList){

        if(!picList) return [ status: 0]

        String url = apiUrl + '/2/statuses/upload_url_text.json'

        def params = [
                access_token    : accessToken,
                status          : content,       //要发布的微博文本内容，必须做URLencode，内容不超过140个汉字。
                visible         : 0,        //微博的可见性，0：所有人能看，1：仅自己可见，2：密友可见，3：指定分组可见，默认为0。
                annotations     : JSONObject.toJSONString([])  //元数据，主要是为了方便第三方应用记录一些适合于自己使用的信息，每条微博可以包含一个或者多个元数据，必须以json字串的形式提交，字串长度不超过512个字符，具体内容可以自定。
        ] as Map<String, Object>

        if(picList.size() == 1){
            params.put('url', picList.get(0))
        }else {

            //todo 循环上传图片，拿到pic_id的拼接
            String picIdStr = '1,2,3'
            params.put('pic_id', picIdStr)
        }


        def res = Unirest.post(url).fields(params).asJson()

        def result = res.body.object
        return res.statusCode == 200 ? [
                status  : 1,
                msg     : '发布成功'
        ] : [
                status  : 0,
                msg     : result.error,
                code    : result.error_code
        ]
    }

    def uploadUrlTextWeiboWithSDK(String accessToken, String content, String picLocalPath){
        try {
            byte[] picByte = readFileImage(picLocalPath);
            ImageItem pic = new ImageItem("pic", picByte);
            String s = java.net.URLEncoder.encode(content, HTTP.UTF_8);
            Timeline tm = new Timeline(accessToken);
            Status status = tm.shareStatus(s, pic);

            return shareSuccess()
        } catch (FileNotFoundException e) {
            return shareFailure('图片路径引用错误', 0)
        } catch (WeiboException e) {
            log.error(['weibo', 'SDK发布带图片微博接口', e.message].join(':::') as String)
            log.error('weibo:::', e)

            Pattern pattern = Pattern.compile('error_code:([0-9]{5})\\/')
            Matcher matcher = pattern.matcher(e.message)
            int code = matcher.find() ? (matcher.group(1) as int) : 0
            return shareFailure(WeiboErrorEnum.getErrorMsg(code), code)
        } catch (Exception e) {
            e.printStackTrace();
            return shareFailure()
        }
    }

    def createWeibo(HttpServletRequest request, String uid, def shareContent) {

        String accessToken = weiboOAuthSrv.getAccessTokenFromMongo(request, uid)
        if(!accessToken) {
            return shareFailure('token不存在，或是授权已过期', 0)
        }

        String cont = StringUtils.substring((shareContent?.digest as String) ?: '', 0, CONTENT_MAX_LENGTH)
        String content = cont.concat(' ').concat(shareContent?.sourceUrl as String)
        def picList = shareContent?.picUrls?.tokenize(',') as List<String>

        //如果没有图片，直接使用微博发布普通文本微博接口
        if(!picList){
            return shareWeibo(accessToken, content);
        }


        //如果有图片，并且只有一张本地上传的，暂时使用weibo-sdk中的方法，上传图片并发布一条新微博
        if(picList && picList.size() == 1){
            String picLocalPath = shareChannelSrv.picLocalUrl(picList?.get(0) as String)
            return picLocalPath ? uploadUrlTextWeiboWithSDK(accessToken, content, picLocalPath) : shareWeibo(accessToken, content)
        }


        //待高级接口申请成功后，使用可以发布网络图片的微博接口
//        uploadUrlTextWeibo(accessToken, content, picList)
    }

    /**
     * 短链转换接口
     * @param accessToken
     * @param content
     * @return
     */
    def shortUrl(String accessToken, String longUrl){
        String url = apiUrl + '/2/short_url/shorten'

        def params = [
                access_token    : accessToken,
                url_long        : longUrl
        ]

        def res = Unirest.post(url).fields(params as Map<String, Object>).asJson()

        def result = res.body.object
        return res.statusCode == 200 ? shareSuccess(result.urls[0].url_short as String) : shareFailure(WeiboErrorEnum.getErrorMsg(result.error_code as int), result.error_code as int)
    }


    public static byte[] readFileImage(String filename) throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(
                new FileInputStream(filename));
        int len = bufferedInputStream.available();
        byte[] bytes = new byte[len];
        int r = bufferedInputStream.read(bytes);
        if (len != r) {
            bytes = null;
            throw new IOException("读取文件不正确");
        }
        bufferedInputStream.close();
        return bytes;
    }

}
