package com.istar.mediabroken.service.analysis

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.istar.mediabroken.entity.analysis.TopNews
import com.istar.mediabroken.repo.analysis.ChannelRepo
import com.istar.mediabroken.repo.capture.SiteRepo
import com.istar.mediabroken.utils.AnalysisUtils
import com.istar.mediabroken.utils.DateUitl
import com.istar.mediabroken.utils.UrlUtils
import groovy.util.logging.Slf4j
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

import java.text.DecimalFormat
import java.text.SimpleDateFormat

import static com.istar.mediabroken.api.ApiResult.apiResult

/**
 * Author: zc
 * Time: 2017/8/3
 */
@Service
@Slf4j
class ChannelService {
    @Autowired
    ChannelRepo channelRepo
    @Autowired
    SiteRepo siteRepo
    @Value('${4power.demoUserId}')
    long demoUserId

    Map modifyAnalysisSites(Map analysisSites) {
        List<String> stringList = [];
        JSONArray sitesList = analysisSites.sites as JSONArray;//去重
        JSONArray newSitesList = [];//去重
        for (int i = 0; i < sitesList.size(); i++) {
            JSONObject obj = sitesList.get(i)
            if (obj) {
                if (stringList != null && stringList.contains(obj.get("siteName") + "," + obj.get("siteDomain") + "," + obj.get("siteType"))) {
                    continue;
                } else {
                    stringList << (obj.get("siteName") + "," + obj.get("siteDomain") + "," + obj.get("siteType"));
                    newSitesList << obj;
                }
            }
        }
        Map map = channelRepo.getAnalysisSites(analysisSites.get("userId")).msg;
        List<String> stringListBef = [];
        if (map == null){
            //创建一个新的
            def userId = analysisSites.userId
            def orgName = analysisSites.orgName
            Map newAnalysisSites = [:]
            newAnalysisSites.userId = userId
            newAnalysisSites.orgName = orgName
            newAnalysisSites.updateTime = new Date()
            newAnalysisSites.sites = newSitesList
            channelRepo.newAnalysisSites(newAnalysisSites)
            return apiResult([status: HttpStatus.SC_OK, msg: "保存成功"])
        }
        JSONArray sitesListBef = map.sites as JSONArray;//用户当前sites
        JSONArray newSitesListBef = [];
        for (int i = 0; i < sitesListBef.size(); i++) {
            JSONObject obj = sitesListBef.get(i)
            if (obj) {
                if (stringListBef != null && stringListBef.contains(obj.get("siteName") + "," + obj.get("siteDomain") + "," + obj.get("siteType"))) {
                    continue;
                } else {
                    stringListBef << (obj.get("siteName") + "," + obj.get("siteDomain") + "," + obj.get("siteType"));
                    newSitesListBef << obj;
                }
            }
        }
        if ((analysisSites.orgName+stringList.toString()).equals(map.orgName+stringListBef.toString())){
            return apiResult([status: HttpStatus.SC_OK, msg: "保存成功"])
        }
        analysisSites.sites = newSitesList;
        boolean result = channelRepo.modifyAnalysisSites(analysisSites)
        if (result) {
            return apiResult([status: HttpStatus.SC_OK, msg: "保存成功"])
        } else {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "保存失败"])
        }
    }

    Map getAnalysisSites(long userId) {
        def result = channelRepo.getAnalysisSites(userId)
        return result;
    }

    List getNoDataSites() {
        List result=new ArrayList()
        def allAnalysisSites = channelRepo.getAllAnalysisSites()
        allAnalysisSites = allAnalysisSites.unique()
        allAnalysisSites.each {
            boolean newsExists = channelRepo.getNewsBySite(it)
            if (!newsExists){
                result<<it
            }
        }
        return result
    }

    Map getWeeklyNews(long userId) {
//        def obj=[title: '谁拍的站出来，真的有龙。谁拍的站出来，真的有龙。谁拍的站出来，真的有龙。', siteName: '网站-劳动午报', publishTime: 1502163199000, reprintCount: 145, commentCount: 124, likeCount: 235]
//        Map res = getAnalysisSites(userId)
//        def sites = res.msg?res.msg.sites:[]
//        if (!sites){
//            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "用户未设置渠道信息"])
//        }
        def channel = channelRepo.getAnalysisSites(userId)
        if (channel.status != HttpStatus.SC_OK || null == channel.msg) {
            userId = demoUserId
            channel = channelRepo.getAnalysisSites(userId)
        }
        if (channel.status != HttpStatus.SC_OK || null == channel.msg) {
            return null
        }
        List list = channelRepo.getWeeklyNews(channel.msg.sites as List)
        List result = new ArrayList()
        for (int i = 0; i < list.size(); i++) {
            TopNews t = list.get(i)
            Map map = new HashMap<>()
            map.put("title", t.title)
            map.put("siteName", t.siteName)
            map.put("publishTime", t.publishTime ?: t.publishTime.toString())
            map.put("reprintCount", t.reprintCount)
            map.put("commentCount", t.commentCount)
            map.put("likeCount", t.likeCount)
            result.add(map)
        }
        return apiResult([status: HttpStatus.SC_OK, msg: result])
    }

    Map getWeeklyNewsSummary1(long userId) {
        def channel = channelRepo.getAnalysisSites(userId)
        if (channel.status != HttpStatus.SC_OK || null == channel.msg) {
            userId = demoUserId
            channel = channelRepo.getAnalysisSites(userId)
        }
        if (channel.status != HttpStatus.SC_OK || null == channel.msg) {
            return null
        }
        def result = []
        def sites = channel.msg.sites as List
        List list = channelRepo.getWeeklyNewsSummary(sites)
        if(!list)
            return null
        def week = DateUitl.getOneWeek()

        def gg = list.groupBy{ it->
            [it.siteDomain,it.newsType]
        }.each { k,v->
            v.sort{it.time}
        }

        sites.each { site ->
            def domain = ""
            def newsType = site.siteType
            switch (newsType as int){
                case 1:
                    domain = site.siteDomain
                    break
                case 2:
                    domain = site.siteName
                    newsType = 4
                    break
                case 3:
                    domain = site.siteName
                    newsType = 6
                    break
            }
            def get = gg.get([domain, newsType as Long])
            def number = []
            if(!get){
                result << ["siteName":site.siteName,"data":[0,0,0,0,0,0,0],"siteType":site.siteType]
            }else{
                def map = new HashMap<String, Long>()
                get.each { it ->
                    map.put(it.time as String,it.publishCount as Long)
                }
                week.each { day ->
                    def count = map.get(day)!=null ? map.get(day) : 0
                    number << count
                }
                result << ["siteName":site.siteName,"data":number,"siteType":site.siteType]
            }
        }
        return apiResult([status: HttpStatus.SC_OK,msg: result])
    }

    List getLatestNewsByChannel(long userId) {
        def result = []
        def channel = channelRepo.getAnalysisSites(userId)
        if (channel.status != HttpStatus.SC_OK || null == channel.msg) {
            userId = demoUserId
            channel = channelRepo.getAnalysisSites(userId)
        }
        if (channel.status != HttpStatus.SC_OK || null == channel.msg) {
            return null
        }
        def newses = channelRepo.getLatestNewsByChannel(channel.msg)
        if (newses) {
            def size = newses.size()
            def prevSimhash = ""
            for (int i = 0; i < size; i++) {
                def news = newses.get(i)
                if (prevSimhash.equals(news.simhash)) {
                    continue
                }
                prevSimhash = news.simhash
                result << ['title': news.title, 'siteName': news.siteName, 'publishTime': DateUitl.convertEsDate(news.publishTime).getTime()]
                if (result.size() >= 5) {
                    break
                }
            }
        }
        return result
    }

    List getWordsCloudByChannel(long userId) {
        def channel = channelRepo.getAnalysisSites(userId)
        if (channel.status != HttpStatus.SC_OK || null == channel.msg) {
            userId = demoUserId
            channel = channelRepo.getAnalysisSites(userId)
        }
        if (channel.status != HttpStatus.SC_OK || null == channel.msg) {
            return null
        }
        def sites = channel.msg
        if(!sites)
            return null
        def endDate = DateUitl.getDayBegin()
        def startDate = DateUitl.addDay(endDate, -7)
        def sdf = new SimpleDateFormat("yyyy-MM-dd")
        def endDay = sdf.format(endDate)
        def startDay = sdf.format(startDate)
        List channelKeywords = channelRepo.getKeywordsByChannel(sites as Map, startDay, endDay)
        return channelKeywords
    }

    //传播力

    private double computePSI(String domain, Long type, String start, String end) {
        Long postWEB = channelRepo.getPublishCount(domain, 1, start, end)
        Long postBBS = channelRepo.getPublishCount(domain, 2, start, end)
        Long postWechat = channelRepo.getPublishCount(domain, 6, start, end)
        Long postWeibo = channelRepo.getPublishCount(domain, 4, start, end)
        Long postApps = channelRepo.getPublishCount(domain, 9, start, end)
        Long postMount = postWEB + postBBS + postWechat + postWeibo + postApps

        Long reprintWEB = channelRepo.getRepintCountByType(domain, type, start, end, 1)
        Long reprintBBS = channelRepo.getRepintCountByType(domain, type, start, end, 2)
        Long reprintWeibo = channelRepo.getRepintCountByType(domain, type, start, end, 4)
        Long reprintWechat = channelRepo.getRepintCountByType(domain, type, start, end, 6)
        Long reprintMount = reprintWEB + reprintBBS + reprintWeibo + reprintWechat

        Long mediaMount = channelRepo.getMediaCount(domain, type, start, end)
        Long mediaImportant = channelRepo.getImportantMediaCount(domain, type, start, end)

        def res = [
                "postWEB"       : postWEB,
                "postBBS"       : postBBS,
                "postWechat"    : postWechat,
                "postWeibo"     : postWeibo,
                "postApps"      : postApps,
                "postMount"     : postMount,
                "reprintWEB"    : reprintWEB,
                "reprintBBS"    : reprintBBS,
                "reprintWeibo"  : reprintWeibo,
                "reprintWechat" : reprintWechat,
                "reprintMount"  : reprintMount,
                "mediaMount"    : mediaMount,
                "mediaImportant": mediaImportant,
        ]
        double psi = AnalysisUtils.getPSI(res)
        return psi
    }

    private double computeMII(String domain, Long type, String start, String end) {
        double rpd = channelRepo.getPerReadByDay(domain, type, start, end)
        double rav = channelRepo.getPerAverageRead(domain, type, start, end)
        double rmax = channelRepo.getPerReadMax(domain, type, start, end)
        double rmount = channelRepo.getReadSum(domain, type, start, end)

        double ppd = channelRepo.getPerCommentByDay(domain, type, start, end)
        double pav = channelRepo.getPerAverageComment(domain, type, start, end)
        double pmax = channelRepo.getPerCommentMax(domain, type, start, end)
        double pmount = channelRepo.getCommentSum(domain, type, start, end)

        double zpd = channelRepo.getPerLikeByDay(domain, type, start, end)
        double zav = channelRepo.getPerAverageLike(domain, type, start, end)
        double zmax = channelRepo.getPerLikeMax(domain, type, start, end)
        double zmount = channelRepo.getLikeSum(domain, type, start, end)

        Long mM = channelRepo.getMediaCount(domain, type, start, end)
        Long mI = channelRepo.getImportantMediaCount(domain, type, start, end)

        def res = [
                "rpd"   : rpd,
                "rav"   : rav,
                "rmax"  : rmax,
                "rmount": rmount,
                "ppd"   : ppd,
                "pav"   : pav,
                "pmax"  : pmax,
                "pmount": pmount,
                "zpd"   : zpd,
                "zav"   : zav,
                "zmax"  : zmax,
                "zmount": zmount,
                "mM"    : mM,
                "mI"    : mI
        ]

        double mii = AnalysisUtils.getMII(res as Map)

        return mii
    }

    private double computeBSI(String domain, Long type, String start, String end) {
        double rpd = channelRepo.getPerReadByDay(domain, type, start, end)
        double rav = channelRepo.getPerAverageRead(domain, type, start, end)
        double rmax = channelRepo.getPerReadMax(domain, type, start, end)
        double rmount = channelRepo.getReadSum(domain, type, start, end)

        double ppd = channelRepo.getPerCommentByDay(domain, type, start, end)
        double pav = channelRepo.getPerAverageComment(domain, type, start, end)
        double pmax = channelRepo.getPerCommentMax(domain, type, start, end)
        double pmount = channelRepo.getCommentSum(domain, type, start, end)

        double zpd = channelRepo.getPerLikeByDay(domain, type, start, end)
        double zav = channelRepo.getPerAverageLike(domain, type, start, end)
        double zmax = channelRepo.getPerLikeMax(domain, type, start, end)
        double zmount = channelRepo.getLikeSum(domain, type, start, end)

        double posMount = channelRepo.getPositiveComments(domain, type, start, end)
        double nagMount = channelRepo.getNegativeComments(domain, type, start, end)

        double keyMatch = channelRepo.getWordMatch(domain, type, start, end)
        double keyInnovation = channelRepo.getWordInnovate(domain, type, start, end)
        double keyPropagation = channelRepo.getWordSpread(domain, type, start, end)

        def res = [
                "rpd"           : rpd,
                "rav"           : rav,
                "rmax"          : rmax,
                "rmount"        : rmount,
                "ppd"           : ppd,
                "pav"           : pav,
                "pmax"          : pmax,
                "pmount"        : pmount,
                "sPmount"       : ppd,
                "zpd"           : zpd,
                "zav"           : zav,
                "zmax"          : zmax,
                "zmount"        : zmount,
                "posMount"      : posMount,
                "nagMount"      : nagMount,
                "keyMatch"      : keyMatch,
                "keyInnovation" : keyInnovation,
                "keyPropagation": keyPropagation
        ]

        double bsi = AnalysisUtils.getBSI(res as Map)
        return bsi
    }

    private double computeTSI(String domain, Long type, String start, String end) {
        double cmount = channelRepo.getManuscriptCount(domain, type, start, end) ?: 0.0
        double tmount = channelRepo.getAverageReprintCount(domain, type, start, end, Integer.MIN_VALUE) ?: 0.0
        double smount = channelRepo.getAverageReprintCount(domain, type, start, end, 6) ?: 0.0
        double wmount = channelRepo.getAverageReprintCount(domain, type, start, end, 4) ?: 0.0
        double imount = channelRepo.getImporantEvent(domain, type, start, end) ?: 0.0

        double pmount = channelRepo.getPerAverageComment(domain, type, start, end) ?: 0.0
        double posMount = channelRepo.getPositiveComments(domain, type, start, end) ?: 0.0
        double nagMount = channelRepo.getNegativeComments(domain, type, start, end) ?: 0.0

        Long mM = channelRepo.getMediaCount(domain, type, start, end) ?: 0.0
        Long mI = channelRepo.getImportantMediaCount(domain, type, start, end) ?: 0.0

        def res = [
                "cmount"  : cmount,
                "tmount"  : tmount,
                "smount"  : smount,
                "wmount"  : wmount,
                "imount"  : imount,
                "pmount"  : pmount,
                "posMount": posMount,
                "nagMount": nagMount,
                "mM"      : mM>1?mM/7:0,
                "mI"      : mI>1?mI/7:0
        ]

        double bsi = AnalysisUtils.getTSI(res as Map)
        return bsi
    }

    private double computeTotalPSI(List sites, String start, String end) {

        long postWEBs = 0L
        long postBBSs = 0L
        long postWechats = 0L
        long postWeibos = 0L
        long postAppss = 0L
        long postMounts = 0L
        long reprintWEBs = 0L
        long reprintBBSs = 0L
        long reprintWeibos = 0L
        long reprintWechats = 0L
        long reprintMounts = 0L
        long mediaMounts = 0L
        long mediaImportants = 0L

        sites.each { site ->

            String siteDomain = site.siteDomain
            Long newsType = site.newsType
            postWEBs += channelRepo.getPublishCount(siteDomain, 1, start, end)
            postBBSs += channelRepo.getPublishCount(siteDomain, 2, start, end)
            postWechats += channelRepo.getPublishCount(siteDomain, 6, start, end)

            postWeibos += channelRepo.getPublishCount(siteDomain, 4, start, end)
            postAppss += channelRepo.getPublishCount(siteDomain, 9, start, end)

            reprintWEBs += channelRepo.getRepintCountByType(siteDomain, newsType, start, end, 1)
            reprintBBSs += channelRepo.getRepintCountByType(siteDomain, newsType, start, end, 2)
            reprintWeibos += channelRepo.getRepintCountByType(siteDomain, newsType, start, end, 4)
            reprintWechats += channelRepo.getRepintCountByType(siteDomain, newsType, start, end, 6)

            mediaMounts += channelRepo.getMediaCount(siteDomain, newsType, start, end)
            mediaImportants += channelRepo.getImportantMediaCount(siteDomain, newsType, start, end)

        }
        postMounts += postWEBs + postBBSs + postWechats + postWeibos + postAppss
        reprintMounts += reprintWEBs + reprintBBSs + reprintWeibos + reprintWechats

        def res = [
                "postWEB"       : postWEBs,
                "postBBS"       : postBBSs,
                "postWechat"    : postWechats,
                "postWeibo"     : postWeibos,
                "postApps"      : postAppss,
                "postMount"     : postMounts,
                "reprintWEB"    : reprintWEBs,
                "reprintBBS"    : reprintBBSs,
                "reprintWeibo"  : reprintWeibos,
                "reprintWechat" : reprintWechats,
                "reprintMount"  : reprintMounts,
                "mediaMount"    : mediaMounts,
                "mediaImportant": mediaImportants,
        ]
        double psi = AnalysisUtils.getPSI(res)
        return psi
    }

    private double computeTotalMII(List sites, String start, String end) {

        double rpds = 0.0
        double ravs = 0.0
        double rmaxs = 0.0
        double rmounts = 0.0
        double ppds = 0.0
        double pavs = 0.0
        double pmaxs = 0.0
        double pmounts = 0.0
        double zpds = 0.0
        double zavs = 0.0
        double zmaxs = 0.0
        double zmounts = 0.0
        long mMs = 0
        long mIs = 0

        sites.each { site ->
            String siteDomain = site.siteDomain
            Long newsType = site.newsType

            rpds += channelRepo.getPerReadByDay(siteDomain, newsType, start, end)
            ravs += channelRepo.getPerAverageRead(siteDomain, newsType, start, end)
            rmaxs += channelRepo.getPerReadMax(siteDomain, newsType, start, end)
            rmounts += channelRepo.getReadSum(siteDomain, newsType, start, end)

            ppds += channelRepo.getPerCommentByDay(siteDomain, newsType, start, end)
            pavs += channelRepo.getPerAverageComment(siteDomain, newsType, start, end)
            pmaxs += channelRepo.getPerCommentMax(siteDomain, newsType, start, end)
            pmounts += channelRepo.getCommentSum(siteDomain, newsType, start, end)

            zpds += channelRepo.getPerLikeByDay(siteDomain, newsType, start, end)
            zavs += channelRepo.getPerAverageLike(siteDomain, newsType, start, end)
            zmaxs += channelRepo.getPerLikeMax(siteDomain, newsType, start, end)
            zmounts += channelRepo.getLikeSum(siteDomain, newsType, start, end)

            mMs += channelRepo.getMediaCount(siteDomain, newsType, start, end)
            mIs += channelRepo.getImportantMediaCount(siteDomain, newsType, start, end)
        }
        def res = [
                "rpd"   : rpds,
                "rav"   : ravs,
                "rmax"  : rmaxs,
                "rmount": rmounts,
                "ppd"   : ppds,
                "pav"   : pavs,
                "pmax"  : pmaxs,
                "pmount": pmounts,
                "zpd"   : zpds,
                "zav"   : zavs,
                "zmax"  : zmaxs,
                "zmount": zmounts,
                "mM"    : mMs,
                "mI"    : mIs
        ]

        double mii = AnalysisUtils.getMII(res)

        return mii
    }

    private double computeTotalBSI(List sites, String start, String end) {

        double rpds = 0.0
        double ravs = 0.0
        double rmaxs = 0.0
        double rmounts = 0.0

        double ppds = 0.0
        double pavs = 0.0
        double pmaxs = 0.0
        double pmounts = 0.0

        double zpds = 0.0
        double zavs = 0.0
        double zmaxs = 0.0
        double zmounts = 0.0

        double posMounts = 0.0
        double nagMounts = 0.0

        double keyMatchs = 0.0
        double keyInnovations = 0.0
        double keyPropagations = 0.0

        sites.each { site ->
            String siteDomain = site.siteDomain
            Long newsType = site.newsType
            rpds += channelRepo.getPerReadByDay(siteDomain, newsType, start, end)
            ravs += channelRepo.getPerAverageRead(siteDomain, newsType, start, end)
            rmaxs += channelRepo.getPerReadMax(siteDomain, newsType, start, end)
            rmounts += channelRepo.getReadSum(siteDomain, newsType, start, end)

            ppds += channelRepo.getPerCommentByDay(siteDomain, newsType, start, end)
            pavs += channelRepo.getPerAverageComment(siteDomain, newsType, start, end)
            pmaxs += channelRepo.getPerCommentMax(siteDomain, newsType, start, end)
            pmounts += channelRepo.getCommentSum(siteDomain, newsType, start, end)

            zpds += channelRepo.getPerLikeByDay(siteDomain, newsType, start, end)
            zavs += channelRepo.getPerAverageLike(siteDomain, newsType, start, end)
            zmaxs += channelRepo.getPerLikeMax(siteDomain, newsType, start, end)
            zmounts += channelRepo.getLikeSum(siteDomain, newsType, start, end)

            posMounts += channelRepo.getPositiveComments(siteDomain, newsType, start, end)
            nagMounts += channelRepo.getNegativeComments(siteDomain, newsType, start, end)

            keyMatchs += channelRepo.getWordMatch(siteDomain, newsType, start, end)
            keyInnovations += channelRepo.getWordInnovate(siteDomain, newsType, start, end)
            keyPropagations += channelRepo.getWordSpread(siteDomain, newsType, start, end)
        }

        def res = [
                "rpd"           : rpds,
                "rav"           : ravs,
                "rmax"          : rmaxs,
                "rmount"        : rmounts,
                "ppd"           : ppds,
                "pav"           : pavs,
                "pmax"          : pmaxs,
                "pmount"        : pmounts,
                "sPmount"       : ppds,
                "zpd"           : zpds,
                "zav"           : zavs,
                "zmax"          : zmaxs,
                "zmount"        : zmounts,
                "posMount"      : posMounts,
                "nagMount"      : nagMounts,
                "keyMatch"      : keyMatchs,
                "keyInnovation" : keyInnovations,
                "keyPropagation": keyPropagations
        ]
        double bsi = AnalysisUtils.getBSI(res)
        return bsi
    }

    private double computeTotalTSI(List sites, String start, String end) {

        double cmounts = 0.0
        double tmounts = 0.0
        double smounts = 0.0
        double wmounts = 0.0
        double imounts = 0.0
        double pmounts = 0.0
        double posMounts = 0.0
        double nagMounts = 0.0
        double mMs = 0.0
        double mIs = 0.0

        sites.each { site ->
            String siteDomain = site.siteDomain
            Long newsType = site.newsType
            cmounts += channelRepo.getManuscriptCount(siteDomain, newsType, start, end)
            tmounts += channelRepo.getAverageReprintCount(siteDomain, newsType, start, end, Integer.MIN_VALUE)
            smounts += channelRepo.getAverageReprintCount(siteDomain, newsType, start, end, 6)
            wmounts += channelRepo.getAverageReprintCount(siteDomain, newsType, start, end, 4)
            imounts += channelRepo.getImporantEvent(siteDomain, newsType, start, end)

            pmounts += channelRepo.getPerAverageComment(siteDomain, newsType, start, end)
            posMounts += channelRepo.getPositiveComments(siteDomain, newsType, start, end)
            nagMounts += channelRepo.getNegativeComments(siteDomain, newsType, start, end)

            mMs += channelRepo.getMediaCount(siteDomain, newsType, start, end)
            mIs += channelRepo.getImportantMediaCount(siteDomain, newsType, start, end)
        }
        def res = [
                "cmount"  : cmounts,
                "tmount"  : tmounts,
                "smount"  : smounts,
                "wmount"  : wmounts,
                "imount"  : imounts,
                "pmount"  : pmounts,
                "posMount": posMounts,
                "nagMount": nagMounts,
                "mM"      : mMs>1?mMs/7:0,
                "mI"      : mIs>1?mIs/7:0
        ]

        double bsi = AnalysisUtils.getTSI(res)
        return bsi
    }

    List getWeeklyNewsSummary(Long userId) {

        def channel = channelRepo.getAnalysisSites(userId)

        if (channel.status != HttpStatus.SC_OK || null == channel.msg) {
            userId = demoUserId
            channel = channelRepo.getAnalysisSites(userId)
        }
        if (channel.status != HttpStatus.SC_OK || null == channel.msg) {
            return null
        }

        def endDate = DateUitl.getDayBegin()
        def startDate = DateUitl.addDay(endDate, -7)
        def sdf = new SimpleDateFormat("yyyy-MM-dd")
        def end = sdf.format(endDate)
        def start = sdf.format(startDate)
        def chanles = channel.msg
        def sites = chanles?.sites
        if (!sites) {
            return null
        }
        def result = []
        sites.each { site ->
            String siteDomain = ""
            Long newsType = 0L
            switch (site.siteType as Long) {
                case 1:
                    siteDomain = UrlUtils.getDomainFromUrl(site.siteDomain as String)
                    newsType = 1L
                    break
                case 2:
                    siteDomain = site.siteName
                    newsType = 4L
                    break
                case 3:
                    siteDomain = site.siteName
                    newsType = 6L
                    break
            }
            DecimalFormat df = new DecimalFormat("#.00")
            double psi = computePSI(siteDomain, newsType, start, end)
            BigDecimal var1 = new BigDecimal(psi * 100)
            def psiFormat = df.format(var1) == ".00" ? "0.00" : df.format(var1)

            double mii = computeMII(siteDomain, newsType, start, end)
            BigDecimal var2 = new BigDecimal(mii * 100)
            def miiFormat = df.format(var2) == ".00" ? "0.00" : df.format(var2)

            double bsi = computeBSI(siteDomain, newsType, start, end)
            BigDecimal var3 = new BigDecimal(bsi * 100)
            def bsiFormat = df.format(var3) == ".00" ? "0.00" : df.format(var3)

            double tsi = computeTSI(siteDomain, newsType, start, end)
            BigDecimal var4 = new BigDecimal(tsi * 100)
            def tsiFormat = df.format(var4) == ".00" ? "0.00" : df.format(var4)

            result <<
                    [siteTypeName: newsTypeToTypeName(newsType), siteName: site.siteName,
                     psi: psiFormat, mii: miiFormat, bsi: bsiFormat, tsi: tsiFormat]
        }

        return result
    }

    Map getWeeklyNewsSummaryTotal(Long userId) {
        def channel = channelRepo.getAnalysisSites(userId)
        if (channel.status != HttpStatus.SC_OK || null == channel.msg) {
            userId = demoUserId
            channel = channelRepo.getAnalysisSites(userId)
        }
        if (channel.status != HttpStatus.SC_OK || null == channel.msg) {
            return null
        }
        def endDate = DateUitl.getDayBegin()
        def startDate = DateUitl.addDay(endDate, -7)
        def sdf = new SimpleDateFormat("yyyy-MM-dd")
        def end = sdf.format(endDate)
        def start = sdf.format(startDate)
        def chanles = channel.msg
        def sites = chanles?.sites
        if (!sites) {
            return null
        }
        def userSites = []
        sites.each { site ->
            String siteDomain = ""
            Long newsType = 0L
            switch (site.siteType as Long) {
                case 1:
                    siteDomain = UrlUtils.getDomainFromUrl(site.siteDomain as String)
                    newsType = 1L
                    break
                case 2:
                    siteDomain = site.siteName
                    newsType = 4L
                    break
                case 3:
                    siteDomain = site.siteName
                    newsType = 6L
                    break
            }
            userSites << ["siteDomain": siteDomain, "newsType": newsType]
        }

        DecimalFormat df = new DecimalFormat("#.00")
        def psi = computeTotalPSI(userSites, start, end)
        BigDecimal var1 = new BigDecimal(psi * 100)
        def psiFormat = df.format(var1) == ".00" ? "0.00" : df.format(var1)

        def mii = computeTotalMII(userSites, start, end)
        BigDecimal var2 = new BigDecimal(mii * 100)
        def miiFormat = df.format(var2) == ".00" ? "0.00" : df.format(var2)

        def bsi = computeTotalBSI(userSites, start, end)
        BigDecimal var3 = new BigDecimal(bsi * 100)
        def bsiFormat = df.format(var3) == ".00" ? "0.00" : df.format(var3)

        def tsi = computeTotalTSI(userSites, start, end)
        BigDecimal var4 = new BigDecimal(tsi * 100)
        def tsiFormat = df.format(var4) == ".00" ? "0.00" : df.format(var4)

        return [psi: psiFormat, mii: miiFormat, bsi: bsiFormat, tsi: tsiFormat]
    }

    static String newsTypeToTypeName(Long newsType) {
        switch (newsType) {
            case 1L:
                return "网站"
            case 6L:
                return "微信"
            case 4L:
                return "微博"
        }
    }

}
