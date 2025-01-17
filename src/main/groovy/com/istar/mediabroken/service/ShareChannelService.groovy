package com.istar.mediabroken.service

import com.alibaba.fastjson.JSONObject
import com.istar.mediabroken.api.ShareResult
import com.istar.mediabroken.entity.app.Agent
import com.istar.mediabroken.entity.compile.Material
import com.istar.mediabroken.entity.compile.ShareChannelResult
import com.istar.mediabroken.repo.ShareChannelRepo
import com.istar.mediabroken.repo.compile.MaterialRepo
import com.istar.mediabroken.service.app.AgentService
import com.istar.mediabroken.service.compile.MaterialService
import com.istar.mediabroken.service.qqom.QqomOAuthService
import com.istar.mediabroken.service.qqom.QqomShareService
import com.istar.mediabroken.service.toutiao.ToutiaoOAuthService
import com.istar.mediabroken.service.toutiao.ToutiaoShareService
import com.istar.mediabroken.service.wechat.WechatOAuthService
import com.istar.mediabroken.service.wechat.WechatShareService
import com.istar.mediabroken.service.weibo.WeiboOAuthService
import com.istar.mediabroken.service.weibo.WeiboShareService
import com.istar.mediabroken.utils.StringUtils
import com.istar.mediabroken.utils.UrlUtils
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.RandomUtils
import org.apache.commons.lang3.time.DateFormatUtils
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import sun.misc.BASE64Decoder

import javax.servlet.http.HttpServletRequest
import static com.istar.mediabroken.api.ApiResult.apiResult
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Author : YCSnail
 * Date   : 2017-03-30
 * Email  : liyancai1986@163.com
 */
@Service
@Slf4j
class ShareChannelService {

    @Value('${image.upload.path}')
    public String uploadPath

    public static final int CHANNEL_TYPE_WEIBO = 1
    public static final int CHANNEL_TYPE_WECHAT = 2
    public static final int CHANNEL_TYPE_TOUTIAO = 3
    public static final int CHANNEL_TYPE_QQOM = 4

    public static final int MAX_CHANNEL_NUM_WEIBO = 2
    public static final int MAX_CHANNEL_NUM_WECHAT = 2
    public static final int MAX_CHANNEL_NUM_TOUTIAO = 2
    public static final int MAX_CHANNEL_NUM_QQOM = 2

    @Autowired
    private ShareChannelRepo channelRepo
    @Autowired
    private WeiboOAuthService weiboOAuthSrv
    @Autowired
    private WechatOAuthService wechatOAuthSrv
    @Autowired
    private ToutiaoOAuthService toutiaoOAuthSrv
    @Autowired
    private QqomOAuthService qqomOAuthSrv
    @Autowired
    private WeiboShareService weiboShareSrv
    @Autowired
    private WechatShareService wechatShareSrv
    @Autowired
    private ToutiaoShareService toutiaoShareSrv
    @Autowired
    private QqomShareService qqomShareSrv
    @Autowired
    private CaptureService captureSrv
    @Autowired
    private ICompileService iCompileSrv
    @Autowired
    private MaterialService materialSrv
    @Autowired
    private MaterialRepo materialRepo
    @Autowired
    MaterialService materialService
    @Autowired
    private AgentService agentSrv

    /**
     * 添加一个新浪微博分享渠道
     * @param userId
     * @param code
     * @return
     */
    def addWeiboShareChannel(HttpServletRequest request, Long userId, String code){

        def list = this.getShareChannels(userId, CHANNEL_TYPE_WEIBO)
        if(list && list.size() >= MAX_CHANNEL_NUM_WEIBO) return null

        def res = weiboOAuthSrv.getAccessToken(request, code)

        if(res?.status == 0) {
            return null
        }

        String uid          = res?.data?.getString('uid')
        String accessToken  = res?.data?.getString('access_token')

        //获取微博用户基本信息
        def weiboUser = weiboOAuthSrv.getWeiboUser(accessToken, uid)
        if(!weiboUser){
            return null
        }

        // 此处将weiboUser中的部分数据记录下来，因为部分微博账号的整个weiboUser，在insert mongodb时报错，原因暂不明确
        def accountInfo = [
                "screen_name"   : weiboUser.screen_name,
                "id"            : weiboUser.id,
                "verified_type" : weiboUser.verified_type,
                "name"          : weiboUser.name,
                "idstr"         : weiboUser.idstr,
                "avatar_hd"     : weiboUser.avatar_hd,
                "profile_image_url" : weiboUser.profile_image_url,
                "avatar_large"  : weiboUser.avatar_large,
                "location"      : weiboUser.location
        ]

        //保存分享渠道
        def result = channelRepo.add(userId, CHANNEL_TYPE_WEIBO, uid, weiboUser?.screen_name as String, weiboUser?.avatar_large as String, accountInfo)

        return result
    }

