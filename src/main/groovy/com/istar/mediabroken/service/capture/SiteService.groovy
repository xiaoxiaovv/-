package com.istar.mediabroken.service.capture

import com.alibaba.fastjson.JSONObject
import com.istar.mediabroken.Const
import com.istar.mediabroken.api.DashboardEnum
import com.istar.mediabroken.entity.PushTypeEnum
import com.istar.mediabroken.entity.SystemSetting
import com.istar.mediabroken.entity.capture.Site
import com.istar.mediabroken.entity.capture.SiteDetail
import com.istar.mediabroken.entity.capture.SiteVsWeibo
import com.istar.mediabroken.repo.account.AccountCustomSettingRepo
import com.istar.mediabroken.repo.account.AccountRepo
import com.istar.mediabroken.repo.account.TeamRepo
import com.istar.mediabroken.repo.admin.SettingRepo
import com.istar.mediabroken.repo.capture.NewsRepo
import com.istar.mediabroken.repo.capture.QuerySessionRepo
import com.istar.mediabroken.repo.capture.SiteDistinctRepo
import com.istar.mediabroken.repo.capture.SiteRepo
import com.istar.mediabroken.repo.system.MessageRepo
import com.istar.mediabroken.service.account.AccountService
import com.istar.mediabroken.service.rubbish.RubbishNewsService
import com.istar.mediabroken.service.weibo.WeiboService
import com.istar.mediabroken.utils.*
import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.Unirest
import groovy.util.logging.Slf4j
import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import org.apache.commons.codec.binary.Base64
import org.apache.commons.io.FileUtils
import org.apache.http.HttpStatus
import org.apache.poi.hssf.usermodel.HSSFCell
import org.apache.poi.hssf.usermodel.HSSFRow
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.search.SearchHit
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

import java.text.SimpleDateFormat

import static com.istar.mediabroken.api.ApiResult.apiResult
import static com.istar.mediabroken.utils.UrlUtils.stripUrl
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR
import static org.apache.http.HttpStatus.SC_OK

/**
 * Author: Luda
 * Time: 2017/7/26
 */
@Service
@Slf4j
class SiteService {
    @Autowired
    SiteDistinctRepo siteDistinctRepo
    @Autowired
    SiteRepo siteRepo
    @Autowired
    AccountRepo accountRepo
    @Autowired
    AccountService accountService
    @Autowired
    NewsRepo newsRepo
    @Autowired
    SettingRepo settingRepo
    @Autowired
    MessageRepo messageRepo
    @Autowired
    QuerySessionRepo querySessionRepo
    @Autowired
    AccountCustomSettingRepo accountCustomSettingRepo
    @Autowired
    RubbishNewsService rubbishNewsService
    @Value('${image.upload.path}')
    String UPLOAD_PATH
    @Value('${env}')
    String env

    @Value('${xgsj.token}')
    String token

    @Value('${xgsj.subject.weibo.id}')
    String weiboId

    @Value('${xgsj.subject.url.find}')
    String subjectUrlFind

    @Autowired
    WeiboService weiboService
    @Autowired
    TeamRepo teamRepo

    @Async
    void addSiteDistinctByEs(Site site) {
        if (site.siteType == Site.SITE_TYPE_WEIBO) {
            boolean esHave = false;
            boolean trace = false;
            def num = newsRepo.getNewsTotalBySite(site)
            if (num > 0) {
                esHave = true;
                trace = true
            }
            boolean subjectHave = false
            //获取主题 查看主题包含那个些 站点
            List subject = this.getXGSJweiboSubject()
            if (subject.contains(site.websiteName)) {
                subjectHave = true;
                trace = true
            }
            siteDistinctRepo.addSiteDistinct(site, esHave, subjectHave, trace)
        }
    }


    List getUserSitesBySiteTypeAndName(Long userId, int siteType, String name) {
        return siteRepo.getUserSitesByTypeAndName(userId, siteType, name)
    }

    List getUserSitesBySiteName(Long userId, String siteName) {
        return siteRepo.getUserSitesBySiteName(userId, siteName)
    }

