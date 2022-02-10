package com.istar.mediabroken.service

import com.alibaba.fastjson.JSONArray
import com.istar.mediabroken.Const
import com.istar.mediabroken.api3rd.CaptureApi3rd
import com.istar.mediabroken.api3rd.YqmsSession
import com.istar.mediabroken.entity.*
import com.istar.mediabroken.entity.capture.Site
import com.istar.mediabroken.repo.account.AccountRepo
import com.istar.mediabroken.repo.CaptureRepo
import com.istar.mediabroken.repo.admin.SettingRepo
import com.istar.mediabroken.utils.DateUitl
import com.istar.mediabroken.utils.DownloadUtils
import com.istar.mediabroken.utils.HttpClientUtil
import com.istar.mediabroken.utils.Paging
import com.istar.mediabroken.utils.SolrPaging
import com.istar.mediabroken.utils.StringUtils
import com.istar.mediabroken.utils.UploadUtil
import com.istar.mediabroken.utils.WordUtils
import com.istar.mediabroken.utils.ZipUitl
import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.time.DateFormatUtils
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.text.SimpleDateFormat

import static com.istar.mediabroken.Const.MOCK_SUBJECT_ID
import static com.istar.mediabroken.Const.SMT_ALL_SITE
import static com.istar.mediabroken.Const.SMT_PROFESSIONAL_SITE
import static com.istar.mediabroken.Const.SMT_WEBCHAT_PUBLCI_ACCOUNT
import static com.istar.mediabroken.Const.SMT_WEB_SITE
import static com.istar.mediabroken.Const.ST_WEBCHAT_PUBLIC_ACCOUNT
import static com.istar.mediabroken.api.ApiResult.apiResult
import static com.istar.mediabroken.api3rd.CaptureApi3rd.SOURCE_TYPE_NEWS
import static com.istar.mediabroken.api3rd.CaptureApi3rd.SOURCE_TYPE_PRINT_MEDIA
import static com.istar.mediabroken.api3rd.CaptureApi3rd.SOURCE_TYPE_WEXIN
import static com.istar.mediabroken.utils.DateUitl.addDay
import static com.istar.mediabroken.utils.DateUitl.getDayBegin
import static com.istar.mediabroken.utils.UrlUtils.stripUrl

@Service
@Slf4j
class CaptureService {

    @Autowired
    CaptureRepo captureRepo

    @Autowired
    AccountRepo accountRepo

    @Autowired
    CaptureApi3rd captureApi3rd

    @Autowired
    SettingRepo settingRepo

    @Value('${hotWeibo.subjectId}')
    String hotWeiboSubjectId

    @Value('${image.upload.path}')
    String UPLOAD_PATH

    @Value('${env}')
    String env

    @Value('${focusNews.yqmsUserId}')
    long focusNewsYqmsUserId

    @Value('${focusNews.yqmsSubjectId}')
    String focusNewsYqmsSubjectId

    @Value('${hotWeixin.yqmsUserId}')
    long hotWeixinYqmsUserId

    @Value('${hotWeixin.yqmsSubjectId}')
    String hotWeixinYqmsSubjectId

    List getValidSites(long userId, Integer siteType) {
        return captureRepo.getValidSites(userId, siteType)
    }

    Map getNewsList(long userId, String siteId, int pageNo, int limit) {
        def site = captureRepo.getSite(userId, siteId)
        if (!site) {
            return [status: HttpStatus.SC_OK, list: []]
        }
        log.debug("{}", site)
        def subjectMap = captureRepo.getSubjectMap(site.subjectId)
        log.debug("{}", subjectMap)
        def yqmsSid = accountRepo.getYqmsSession(subjectMap.yqmsUserId)
//        log.debug("{}", yqmsSid)
        def pageList = captureApi3rd.getNewsList(yqmsSid, subjectMap.yqmsUserId, subjectMap.yqmsSubjectId, site.siteType, pageNo, limit)
        log.debug("{}", pageList)
        return pageList
    }

    Map getNews(String newsId) {
        if (newsId.startsWith('solr:')) {
            def news = captureApi3rd.getNewsBySolr(newsId.substring(5))
            if (news) {
                return toNews(news)
            } else {
                [:]
            }
        } else {
            // todo 随机获取一个用户session,看看行不行
            def yqmsSession = accountRepo.getYqmsSession()
            return captureApi3rd.getNews(yqmsSession.session, yqmsSession.yqmsUserId, newsId)
        }
    }
/**
 * 我的素材编辑页，新闻详情的获取
 * @param newsId
 * @return
 */
    Map getNewsById4Material(String newsId) {
        def news = getNews(newsId)
        if(null == news || news.size() == 0){
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg: "没有找到相关的新闻素材"])
        }
        def rep = [:]
        rep.type = 1
        rep.title = news.title
        rep.author = ""
        rep.contentAbstract = news.abstract
        rep.keyWords = ""
        rep.classification = ""
        rep.source = news.source
        rep.originalAuthor = news.author
        rep.firstPublishSite = news.firstPublishSite
        rep.firstPublishTime = news.firstPublishTime.getTime()
        rep.originalUrl = news.url