    /**
     * 添加一个微信公众号第三方平台渠道
     * @param userId
     * @param code
     * @return
     */
    def addWechatShareChannel(HttpServletRequest request, Long userId, String authCode){

        def list = this.getShareChannels(userId, CHANNEL_TYPE_WECHAT)
        if(list && list.size() >= MAX_CHANNEL_NUM_WECHAT) return null

        def authorizationInfo = wechatOAuthSrv.getAuthorizationInfo(request, authCode)

        if(!authorizationInfo) {
            return null
        }

        //获取微信公众号用户基本信息
        def wechatAccount = wechatOAuthSrv.getWechatAccountInfo(request, authorizationInfo.authorizer_appid as String)
        if(!wechatAccount){
            return null
        }

        //保存分享渠道
        def result = channelRepo.add(userId, CHANNEL_TYPE_WECHAT, authorizationInfo.authorizer_appid as String, wechatAccount?.nick_name as String, wechatAccount?.head_img as String, wechatAccount)

        return result
    }

    /**
     * 添加一个头条号第三方平台渠道
     * @param userId
     * @param code
     * @return
     */
    def addToutiaoShareChannel(HttpServletRequest request, Long userId, String code){

        def list = this.getShareChannels(userId, CHANNEL_TYPE_TOUTIAO)
        if(list && list.size() >= MAX_CHANNEL_NUM_TOUTIAO) return null

        def res = toutiaoOAuthSrv.getAccessToken(request, code)
        if(res?.status == 0) {
            return null
        }

        String uid          = res?.data?.uid
        String accessToken  = res?.data?.access_token

        //获取头条号用户基本信息
        def toutiaoUser = toutiaoOAuthSrv.getToutiaoUser(request, accessToken, uid)
        if(!toutiaoUser){
            return null
        }

        def accountInfo = JSONObject.parse(toutiaoUser.toString())

        if(accountInfo.extra) {
            accountInfo.extra = JSONObject.toJSONString(accountInfo.extra)
        }
        //保存分享渠道
        def result = channelRepo.add(userId, CHANNEL_TYPE_TOUTIAO, toutiaoUser?.uid as String, toutiaoUser?.screen_name as String, toutiaoUser?.avatar_url as String, accountInfo)

        return result
    }

    /**
     * 添加一个企鹅号第三方平台渠道
     */
    def addQqomShareChannel(HttpServletRequest request, Long userId, String code){

        def list = this.getShareChannels(userId, CHANNEL_TYPE_QQOM)
        if(list && list.size() >= MAX_CHANNEL_NUM_QQOM) return null

        def res = qqomOAuthSrv.getAccessToken(request, code)

        if(res?.status == 0) {
            return null
        }

        String uid          = res?.data?.getString('openid')
        String accessToken  = res?.data?.getString('access_token')

        //获取微博用户基本信息
        def qqomUser = qqomOAuthSrv.getQqomUser(accessToken, uid)
        if(!qqomUser){
            return null
        }

        def accountInfo = JSONObject.parse(qqomUser.toString())
        //保存分享渠道
        def result = channelRepo.add(userId, CHANNEL_TYPE_QQOM, uid, qqomUser?.nick as String, qqomUser?.header as String, accountInfo)

        return result
    }

    /**
     * 获取某用户的所有已授权分享渠道
     * @param userId
     * @return
     */
    List getShareChannels(Long userId){

        def result = channelRepo.getShareChannels(userId)

        return result
    }

    List getShareChannels(Long userId, int channelType){
        return channelRepo.getShareChannels(userId, channelType)
    }

    /**
     * 根据渠道ID删除某用户的一个渠道
     * @param userId
     * @param channelId
     * @return
     */
    def delShareChannel(HttpServletRequest request, Long userId, Long channelId){

        def channel = channelRepo.getChannelById(channelId)
        if(!channel) return

        //取消授权
        def res = 1
        if(channel.channelType == CHANNEL_TYPE_WEIBO) {
            String accessToken = weiboOAuthSrv.getAccessTokenFromMongo(request, channel?.channelInfo?.uid as String)
            res = weiboOAuthSrv.cancel(accessToken)
        }
        channelRepo.del(channelId)
        return res
    }
    def delWechatShareChannel (String authorizerAppId) {
        channelRepo.delWechatChannel(authorizerAppId)
    }
    //同步各个渠道并且记录同步记录
    def shareMaterialAndAddHistory(HttpServletRequest request, Long userId, String agentId, String orgId, String teamId, String materialId, List<Long> channelIds, String timeStamp, int wechatSyncType) {
        def res = this.shareMaterial(request, userId, agentId, orgId, teamId, materialId, channelIds, timeStamp, wechatSyncType)
        try {
            def object = res.result[0]
            def map = [createTime: new Date()]
            map.putAll(object)
            def history = materialRepo.addHistory(materialId, map)
        } catch (e) {
            log.info("添加同步渠道失败:{},失败渠道：{}", e, res)
        } finally {
            return res
        }
    }