    Map getUserSites(Long userId, int siteType) {
        def sites = siteRepo.getUserSitesByType(userId, siteType)
        if (sites.size() == 0) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "用户没有添加站点信息"])
        }
        def webCount = siteRepo.getUserSitesByType(userId, Site.SITE_TYPE_WEBSITE).size()
        def weChatCount = siteRepo.getUserSitesByType(userId, Site.SITE_TYPE_WECHAT).size()
        def weiBoCount = siteRepo.getUserSitesByType(userId, Site.SITE_TYPE_WEIBO).size()

        Map setting = accountService.getCaptureSiteAccountProfile(userId)
        def maxMediaSiteCount = setting.maxMediaSiteCount ?: 0
        def maxWechatSiteCount = setting.maxWechatSiteCount ?: 0
        def maxWeiboSiteCount = setting.maxWeiboSiteCount ?: 0
        Map webStatus = [
                webCount: webCount,
                allowWeb: maxMediaSiteCount
        ]
        Map weChatStatus = [
                weChatCount: weChatCount,
                allowWeChat: maxWechatSiteCount
        ]
        Map weiBoStatus = [
                weiBoCount: weiBoCount,
                allowWeiBo: maxWeiboSiteCount
        ]
        def map = [
                "webStatus"   : webStatus,
                "wechatStatus": weChatStatus,
                "weiboStatus" : weiBoStatus
        ]
        return apiResult([status: HttpStatus.SC_OK, msg: sites, siteCount: map])
    }

    Map getUserSiteById(Long userId, String siteId) {
        def site = siteRepo.getUserSiteById(userId, siteId)
        if (!site) {
            site = []
//            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "没有找到对应的站点信息"])
        }
        return apiResult([status: HttpStatus.SC_OK, msg: site])
    }

    void removeSite(long userId, String siteId) {
        siteRepo.removeSite(userId, siteId)
    }

    void removeSiteList(long userId, List siteIds) {
        siteRepo.removeSiteList(userId, siteIds)
    }

    Map addUserSite(Long userId, String siteName, String websiteName, String websiteDomain, Integer siteType) {
        if (siteName.equals("") || websiteName.equals("")) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, '请补全站点信息');
        }

        HanyuPinyinOutputFormat upperCaseFormat = new HanyuPinyinOutputFormat();
        upperCaseFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE)
        char word = siteName.substring(0, 1) as char
        def pinYin = PinyinHelper.toHanyuPinyinStringArray(word, upperCaseFormat)
        def pinYinPrefix = (pinYin ? pinYin[0] : word) as String

        def site = new Site(
                userId: userId,
                siteName: siteName,
                websiteName: websiteName,
                websiteDomain: UrlUtils.webSiteDomainUrl(websiteDomain),
                siteType: siteType,
                isAutoPush: false,
                domainReverse: websiteDomain.equals("") ? "" : UrlUtils.getReverseDomainFromUrl(websiteDomain),
                pinYinPrefix: pinYinPrefix,
                updateTime: new Date(),
                createTime: new Date(),
        )

        def siteList = []
        siteList.add(site)

        return addSiteList(userId, siteList)
    }

    List getSiteDetailByIds(List siteDetailList, long userId) {
        List<Site> siteList = new ArrayList<Site>();
        siteDetailList.each {
            int siteType = 0;
            if ("网站".equals(it.siteType)) {
                siteType = Site.SITE_TYPE_WEBSITE
            }
            if ("微信公众号".equals(it.siteType)) {
                siteType = Site.SITE_TYPE_WECHAT
            }
            if ("微博".equals(it.siteType)) {
                siteType = Site.SITE_TYPE_WEIBO
            }

            HanyuPinyinOutputFormat upperCaseFormat = new HanyuPinyinOutputFormat();
            upperCaseFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE)
            char word = it.siteName.substring(0, 1) as char
            def pinYin = PinyinHelper.toHanyuPinyinStringArray(word, upperCaseFormat)
            def pinYinPrefix = (pinYin ? pinYin[0] : word) as String

            siteList << new Site(
                    userId: userId,
                    siteName: it.siteName,
                    websiteName: it.siteName,
                    websiteDomain: UrlUtils.webSiteDomainUrl(it.siteDomain),
                    siteType: siteType,
                    isAutoPush: false,
                    domainReverse: it.siteDomain.equals("") ? "" : UrlUtils.getReverseDomainFromUrl(it.siteDomain),
                    pinYinPrefix: pinYinPrefix,
                    updateTime: new Date(),
                    createTime: new Date(),
            );
        }
        return siteList;
    }

    String sitesTemplate() {
        String filePath;
        if (Const.ENV_ONLINE == env || Const.ENV_TEST == env) {
            filePath = new File(WordUtils.class.getResource("/").path, 'template').path
        } else {
            filePath = new File(WordUtils.class.getResource("/").path.replace('/classes/', '/resources/'), 'template').path
        }

        def pathAndFile = filePath.toString() + File.separator + "sites.xls";
        return pathAndFile;
    }

    //重写导入方法start
    def manySitesImport(long userId, File excelFile) {
        String excelFolder = "/${UPLOAD_PATH}/upload/sites";
        //如果文件夹不存在则创建，如果已经存在则删除
        File outPath = new File(excelFolder)
        if (!outPath.exists()) {
            FileUtils.forceMkdir(outPath)
        } else {
            FileUtils.forceDelete(outPath)
            FileUtils.forceMkdir(outPath)
        }
        InputStream fis = new FileInputStream(excelFile)

        List<Map<String, String>> data = UploadUtil.parseExcel(fis);
        //过滤格式不合法的
        List siteList = [];
        for (int i = 0; i < data.size(); i++) {
            Map<String, String> map = data.get(i);
            String siteName = String.valueOf(map.get("站点或公众号名称（必填）"));
            if ("".equals(siteName) || null == siteName || "null".equals(siteName)) {
                continue;
            }
            String websiteDomain = String.valueOf(map.get("站点域名（必填）"));
            websiteDomain = "null".equals(websiteDomain) ? "" : websiteDomain;
            String siteType = map.get("所属类型（必选）");
            int siteTypeInt = 0;//        siteType 1网站 2微信公众号 3微博
            switch (siteType) {
                case "网站":
                    siteTypeInt = 1;
                    break;
                case "微信公众号":
                    siteTypeInt = 2;
                    break;
                case "微博":
                    siteTypeInt = 3;
                    break;
            }
            if ("微信公众号".equals(siteType) && (!"".equals(websiteDomain))) {
                continue
            }
            if ("微博".equals(siteType) && (!"".equals(websiteDomain))) {
                continue
            }
            HanyuPinyinOutputFormat upperCaseFormat = new HanyuPinyinOutputFormat();
            upperCaseFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE)
            char word = siteName.substring(0, 1) as char
            def pinYin = PinyinHelper.toHanyuPinyinStringArray(word, upperCaseFormat)
            def pinYinPrefix = (pinYin ? pinYin[0] : word) as String
            if (siteTypeInt != 0) {
                siteList << new Site(
                        siteId: UUID.randomUUID().toString(),
                        userId: userId,
                        siteName: siteName,
                        websiteName: siteName,
                        websiteDomain: UrlUtils.webSiteDomainUrl(websiteDomain),
                        siteType: siteTypeInt,
                        isAutoPush: false,
                        domainReverse: websiteDomain.equals("") ? "" : UrlUtils.getReverseDomainFromUrl(websiteDomain),
                        pinYinPrefix: pinYinPrefix,
                        updateTime: new Date(),
                        createTime: new Date(),
                );
            }
        }
        // 去重（网站，公众号和微博）1 上传的siteList和上传的siteList 自身去重
        def distinctSelfList = siteListDistinctSelf(siteList)
        if (distinctSelfList.size() <= 0) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "导入站点无效"])
        }
        // 去重（网站，公众号和微博）2 上传的siteList和用户已有list比对去重
        def userSiteList = siteListDistinctWithUserSiteList(userId, distinctSelfList)
        if (userSiteList.size() <= 0) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "导入站点无效"])
        }
        // 去除多余的公众号和网址，微博数量
        Map delOverNumberMap = siteListDelOverNumber(userId, userSiteList)
        //去除是否用户已经有此站点
        def newDelOverNumberList = delOverNumberMap.newList
        if (newDelOverNumberList.size() <= 0) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "导入站点无效"])
        }
        def message = delOverNumberMap.message
        def result = addImportSiteList(newDelOverNumberList)
        return apiResult(message);
    }

    //重写导入方法end
    def addImportSiteList(List<Site> siteList) {
        for (int i = 0; i < siteList.size(); i++) {
            def site = siteList[i];
            if (site != null) {
                siteRepo.addSite(site)
                //去重
                this.addSiteDistinctByEs(site)
            }
        }
    }
    //出去多于的微博，公众号和网址
    def siteListDelOverNumber(long userId, List<Site> userSiteList) {
        //获得存储站点的上限
        Map setting = accountService.getCaptureSiteAccountProfile(userId)
        if (userSiteList == null || userSiteList.size() == 0) {
            return userSiteList = []
        }
        if (setting == null || setting.size() == 0) {
            return userSiteList = []
        }
        def maxMediaSiteCount = setting.maxMediaSiteCount ?: 0
        def maxWechatSiteCount = setting.maxWechatSiteCount ?: 0
        def maxWeiboSiteCount = setting.maxWeiboSiteCount ?: 0
        //上传的数量
        def countMedia = []
        def countWeChat = []
        def countWebo = []
        userSiteList.each { site ->
            site.websiteDomain = stripUrl(site.websiteDomain)
            site.websiteName = site.websiteName.trim()
            site.websiteName = site.websiteName.trim()
            switch (site.siteType) {
                case 1:
                    countMedia << site.websiteDomain
                    break
                case 2:
                    countWeChat << site.websiteName
                    break
                case 3:
                    countWebo << site.websiteName
                    break
            }
        }
        //获得用户已有的存储数量
        def webList = siteRepo.getUserSitesByType(userId, 1)
        def webSize = webList.size()
        def charList = siteRepo.getUserSitesByType(userId, 2)
        def chatSize = charList.size()
        def weiboList = siteRepo.getUserSitesByType(userId, 3)
        def weiboSize = weiboList.size()
        //还能存多少条
        def mediaCount = maxMediaSiteCount - webSize
        def weChatCount = maxWechatSiteCount - chatSize
        def weiboCount = maxWeiboSiteCount - weiboSize
        List<Site> newWebList = []
        List<Site> newChatList = []
        List<Site> newWeiboList = []
        for (int i = 0; i < userSiteList.size(); i++) {
            def site = userSiteList[i];
            if (site.siteType == 1) {
                newWebList.add(site)
            }
            if (site.siteType == 2) {
                newChatList.add(site)
            }
            if (site.siteType == 3) {
                newWeiboList.add(site)
            }
        }
        List<Site> newList = []
        //导入的数量
        def importMediaCount = 0;
        def importweChatCount = 0;
        def importweiBoCount = 0;
        for (int i = 0; i < mediaCount; i++) {
            newList.add(newWebList[i])
        }

        for (int i = 0; i < weChatCount; i++) {
            newList.add(newChatList[i])
        }

        for (int i = 0; i < weiboCount; i++) {
            newList.add(newWeiboList[i])
        }
        //提示语用的的数据
        for (int i = 0; i < newList.size(); i++) {
            def site = newList[i];
            if (site != null) {
                if (site.siteType == 1) {
                    importMediaCount++
                }
                if (site.siteType == 2) {
                    importweChatCount++
                }
                if (site.siteType == 3) {
                    importweiBoCount++
                }
            }
        }
        //提示语
        def message = [status: HttpStatus.SC_OK,
                       msg   : ("本次导入了网站${importMediaCount}个、" +
                               "公众号${importweChatCount}个，" +
                               "微博${importweiBoCount}个，" +
                               "还可以导入网站${maxMediaSiteCount - webSize - importMediaCount > 0 ? maxMediaSiteCount - webSize - importMediaCount : 0}个、" +
                               "公众号${maxWechatSiteCount - chatSize - importweChatCount > 0 ? maxWechatSiteCount - chatSize - importweChatCount : 0}个、" +
                               "微博${maxWeiboSiteCount - weiboSize - importweiBoCount > 0 ? maxWeiboSiteCount - weiboSize - importweiBoCount : 0}个。"
                       ) as String]
        def map = [
                newList: newList,
                message: message
        ]
        return map
    }
    //去重（网站，微博和公众号） 自身list 去重
    List<Site> siteListDistinctSelf(List<Site> siteList) {
        //自身去重
        for (int i = 0; i < siteList.size(); i++) {
            def site1 = siteList[i];
            for (int j = siteList.size() - 1; j > i; j--) {
                def site2 = siteList[j];
                //网址去重 对导入的url 和微信公众号 微博进行处理
                if (site1.siteType == 1 && stripUrl(site1.websiteDomain).equals(stripUrl(site2.websiteDomain))) {
                    siteList.remove(j)
                }
                if (site1.siteType == 2 && site2.siteType == 2 && site1.websiteName.trim().equals(site2.websiteName.trim())) {
                    siteList.remove(j)
                }
                if (site1.siteType == 3 && site2.siteType == 3 && site1.websiteName.trim().equals(site2.websiteName.trim())) {
                    siteList.remove(j)
                }
            }
        }
        if (siteList.size() == 0) {
            return siteList = []
        }
        return siteList
    }

    //导入列表和用户站点列表去重
    List<Site> siteListDistinctWithUserSiteList(long userId, List<Site> sitelist) {
        def userSitesList = siteRepo.getUserSites(userId)
        for (int i = 0; i < userSitesList.size(); i++) {
            def userSite = userSitesList[i];
            for (int j = 0; j < sitelist.size(); j++) {
                def uploadSite = sitelist[j];
                //用户已有站点
                def websiteDomain = userSite.websiteDomain
                def type = userSite.siteType
                def name = userSite.websiteName
                //导入站点
                def siteDomain = stripUrl(uploadSite.websiteDomain)
                def siteType = uploadSite.siteType
                def siteName = uploadSite.websiteName.trim()
                if (type == 1 && siteType == 1 && siteDomain.equals(websiteDomain)) {
                    sitelist.remove(j)
                }
                if (type == 2 && siteType == 2 && siteName.equals(name)) {
                    sitelist.remove(j)
                }
                if (type == 3 && siteType == 3 && siteName.equals(name)) {
                    sitelist.remove(j)
                }
            }
        }
        return sitelist
    }

    Map sitesImport(long userId, File excelFile) {
        def result = [status: HttpStatus.SC_OK, msg: '导入成功', siteId: '']
        String excelFolder = "/${UPLOAD_PATH}/upload/sites";
        //如果文件夹不存在则创建，如果已经存在则删除
        File outPath = new File(excelFolder)
        if (!outPath.exists()) {
            FileUtils.forceMkdir(outPath)
        } else {
            FileUtils.forceDelete(outPath)
            FileUtils.forceMkdir(outPath)
        }

        InputStream fis = new FileInputStream(excelFile)

        List<Map<String, String>> data = UploadUtil.parseExcel(fis);
        List siteList = [];
        for (int i = 0; i < data.size(); i++) {
            Map<String, String> map = data.get(i);
            String siteName = String.valueOf(map.get("站点或公众号名称（必填）"));
            if ("".equals(siteName)) {
                continue;
            }
            String websiteDomain = String.valueOf(map.get("站点域名（必填）"));
            websiteDomain = "null".equals(websiteDomain) ? "" : websiteDomain;
            String siteType = map.get("所属类型（必选）");
            int siteTypeInt = 0;//        siteType 1网站 2微信公众号
            switch (siteType) {
                case "网站":
                    siteTypeInt = 1;
                    break;
                case "微信公众号":
                    siteTypeInt = 2;
                    break;
            }
            if ("微信公众号".equals(siteType) && (!"".equals(websiteDomain))) {
                result.msg = '导入数据有误，微信公众号不需要填写站点域名';
                result.status = HttpStatus.SC_BAD_REQUEST;
                result.siteId = "error";
                return result;
            }
            HanyuPinyinOutputFormat upperCaseFormat = new HanyuPinyinOutputFormat();
            upperCaseFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE)
            char word = siteName.substring(0, 1) as char
            def pinYin = PinyinHelper.toHanyuPinyinStringArray(word, upperCaseFormat)
            def pinYinPrefix = (pinYin ? pinYin[0] : word) as String
            if (siteTypeInt != 0) {
                siteList << new Site(
                        userId: userId,
                        siteName: siteName,
                        websiteName: siteName,
                        websiteDomain: UrlUtils.webSiteDomainUrl(websiteDomain),
                        siteType: siteTypeInt,
                        isAutoPush: false,
                        domainReverse: websiteDomain.equals("") ? "" : UrlUtils.getReverseDomainFromUrl(websiteDomain),
                        pinYinPrefix: pinYinPrefix,
                        updateTime: new Date(),
                        createTime: new Date(),
                );
            }
        }

        result = addSiteList(userId, siteList)
        return result;
    }

    Map addSiteList(Long userId, List<Site> siteList) {

        if (siteList == null || siteList.size() == 0) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "请输入站点信息"])
        }

        Map setting = accountService.getCaptureSiteAccountProfile(userId)
        if (setting == null || setting.size() == 0) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "没有找到相关的系统配置信息"])
        }

        def maxSiteCount = 0
        setting.each { k, v ->
            maxSiteCount += v
        }
        //获取当前site的总数
        Map siteCounts = siteRepo.getSiteSummaryCountByType(userId)
        def currSiteTotalCount = 0
        siteCounts.each {
            currSiteTotalCount += it.value
        }
        if ((currSiteTotalCount + siteList.size()) > maxSiteCount) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,
                              msg   : ("站点数超出限制," +
                                      "已经配置${currSiteTotalCount}个，" +
                                      "还可以配置${maxSiteCount - currSiteTotalCount > 0 ? maxSiteCount - currSiteTotalCount : 0}个") as String])
        }
        def maxMediaSiteCount = 0
        def maxWechatSiteCount = 0
        def maxWeiboSiteCount = 0

        def account = accountRepo.getUserById(userId)
        String productType = account.productType
        if (!productType) {
            productType = accountService.getDefAppVersion().key
        }
        def systemSetting = settingRepo.getSystemSetting("appVersion", productType)
        if (setting.maxMediaSiteCount == null) {
            maxMediaSiteCount = systemSetting.content.captureSite.maxMediaSiteCount
        } else {
            maxMediaSiteCount = setting.maxMediaSiteCount ?: 0
        }
        if (setting.maxWechatSiteCount == null) {
            maxWechatSiteCount = systemSetting.content.captureSite.maxWechatSiteCount
        } else {
            maxWechatSiteCount = setting.maxWechatSiteCount ?: 0
        }
        if (setting.maxWeiboSiteCount == null) {
            maxWeiboSiteCount = systemSetting.content.captureSite.maxWeiboSiteCount
        } else {
            maxWeiboSiteCount = setting.maxWeiboSiteCount ?: 0
        }


        def mediaSiteDomains = []
        def wechatNames = []
        def weiboNames = []

        siteList.each { site ->
            site.websiteDomain = stripUrl(site.websiteDomain)
            site.websiteName = site.websiteName.trim()
            switch (site.siteType) {
                case Site.SITE_TYPE_WEBSITE:
                    mediaSiteDomains << site.websiteDomain
                    break
                case Site.SITE_TYPE_WECHAT:
                    wechatNames << site.websiteName
                    break
                case Site.SITE_TYPE_WEIBO:
                    weiboNames << site.websiteName
                    break
            }

        }
        if (mediaSiteDomains.size() > 0) {
            def mediaSiteCount = siteCounts.get(Site.SITE_TYPE_WEBSITE) ? siteCounts.get(Site.SITE_TYPE_WEBSITE) : 0;
            if ((mediaSiteCount + mediaSiteDomains.size()) > maxMediaSiteCount) {
                def siteCount = mediaSiteCount;
                return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,
                                  msg   : ("网站数超出限制," +
                                          "已经配置${siteCount}个," +
                                          "还可以配置${maxMediaSiteCount - siteCount > 0 ? maxMediaSiteCount - siteCount : 0}个") as String])

            }
        }
        if (wechatNames.size() > 0) {
            def wechatSiteCount = siteCounts.get(Site.SITE_TYPE_WECHAT) ? siteCounts.get(Site.SITE_TYPE_WECHAT) : 0;
            if ((wechatSiteCount + wechatNames.size()) > maxWechatSiteCount) {
                def siteCount = wechatSiteCount;
                return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,
                                  msg   : ("微信公众号数超出限制," +
                                          "已经配置${siteCount}个," +
                                          "还可以配置${maxWechatSiteCount - siteCount > 0 ? maxWechatSiteCount - siteCount : 0}个") as String])
            }
        }
        if (weiboNames.size() > 0) {
            def weiboSiteCount = siteCounts.get(Site.SITE_TYPE_WEIBO) ? siteCounts.get(Site.SITE_TYPE_WEIBO) : 0;
            if ((weiboSiteCount + weiboNames.size()) > maxWeiboSiteCount) {
                def siteCount = weiboSiteCount;
                return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,
                                  msg   : ("微博号数超出限制," +
                                          "已经配置${siteCount}个," +
                                          "还可以配置${maxWeiboSiteCount - siteCount > 0 ? maxWeiboSiteCount - siteCount : 0}个") as String])
            }
        }

        def existCount = siteRepo.getSite(userId, Site.SITE_TYPE_WEBSITE, mediaSiteDomains)
        if (existCount) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: existCount.siteName + "站点已存在，请不要重复输入！"])
        }
        existCount = siteRepo.getSite(userId, Site.SITE_TYPE_WECHAT, wechatNames)
        if (existCount) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: existCount.siteName + "微信公众号已存在，请不要重复输入！"])
        }
        existCount = siteRepo.getSite(userId, Site.SITE_TYPE_WEIBO, weiboNames)
        if (existCount) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: existCount.siteName + "微博已存在，请不要重复输入！"])
        }
        def siteIds = []
        siteList.each { site ->
            site.siteId = UUID.randomUUID().toString()
            siteIds << site.siteId
            siteRepo.addSite(site)
            //再去重表内添加
            this.addSiteDistinctByEs(site)
        }
        return apiResult([siteIds: siteIds, status: HttpStatus.SC_OK, msg: "添加站点成功"])
    }

    /**
     * 更新站点信息
     * @param userId
     * @param siteId
     * @param siteName
     * @param websiteName
     * @param websiteDomain
     * @param siteType
     * @return
     */
    Map modifyUserSite(Long userId, String siteId, String siteName, String websiteName, String websiteDomain, Integer siteType) {

        if (siteName.equals("") || websiteName.equals("")) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, '请补全站点信息');
        }

        HanyuPinyinOutputFormat upperCaseFormat = new HanyuPinyinOutputFormat();
        upperCaseFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE)
        char word = siteName.substring(0, 1) as char
        def pinYin = PinyinHelper.toHanyuPinyinStringArray(word, upperCaseFormat)
        def pinYinPrefix = (pinYin ? pinYin[0] : word) as String

        def site = new Site([
                _id          : siteId,
                userId       : userId,
                siteName     : siteName,
                websiteName  : websiteName,
                websiteDomain: UrlUtils.webSiteDomainUrl(websiteDomain),
                siteType     : siteType,
                domainReverse: websiteDomain.equals("") ? "" : UrlUtils.getReverseDomainFromUrl(websiteDomain),
                pinYinPrefix : pinYinPrefix,
                updateTime   : new Date(),
                message      : "",
                approved     : "",
                dataStatus   : ""
        ]
        )

        //拿到旧的站点.如果域名和站点类型没有变化，则不做修改
        def oldSite = siteRepo.getUserSiteById(site.userId, site.siteId)
        if (!oldSite) {
            return apiResult([status: HttpStatus.SC_BAD_REQUEST, msg: '站点信息错误'])
        }
        if (oldSite.siteType == site.siteType) {
            if (oldSite.siteType == Site.SITE_TYPE_WEBSITE && oldSite.websiteDomain == site.websiteDomain && oldSite.siteName == site.siteName && oldSite.websiteName == site.websiteName) {
                return apiResult(status: HttpStatus.SC_OK, msg: 'success')
            }
            if (oldSite.siteType == Site.SITE_TYPE_WECHAT && oldSite.websiteName == site.websiteName && oldSite.siteName == site.siteName) {
                return apiResult(status: HttpStatus.SC_OK, msg: 'success')
            }
            if (oldSite.siteType == Site.SITE_TYPE_WEIBO && oldSite.websiteName == site.websiteName && oldSite.siteName == site.siteName) {
                return apiResult(status: HttpStatus.SC_OK, msg: 'success')
            }
        }
        def isAvailable4Modify = isAvailable4Modify(site, oldSite)
        if (isAvailable4Modify.status == HttpStatus.SC_OK) {
            siteRepo.modifySite(site)
            //去重表添加
            this.addSiteDistinctByEs(site)
            return apiResult(status: HttpStatus.SC_OK, msg: 'success')
        } else {
            return isAvailable4Modify
        }
    }

    /**
     * 编辑站点的自动推送状态
     * @param userId
     * @param siteId
     * @param isAutoPush
     * @return
     */
    Map modifySiteAutoPush(Long userId, String siteId, Boolean isAutoPush) {

        def oldSite = siteRepo.getUserSiteById(userId, siteId)
        if (!oldSite) {
            return apiResult([status: HttpStatus.SC_BAD_REQUEST, msg: '站点信息错误'])
        }

        if (oldSite.isAutoPush == isAutoPush) {
            return apiResult([status: HttpStatus.SC_BAD_REQUEST, msg: '站点信息没有改变'])
        }

        //todo 更新之前先判断是否允许自动推送
        siteRepo.modifySiteAutoPush(userId, siteId, isAutoPush)
        return apiResult()
    }
    //判断是否可修改成此站点
    Map isAvailable4Modify(Site site, Site oldSite) {
        //2. 比较域名或名称是否已经存在
        boolean isExist = siteRepo.isSiteExist(site)
        if (isExist) {
            return apiResult([status: HttpStatus.SC_BAD_REQUEST, msg: '已存在该站点的配置'])
        }

        //3. 如果类型变了，校验数据是否超过了站点数设置
        if (oldSite.siteType != site.siteType) {
            Map setting = accountService.getCaptureSiteAccountProfile(site.userId)
            if (setting == null || setting.size() == 0) {
                return apiResult([status: HttpStatus.SC_BAD_REQUEST, msg: "没有找到相关的系统配置信息"])
            }

            def userSiteTypeCount = siteRepo.getUserSiteCountByType(site.userId, site.siteType)
            def maxSiteCount = 0
            switch (site.siteType) {
                case Site.SITE_TYPE_WEBSITE:
                    maxSiteCount = setting.maxMediaSiteCount ?: 0
                    break;
                case Site.SITE_TYPE_WECHAT:
                    maxSiteCount = setting.maxWechatSiteCount ?: 0
                    break;
                case Site.SITE_TYPE_WEIBO:
                    maxSiteCount = setting.maxWeiboSiteCount ?: 0
                    break;
            }
            if (userSiteTypeCount >= maxSiteCount) {
                return apiResult([status: HttpStatus.SC_BAD_REQUEST, msg: "站点数超过" + maxSiteCount as String])
            }
        }
        return apiResult([status: HttpStatus.SC_OK, msg: true])
    }

    Map getUserSiteNews(Long userId, String siteId, int hot, Date startTime, Date endTime, int orientation, boolean hasPic, int order, int queryScope, String keyWords, int pageSize, String queryId) {
        def newsList = []
        List<Site> sites = []
        int siteType = 0
        //拿到用户站点信息
        if ("-1".equals(siteId) || "-2".equals(siteId) || "-3".equals(siteId)) {
            siteType = Math.abs(siteId as int)
            sites = siteRepo.getUserSitesByType(userId, siteType)
            if (sites.size() <= 0) {
                return apiResult([status: HttpStatus.SC_BAD_REQUEST, msg: '没有配置对应的站点'])
            }
        } else {
            Site site = siteRepo.getUserSiteById(userId, siteId)
            if (!site) {
                return apiResult([status: HttpStatus.SC_BAD_REQUEST, msg: '站点信息错误', newsList: newsList])
            } else {
                siteType = site.siteType
            }
            sites << site
        }
        //解析数据
        String id = ""
        Date time = new Date()
        int offset = 0

        if (queryId) {
            try {
                List queryKeys = queryId.split(",")
                id = queryKeys.get(0)
                time = new Date(queryKeys.get(1) as long)
                offset = queryKeys.get(2) as int
                querySessionRepo.removeQuerySessionByTime(id, time)
            } catch (Exception e) {
                log.error("queryId格式不合法,解析失败", e)
                return apiResult([status: HttpStatus.SC_OK, newsList: [], msg: 'queryId格式不合法', queryId: queryId])
            }
        } else {
            id = UUID.randomUUID().toString()
        }
        def newsTitleList = []

        while (true) {
            List currentNewsList = siteRepo.getSitesNewsFromEs(sites, hot, startTime, endTime, orientation, hasPic, order, queryScope, keyWords, pageSize, offset)
            def validResult = getValidNews(currentNewsList, pageSize - newsList.size(), id, newsTitleList, userId)
            offset += validResult.index
            newsList += validResult.newsList
            if (newsList.size() >= pageSize) {
                break;
            }
            if (currentNewsList.size() < pageSize) {
                break;
            }
        }

        queryId = id + "," + new Date().getTime().toString() + "," + offset

        if (newsList && newsList.size() > 0) {
            def newsIds = []
            newsList.each {
                it.firstPublishSiteName = it.firstPublishSiteName ?: it.siteName
                it.siteType = siteType != 0 ? siteType : (it.newsType == 1 ? Site.SITE_TYPE_WEBSITE : (it.newsType == 6) ? Site.SITE_TYPE_WECHAT : Site.SITE_TYPE_WEIBO)
                it.content = (it.newsType == 1 || it.newsType == 6) ? "" : StringUtils.removeWeiboSuffix(it.content)
                newsIds.add(it.id)
            }
            List col = newsRepo.getExistNewsOperation(userId, newsIds, 3)
            List push = newsRepo.getExistNewsOperation(userId, newsIds, 1)
            newsList.each { it ->
                it.put("isCollection", col.contains(it.id))
                it.put("isPush", push.contains(it.id))
            }
            return apiResult([status: HttpStatus.SC_OK, newsList: newsList, msg: '', queryId: queryId])
        } else {
            if ("".equals(keyWords)) {
                def sdf = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss')
                String start = "1970-01-01 00:00:00"
                Date startDate = sdf.parse(start)
                newsList = siteRepo.getSitesNewsFromEs(sites, 0, startDate, new Date(), 0, false, 1, 1, "", 1, 1)
                if (newsList && newsList.size() > 0) {
                    newsList.each {
                        it.firstPublishSiteName = it.firstPublishSiteName ?: it.siteName
                    }
                    return apiResult([status: HttpStatus.SC_OK, newsList: [], msg: '查询无数据', queryId: queryId])
                } else {
                    String msg = sites[0] ? (sites[0].message ?: "暂无数据，请稍后查询") : "暂无数据，请稍后查询"
                    if ("-1".equals(siteId) || "-2".equals(siteId) || "-3".equals(siteId)) {
                        msg = "暂无数据，请稍后查询"
                    }
                    return apiResult([status: HttpStatus.SC_OK, newsList: [], msg: msg, queryId: queryId])
                }
            } else {
                return apiResult([status: HttpStatus.SC_OK, newsList: [], msg: '查询无数据', queryId: queryId])
            }
        }
    }

    Map getValidNews(List newsList, int maxCount, String id, List newsTitleList, long userId) {
        Map result = ["newsList": [], "index": newsList.size()]
        def resultList = []
        for (int i = 0; i < newsList.size(); i++) {
            result.index = i + 1
            def news = newsList.get(i)
            //2 查看Simhash是否存在在已经返回的list中
            def isExist = querySessionRepo.isQuerySessionRecordExist(id, news.simhash)
            if (isExist) {
                continue
            }
            if (newsTitleList.contains(news.title)) {
                continue
            }
            //查看新闻是不是被用户标记过为垃圾数据
            if (rubbishNewsService.isRubbishNews(userId, news.id as String, news.simhash as String)) {
                continue
            }
            //3 如果没有存在，则添加到返回的newslist
            resultList << news
            newsTitleList << news.title
            //4 插入到querysession
            querySessionRepo.addQuerySession(id, news.simhash)
            if (resultList.size() >= maxCount) {
                break
            }
        }
        result.newsList = resultList
        return result
    }

    Map getSitesRecommendation(long userId, List siteDetailList) {
        //进行判断
        boolean result = null
        def list = []
        def sitesList = siteRepo.getUserSites(userId)
        for (int i = 0; i < siteDetailList.size(); i++) {
            SiteDetail siteDetail = siteDetailList[i]
            result = siteDetailIsContainsOrNotContainsUserSite(sitesList, siteDetail)
            list.add([
                    id            : siteDetail.getId(),
                    area          : siteDetail.getArea(),
                    siteName      : siteDetail.getSiteName(),
                    classification: siteDetail.getClassification(),
                    siteDomain    : siteDetail.getSiteDomain(),
                    attr          : siteDetail.getAttr(),
                    siteType      : siteDetail.getSiteType(),
                    isHave        : result
            ])
        }
        return [list: list]
    }

    Map getUserSitesNews(long userId, boolean hasPic, int hours, int pageSize, int pageNo, int siteType) {
        def sites = siteRepo.getUserSitesByType(userId, siteType)
        if (sites.isEmpty()) {
            return apiResult([status: HttpStatus.SC_BAD_REQUEST, msg: '站点信息错误'])
        }
        def result = siteRepo.getSitesHotNewsFromEs(sites, hasPic, hours, pageSize, pageNo)
        return apiResult([status: SC_OK, msg: result])
    }

    Map getLastSitesNews(boolean hasPic, int pageSize, int pageNo, int siteType) {
        def result = siteRepo.getSitesLastNewsFromEs(siteType, hasPic, pageSize, pageNo)
        return apiResult([status: SC_OK, msg: result])
    }

    Map getUserSitesNews(long userId, boolean hasPic, int hours, int pageSize, int pageNo) {
        def sites = siteRepo.getUserSites(userId)
        if (sites.isEmpty()) {
            return apiResult([status: HttpStatus.SC_BAD_REQUEST, msg: '站点信息错误'])
        }
        def result = siteRepo.getSitesHotNewsFromEs(sites, hasPic, hours, pageSize, pageNo)
        return apiResult([status: SC_OK, msg: result])
    }

    List getSitesHotNews(List<Site> sites, boolean hasPic, int maxCount, Date startDate, Date endDate) {
        if (sites.isEmpty()) {
            return null
        }
        SearchResponse scrollResp = null
        def resultMap = [:]
        def result = []
        while (true) {
            if (!scrollResp) {
                scrollResp = siteRepo.getSitesHotNewsByTime(sites, hasPic, 50, "reprintCount", startDate, endDate, null, null)
            } else {
                scrollResp = siteRepo.getScrollData(scrollResp, 10000)
            }
            if (scrollResp.getHits().getHits().length == 0) {
                break;
            }
            for (SearchHit hit : scrollResp.getHits().getHits()) {
                def news = hit.getSource()
                if (resultMap.containsKey(news.simhash)) {
                    continue
                }
                resultMap.put(news.simhash, news)
                if (resultMap.size() >= maxCount) {
                    break
                }
            }
            if (resultMap.size() >= maxCount) {
                break
            }
        }
        resultMap.each { key, value ->
            value.put("captureTime", DateUitl.convertEsDate(value.captureTime as String).getTime())
            result << value
        }
        return result
    }

    Map getUserSitesNewsByTime(long userId, boolean hasPic, int maxCount, int siteType, Date startDate, Date endDate) {
        def sites = siteRepo.getUserSitesByType(userId, siteType)
        if (sites.isEmpty()) {
            return apiResult([status: HttpStatus.SC_BAD_REQUEST, msg: '没有找到配置的站点'])
        }
        def matchedNews = getSitesHotNews(sites, hasPic, maxCount, startDate, endDate)
        def moreNews = []
        if (matchedNews.size() < maxCount) {
            def crrStartDate = new Date(startDate.getTime() - 72 * 3600 * 1000L)
            moreNews = getSitesHotNews(sites, hasPic, maxCount - matchedNews.size(), crrStartDate, startDate)
            matchedNews = matchedNews + moreNews
        }
        return apiResult([status: SC_OK, newsList: matchedNews])
    }

    Map getSitesHotNewsFourHours(Long userId) {
        def result = getUserSitesNews(userId, false, 4, 100, 1, 1)
        if (result.status != SC_OK) {
            def recall = getLastSitesNews(false, 100, 1, 1)
            def record = getTopRecord(recall.msg as List<Map<String, Object>>, 5)
            record.each { it ->
                def transfer = DateUitl.convertEsDate(it.captureTime as String)
                it.remove("captureTime")
                it.put("captureTime", transfer.getTime())
            }
            return apiResult([status: SC_OK, msg: record])
        }
        def data = (result.msg as List<Map<String, Object>>)
        def record = getTopRecord(data, 5)
        if (record.size() < 5) {
            def recall = getLastSitesNews(false, 100, 1, 1)
            record += getTopRecord(recall.msg as List<Map<String, Object>>, 5 - record.size())
        }
        record.each { it ->
            def transfer = DateUitl.convertEsDate(it.captureTime as String)
            it.remove("captureTime")
            it.put("captureTime", transfer.getTime())
        }
        return apiResult([status: SC_OK, msg: record])
    }

    Map getSitesHotNewsEightHours(Long userId) {
        def result = getUserSitesNews(userId, false, 8, 100, 1, 1)
        if (result.status != SC_OK) {
            def recall = getLastSitesNews(false, 100, 1, 1)
            def record = getTopRecord(recall.msg as List<Map<String, Object>>, 5)
            record.each { it ->
                def transfer = DateUitl.convertEsDate(it.captureTime as String)
                it.remove("captureTime")
                it.put("captureTime", transfer.getTime())
            }
            return apiResult([status: SC_OK, msg: record])
        }
        def data = (result.msg as List<Map<String, Object>>)
        def topRecord = getTopRecord(data, 5)
        if (topRecord.size() < 5) {
            def recall = getLastSitesNews(false, 100, 1, 1)
            topRecord += getTopRecord(recall.msg as List<Map<String, Object>>, 5 - topRecord.size())
        }
        topRecord.each { it ->
            def transfer = DateUitl.convertEsDate(it.captureTime as String)
            it.remove("captureTime")
            it.put("captureTime", transfer.getTime())
        }
        return apiResult([status: SC_OK, msg: topRecord])
    }

    Map getSitesHotNewsFullDay(Long userId) {
        def result = getUserSitesNews(userId, true, 24, 100, 1)
        if (result.status != SC_OK) {
            return result
        }
        def data = (result.msg as List<Map<String, Object>>)
        def record = getTopRecordWithImg(data, 3)
        if (record.size() < 3) {
            def recall = getLastSitesNews(true, 100, 1, 1)
            record += getTopRecordWithImg(recall.msg as List<Map<String, Object>>, 3 - record.size())
        }
        record.each { it ->
            def transfer = DateUitl.convertEsDate(it.captureTime as String)
            it.remove("captureTime")
            it.put("captureTime", transfer.getTime())
        }
        return apiResult([status: SC_OK, msg: record])
    }

    List getTopRecordWithImg(List list, int limit) {
        def result = []
        def simFilter = new HashSet()
        for (Map<String, Object> obj : list) {
            if (simFilter.contains(obj.simhash) && result.size() < limit) {
                continue
            }
            if (!isImgAvailable(obj.cover)) {
                if (!isImgAvailable(new String(Base64.decodeBase64(obj.cover.substring("https://yqms3.zhxgimg.com/download/img/".length()) as byte[])))) {
                    continue
                } else {
                    obj.cover = new String(Base64.decodeBase64(obj.cover.substring("https://yqms3.zhxgimg.com/download/img/".length()) as byte[]))
                }
            }
            result << obj
            simFilter.add(obj.simhash)
            if (simFilter.size() >= limit) {
                break
            }
        }
        return result
    }

    private boolean isImgAvailable(String url) {
        try {
            def urlStr = new URL(url);
            def connection = (HttpURLConnection) urlStr.openConnection();
            def state = connection.getResponseCode();
            if (state == 200) {
                return true
            }
            return false
        } catch (Exception e) {
            return false
        }
    }

    Map getSitesHotWeChatFullDay(Long userId) {
        def result = getUserSitesNews(userId, false, 24, 100, 1, 2)
        if (result.status != SC_OK) {
            def recall = getLastSitesNews(false, 100, 1, 2)
            def record = getTopRecord(recall.msg as List<Map<String, Object>>, 5)
            record.each { it ->
                def transfer = DateUitl.convertEsDate(it.captureTime as String)
                it.remove("captureTime")
                it.put("captureTime", transfer.getTime())
            }
            return apiResult([status: SC_OK, msg: record])
        }
        def data = (result.msg as List<Map<String, Object>>)
        def record = getTopRecord(data, 5)

        if (record.size() < 5) {
            def recall = getLastSitesNews(false, 100, 1, 2)
            record += getTopRecord(recall.msg as List<Map<String, Object>>, 5 - record.size())
        }
        record.each { it ->
            def transfer = DateUitl.convertEsDate(it.captureTime as String)
            it.remove("captureTime")
            it.put("captureTime", transfer.getTime())
        }

        return apiResult([status: SC_OK, msg: record])
    }

    Map getHotWeiBoNews(long userId, int maxCount) {
        List<Site> siteList = siteRepo.getUserSites(userId)
        def siteNameList = []
        for (int i = 0; i < siteList.size(); i++) {
            Site site = siteList.get(i)
            SiteVsWeibo weibo = siteRepo.getSiteWeibo(site)
            if (weibo) {
                if (siteNameList.contains(weibo.siteName)) {
                    continue;
                } else {
                    siteNameList << weibo.siteName
                }
            }
        }


        def endDate = new Date()
        def startDate = new Date(endDate.getTime() - 24 * 3600 * 1000L)
        def noSiteNameList = []
        def dayHotWeiboSite = getHotWeiboNewsByTime(siteNameList, noSiteNameList, startDate, endDate, maxCount)
        if (dayHotWeiboSite.size() < maxCount) {
            noSiteNameList = siteNameList
            siteNameList = []
            maxCount -= dayHotWeiboSite.size()
            def dayHotWeibo = getHotWeiboNewsByTime(siteNameList, noSiteNameList, startDate, endDate, maxCount)
            dayHotWeiboSite += dayHotWeibo
            def beforeYesterdayHotWeibo = []
            if (dayHotWeibo.size() < maxCount) {
                def crrStartDate = new Date(startDate.getTime() - 72 * 3600 * 1000L)
                beforeYesterdayHotWeibo = getHotWeiboNewsByTime(siteNameList, noSiteNameList, crrStartDate, startDate, maxCount - dayHotWeibo.size())
                dayHotWeiboSite += beforeYesterdayHotWeibo
            }
        }
        return apiResult([status: SC_OK, weiboList: dayHotWeiboSite])
    }

    private List getHotWeiboNewsByTime(
            def siteNameList, def noSiteNameList, Date startDate, Date endDate, int maxCount) {
        SearchResponse scrollResp = null
        def resultMap = [:]
        def result = []
        while (true) {
            if (!scrollResp) {
                scrollResp = newsRepo.getHotWeiboNews(siteNameList, noSiteNameList, startDate, endDate)
            } else {
                scrollResp = newsRepo.getScrollData(scrollResp)
            }
            if (scrollResp.getHits().getHits().length == 0) {
                break;
            }
            for (SearchHit hit : scrollResp.getHits().getHits()) {
                def news = hit.getSource()
                if (resultMap.containsKey(news.content)) {
                    continue
                }
                resultMap.put(news.content, news)
                if (resultMap.size() >= maxCount) {
                    break
                }
            }
            if (resultMap.size() >= maxCount) {
                break
            }
        }
        resultMap.each { key, value ->
            value.put("captureTime", DateUitl.convertEsDate(value.publishTime as String).getTime())
            result << value
        }
        return result
    }

    private List<Map<String, Object>> getTopRecord(List<Map<String, Object>> list, int limit) {
        def result = []
        def simFilter = new HashSet()
        for (Map<String, Object> obj : list) {
            if (!simFilter.contains(obj.simhash) && result.size() < limit) {
                result << obj
                simFilter.add(obj.simhash)
            }
            if (simFilter.size() == limit)
                break
        }
        return result
    }

    Map getSitesWholeNetFullDay(Long userId) {
        def result = siteRepo.getSitesWholeNetHotNewsFromEs(false, 24, 100, 1)
        def record = getTopRecord(result, 5)
        if (record.size() < 5) {
            def recall = getLastSitesNews(false, 100, 1, 3)
            record += getTopRecord(recall.msg as List<Map<String, Object>>, 5 - record.size())
        }
        record.each { it ->
            def transfer = DateUitl.convertEsDate(it.captureTime as String)
            it.remove("captureTime")
            it.put("captureTime", transfer.getTime())
        }
        record.each { it ->
            def transfer = DateUitl.convertEsDate(it.captureTime as String)
            it.remove("captureTime")
            it.put("captureTime", transfer.getTime())
        }
        return apiResult([status: SC_OK, msg: record])
    }

    void importSiteClassification(String pathname) {
        InputStream fis = new FileInputStream(new File(pathname))
        HSSFWorkbook book = new HSSFWorkbook(fis);
        HSSFSheet sheet = book.getSheetAt(0);
        int lastRow = sheet.getLastRowNum();
        def resultList = []
        for (int i = 1; i < lastRow + 1; i++) {
            Map map = new HashMap();
            Map weChatMap = new HashMap();
            HSSFRow row = sheet.getRow(i);
            if (row != null) {
                def cellCount = row.lastCellNum
                def credibility = [];
                for (int j = 0; j < cellCount + 1; j++) {
                    def cellString = ""

                    HSSFCell cell = row.getCell(j);
                    if (cell) {
                        if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
                            cellString = cell.getNumericCellValue() as String
                        } else {
                            cellString = cell.getStringCellValue()
                        }
                    }
                    if (cellString) {
                        cellString = cellString.trim()
                    }
                    switch (j) {
                        case 0:
                            map.put("siteName", cellString)
                            weChatMap.put("siteName", cellString)
                            break;
                        case 1:
                            map.put("url", cellString)
                            weChatMap.put("url", "")
                            break;
                        case 2:
                            map.put("siteDomain", cellString)
                            weChatMap.put("siteDomain", "")
                            break;
                        case 3:
                            map.put("classification", cellString)
                            weChatMap.put("classification", cellString)
                            break;
                        case 4:
                            map.put("attr", cellString)
                            weChatMap.put("attr", cellString)
                            break;
                        case 5:
                            map.put("area", cellString)
                            weChatMap.put("area", cellString)
                            break;
                        case 6:
                            if (cellString.equals("1.0")) {
                                credibility << "中央媒体"
                            }
                            break;
                        case 7:
                            if (cellString.equals("1.0")) {
                                credibility << "其他"
                            }
                            break;
                        case 8:
                            if (cellString.equals("1.0")) {
                                credibility << "一级资质"
                            }
                            break;
                        case 9:
                            if (cellString.equals("1.0")) {
                                credibility << "第一梯队"
                            }
                            break;
                        case 10:
                            if (cellString.equals("网站")) {
                                map.put("siteType", "网站")
                            }
                            break;
                        case 11:
                            map.put("level", Double.valueOf(cellString) as int)
                            weChatMap.put("level", Double.valueOf(cellString) as int)
                            break;
                        case 12:
                            weChatMap.put("siteName", cellString)
                            break
                        case 13:
                            weChatMap.put("wechatName", cellString ?: cellString.trim())
                            break
                        case 14:
                            def tags = []
                            if (cellString) {
                                cellString = cellString.replaceAll("，", ",")
                            }
                            def tag = cellString.split(",")
                            for (int k = 0; k < tag.size(); k++) {
                                if (tag[k])
                                    tags << tag[k]
                            }
                            map.put("tags", tags)
                            weChatMap.put("tags", tags)
                            break
                    }
                }
//                map.put("credibility", credibility)
//                weChatMap.put("credibility", credibility)
                map.put("_id", UUID.randomUUID().toString())
                weChatMap.put("_id", UUID.randomUUID().toString())
                map.put("createTime", new Date())
                map.put("updateTime", new Date())
                weChatMap.put("createTime", new Date())
                weChatMap.put("updateTime", new Date())
            }
            resultList << map
            if (!(weChatMap.siteName.equals("/"))) {
                weChatMap.put("siteType", "微信公众号")
                resultList << weChatMap
            }
        }
        siteRepo.addSiteDetailList(resultList)
    }

    void importGHMSSiteClassification(String pathname) {
        InputStream fis = new FileInputStream(new File(pathname))
        HSSFWorkbook book = new HSSFWorkbook(fis);
        HSSFSheet sheet = book.getSheetAt(0);
        int lastRow = sheet.getLastRowNum();
        def resultList = []
        for (int i = 1; i < lastRow + 1; i++) {
            Map map = new HashMap();
//            Map weChatMap = new HashMap();
            HSSFRow row = sheet.getRow(i);
            if (row != null) {
                def cellCount = row.lastCellNum
                def credibility = [];
                for (int j = 0; j < cellCount + 1; j++) {
                    def cellString = ""

                    HSSFCell cell = row.getCell(j);
                    if (cell) {
                        if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
                            cellString = cell.getNumericCellValue() as String
                        } else {
                            cellString = cell.getStringCellValue()
                        }
                    }
                    if (cellString) {
                        cellString = cellString.trim()
                    }
                    switch (j) {
                        case 0:
                            map.put("siteName", cellString)
                            break;
                        case 1:
                            map.put("url", cellString)
                            break;
                        case 2:
                            map.put("siteDomain", cellString)
                            break;
                        case 3:
                            map.put("classification", cellString)
                            break;
                        case 4:
                            map.put("attr", cellString)
                            break;
                        case 5:
                            map.put("area", cellString)
                            break;
                    /*case 6:
                        if (cellString.equals("1.0")) {
                            credibility << "中央媒体"
                        }
                        break;
                    case 7:
                        if (cellString.equals("1.0")) {
                            credibility << "其他"
                        }
                        break;
                    case 8:
                        if (cellString.equals("1.0")) {
                            credibility << "一级资质"
                        }
                        break;
                    case 9:
                        if (cellString.equals("1.0")) {
                            credibility << "第一梯队"
                        }
                        break;*/
                        case 6:
                            if (cellString.equals("网站")) {
                                map.put("siteType", "网站")
                            }
                            break;
                    /*case 11:
                        map.put("level", Double.valueOf(cellString) as int)
                        weChatMap.put("level", Double.valueOf(cellString) as int)
                        break;
                    case 12:
                        weChatMap.put("siteName", cellString)
                        break
                    case 13:
                        weChatMap.put("wechatName", cellString ?: cellString.trim())
                        break
                    case 14:
                        def tags = []
                        if (cellString) {
                            cellString = cellString.replaceAll("，", ",")
                        }
                        def tag = cellString.split(",")
                        for (int k = 0; k < tag.size(); k++) {
                            if (tag[k])
                                tags << tag[k]
                        }
                        map.put("tags", tags)
                        weChatMap.put("tags", tags)
                        break*/
                    }
                }
//                map.put("credibility", credibility)
//                weChatMap.put("credibility", credibility)
                map.put("_id", UUID.randomUUID().toString())
                map.put("createTime", new Date())
                map.put("updateTime", new Date())
            }
            resultList << map
//            if (!(weChatMap.siteName.equals("/"))) {
//                weChatMap.put("siteType", "微信公众号")
//                resultList << weChatMap
//            }
        }
        siteRepo.addGhmsSiteDetailList(resultList)
    }

    void importSiteVsWeibo(String pathname) {
        InputStream fis = new FileInputStream(/*"F:/20171009weibo.xls"*/ new File(pathname))
        HSSFWorkbook book = new HSSFWorkbook(fis);
        HSSFSheet sheet = book.getSheetAt(0);
        int lastRow = sheet.getLastRowNum();
        def resultList = []
        for (int i = 1; i < lastRow + 1; i++) {

            SiteVsWeibo weibo1 = new SiteVsWeibo()
            SiteVsWeibo weibo2 = new SiteVsWeibo()
            HSSFRow row = sheet.getRow(i);
            if (row != null) {
                HSSFCell cellSiteName = row.getCell(0);
                HSSFCell cellWebsiteDomain = row.getCell(1);
                HSSFCell cellWeiboName = row.getCell(2);
                HSSFCell cellWeiXinName = row.getCell(3);

                if ((!cellSiteName) || (!cellWeiboName) || (!cellWebsiteDomain)) {
                    continue;
                }
                String valSiteName = cellSiteName.getStringCellValue();
                String valWebsiteDomain = cellWebsiteDomain.getStringCellValue();
                String valWeiboName = "/".equals(cellWeiboName.getStringCellValue()) ? "" : cellWeiboName.getStringCellValue();
                String valWeiXinName = "/".equals(cellWeiXinName.getStringCellValue()) ? "" : cellWeiXinName.getStringCellValue();
                if ((!valSiteName) || (!valWeiboName)) {
                    continue;
                }
                if (valWebsiteDomain) {
                    weibo1.siteType = 1
                    weibo1.siteName = valSiteName
                    weibo1.siteDomain = valWebsiteDomain
                    weibo1.weiboName = valWeiboName
                    weibo1.createTime = new Date()
                    weibo1.updateTime = new Date()
                    resultList.add(weibo1)
                }
                if (valWeiXinName) {
                    weibo2.siteType = 2
                    weibo2.siteName = valWeiXinName
                    weibo2.weiboName = valWeiboName
                    weibo2.createTime = new Date()
                    weibo2.updateTime = new Date()
                    resultList.add(weibo2)
                }
            }
        }
        siteRepo.removeSiteVsWeibo()
        siteRepo.addSiteVsWeiboList(resultList)
        return
    }

    Map importSiteMessage(String pathname) {
//        String pathname = "D:\\查询无数据的域名站点\\excel.xls"
        InputStream fis = new FileInputStream(new File(pathname))
        HSSFWorkbook book = new HSSFWorkbook(fis);
        HSSFSheet sheet = book.getSheetAt(0);
        int lastRow = sheet.getLastRowNum();
        def resultList1 = []
        def resultList2 = []
        for (int i = 1; i < lastRow + 1; i++) {
            Map map = new HashMap();
            Map weChatMap = new HashMap();
            HSSFRow row = sheet.getRow(i);
            if (row != null) {
                HSSFCell cellId = row.getCell(0);
                HSSFCell cellSiteName = row.getCell(2);
                HSSFCell cellWebsiteDomain = row.getCell(3);
                HSSFCell cellSiteTypeName = row.getCell(6);
                HSSFCell cellMessage = row.getCell(8);

                if ((!cellId) || (!cellSiteName) || (!cellSiteTypeName) || (!cellMessage)) {
                    continue;
                }
                String valId = cellId.getStringCellValue();
                String valSiteName = cellSiteName.getStringCellValue();
                String valWebsiteDomain = cellWebsiteDomain.getStringCellValue();
                String valSiteTypeName = cellSiteTypeName.getStringCellValue();
                String valMessage = cellMessage.getStringCellValue();
                int siteType = 0
                if ("网站".equals(valSiteTypeName)) {
                    siteType = 1
                } else {
                    siteType = 2
                }
                if (("网站".equals(valSiteTypeName)) && ("".equals(valWebsiteDomain))) {
                    continue;
                }
                if (valId && valMessage) {
                    map.put("siteName", valSiteName)
                    map.put("websiteDomain", valWebsiteDomain)
                    map.put("siteType", siteType)
                    map.put("message", valMessage)
                }
            }
            if (map.get("siteType") == 1) {
                resultList1 << map
            } else if (map.get("siteType") == 2) {
                resultList2 << map
            }
        }
        boolean result = siteRepo.modifySiteMessageBySiteType(resultList1, resultList2)
        List siteList = siteRepo.getSiteListBySiteType(resultList1, resultList2)
        siteList.each {
            messageRepo.addMessage(it.userId, "站点设置提示", "您设置的站点 " + it.siteName + " " + it.message)
        }
        if (result) {
            return apiResult([status: HttpStatus.SC_OK, msg: "修改成功"])
        } else {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "修改失败"])
        }
    }

    Map getUrlSuggestion(int siteType, String url, String name) {
        def result = []
        if (!siteType) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "请选择站点类型"])
        } else {
            if (url && name) {
                return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "请输入站点名称或者站点域名"])
            }
            result = siteRepo.searchSite(siteType, url, name)
        }

        return apiResult([status: HttpStatus.SC_OK, msg: result])
    }

    List<Site> getAllSites() {
        List<Site> sites = siteRepo.getAllSites()
        if (sites.size() == 0) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "没有站点信息"])
        }
        return sites
    }

    List<Site> getSitesByDate(Date timeStart, Date timeEnd) {
        List<Site> sites = siteRepo.getSitesByDate(timeStart, timeEnd)
        if (sites.size() == 0) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "没有站点信息"])
        }
        return sites
    }

    boolean siteAutoPush() {
        def pageSize = 10
        def pageNo = 1
        def esPageSize = 50
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss")
        while (true) {
            //拿到所有配置autopush的site
            List siteList = siteRepo.getAutoPushSites(pageNo, pageSize)
//            log.info("siteList {}", siteList)
            if (!(siteList.size() > 0)) break
            Map siteMap = new HashMap<String, Object>();
            for (int i = 0; i < siteList.size(); i++) {
                def newsPageNo = 1
                siteMap = siteList.get(i)
                Site site = siteMap.site
                def siteAutoPush = siteMap.siteAutoPush
//                log.info("site {}", site)
//                log.info("siteAutoPush {}", siteAutoPush)
                def prevEndTime = null
                siteAutoPush.prevStartTime = siteAutoPush.prevEndTime
                def user = accountRepo.getUserById(site.userId)
                if ("0".equals(user.orgId) || "0".equals(user.orgId) || (!user.orgId)) {
                    continue
                }
                def team = teamRepo.getTeam(user.teamId)
                String publisher = user?.realName ?: user.userName;
                String teamName = team?.teamName ?: "";
                //循环请求api拿到对应的信息列表
                def isUpdated = false
                SearchResponse scrollResp = null
                while (true) {
                    if (!scrollResp) {
                        Date startTime = DateUitl.convertAutoPushDate(siteAutoPush.prevStartTime)
                        scrollResp = siteRepo.getSiteNewsByTime(site, esPageSize, startTime, new Date())
                    } else {
                        scrollResp = siteRepo.getScrollData(scrollResp, 100000)
                    }
                    if (scrollResp.getHits().getHits().length == 0) {
                        break;
                    }
                    def newsIdList = []
                    def newsList = []
                    for (SearchHit hit : scrollResp.getHits().getHits()) {
                        def news = hit.getSource()
                        if ("Y".equals(news.htmlContent)) {
                            news.htmlContent = newsRepo.getNewsHtmlContent(news.id)
                        } else {
                            news.htmlContent = ""
                        }
                        if (news.get("htmlContent")) {
                            news.content = StringUtils.illegalText2Html(news.htmlContent)
                            news.remove("htmlContent")
                            news.isHtmlContent = true
                            news.imgUrls = []
                        } else {
                            //不带样式htmlContent的数据，存储前需要去做一些样式的处理
                            news.content = StringUtils.ContentText2Html(news.content)
                            news.isHtmlContent = false
                        }
                        if (news) {
                            newsList.add(news)
                            newsIdList.add(news.id)
                            //将拿到的信息推送到信息列表
                            isUpdated = true
                            prevEndTime = news.captureTime
                        }
                    }
                    this.createNewsPush(newsList, site.userId, user.orgId, user.teamId, teamName, publisher, newsIdList.join(','), site.siteId)
                    if (scrollResp.getHits().getHits().length < esPageSize) {
                        break;
                    }
                }
                //更新siteAutoPush
                if (isUpdated) {
                    siteAutoPush.prevEndTime = simpleDateFormat.format(DateUitl.convertEsDate(prevEndTime))
                    siteAutoPush.updateTime = new Date()
                    siteRepo.modifySiteAutoPush(siteAutoPush)
                }
            }
            pageNo++
        }
    }

    def createNewsPush(
            def newsList, long userId, String orgId, String teamId, String teamName, String publisher, String newsIds, String siteId) {
        List newsIdList = newsIds.split(",");
        newsIdList = newsIdList.unique();
        def now = new Date();
        def newsDetailList = new ArrayList();
        SimpleDateFormat pushDayFormat = new SimpleDateFormat("yyyyMMdd");
        newsList.each {
            it.publishDay = pushDayFormat.format(DateUitl.convertEsDate(it.publishTime))
            it.publishTime = DateUitl.convertEsDate(it.publishTime)
            def newsDetail = [
                    newsId    : it.id,
                    news      : it,
                    pushType  : PushTypeEnum.NEWS_PUSH.index,
                    status    : Const.PUSH_STATUS_NOT_PUSH,
                    siteId    : siteId,
                    orgId     : orgId,
                    teamId    : teamId,
                    teamName  : teamName,
                    publisher : publisher,
                    isAutoPush: true,
                    userId    : userId,
                    updateTime: now,
                    createTime: now,

            ]
            newsDetailList.add(newsDetail);
        }
        return siteRepo.addNewsPushList(userId, orgId, newsDetailList, newsIdList);
    }

    Map getSiteClassification(String type, String key) {
        List result = new ArrayList()
        def content = []
        SystemSetting setting = settingRepo.getSystemSetting(type, key)
        if (setting && setting.content) {
            content = setting.content
        }
        if (content) {
            for (int i = 0; i < content.size(); i++) {
                Map map = new HashMap()
                map.put("value", content[i])
                result << map
            }
        }
        return apiResult([status: HttpStatus.SC_OK, msg: result])
    }

    //判断中siteDetail是否包站点site
    def siteDetailIsEqualsSite(Site site, SiteDetail siteDetail) {
        def websiteDomain = site.websiteDomain
        def type = site.siteType
        def name = site.websiteName
        def siteDomain = siteDetail.siteDomain
        def siteType = siteDetail.siteType
        def siteName = siteDetail.siteName
        if (type == 1 && "网站".equals(siteType) && siteDomain.equals(websiteDomain)) {
            return true
        } else if (type == 2 && "微信公众号".equals(siteType) && siteName.equals(name)) {
            return true
        } else if (type == 3 && "微博".equals(siteType) && siteName.equals(name)) {
            return true
        } else {
            return false
        }
    }

    def siteDetailIsContainsOrNotContainsUserSite(List<Site> sitesList, SiteDetail siteDetail) {
        boolean result = false
        for (int j = 0; j < sitesList.size(); j++) {
            def site = sitesList[j];
            result = siteDetailIsEqualsSite(site, siteDetail)
            if (result) {
                break
            }
        }
        return result
    }

    Map getSiteAttr(String type, String key) {
        List result = new ArrayList()
        def content = []
        SystemSetting setting = settingRepo.getSystemSetting(type, key)
        if (setting && setting.content) {
            content = setting.content
        }
        if (content) {
            for (int i = 0; i < content.size(); i++) {
                Map map = new HashMap()
                map.put("value", content[i])
                result << map
            }
        }
        return apiResult([status: HttpStatus.SC_OK, msg: result])
    }

    Map getSiteArea(String type, String key) {
        List result = new ArrayList()
        def content = []
        SystemSetting setting = settingRepo.getSystemSetting(type, key)
        if (setting && setting.content) {
            content = setting.content
        }
        if (content) {
            for (int i = 0; i < content.size(); i++) {
                Map map = new HashMap()
                map.put("value", content[i])
                result << map
            }
        }
        result.remove(0)
        return apiResult([status: HttpStatus.SC_OK, msg: result])
    }

    def getUserAllHeadLineSite(long userId) {
        def headLineSites = []
        def siteList = siteRepo.getUserSitesByType(userId, Site.SITE_TYPE_WEBSITE)
        List<SiteDetail> list = siteRepo.getAllHeadLineSites()
        siteList.each { elem ->
            list.each { elem1 ->
                if (elem.websiteDomain.equals(elem1.siteDomain)) {
                    headLineSites.add(elem)
                }
            }
        }
        return headLineSites
    }

    def getUserSelectedHeadLineSite(long userId, String siteIds) {
        def headLineSites = []
        List siteIdList = siteIds.split(",")
        def siteList = siteRepo.getUserSitesByIds(userId, siteIdList)
        List<SiteDetail> list = siteRepo.getAllHeadLineSites()
        siteList.each { elem ->
            list.each { elem1 ->
                if (elem.websiteDomain.equals(elem1.siteDomain)) {
                    headLineSites.add(elem)
                }
            }
        }
        return headLineSites
    }

    def getUserSelectSite(long userId, String siteIds) {
        List siteIdList = siteIds.split(",")
        def siteList = siteRepo.getUserSitesByIds(userId, siteIdList)
        return siteList
    }

    def getUserSite(long userId) {
        return siteRepo.getUserSitesByType(userId, Site.SITE_TYPE_WEBSITE)
    }

    def saveWeiboKeyWords(long userId, String weiboKeyWords) {
        def content = []
        if (weiboKeyWords) {
            def weiboKeyWordList = weiboKeyWords.split(",")
            for (int i = 0; i < weiboKeyWordList.size(); i++) {
                def weiboKeyWord = weiboKeyWordList[i]
                weiboKeyWord = weiboKeyWord.trim()
                weiboKeyWord = weiboKeyWord.replaceAll("[　]+", "")
                weiboKeyWord = weiboKeyWord.replaceAll("[ ]+", " ")
                content << weiboKeyWord
            }
        }
        def weiboKey = accountCustomSettingRepo.getOneAccountCustomSettingByKey(userId, DashboardEnum.weiboKeyWords.key)
        if (!weiboKey) {
            def account = accountRepo.getUserById(userId)
            accountCustomSettingRepo.addAccountCustomSetting(userId, DashboardEnum.weiboKeyWords.key, content, account.orgId)
        } else {
            accountCustomSettingRepo.modifyAccountCustomSetting(userId, DashboardEnum.weiboKeyWords.key, content)
        }
    }


    void importNewsMonitorSite(String pathname) {
        InputStream fis = new FileInputStream(new File(pathname))
        HSSFWorkbook book = new HSSFWorkbook(fis);
        HSSFSheet sheet = book.getSheetAt(0);
        int lastRow = sheet.getLastRowNum();
        def resultList = []
        for (int i = 1; i < lastRow + 1; i++) {
            Map map = new HashMap();
            HSSFRow row = sheet.getRow(i);
            if (row != null) {
                def cellCount = row.lastCellNum
                def credibility = [];
                for (int j = 0; j < cellCount + 1; j++) {
                    def cellString = ""

                    HSSFCell cell = row.getCell(j);
                    if (cell) {
                        if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
                            cellString = cell.getNumericCellValue() as String
                        } else {
                            cellString = cell.getStringCellValue()
                        }
                    }
                    if (cellString) {
                        cellString = cellString.trim()
                    }
                    switch (j) {
                        case 0:
                            map.put("siteName", cellString)
                            break;
                        case 1:
                            map.put("siteType", cellString)
                            break;
                        case 2:
                            map.put("siteDomain", cellString)
                            break;

                    }
                }
//                map.put("credibility", credibility)
//                weChatMap.put("credibility", credibility)
                map.put("_id", UUID.randomUUID().toString())
                map.put("createTime", new Date())
                map.put("updateTime", new Date())
            }
            resultList << map

        }
        siteRepo.addNewsMonitorSite(resultList)
    }


    Map importNewsList() {
        String pathname = "D:\\台湾女学生五线城市新闻数量.xls"
        InputStream fis = new FileInputStream(new File(pathname))
        HSSFWorkbook book = new HSSFWorkbook(fis);
        HSSFSheet sheet = book.getSheetAt(0);
        List resultList = []
        int lastRow = sheet.getLastRowNum();
        List weiboList = []
        for (int i = 1; i < lastRow + 1; i++) {
            Map map = new HashMap();
            HSSFRow row = sheet.getRow(i);
            if (row != null) {
                String indexNo = row.getCell(0).getNumericCellValue() as String
                String title = row.getCell(1).getStringCellValue().trim()
                String publishTime = row.getCell(2).getStringCellValue().trim()
                String source = row.getCell(3).getStringCellValue().trim()
                String author = row.getCell(4).getStringCellValue().trim()
                String url = row.getCell(5).getStringCellValue().trim()
                String orientation = row.getCell(6).getStringCellValue().trim()
                String newsType = row.getCell(7).getStringCellValue().trim()
                String newsId = Md5Util.md5(url)
                map.put("indexNo", indexNo)
                map.put("title", title)
                map.put("publishTime", publishTime)
                map.put("source", source)
                map.put("author", author)
                map.put("url", url)
                map.put("orientation", orientation)
                map.put("newsType", newsType)
                map.put("_id", newsId)
                def currNews = newsRepo.getNewsById(newsId)
                if (currNews) {
                    map.put("replyCount", currNews.replyCount)
                    map.put("reprintCount", currNews.reprintCount)
                    map.put("commentCount", currNews.commentCount)
                    map.put("likeCount", currNews.likeCount)
                    resultList.add(map)
                    newsRepo.addNews(map)
                } else if ("微博".equals(newsType)) {
                    weiboList.add(map)
                } else if ("微信".equals(newsType)) {
                    Map weiChatMap = newsRepo.getWeChatNewsByTitle(author, title)
                    if (weiChatMap) {
                        map.put("replyCount", weiChatMap.replyCount)
                        map.put("reprintCount", weiChatMap.reprintCount)
                        map.put("commentCount", weiChatMap.commentCount)
                        map.put("likeCount", weiChatMap.likeCount)
                    }
                    newsRepo.addNews(map)
                } else {
                    newsRepo.addNews(map)
                }
            }
        }
        def weiboInfoMap = [:]
        def result = weiboService.getWeiboInfo(weiboList.subList(0, 99))
        weiboInfoMap.putAll(result)
        weiboInfoMap.putAll(weiboService.getWeiboInfo(weiboList.subList(100, 199)))
        weiboInfoMap.putAll(weiboService.getWeiboInfo(weiboList.subList(200, 299)))
        weiboInfoMap.putAll(weiboService.getWeiboInfo(weiboList.subList(300, weiboList.size())))
        weiboList.each {
            if (weiboInfoMap.get(it.url)) {
                it.put("reprintCount", weiboInfoMap.get(it.url).reposts)
                it.put("commentCount", weiboInfoMap.get(it.url).comments)
                it.put("likeCount", weiboInfoMap.get(it.url).attitudes)
            }
            newsRepo.addNews(it)
        }
        return
    }

    def getSiteNewsCountsByUser(long userId, Integer siteType) {
        long sys_startTime = System.currentTimeMillis();

        def siteList = (siteType == null) ? siteRepo.getUserSites(userId) : siteRepo.getUserSitesByType(userId, siteType)

        def map = [:]
        for (Site site : siteList) {
            map[site.siteId] = this.getSiteNewsCountBySite(site)
        }

        long sys_endTime = System.currentTimeMillis()
        println(sys_endTime - sys_startTime)
        return map
    }

    def getSiteNewsCountBySite(long userId, Site site) {

        long sys_st = System.currentTimeMillis();

        Date startDate = site.resetTime ?: null
        long count = siteRepo.getSiteNewsCountByTime(site, startDate, null)
        siteRepo.modifySiteCountInfo(userId, site.siteId, count)

        long sys_et = System.currentTimeMillis()
        return [
                count        : count,
                siteName     : site.siteName,
                websiteName  : site.websiteName,
                websiteDomain: site.websiteDomain,
                siteType     : site.siteType,
                time         : sys_et - sys_st
        ]
    }

    /**
     * 获取用于在某一站点的新闻未读数（自用户上次查看过该站点后，站点新采集的新闻数）
     * @param userId
     * @param siteId
     * @return
     */
    def getSiteNewsCountBySiteId(long userId, String siteId) {

        Site site = siteRepo.getUserSiteById(userId, siteId)

        if (site.countTime
                && System.currentTimeMillis() - site.countTime.time < 10 * 60 * 1000        //如果统计间隔小于10分钟，则返回上次统计的数量
        ) {
            return [
                    count        : site.count,
                    siteName     : site.siteName,
                    websiteName  : site.websiteName,
                    websiteDomain: site.websiteDomain,
                    siteType     : site.siteType
            ]
        } else {
            return this.getSiteNewsCountBySite(userId, site)
        }
    }

    def resetSiteCountInfo(long userId, String siteId) {
        siteRepo.resetSiteCountInfo(userId, siteId)
    }

    Map getRecentNewsBySite(Site site) {
        List<Map<String, Object>> result = siteRepo.getRecentNewsBySite(site)
        if (result) {
            return result.get(0)
        } else {
            return null
        }
    }

    List<SiteDetail> getAllSiteDetail() {
        siteRepo.getAllSiteDetail()
    }

    List getXGSJweiboSubject() {

        def list = new ArrayList<>()

        HttpResponse response = null;
        JSONObject object = null;
        try {

            response = Unirest.get(subjectUrlFind)
                    .queryString("token", token)
                    .queryString("id", weiboId)
                    .asJson();
            object = JSONObject.parseObject(response.getBody().toString());
            JSONObject data = object.get("data")
            JSONObject entity = data.get("entity")
            list = entity.get("filter_source")

            return list
        } catch (Exception e) {
            log.info("智慧星光查询主题错误{}", e.toString())
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        String url = "https://xgsj.istarshine.com/v1/search";
        String staturl = "https://xgsj.istarshine.com/v1/subject/view";
        String kafkaResult = "kafka://192.168.20.11:9092,192.168.20.12:9092,192.168.20.14:9092/supervisor_result_test";
        HttpResponse response = null;
        JSONObject object = null;
        String[] result_url = { kafkaResult };
        String[] domain = {};
        try {

            response = Unirest.get(staturl)
//                    .field("id", "771197934454784")
                    .queryString("id", "771197934454784")
                    .queryString("token", "43857c37-8390-4d01-b46d-1c071fdcb153")
                    .asJson();
            object = JSONObject.parseObject(response.getBody().toString());
            JSONObject data = object.get("data")
            JSONObject entity = data.get("entity")
            List<String> get = entity.get("filter_source")

            System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