//        rep.publishTime = news.publishTime.getTime()
//        rep.reprintCount = news.reprintCount
        rep.content = news.content
        // 获取文章内容中的的图片
        def picList = []
        def pic = "";
        picList.addAll(StringUtils.extractImgUrl(news.content))
        //根据图片大小，取出第一张
        try {
            InputStream is = null
            BufferedImage img = null

            picList?.each {
                is = new URL(it).openStream()
                img = ImageIO.read(is)
                if(img.width > 300 && img.height > 200){
                    pic = it
                    return
                }
            }
        }catch (Exception e) {
            pic = picList?.get(0)
        }
        rep.imgs = picList && picList.size() > 1 ? picList.unique() : picList;
        rep.picUrl=pic;
        return apiResult([detail: rep])
    }
    private static Map toNews(News news) {
        return [
                newsId: news.newsId,
                title: news.title,
                contentAbstract: news.contentAbstract,
                source: news.source,
                author: news.author ? news.author : news.source,
                url: news.url,
                time: news.createTime.getTime(),
                createTime: news.createTime,
                newsType: news.newsType,
                reprintCount: news.reprintCount,
                heat: news.heat,
                content: news.content,
                publishTime: news.createTime,
                firstPublishTime: news.createTime,
                abstract: news.contentAbstract,
                firstPublishSite: news.site,
                source:news.site,
        ]
    }

    List getRelatedNews(String newsId) {
        if (newsId.startsWith('solr:')) {
            def news = captureApi3rd.getNewsBySolr(newsId.substring(5))
            def result = []
            if (news) {
                //当前需求，暂时设置300条
                def newsList = captureApi3rd.getRelatedNewsBySolr(newsId.substring(5),news.title,1,300)
                newsList.list.each {
                    result.add(toNews(it))
                }
            }
            return result;
        }else {
            def yqmsSession = accountRepo.getYqmsSession()
            def rep = captureApi3rd.getRelatedNews(yqmsSession.session, yqmsSession.yqmsUserId, newsId)
            return rep.list
        }
    }

    List getFocusNews(userId, String siteId) {
        def site = captureRepo.getSite(userId, siteId)
        if (!site) {
            return []
        }
        def subjectMap = captureRepo.getSubjectMap(site.subjectId)
        def yqmsSid = accountRepo.getYqmsSession(subjectMap.yqmsUserId)
        def rep = captureApi3rd.getHotNewsToday(yqmsSid, subjectMap.yqmsUserId, subjectMap.yqmsSubjectId)
        return rep.list
    }

    List getHotWeibo(long userId) {
        def yqmsSession = accountRepo.getYqmsSession()
        def rep = captureApi3rd.getHotNewsToday(yqmsSession.session, yqmsSession.yqmsUserId, hotWeiboSubjectId)
        return rep.list
    }

    SpecialFocus getSpecialFocus(long userId) {
        return captureRepo.getSpecialFocus(userId)
    }

    void modifySpecialFocus(long userId, String siteId, String keywords) {
        def oldSpecialFocus = captureRepo.getSpecialFocus(userId)
        def site = captureRepo.getSite2(userId, siteId)
        if (oldSpecialFocus) {
            doModifySpecialFocus(site, oldSpecialFocus, userId, keywords)
        } else {
            addSpecialFocus(keywords, site, userId)
        }
    }

    private void addSpecialFocus(String keywords, Site site, long userId) {
        def newSpecialFocus = new SpecialFocus(
                siteId: site.siteId,
                userId: userId,
                siteName: site.siteName,

                websiteName: site.websiteName,
                websiteDomain: site.websiteDomain,
                channelName: site.channelName,
                channelDomain: site.channelDomain,
                focusKeywords: keywords,
                subjectId: site.subjectId,
                isShow: site.isShow,
                siteType: site.siteType,

                createTime: new Date(),
        )
        def yqmsSession = accountRepo.getYqmsSession2()
        def yqmsSubject
        if (newSpecialFocus.siteType == ST_WEBCHAT_PUBLIC_ACCOUNT) {
            yqmsSubject = captureApi3rd.addWechatSubject(yqmsSession, createSubjectName('wc'), newSpecialFocus.websiteName, newSpecialFocus.focusKeywords)
        } else {
            yqmsSubject = captureApi3rd.addWebsiteSubject(yqmsSession, createSubjectName('st'), newSpecialFocus.websiteDomain, newSpecialFocus.channelDomain, newSpecialFocus.focusKeywords)
        }
        if (yqmsSubject.status == HttpStatus.SC_OK) {
            newSpecialFocus.yqmsUserId = yqmsSession.userId
            newSpecialFocus.yqmsSubjectId = yqmsSubject.subjectId
            newSpecialFocus.yqmsSubjectName = yqmsSubject.subjectName
            captureRepo.modifySpecialfocus(newSpecialFocus)
        }
    }

    private void doModifySpecialFocus(Site site, SpecialFocus oldSpecialFocus, long userId, String keywords) {
        oldSpecialFocus.siteId = site.siteId
        oldSpecialFocus.userId = userId
        oldSpecialFocus.siteName = site.siteName

        oldSpecialFocus.websiteName = site.websiteName
        oldSpecialFocus.websiteDomain = site.websiteDomain
        oldSpecialFocus.channelName = site.channelName
        oldSpecialFocus.channelDomain = site.channelDomain
        oldSpecialFocus.focusKeywords = keywords
        oldSpecialFocus.subjectId = site.subjectId
        oldSpecialFocus.isShow = site.isShow
        oldSpecialFocus.siteType = site.siteType

        def yqmsSession = accountRepo.getYqmsSession2(oldSpecialFocus.yqmsUserId)
        def status
        if (oldSpecialFocus.siteType == ST_WEBCHAT_PUBLIC_ACCOUNT) {
            status = captureApi3rd.modifyWechatSubject(yqmsSession, oldSpecialFocus.yqmsSubjectId, oldSpecialFocus.yqmsSubjectName,
                    oldSpecialFocus.websiteName, oldSpecialFocus.focusKeywords)
        } else {
            status = captureApi3rd.modifyWebsiteSubject(yqmsSession, oldSpecialFocus.yqmsSubjectId, oldSpecialFocus.yqmsSubjectName,
                    oldSpecialFocus.websiteDomain, oldSpecialFocus.channelDomain, oldSpecialFocus.focusKeywords)
        }

        if (status.status == HttpStatus.SC_OK) {
            captureRepo.modifySpecialfocus(oldSpecialFocus)
        }

    }


    int getNewNewsCount(long userId, String siteId, Date updateTime) {
        def site = captureRepo.getSite(userId, siteId)
        if (!site) {
            return 0
        }
        def subjectMap = captureRepo.getSubjectMap(site.subjectId)
        def yqmsSid = accountRepo.getYqmsSession(subjectMap.yqmsUserId)
        def total = captureApi3rd.getNewNewsCount(yqmsSid, subjectMap.yqmsUserId, subjectMap.yqmsSubjectId, updateTime)
        return total.count
    }

    Map getCaptureSiteAccountProfile(long userId){
        AccountProfile accountProfile = accountRepo.getAccountProfileByUser(userId)
        //是否存在，如果不存在则新增一个，如果存在则修改
        if(accountProfile != null && accountProfile.captureSite.size() > 0){
            return accountProfile.captureSite
        }
        //从systemSetting中获取
        SystemSetting setting = settingRepo.getSystemSetting("captureSite","maxSiteCount")
        if(setting == null){
            return [:]
        }
        Date now = new Date();
        if(accountProfile == null){
            accountProfile = new AccountProfile()
            accountProfile.userId = userId
            accountProfile.createTime = now
        }
        accountProfile.captureSite = setting.content
        accountProfile.updateTime = now
        accountRepo.saveAccountProfile(accountProfile)

        return setting.content
    }
    List getSiteByIds(List siteIdList,long userId){
        List<SiteDetail> siteDetailList = captureRepo.getSiteByIds(siteIdList);
        List<Site> siteList = new ArrayList<Site>();
        siteDetailList.each {
            siteList << new Site(
                    userId: userId,
                    siteName: it.siteName,
                    websiteName: it.siteName,
                    websiteDomain: it.siteDomain,
                    channelName: "",
                    channelDomain: "",
                    isShow: true,
                    captureInterval: 0,
                    siteType: it.siteType,
                    isAutoPush: false
            );
        }
        return siteList;
    }
    /**
     * 批量添加站点
     * @param userId
     * @param siteList
     * @return
     */
    Map addSiteList(Long userId, List<Site> siteList){

        if(siteList == null || siteList.size() == 0){
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg: "请输入站点信息"])
        }

        Map setting = getCaptureSiteAccountProfile(userId)
        if(setting == null || setting.size() == 0){
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg: "没有找到相关的系统配置信息"])
        }

        def maxSiteCount = 0
        setting.each {k,v->
            maxSiteCount += v
        }
        //获取当前site的总数
        Map siteCounts = captureRepo.getSiteSummaryCountByType(userId)
        def currSiteTotalCount = 0
        siteCounts.each {
            currSiteTotalCount += it.value
        }
        if((currSiteTotalCount + siteList.size()) > maxSiteCount ){
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,
                              msg: ("站点总数超出了最大的站点数${maxSiteCount}," +
                                      "已经配置了${currSiteTotalCount}个，" +
                                      "还可以添加${maxSiteCount-currSiteTotalCount}个") as String])
        }
        def maxMediaSiteCount = setting.maxMediaSiteCount?:0
        def maxWechatSiteCount = setting.maxWechatSiteCount?:0
        def maxProfessionalSiteCount = setting.maxProfessionalSiteCount?:0

        def mediaSiteDomains = []
        def wechatNames = []
        def professionalSiteDomains = []

        siteList.each { site ->
            site.websiteDomain = stripUrl(site.websiteDomain)
            site.websiteName = site.websiteName.trim()
            switch (site.siteType){
                case 1:
                    mediaSiteDomains << site.websiteDomain
                    break
                case 2:
                    wechatNames << site.websiteName
                    break
                case 3:
                    professionalSiteDomains << site.websiteDomain
                    break
            }

        }

        if(((siteCounts.get("1")?siteCounts.get("1") : 0) + mediaSiteDomains.size()) > maxMediaSiteCount ){
            def siteCount = (siteCounts.get("1")?siteCounts.get("1") : 0);
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,
                              msg: ("媒体网站总数不能超过${maxMediaSiteCount}," +
                                      "媒体网站数${(siteCounts.get("1")?siteCounts.get("1") : 0)}," +
                                      "还可添加${maxMediaSiteCount - siteCount}条") as String])

        }

        if(((siteCounts.get("2")?siteCounts.get("2") : 0) + wechatNames.size()) > maxWechatSiteCount ){
            def siteCount = (siteCounts.get("2")?siteCounts.get("2") : 0);
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,
                              msg: ("微信公众号总数不能超过${maxWechatSiteCount}," +
                                      "微信公众号数${(siteCounts.get("2")?siteCounts.get("2") : 0)}," +
                                      "还可添加${maxWechatSiteCount - siteCount}条") as String])
        }

        if(((siteCounts.get("3")?siteCounts.get("3") : 0) + professionalSiteDomains.size()) > maxProfessionalSiteCount ){
            def siteCount = (siteCounts.get("3")?siteCounts.get("3") : 0);
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,
                              msg: ("专业网站总数不能超过${maxProfessionalSiteCount}," +
                                      "专业网站点数${(siteCounts.get("3")?siteCounts.get("3") : 0)}," +
                                      "还可添加${maxProfessionalSiteCount - siteCount}条") as String])
        }

        def existCount = captureRepo.getSiteCount(userId,1, mediaSiteDomains)
        if(existCount > 0){
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg: "重复的媒体站点输入"])
        }
        existCount = captureRepo.getSiteCount(userId,2, wechatNames)
        if(existCount > 0){
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg: "重复的微信公众号输入"])
        }
        existCount = captureRepo.getSiteCount(userId,3, professionalSiteDomains)
        if(existCount > 0){
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg: "重复的专业站点输入"])
        }
        def siteIds = []
        siteList.each { site ->
            SubjectMap subjectMap
            subjectMap = captureRepo.getSubjectMap(site)
            if (!subjectMap) {
                subjectMap = addSubjectMap(site)
            }
            site.siteId = UUID.randomUUID().toString()
            siteIds << site.siteId
            site.createTime = new Date()
            site.subjectId = subjectMap.subjectId
            captureRepo.addSite(site)
        }
        return apiResult([siteIds:siteIds,status: HttpStatus.SC_OK,msg: "导入成功"])
    }

    /**
     * 添加站点
     * @param site
     * @return
     */
    Map addSite(Site site) {
        Map result = [status: HttpStatus.SC_OK, msg: '导入完成',siteId:'']
        //按照url（微信公众号，名称）查询，判断是否存在相同数据 isSiteExist=true表示已存在该数据，不再添加
        Map setting = getCaptureSiteAccountProfile(site.userId)
        if(setting == null || setting.size() == 0){
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg: "没有找到相关的系统配置信息"])
        }
        def maxSiteCount = 0
        setting.each {k,v->
            maxSiteCount += v
        }
        //获取当前site的总数
        Map siteCounts = captureRepo.getSiteSummaryCountByType(site.userId)
        def currSiteTotalCount = 0
        siteCounts.each {
            currSiteTotalCount += it.value
        }
        if((currSiteTotalCount + 1) > maxSiteCount ){
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,
                              msg: ("站点总数超出了最大的站点数${maxSiteCount}," +
                                      "已经配置了${currSiteTotalCount}个，" +
                                      "还可以添加${maxSiteCount-currSiteTotalCount}个") as String])
        }
        def maxCurrSiteTypeCount = 0
        def currSiteTypeCount = 0
        def siteTypeName = ""
        switch (site.siteType){
            case 1:
                maxCurrSiteTypeCount = setting.maxMediaSiteCount?:0
                siteTypeName = "媒体网站"
                break;
            case 2:
                maxCurrSiteTypeCount = setting.maxWechatSiteCount?:0
                siteTypeName = "微信公众号"
                break;
            case 3:
                maxCurrSiteTypeCount = setting.maxProfessionalSiteCount?:0
                siteTypeName = "专业网站"
                break;
        }
        currSiteTypeCount = siteCounts.get(site.siteType as String)?siteCounts.get(site.siteType as String) : 0

        if(currSiteTypeCount + 1 > maxCurrSiteTypeCount ){
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,
                              msg: ("${siteTypeName}总数不能超过${maxCurrSiteTypeCount}条," +
                                      "当前${siteTypeName}数${currSiteTypeCount}，" +
                                      "还可添加${maxCurrSiteTypeCount - currSiteTypeCount}条") as String])
        }

        if (site.websiteDomain) {
            site.websiteDomain = stripUrl(site.websiteDomain)
        }
        SubjectMap subjectMap
        subjectMap = captureRepo.getSubjectMap(site)
        if (!subjectMap) {
            subjectMap = addSubjectMap(site)
        }
        site.siteId = UUID.randomUUID().toString()
        site.createTime = new Date()
        site.subjectId = subjectMap.subjectId
        boolean isSiteExist = captureRepo.isSiteExist(site,"0");
        if(!isSiteExist){
            result.siteId = captureRepo.addSite(site)
            return result;
        }else{
            result.status=HttpStatus.SC_BAD_REQUEST;
            result.msg='已存在相同站点';
            result.siteId='error';
            return result;
        }
    }

    String sitesTemplate() {
        String filePath ;
        if (Const.ENV_ONLINE == env || Const.ENV_TEST == env) {
            filePath = new File(WordUtils.class.getResource("/").path, 'template').path
        } else {
            filePath = new File(WordUtils.class.getResource("/").path.replace('/classes/', '/resources/'), 'template').path
        }

        def pathAndFile = filePath.toString() + File.separator + "sites.xls";
        return pathAndFile;
    }

    Map sitesImport(long userId,File excelFile){
        def result = [status: HttpStatus.SC_OK, msg: '导入成功',siteId:'']
        String excelFolder = "/${UPLOAD_PATH}/upload/sites";
        //如果文件夹不存在则创建，如果已经存在则删除
        File outPath = new File(excelFolder)
        if(!outPath.exists()){
            FileUtils.forceMkdir(outPath)
        }else {
            FileUtils.forceDelete(outPath)
            FileUtils.forceMkdir(outPath)
        }

        InputStream fis = new FileInputStream(excelFile)

        List<Map<String, String>>  data = UploadUtil.parseExcel(fis);
        List siteList = [];
        def mediaDomains = []
        def weixinNames = []
        def professionalDomains = []
        for (int i=0;i<data.size();i++){
            Map<String, String> map = data.get(i);
            boolean isAutoPush =false;
            if ("是".equals(map.get("自动推送"))){
                isAutoPush=true;
            }
            boolean isShow=false;
            if("是".equals(map.get("显示在导航栏"))){
                isShow=true;
            }
            String siteName =String.valueOf(map.get("导航显示名称"));
            String websiteName = String.valueOf(map.get("站点名称或公众号名称"));
            String websiteDomain=String.valueOf(map.get("站点URL"));
            websiteDomain="null".equals(websiteDomain)?"":websiteDomain;
            String siteType=map.get("所属类型");
            int siteTypeInt=1;//        siteType 1媒体网站 2微信公众号 3专业网站
            switch (siteType){
                case "媒体网站":
                    siteTypeInt=1;
                    mediaDomains << websiteDomain
                    break;
                case "微信公众号":
                    siteTypeInt=2;
                    weixinNames << websiteName
                    break;
                case "专业网站":
                    siteTypeInt=3;
                    professionalDomains << websiteDomain
                    break;
            }
            if ("微信公众号".equals(siteType)&&!"".equals(websiteDomain)){
                result.msg='导入数据有误，请检查后重新导入';
                result.status=HttpStatus.SC_BAD_REQUEST;
                result.siteId="error";
                return result;
            }
            siteList << new Site(
                    userId: userId,
                    siteName: siteName,
                    websiteName: websiteName,
                    websiteDomain: websiteDomain,
                    isShow: isShow,
                    siteType: siteTypeInt,
                    isAutoPush: isAutoPush
            );
        }

        result=addSiteList(userId,siteList)
        return result;
    }

    SubjectMap addSubjectMap(Site site) {
        def yqmsSession = accountRepo.getYqmsSession()
        def yqmsSubjectId
        def subjectType
        if (site.siteType == ST_WEBCHAT_PUBLIC_ACCOUNT) {
            subjectType = SMT_WEBCHAT_PUBLCI_ACCOUNT
            // todo 考虑用拼音或者或者名称来显示
            def subjectName = createSubjectName('wc')
            yqmsSubjectId = [subjectId: MOCK_SUBJECT_ID, subjectName: subjectName]
        } else {
            subjectType = Const.SMT_WEB_SITE
            // todo 考虑用拼音或者或者名称来显示
            def subjectName = createSubjectName('st')
            yqmsSubjectId = [subjectId: MOCK_SUBJECT_ID, subjectName: subjectName]
        }
        def subjectMap = new SubjectMap(
                subjectId: UUID.randomUUID().toString(),
                yqmsSubjectName: yqmsSubjectId.subjectName,
                yqmsSubjectId: yqmsSubjectId.subjectId,
                yqmsUserId: yqmsSession.yqmsUserId as long,
                websiteDomain: site.websiteDomain,
                channelDomain: site.channelDomain,
                account: site.websiteName,
                focusKeywords: site.focusKeywords,
                subjectType: subjectType
        )
        captureRepo.addSubjectMap(subjectMap)
        return subjectMap
    }

    void addRealSubjectId2SubjectMap(SubjectMap subjectMap) {
        def yqmsSession = accountRepo.getYqmsSession2()
        def yqmsSubject
        if (subjectMap.subjectType == SMT_WEBCHAT_PUBLCI_ACCOUNT) {
            yqmsSubject = captureApi3rd.addWechatSubject(yqmsSession, subjectMap.yqmsSubjectName, subjectMap.account, subjectMap.focusKeywords)
        } else {
            yqmsSubject = captureApi3rd.addWebsiteSubject(yqmsSession, subjectMap.yqmsSubjectName, subjectMap.websiteDomain, subjectMap.channelDomain, subjectMap.focusKeywords)
        }
        if (yqmsSubject.status == HttpStatus.SC_OK) {
            subjectMap.yqmsSubjectId = yqmsSubject.subjectId
            captureRepo.modifySubjectMap(subjectMap)
        }
    }

    String createSubjectName(String prefix) {
        def sdf = new SimpleDateFormat("yyMMddHHmmssSSS")
        return prefix + sdf.format(new Date())
    }


    List getSpecialFocusInfo(long userId, int limit) {
        def specialFocus = captureRepo.getSpecialFocus(userId)
        if (!specialFocus) {
            return  []
        }
        def yqmsSid = accountRepo.getYqmsSession(specialFocus.yqmsUserId)
        def pageList = captureApi3rd.getNewsList2(yqmsSid, specialFocus.yqmsUserId, specialFocus.yqmsSubjectId, 1, limit)
        return pageList.list
    }

    Paging<NewsPush> getPagingNewPushList(Long userId, String orgId, int status, int pageNo, int limit) {
        long total = captureRepo.getNewsPushTotal(userId, orgId, status)
        def paging = new Paging<NewsPush>(pageNo, limit, total)
        captureRepo.getNewsPushList(userId, orgId, status, paging)
        return paging
    }

    def createNewsPush(long userId, String orgId, String newsIds, String siteId) {
        List newsIdList = newsIds.split(",");
        newsIdList.unique();
        def now = new Date();
        def newsDetailList = new ArrayList();
        SimpleDateFormat pushDayFormat = new SimpleDateFormat("yyyyMMdd");
        newsIdList.each {
            def news = this.getNews(it);
            news.publishDay = pushDayFormat.format(news.publishTime)
            def newsDetail = [
                    newsId      : it,
                    news        : news,
                    pushType    : PushTypeEnum.NEWS_PUSH.index,
                    status      : Const.PUSH_STATUS_NOT_PUSH,
                    siteId      : siteId,
                    orgId       : orgId,
                    userId      : userId,
                    updateTime  : now,
                    createTime  : now,

            ]
            newsDetailList.add(newsDetail);
        }
        return captureRepo.addNewsPushList(userId, orgId, newsDetailList, newsIdList);
    }

    NewsPush getNewsPush(long userId, String orgId, String newsId){
        return captureRepo.getNewsPush(userId, orgId, newsId);
    }
    def getOpenNewsPushList(String orgId){
        captureRepo.getOpenNewsPushList(orgId)
    }

    def updateNewsPush2Pushed(String orgId, String newsIds){
        captureRepo.modifyNewsPushStatus(orgId, newsIds.tokenize(','), Const.PUSH_STATUS_PUSHED)
    }

    List<News> getNewsListByNewsIds(List<String> newsIdList){
        List<News> list = []
        newsIdList?.each {
            // todo 调用接口速度太慢,是否可能用保存的推送数据?
            def news = this.getNews(it)
            if (news) {
                news.newsId = it
                list << News.toObject(news)
            }
        }
        return list
    }
    List<News> getNewsListByOperationIds(List<String> newsOperationIdList){
        return captureRepo.getNewsFromNewsOperationByIds(newsOperationIdList)
    }

    void createNewsShare(long userId, String orgId, String newsId, def shareChannelList, def shareContent) {
        def news = getNews(newsId)
        Date now = new Date()
        captureRepo.addNewsShare([
                newsId      : newsId,
                news        : news,
                shareChannel: shareChannelList,//以后根据开放的渠道，记录所有的渠道
                shareContent: shareContent,
                userId      : userId,
                orgId       : orgId,
                createTime  : now,
                updateTime  : now
        ])
    }

    Paging<NewsShare> getPagingNewsShareList(Long userId, String orgId, int pageNo, int limit) {
        long total = captureRepo.getNewsShareTotal(userId, orgId)
        def paging = new Paging<NewsShare>(pageNo, limit, total)
        captureRepo.getNewsShareList(userId, orgId, paging)
        return paging
    }

    List<NewsPush> getNewsPushList(long userId, String orgId, List<String> newsIdList){
        return newsIdList ? captureRepo.getNewsPushListById(userId, orgId, newsIdList) : null
    }

    // 根据相关新闻计算每天的文章转载数
    Map getNewsStats(String newsId, int days) {
        def news = getRelatedNews(newsId)
        def heatTrend = caculateHeatTrend(news, days)
        def spreadSummary = caculateSpreadSummary(news, days)

        return [heatTrend: heatTrend, spreadSummary: spreadSummary]
    }

    private Map caculateHeatTrend(List news, int days) {
        def startTime = addDay(getDayBegin(), - days)//获取days获取新闻数组的开始时间
        def heatTrend = [:]
        def sdf = new SimpleDateFormat('yyyy-MM-dd')
        news.each {
            if (startTime.getTime() < it.time) {//时间范围内
                def date = sdf.format(new Date(it.time))
                def row = heatTrend.get(date)
                if (row==null) {
                    row = [reprintedCountByMedia: 0, forwardedCountByWeibo: 0]
                    heatTrend.put(date, row)
                }
                row.reprintedCountByMedia++
                if (getMediaType(it.source) == '微博') {
                    row.forwardedCountByWeibo++
                }
            }
        }
        return heatTrend
    }

    private Map caculateSpreadSummary(List news, int days) {
        def startTime = addDay(getDayBegin(), - days)
        def summary = [
                reprintMediaCount: 0,
                reprintedCount: 0,
                collectedCountBySearchEngines: 0,
                publishedCountByKeyChannel: 0,
                forwardedCountByWeibo: 0,
                readingCountFromWeMedia: 0,
                commentsCount: 0,
                likesCount: 0
        ]
        def medias = [:]
        news.each {
            if (startTime.getTime() < it.time) {
                medias.put(it.source, 1)
                summary.reprintedCount++
                if (getMediaType(it.source) == '微博') {
                    summary.forwardedCountByWeibo++
                }
             }
        }
        summary.reprintedCount = summary.reprintedCount > 0 ? summary.reprintedCount - 1 : 0             // 把自己去掉
        summary.reprintMediaCount = medias.size()
        return summary
    }

    String getMediaType(String s) {
        if (s.indexOf('微博') > 0) {
            return '微博'
        } else if (s.indexOf('微信') > 0) {
            return '微信'
        } else {
            return '网站'
        }
    }

    int getSiteCount(long userId) {
        return captureRepo.getSiteCount(userId)
    }

    Map getNewsWord(String newsId,LoginUser user){

        def news = getNews(newsId)
        if(!news) {
            return apiResult(HttpStatus.SC_NOT_FOUND, '新闻不存在，请刷新重试！')
        }
        def data = [
                fileName    : newsId.replaceAll(":",""),
                title       : news.title as String,
                content     : StringUtils.html2text(news.content),
                time        : news.publishTime.toLocaleString()
        ]
        data.fileType = 'news'
        String outfilePath = "/${UPLOAD_PATH}/download/${data.fileType as String}/${data.fileName as String}";
        File outPath = new File(outfilePath)
        if(!outPath.exists()){
            FileUtils.forceMkdir(outPath)
        }else {
            FileUtils.forceDelete(outPath)
            FileUtils.forceMkdir(outPath)
        }
        def result = WordUtils.createWord(UPLOAD_PATH, data, env)
        if(!(result.status.equals(HttpStatus.SC_OK))){
            return result
        }
        List<String> imgList = StringUtils.extractImgUrl(news.content)
        int length = imgList.size()
        for (int i = 0; i < length; i++) {
            String imgUrl = imgList.get(i);
            if(imgUrl.startsWith("https")){
                String outFileName = HttpClientUtil.downLoadFromUrl(imgUrl,i as String,outfilePath)
            }else {
                String outFileName = DownloadUtils.downLoadFromUrl(imgUrl,i as String,outfilePath)
            }
        }
        File file = new File(UPLOAD_PATH.concat(result.msg))
        def fileName = StringUtils.removeSpecialCode(data.get("title"))
        if(fileName.length() > 30){
            fileName = fileName.substring(0,30)
        }
        def zipPath = file.getParent() + "/" + fileName + ".zip"
        ZipUitl.zip(file.getParent(),zipPath )
        result.msg = zipPath
        //下载历史记录
        captureRepo.addNewsDownload(user,newsId,news)
        return result
    }
    Paging<NewsDownload> getPagingNewsDownloadList(Long userId, String orgId, int pageNo, int limit) {
        long total = captureRepo.getNewsDownloadTotal(userId, orgId)
        def paging = new Paging<NewsDownload>(pageNo, limit, total)
        captureRepo.getNewsDownloadList(userId, orgId, paging)
        return paging
    }
    void recommandSites(long userId, String[] contentTypes, String[] mediaTypes) {
        def recommandSites = []
        recommandSites.addAll(captureRepo.getRecommandWebSites(contentTypes, mediaTypes, 5))
        recommandSites.addAll(captureRepo.getRecommandWechatAccounts(contentTypes, mediaTypes, 5))

        recommandSites.each {
            it.userId = userId
            it.isShow = true
            it.channelName = ""
            it.channelDomain = ""
            addSite(it)
        }

        accountRepo.modifyIsSitesInited(userId, true)
    }

    void recommandDefaultSites(long userId) {
        def recommandSites = []

        recommandSites << createSite('千龙网', 'qianlong.com', SMT_PROFESSIONAL_SITE, userId)
        recommandSites << createSite('爱卡汽车', 'xcar.com.cn', SMT_WEB_SITE, userId)
        recommandSites << createSite('凤凰财经', '', SMT_WEBCHAT_PUBLCI_ACCOUNT, userId)
        recommandSites << createSite('人民网', '', SMT_WEBCHAT_PUBLCI_ACCOUNT, userId)

        recommandSites.each {
            addSite(it)
        }

        accountRepo.modifyIsSitesInited(userId, true)
    }

    private Site createSite(String siteName, String siteDomain, int siteType, long userId) {
        new Site(
                siteName: siteName,
                websiteName: siteName,
                websiteDomain: siteDomain,
                siteType: siteType,
                userId: userId,
                isShow: true,
                channelName: "",
                channelDomain: "")
    }

    Paging<News> queryNewsList(long userId, String siteId, String query, int pageNo, int limit) {
        def site = captureRepo.getSite(userId, siteId)
        if (!site) {
            return [status: HttpStatus.SC_OK, list: []]
        }
        def subjectMap = captureRepo.getSubjectMap(site.subjectId)
        def yqmsSid = accountRepo.getYqmsSession(subjectMap.yqmsUserId)
        def pageList = captureApi3rd.queryNewsList(yqmsSid, subjectMap.yqmsUserId, subjectMap.yqmsSubjectId, query, site.siteType, pageNo, limit)
        return pageList
    }

    boolean siteAutoPush(){
        def pageSize = 10
        def pageNo = 1
        while (true){
            //拿到所有配置autopush的site
            List siteList = captureRepo.getAutoPushSites(pageNo,pageSize)
            log.info("siteList {}",siteList)
            if (!(siteList.size() > 0)) break
            siteList.each {
                def newsPageNo = 1
                def site = it.site
                def siteAutoPush = it.siteAutoPush
                log.info("site {}",site)
                log.info("siteAutoPush {}",siteAutoPush)
                def prevEndTime = System.currentTimeMillis()
                siteAutoPush.prevStartTime = siteAutoPush.prevEndTime
                def user = accountRepo.getUserById(site.userId)
                //循环请求api拿到对应的信息列表
                def isUpdated = false
                while (true){
                    Map newsMap = getNewsListByTime(site, siteAutoPush,newsPageNo,pageSize)
                    log.info("newsmap {}",newsMap)
                    def newsSize = newsMap.list.size()
                    if (newsMap.status == HttpStatus.SC_OK && newsSize > 0){
                        def newsList = newsMap.list
                        def newsIdList = []
                        newsList.each {
                            newsIdList.add(it.newsId)
                        }
                        this.createNewsPush(site.userId, user.orgId, newsIdList.join(','), site._id)
                        //将拿到的信息推送到信息列表
                        if(newsPageNo == 1){
                            prevEndTime = newsList.get(0).time
                            def date  = new Date(prevEndTime)
                            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyyMMddHHmmss")
                            String currTime = simpleDateFormat.format(date)
                            siteAutoPush.prevEndTime = currTime
                            siteAutoPush.updateTime = new Date()
                            captureRepo.modifySiteAutoPush(siteAutoPush)
                        }
                        //todo 校验推送的新闻条数是否正确
                        siteAutoPush.prevNewsCount += newsSize
                        siteAutoPush.prevPushCount += newsSize
                        siteAutoPush.totalNewsCount += newsSize
                        siteAutoPush.totalPushCount += newsSize
                        isUpdated = true
                    }else {
                        break
                    }
                    newsPageNo++
                }
                //更新siteAutoPush
                if(isUpdated){
                    siteAutoPush.updateTime = new Date()
                    captureRepo.modifySiteAutoPush(siteAutoPush)
                    log.info("updated siteAutoPush {}",siteAutoPush)
                }
            }
            pageNo++
        }
    }
    Map getNewsListByTime(def site,def siteAutoPush, int pageNo, int limit) {
        def date  = new Date()
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyyMMddHHmmss");
        String currTime = simpleDateFormat.format(date);
        log.debug("{}", site)
        log.debug("{}", siteAutoPush)
        def subjectMap = captureRepo.getSubjectMap(site.subjectId)
        log.debug("{}", subjectMap)
        def yqmsSid = accountRepo.getYqmsSession(subjectMap.yqmsUserId)
        def pageList = captureApi3rd.getNewsListByTime(yqmsSid, subjectMap.yqmsUserId, subjectMap.yqmsSubjectId, pageNo, limit,siteAutoPush.prevEndTime,currTime)
        log.debug("{}", pageList)
        return pageList
    }

    Paging<News> getNewsListBySite(String field,String site, Date start, Date end, int pageNo, int limit) {
        return captureApi3rd.getNewsListBySite(field,site, start, end, pageNo, limit)
    }

    Paging<News> getNewsListByTitle(String title, Date start, Date end,  int pageNo, int limit) {
        return captureApi3rd.getNewsListByTitle(title, start, end, pageNo, limit)
    }

    SolrPaging<News> getNewsListBySite(String field, String site, Date start, Date end, String cursor, int limit) {
        return captureApi3rd.getNewsListBySite(field,site, start, end, cursor, limit)
    }

    SolrPaging<News> getNewsListByTitle(String title, Date start, Date end,  String cursor, int limit) {
        return captureApi3rd.getNewsListByTitle(title, start, end, cursor, limit)
    }


    int getFocusCount(long userId) {
        return captureRepo.getFocusCount(userId)
    }


    String addFocus(Focus focus) {
        def yqmsSession = getYqmsUserForFocusSubject()
        def yqmsSubject = captureApi3rd.addFocusSubject(yqmsSession, focus)

        focus.focusId = UUID.randomUUID().toString()
        focus.yqmsSubjectId = yqmsSubject.subjectId
        focus.yqmsSubjectName = yqmsSubject.subjectName
        focus.yqmsUserId = yqmsSession.userId
        focus.createTime = new Date()
        return captureRepo.addFocus(focus)
    }

    String modifyFocus(Focus newFocus) {
        def oldFocus = captureRepo.getFocus(newFocus)
        newFocus.yqmsSubjectId = oldFocus.yqmsSubjectId
        newFocus.yqmsSubjectName = oldFocus.yqmsSubjectName
        newFocus.yqmsUserId = oldFocus.yqmsUserId
        newFocus.createTime = oldFocus.createTime

        def yqmsSession = accountRepo.getYqmsSession2(newFocus.yqmsUserId)
        captureApi3rd.modifyFocusSubject(yqmsSession, newFocus)
        return captureRepo.modifyFocus(newFocus)
    }

    YqmsSession getYqmsUserForFocusSubject() {
        // todo vvv high 资源分配
        def session = accountRepo.getYqmsSession()
        return new YqmsSession(userId: session.yqmsUserId, sid: session.session)
    }

    List getFocusList(long userId) {
        return captureRepo.getFocusList(userId)
    }

    Focus getFocus(long userId, String focusId) {
        return captureRepo.getFocus(new Focus(userId: userId, focusId: focusId))
    }

    void removeFocus(long userId, String focusId) {
        def focus = captureRepo.getFocus(new Focus(userId: userId, focusId: focusId))
        def session = accountRepo.getYqmsSession2(focus.yqmsUserId)
        captureApi3rd.removeFocusSubject(session, focus)
        captureRepo.removeFocus(focus)
    }

    Paging<News> getFocusNewsList(long userId, String focusId, int pageNo, int limit) {
        def focus = captureRepo.getFocus(new Focus(userId: userId, focusId: focusId))
        if (!focus) {
            return new Paging<News>(pageNo, limit, 0)
        }
        def session = accountRepo.getYqmsSession2(focus.yqmsUserId)
        def pageList = captureApi3rd.getPagingFocusNewsList(session, focus, pageNo, limit)
        return pageList
    }

    List<News> getFocusNewsList(long userId, int limit) {
        def session = getSession(focusNewsYqmsUserId)
        def list = captureApi3rd.getFocusNewsList(session, focusNewsYqmsSubjectId, limit)
        return list
    }

    List<News> getHotWeixinList(long userId, int limit) {
//        def session = getSession(focusNewsYqmsUserId)
//        def list = captureApi3rd.getFocusNewsList(session, focusNewsYqmsSubjectId, limit)
        def session = getSession(hotWeixinYqmsUserId)
        def list = captureApi3rd.getHotWeixinList(session, hotWeixinYqmsSubjectId, limit)
        return list
    }

    private YqmsSession getSession(YqmsSubject subject) {
        def session = accountRepo.getYqmsSession2(subject.yqmsUserId)
        session
    }

    private YqmsSession getSession(long yqmsUserId) {
        def session = accountRepo.getYqmsSession2(yqmsUserId)
        session
    }

    private PersonalAllSite getPersonalAllSiteSubject(long userId) {
        def subject = captureRepo.getPersonlAllSiteSubject(userId)
        if (!subject) {
            def siteInfo = getSiteInfo(userId)
            def session = getUserForPersonalAllSiteSubject()
            println session
            if (!siteInfo.domains && !siteInfo.wecharAccounts) {
                throw new RuntimeException('没有域名和微信帐号信息,无法创建专题')
            }
            def yqmsSubject = captureApi3rd.addAllSiteSubject(session, siteInfo.domains, siteInfo.wecharAccounts)
            if (yqmsSubject) {
                subject = new PersonalAllSite(
                        userId: userId,
                        yqmsUserId: session.userId,
                        yqmsSubjectName: yqmsSubject.yqmsSubjectName,
                        yqmsSubjectId: yqmsSubject.yqmsSubjectId
                )
                captureRepo.addPersionAllSiteSubject(subject)
            } else {
                throw new RuntimeException('无法创建专题')
            }
        }
        return subject
    }

    YqmsSession getUserForPersonalAllSiteSubject() {
        // 得到用户列表,按使用次数排序
        def yqmsUserIds = captureRepo.getYqmsUserIdsForPersonalAllSiteSubject()
        log.debug('{}', yqmsUserIds)

        // 得到可用用户列表
        def validYqmsUserIds = accountRepo.getYqmsUserIds()
        log.debug('{}', validYqmsUserIds)

        // 去掉已经存在的,,否则,返回使用次数最少的
        def yqmsUserId
        validYqmsUserIds.removeAll(yqmsUserIds)
        log.debug('validYqmsUserIds: {}', validYqmsUserIds)
        if (validYqmsUserIds) {
            yqmsUserId = validYqmsUserIds[0]
        } else {
            yqmsUserId = yqmsUserIds[0]
        }

        log.debug('{}', yqmsUserId)

        return accountRepo.getYqmsSession2(yqmsUserId)
    }

    Map getSiteInfo(long userId) {
        def sites = captureRepo.getValidSites(userId, SMT_ALL_SITE)
        def domains = []
        def wecharAccounts = []
        sites.each {
            if (it.siteType == SMT_WEBCHAT_PUBLCI_ACCOUNT) {
                log.debug("{}", it.websiteName)
                // todo vvv high websiteName 去除wwww
                wecharAccounts << it.websiteName
            } else {
                log.debug("{}", it.websiteDomain)
                if (it.websiteDomain) {
                    domains << stripUrl(it.websiteDomain)
                }

            }
        }
        return [domains: domains.join(','), wecharAccounts: wecharAccounts.join(',')]
    }

    Map getSiteInfo2(long userId, List siteTypes, Integer classification, Integer area) {
        def sites = captureRepo.getValidSitesBySiteTypes(userId, siteTypes)
        def siteNames = new HashSet()
        def wechatAccounts = new HashSet()
        def minTime = System.currentTimeMillis()
        sites.each {
            minTime = it.createTime.getTime() < minTime ? it.createTime.getTime() : minTime
            if(captureRepo.isSiteDetailMatched(it,classification,area)){
                if (it.siteType != SMT_WEBCHAT_PUBLCI_ACCOUNT) {
                        siteNames << it.websiteName
                } else {
                    wechatAccounts << it.siteName
                }
            }
        }
        return [siteNames: siteNames.toList(), wechatAccounts: wechatAccounts.toList(), minTime: new Date(minTime)]
    }

    Paging<News> queryPagingFocusNewsList(long userId, String focusId, String keywords, int siteType, int pageNo, int limit) {
        def focus = captureRepo.getFocus(new Focus(userId: userId, focusId: focusId))
        if (!focus) {
            return new Paging<News>(pageNo, limit, 0)
        }
        def session = getSession(focus)

        log.debug('siteType: {}', siteType)
        def params = [:]
        if (siteType == SMT_WEBCHAT_PUBLCI_ACCOUNT) {
            params.sourceType = [SOURCE_TYPE_WEXIN]
        } else if (siteType == SMT_WEB_SITE || siteType == SMT_PROFESSIONAL_SITE) {
            params.sourceType = [SOURCE_TYPE_NEWS, SOURCE_TYPE_PRINT_MEDIA]
        }

        return captureApi3rd.queryNewsListByParams(session, focus.yqmsSubjectId, keywords, params, pageNo, limit)
    }

    // todo vvvvvv high 查询solr最新数据
    List<News> getLatestNews(long userId, int limit) {
        def session = getSession(focusNewsYqmsUserId)
        def pageList = captureApi3rd.getLatestNewsList(session, focusNewsYqmsSubjectId, 1, limit)
        return pageList.list
    }

    Paging<News> queryPagingNewsList(long userId, List keyWords, List siteTypes, int pageNo, int limit,
    Integer heat, Integer time, String startTime, String endTime, Integer classification,
                                     Integer area, Integer  orientation, Integer order
    ) {
        def siteInfo = getSiteInfo2(userId, siteTypes, classification, area)
        if (siteInfo.siteNames || siteInfo.wechatAccounts) {
            return captureApi3rd.getNewsListByKeywords(keyWords, siteTypes, siteInfo.siteNames, siteInfo.wechatAccounts,
                    siteInfo.minTime, pageNo, limit, heat, time, startTime, endTime, orientation, order)
        } else {
            return new Paging<News>(pageNo, limit, 0)
        }
    }

    def getSumaryToday(long userId) {
        def today = DateUitl.getDayBegin()
        return captureRepo.getSumaryToday(userId,today)
    }

    /**
     * 临时替换getTrendWeekly方法，以后要删掉
     * @param userId
     * @return
     */
    @Deprecated
    List getSummaryWeeklyTemp(long userId) {
        def result = []
        def currDate = DateUitl.getDayBegin()
        for (int i = -6; i <= 0; i++) {
            def dateOfStart = DateUitl.addDay(currDate,i)
            def dateOfEnd = (Date)dateOfStart.clone()
            dateOfEnd.setTime(dateOfEnd.getTime() + 24*60*60*1000 - 1);
            def captureCount = 0
            def pushCount = 0
            def shareCount = 0
            //获取当天总共的采集的数据
            captureCount = captureRepo.getCaptureCountByDate(userId,dateOfStart)
            //获取当天user总共的推送的数据
//            pushCount = captureRepo.getUserNewsPushedCountByDate(userId,dateOfStart,dateOfEnd)
            pushCount = captureRepo.getPushCountByDate(userId,dateOfStart)
            //获取当天user总共的分享的数据
            shareCount = captureRepo.getUserNewsShareCountByDate(userId,dateOfStart,dateOfEnd)
            result.add( [
                    "date" : dateOfStart,
                    "captureCount" : captureCount,
                    "pushCount" : pushCount,
                    "shareCount" : shareCount
            ])
        }
        return result
    }

    List getSiteTopN(long userId,int siteType) {
        def today = DateUitl.getDayBegin()
        return captureRepo.getSiteTopN(userId,today,siteType)
    }

    List getTrendWeekly(long userId) {
        def start = getBeginDayOf7Day()
        def today = DateUitl.getDayBegin()
        return captureRepo.getTrendWeekly(userId, start, today)
    }
    Date getBeginDayOf7Day(){
        Calendar cal = new GregorianCalendar()
        cal.setTime(getDayBegin())
        cal.add(Calendar.DAY_OF_MONTH, -7)

        return cal.getTime()
    }
    /**
     * 新闻相关信息的统计功能
     * @return
     */
    boolean captureStatCount(){
        def currDateStart = DateUitl.getDayBegin()
        def currDateEnd = new Date();
        currDateEnd.setTime(currDateStart.getTime() + 24*60*60*1000 - 1);
        def startDate = DateFormatUtils.format(currDateStart, "yyyyMMdd000000")
        def endDate = DateFormatUtils.format(currDateStart, "yyyyMMdd235959")
        def pageSize = 10
        def pageNo = 1
        while (true){
            //拿到所有配置autopush的site
            List siteList = captureRepo.getSitesByPage(pageNo,pageSize)
            if (!(siteList.size() > 0)) break
            siteList.each {
                //统计push的信息
                def pushCount = captureRepo.getNewsPushedCount(it.userId,it._id,currDateStart,currDateEnd)
                //统计share的信息
                def shareCount = captureRepo.getNewsShareCount(it.userId,it._id,currDateStart,currDateEnd)
                //统计当天抓取信息
                def subjectMap = captureRepo.getSubjectMap(it.subjectId)
                def yqmsSession = accountRepo.getYqmsSession2(subjectMap.yqmsUserId)
                long captureCount = captureApi3rd.getNewsCount(yqmsSession, subjectMap.yqmsUserId, subjectMap.yqmsSubjectId, startDate, endDate);
                //用户/site/日期 入库
                CaptureStat captureStat = new CaptureStat();
                captureStat.userId = it.userId
                captureStat.siteId = it._id
                captureStat.siteName = it.siteName
                captureStat.siteType = it.siteType
                captureStat.captureCount = captureCount
                captureStat.pushCount = pushCount
                captureStat.shareCount = shareCount
                captureStat.date = currDateStart
                captureStat.updateTime = new Date()
                captureStat._id = captureStat.createId();
                //自动生成pushCount，数据，需要删除
                Random rand =new Random();
                def ranNum = (rand.nextInt(4) + 1)
                if(pushCount >= captureCount){
                    //pushCount大于captureCount时pushCount=captureCount
                    captureStat.pushCount = captureCount
                }else {
                    //获取上一条数据
                    def oldCaptureStat = captureRepo.getCaptureStatById(captureStat._id)
                    //如果有数据
                    if(oldCaptureStat != null){
                        //如果新数据小于旧数据则用旧数据加随机数
                        if (captureStat.pushCount <= oldCaptureStat.pushCount){
                            captureStat.pushCount = oldCaptureStat.pushCount + ranNum > captureCount ? captureCount : oldCaptureStat.pushCount + ranNum
                        }
                    }else {
                        //如果没有旧数据
                        if(pushCount == 0 && captureCount > 0){
                                captureStat.pushCount = pushCount + ranNum > captureCount ? captureCount :  pushCount + ranNum
                        }
                    }
                }
                captureRepo.saveCaptureStat(captureStat);
            }
            pageNo++
        }
        return true
    }

    void updateFocusCaptureCount() {
        def pageSize = 10
        def pageNo = 1
        while (true){
            def focusList = captureRepo.getFocusListByPage(pageSize,pageNo)
            if(focusList.size() == 0){
                break
            }
            focusList.each {
                def yqmsSession = accountRepo.getYqmsSession2(it.yqmsUserId)
                long captureCount = captureApi3rd.getNewsCount(yqmsSession, it.yqmsUserId, it.yqmsSubjectId, "", "");
                it.captureCount = captureCount
                captureRepo.modifyFocus(it)
            }
            pageNo++
        }
    }


    Paging<SiteClassification> getGetSiteClassification(int pageNo, int limit) {
        //拿到当前10个站点
        def total = captureRepo.getRootSiteClassificationCount();
        def paging = new Paging<SiteClassification>(pageNo, limit, total)
        captureRepo.getRootSiteClassifications(paging)
        paging.list.each {
            it.subSiteClassification = getSiteClassificationByParentId(it.id, 1, 6)
        }
        return paging
    }

    Paging<SiteClassification> getSiteClassificationByParentId(String parentId, int pageNo, int limit) {
        //拿到当前10个站点
        def total = captureRepo.getSubSiteClassificationCount(parentId);
        def paging = new Paging<SiteClassification>(pageNo, limit, total)
        captureRepo.getSubSiteClassifications(parentId, paging)
        return paging
    }

     Map getSitesByClassification(long userId, String classificationInfo, int pageNo, int limit) {
         List<String> existsMediaWebSiteDomains = captureRepo.getUserWebSiteDomains(userId,1);
         List<String> existsWeixinWebSiteDomains = captureRepo.getUserWebSiteDomains(userId,2);
         List<String> existsProfessionalWebSiteDomains = captureRepo.getUserWebSiteDomains(userId,3);
         //解析数组
         Map result=new HashMap();
         def parentIds = []
         def subIds = []
         JSONArray classifications = JSONArray.parse(classificationInfo)
         classifications.each {
             if(it.get("parentId")){
                 parentIds << it.get("parentId").toString()
                 if(it.get("subIds")){
                     it.get("subIds").each{
                         subIds<< it
                     }
                 }
             }
         }
         def mediaTypeTotal = captureRepo.getSitesByClassificationCount(parentIds,subIds,1,existsMediaWebSiteDomains);
         def weixinTypeTotal = captureRepo.getSitesByClassificationCount(parentIds,subIds,2,existsWeixinWebSiteDomains);
         def professionalTypeTotal = captureRepo.getSitesByClassificationCount(parentIds,subIds,3,existsProfessionalWebSiteDomains);

         def mediaTypePaging = new Paging<SiteDetail>(pageNo, limit, mediaTypeTotal)
         def weixinTypePaging = new Paging<SiteDetail>(pageNo, limit, weixinTypeTotal)
         def professionalTypePaging = new Paging<SiteDetail>(pageNo, limit, professionalTypeTotal)
         List<SiteDetail> mediaTypeList = new ArrayList<SiteDetail>();
         List<SiteDetail> weixinTypeList = new ArrayList<SiteDetail>();
         List<SiteDetail> professionalTypeList = new ArrayList<SiteDetail>();
         captureRepo.getSitesByClassification(parentIds, subIds, mediaTypePaging, 1, existsMediaWebSiteDomains)
         captureRepo.getSitesByClassification(parentIds, subIds, weixinTypePaging, 2, existsWeixinWebSiteDomains)
         captureRepo.getSitesByClassification(parentIds, subIds, professionalTypePaging, 3, existsProfessionalWebSiteDomains)

         mediaTypePaging.list.each {
             mediaTypeList.add(it)
         }
         weixinTypePaging.list.each {
             weixinTypeList.add(it)
         }
         professionalTypePaging.list.each {
             professionalTypeList.add(it)
         }
         result.put("professionalTypeList",professionalTypeList);//3
         result.put("weixinTypeList",weixinTypeList);//2
         result.put("mediaTypeList",mediaTypeList);//1
        return result
    }

    Map addSitesFormRecommend(List siteDetailIdList, long userId){
        Map setting = getCaptureSiteAccountProfile(userId)
        if(setting == null || setting.size() == 0){
             return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg: "没有找到相关的系统配置信息"])
        }
        def maxSiteCount = 0
        setting.each {k,v->
            maxSiteCount += v
        }
        //获取当前site的总数
        Map siteCounts = captureRepo.getSiteSummaryCountByType(userId)
        def currSiteTotalCount = 0
        siteCounts.each {
            currSiteTotalCount += it.value
        }
        if((currSiteTotalCount + siteDetailIdList.size()) > maxSiteCount ){
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,
                              msg: ("站点总数超出了最大的站点数${maxSiteCount}," +
                                      "已经配置了${currSiteTotalCount}个，" +
                                      "还可以添加${maxSiteCount-currSiteTotalCount}个") as String])
        }
        //拿到所有站点的信息
        def siteDetailList = captureRepo.getSiteDetailByIds(siteDetailIdList)
        def siteDetailSummaryCount = captureRepo.getSiteDetailCountByType(siteDetailIdList)

        def maxMediaSiteCount = setting.maxMediaSiteCount?:0
        def maxWechatSiteCount = setting.maxWechatSiteCount?:0
        def maxProfessionalSiteCount = setting.maxProfessionalSiteCount?:0

        if(((siteCounts.get("3")?siteCounts.get("3") : 0) +
                (siteDetailSummaryCount.get("3")?siteDetailSummaryCount.get("3") : 0)) > maxProfessionalSiteCount ){
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,
                              msg: ("专业网站总数不能超过${maxProfessionalSiteCount}," +
                                      "专业网站点数${(siteCounts.get("3")?siteCounts.get("3") : 0)}，" +
                                      "还可添加${maxProfessionalSiteCount - (siteCounts.get("3")?siteCounts.get("3") : 0)}") as String])
        }
        if(((siteCounts.get("2")?siteCounts.get("2") : 0) +
                (siteDetailSummaryCount.get("2")?siteDetailSummaryCount.get("2") : 0)) > maxWechatSiteCount ){
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,
                              msg: ("微信公众号总数不能超过${maxWechatSiteCount}," +
                                      "微信公众号数${(siteCounts.get("2")?siteCounts.get("2") : 0)}，" +
                                      "还可添加${maxWechatSiteCount - (siteCounts.get("2")?siteCounts.get("2") : 0)}") as String])
        }
        if(((siteCounts.get("1")?siteCounts.get("1") : 0) +
                (siteDetailSummaryCount.get("1")?siteDetailSummaryCount.get("1") : 0)) > maxMediaSiteCount ){
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,
                              msg: ("媒体网站总数不能超过${maxMediaSiteCount}," +
                                      "媒体网站数${(siteCounts.get("1")?siteCounts.get("1") : 0)}，" +
                                      "还可添加${maxMediaSiteCount - (siteCounts.get("1")?siteCounts.get("1") : 0)}") as String])

        }
        //判断是否有重复的站点
        def mediaDomains = []
        def weixinNames = []
        def professionalDomains = []
        siteDetailList.each {
            switch (it.siteType) {
                case 1 :
                    mediaDomains << it.siteDomain
                    break
                case 2 :
                    weixinNames << it.siteName
                    break
                case 3 :
                    professionalDomains << it.siteDomain
                    break
            }
        }
        def existCount = captureRepo.getSiteCount(userId,1, mediaDomains)
        if(existCount > 0){
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg: "重复的媒体站点输入"])
        }
        existCount = captureRepo.getSiteCount(userId,2, weixinNames)
        if(existCount > 0){
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg: "重复的微信公众号输入"])
        }
        existCount = captureRepo.getSiteCount(userId,3, professionalDomains)
        if(existCount > 0){
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg: "重复的专业站点输入"])
        }

        //判断是否有重复的公众号站点 todo
        siteDetailList.each {
            Site site = new Site(
                    userId: userId,
                    siteName: it.siteName,
                    websiteName: it.siteName,
                    websiteDomain: it.siteDomain,
                    channelName: "",
                    channelDomain: "",
                    isShow: true,
                    captureInterval: 0,
                    siteType: it.siteType,
                    isAutoPush: false
            )
            if (site.websiteDomain) {
                site.websiteDomain = stripUrl(site.websiteDomain)
            }
            SubjectMap subjectMap
            subjectMap = captureRepo.getSubjectMap(site)
            if (!subjectMap) {
                subjectMap = addSubjectMap(site)
            }
            site.siteId = UUID.randomUUID().toString()
            site.createTime = new Date()
            site.subjectId = subjectMap.subjectId
            captureRepo.addSite(site)
        }
        return apiResult()
    }

    void addSubjectForSite() {
        def subjects = captureRepo.getMockSubjects()
        subjects.each { subjectMap ->
            addRealSubjectId2SubjectMap(subjectMap)
        }
    }

    /**
     * 获取当前用户的分类筛选偏好
     * @param userId
     * @return
     */
    List getCaptureFiltersByUserId(Long userId){
        return captureRepo.getCaptureFilters(userId)
    }
    /**
     * 初始化用户分类筛选偏好
     * @param userId
     * @return
     */
    List initUserCaptureFilters(Long userId){
        def list = []
        for (int i = 1; i < 4; i++) {
            def curCaptureFilter = new CaptureFilter([
                    _id : UUID.randomUUID().toString(),
                    userId: userId,
                    name : "偏好"+ (i as String),
                    mediaType : [1,2,3],
                    keyWords : [],
                    heat: 0,
                    time:0,
                    startTime:null,
                    endTime: null,
                    classification:0,
                    area:0,
                    orientation:0,
                    order: 1,
                    updateTime:new Date(),
                    createTime:new Date(),
            ])
            captureRepo.saveCaptureFilter(curCaptureFilter)
            list.add(curCaptureFilter)
        }
        return list
    }
    Map modifyCaptureFilter(Long userId,String id,String name,String mediaType,String keyWords,String heat,String time,
                            String startTime,String endTime,String classification,String area,
                            String orientation,String order){
        if(id.equals("")){
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg: "没有找到筛选偏好ID"])
        }
        if(name.equals("") && mediaType.equals("") && keyWords.equals("") && heat.equals("")
                && time.equals("") && startTime.equals("") && endTime.equals("")
                && classification.equals("") && area.equals("") && orientation.equals("")
                && order.equals("")){
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg: "没有找到要更新的内容"])
        }
        //拿到原来的筛选
        CaptureFilter captureFilter = captureRepo.getCaptureFilter(userId, id)
        if(!captureFilter){
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg: "没有找到要对应的筛选偏好"])
        }
        //校验更新的筛选
        if(!(name.equals(""))){
            captureFilter.setName(name)
        }
        if(!(mediaType.equals(""))){
            def mediaTypes = []
            mediaType.split(",").each {
                mediaTypes << (it as Integer)
            }
            captureFilter.setMediaType(mediaTypes)
        }
        def keyWordsList = []
        if(!(keyWords.equals(""))){
            def keyWordsSeplit = keyWords.split(",")
            if (keyWordsSeplit.size()>5){
                return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg: "关键词不大于5组"])
            }else{
                for (int i = 0; i <keyWordsSeplit.size() ; i++) {
                    if (keyWordsSeplit[i].length()>5){
                        return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg: "每组关键词长度不大于5"])
                        break;
                    }else{
                        keyWordsList << (keyWordsSeplit[i] as String)
                    }
                }
            }
        }
            captureFilter.setKeyWords(keyWordsList)
        if(!(heat.equals(""))){
            try {
                captureFilter.setHeat(Integer.parseInt(heat))
            }catch (Exception e){
                return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg: "热度格式不正确"])
            }
        }
        if(!(time.equals(""))){
            try {
                captureFilter.setTime(Integer.parseInt(time))
            }catch (Exception e){
                return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg: "时间格式不正确"])
            }
            if(captureFilter.time == -1){
                if(startTime.equals("") && endTime.equals("")){
                    return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg: "请填写开始时间或结束时间"])
                }else {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    if(!(startTime.equals(""))){
                        try {
                            captureFilter.setStartTime(simpleDateFormat.parse(startTime))
                        }catch (Exception e){
                            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg: "开始时间格式不正确"])
                        }
                    }
                    if(!(endTime.equals(""))){
                        try {
                            captureFilter.setEndTime(simpleDateFormat.parse(endTime))
                        }catch (Exception e){
                            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg: "结束时间格式不正确"])
                        }
                    }
                }
            }
        }
        if(!(classification.equals(""))){
            try {
                captureFilter.setClassification(Integer.parseInt(classification))
            }catch (Exception e){
                return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg: "分类信息格式不正确"])
            }
        }
        if(!(area.equals(""))){
            try {
                captureFilter.setArea(Integer.parseInt(area))
            }catch (Exception e){
                return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg: "区域格式不正确"])
            }
        }
        if(!(orientation.equals(""))){
            try {
                captureFilter.setOrientation(Integer.parseInt(orientation))
            }catch (Exception e){
                return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg: "倾向格式不正确"])
            }
        }
        if(!(order.equals(""))){
            try {
                captureFilter.setOrder(Integer.parseInt(order))
            }catch (Exception e){
                return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg: "排序格式不正确"])
            }
        }
        //更新筛选
        captureFilter.setUpdateTime(new Date())
        captureRepo.saveCaptureFilter(captureFilter)
        return apiResult()
    }
    /**
     * 获取系统分类信息
     * @return
     */
    List getCaptureFilterClassifications(){
        SystemSetting setting = settingRepo.getSystemSetting("site","classification")
        if(setting){
            return setting.content
        }else {
            return []
        }
    }
    /**
     * 获取区域信息
     * @return
     */
    List getCaptureFilterAreas(){

        SystemSetting setting = settingRepo.getSystemSetting("site","area")
        if(setting){
            return setting.content
        }else {
            return []
        }
    }

}