    def shareMaterial(HttpServletRequest request, Long userId, String agentId, String orgId, String teamId, String materialId, List<Long> channelIds, String timeStamp, int wechatSyncType) {
        //1 准备数据
        Material material = materialRepo.getUserMaterial(userId, materialId)
        if(!material) {
            return apiResult(HttpStatus.SC_NOT_FOUND, '同步内容不存在！')
        }
        def channelList = channelRepo.getChannelsByIds(userId, channelIds)

        //2 记录分享历史
        //将需要同步的内容先保存（从material->articleOperation）, 分享出去的内容可能需要编加加的文章页地址
        def articleSyncResult = channelList ? materialSrv.addArticle(userId, orgId, materialId, MaterialRepo.ARTICLE_TYPE_SYNC) : null

        //3 组装分享内容
        def article = articleSyncResult?.msg
        def shareContent = this.wrapArticleSyncContent(request, article, wechatSyncType)

        //4 share
        def result = this.share(request, channelList, shareContent)

        ShareChannelResult shareChannelResult = new ShareChannelResult(
                "_id" : UUID.randomUUID().toString(),
                orgId: orgId,
                timeStamp: timeStamp,
                shareResult: result[0]
        )
        //5 todo 考虑在同步完成后，把同步的渠道和结果做记录保存，保存同步文稿到newsOperation表operationType = 5表示文稿同步的
        if (result.detail[0].status == ShareResult.success)
        {
            channelRepo.addShareChannelResult(shareChannelResult)
            materialService.findAndModifyArticlePush(material, userId, agentId, orgId, teamId, 5, result, timeStamp)
        }

        //6 更新文稿的发布状态
        def successList = result?.find { it.detail.status == ShareResult.success }
        def failure = result?.find { it.detail.status == ShareResult.failure }
        if(successList) {
            materialSrv.updateMaterialStatus2Published(userId, materialId, 1)
        }
        if (failure) {
            materialSrv.updateMaterialStatus2Published(userId, materialId, 4)
        }

        return apiResult([result : result])
    }

    def wrapArticleSyncContent(HttpServletRequest request, def article, int wechatSyncType) {

        Agent agent = agentSrv.getAgent(request)

        String qrcode = (agent.qrcode) ?: (UrlUtils.getBjjHost(request) + '/theme/default/images/qrcode.png')

        String bjjFooterDom = """<div style="text-align:center;">
            <img src="${qrcode}">
            <div class="">文章由${agent.siteName}生成</div>
        </div>
        """
        Material material = new Material(article.material as Map)

        def shareContent = [
                videoUrl    : StringUtils.getFirstVideoUrl(material.content),
                content     : material.content + bjjFooterDom,
                picUrls     : material.picUrl,      //用于接收分享到微博时的多张图片
                title       : material.title,
                author      : material.author,
                source      : material.source,
                sourceUrl   : this.articleUrl(request, article.id as String),
                digest      : material.contentAbstract,
                thumbUrl    : material.picUrl,  //用于接收分享到微信和头条号时的文章封面图片
                wechatSyncType  : wechatSyncType    //微信同步类型，0-同步到素材 1-直接群发
        ]
        return shareContent
    }

    def share(HttpServletRequest request, def channelList, def shareContent) {

        def result = []

        if (shareContent.videoUrl){
            //同步视频
            channelList?.each {
                def res = null
                //三个渠道可以抽象出一个对象接口，根据不同的类型，实现不同的业务逻辑
                switch (it.channelType as int) {
                    case CHANNEL_TYPE_WEIBO:
                        res = weiboShareSrv.createWeibo(request, it.channelInfo.uid as String, shareContent)
                        break;
                    case CHANNEL_TYPE_WECHAT:
                        res = wechatShareSrv.createWechat(request, it.channelInfo.uid as String, shareContent)
                        break;
                    case CHANNEL_TYPE_TOUTIAO:
                        res = toutiaoShareSrv.createToutiaoWithVideo(request, it.channelInfo.uid as String, shareContent)
                        break;
                    case CHANNEL_TYPE_QQOM:
                        res = qqomShareSrv.createQqShare(request, it.channelInfo.uid as String, shareContent)
                        break;
                }

                result << [
                        id       : it.id,
                        channelType     : it.channelType,
                        channelUsername : it.channelInfo.name,
                        detail          : res
                ]
            }
        }else {
            //同步图文
            channelList?.each {
                def res = null
                //三个渠道可以抽象出一个对象接口，根据不同的类型，实现不同的业务逻辑
                switch (it.channelType as int) {
                    case CHANNEL_TYPE_WEIBO:
                        res = weiboShareSrv.createWeibo(request, it.channelInfo.uid as String, shareContent)
                        break;
                    case CHANNEL_TYPE_WECHAT:
                        res = wechatShareSrv.createWechat(request, it.channelInfo.uid as String, shareContent)
                        break;
                    case CHANNEL_TYPE_TOUTIAO:
                        res = toutiaoShareSrv.createToutiao(request, it.channelInfo.uid as String, shareContent)
                        break;
                    case CHANNEL_TYPE_QQOM:
                        res = qqomShareSrv.createQqShare(request, it.channelInfo.uid as String, shareContent)
                        break;
                }

                result << [
                        id       : it.id,
                        channelType     : it.channelType,
                        channelUsername : it.channelInfo.name,
                        detail          : res
                ]
            }
        }


        return result
    }

    /**
     * 获取图片本地地址
     * @param pic
     * @return
     */
    String picLocalUrl(String pic) {
        if(pic?.startsWith('data:image')) {
            pic = convertBase64DataToImage(pic)
        }

        Pattern pattern = Pattern.compile('(\\/upload\\/(share|editor|avatar|style|sucai|img)\\/[0-9]{4}\\/[0-9]{2}\\/[0-9]{2}\\/)(.+.(?i)[png|jpeg|jpg|gif])');

        Matcher matcher = pattern.matcher(pic);
        return matcher.find() ? uploadPath + matcher.group(0) : ''
    }

    /**
     * 将base64类型的图片，转化为服务器端本地图片
     * @param base64Img
     * @return
     */
    String convertBase64DataToImage(String base64Img) {
        if(!base64Img) return ''
        try {
            //准备图片数据
            String base64ImgData = base64Img.replaceAll('^data:image/.+;base64,', '')
            //准备文件名称
            String completeFilePath = new StringBuilder()
                    .append('/upload/')
                    .append('img')
                    .append(DateFormatUtils.format(new Date(), "/yyyy/MM/dd/"))
                    .toString()
            String fileName = new StringBuilder()
                    .append(DateFormatUtils.format(new Date(), "HHmmssSSS"))
                    .append(RandomUtils.nextInt(0, 1000))
                    .append('.')
                    .append('png')
                    .toString()

            File fileDirectory = new File(uploadPath + completeFilePath);
            if (!fileDirectory.exists()) {
                fileDirectory.mkdirs()
            }
            String filePath = completeFilePath.concat(fileName)

            BASE64Decoder decoder = new BASE64Decoder();
            byte[] bs = decoder.decodeBuffer(base64ImgData);
            FileOutputStream os = new FileOutputStream(uploadPath + filePath);
            os.write(bs)
            os.flush();
            os.close();
            return filePath
        } catch (IOException e) {
            log.error('{}', e)
            return ''
        }
    }

    String dealBase64Image2Local(HttpServletRequest request, String content) {
        String host = UrlUtils.getBjjHost(request)
        def imgList = StringUtils.extractImgUrl(content)
        imgList?.each {
            if(it?.startsWith('data:image')) {
                def img = this.convertBase64DataToImage(it as String)
                it = it.replaceAll('\\+', '\\\\+')
                content = content.replaceAll(it as String, host.concat(img))
            } else if (it?.startsWith('//')) {
                String url = "https:" + it
                content = content.replace(it as String, url)
            }
        }
        return content
    }

    /**
     * 根据文章ID获取文章在编++的原文地址
     * @param request
     * @param articleId
     * @return
     */
    String articleUrl (HttpServletRequest request, String articleId) {
        return UrlUtils.getBjjHost(request) + '/compile/article.html?id=' + articleId
    }

    public static void main(String[] args){

        String u = 'http://bjj.istarshine.com/upload/share/2017/11/03/12343.pNga12'

        def srv = new ShareChannelService()
        println srv.picLocalUrl(u)
    }

}
