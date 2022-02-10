package com.istar.mediabroken.service.evaluate

import com.istar.mediabroken.entity.capture.Site
import com.istar.mediabroken.entity.evaluate.EvaluateNews
import com.istar.mediabroken.entity.evaluate.EvaluateReport
import com.istar.mediabroken.entity.evaluate.SiteInfoWeekly
import com.istar.mediabroken.repo.evaluate.EvaluateNewsRepo
import com.istar.mediabroken.repo.evaluate.EvaluateReportRepo
import com.istar.mediabroken.repo.evaluate.KeywordsWeeklyRepo
import com.istar.mediabroken.repo.evaluate.SiteInfoWeeklyRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
/**
 * @author hanhui
 * @date 2018/6/28 13:52
 * @desc 内容测评相关
 * */
@Service
class EvaluateContentService {
    @Autowired
    EvaluateNewsRepo evaluateNewsRepo
    @Autowired
    EvaluateReportRepo evaluateReportRepo
    @Autowired
    EvaluateService evaluateService
    @Autowired
    KeywordsWeeklyRepo keywordsWeeklyRepo
    @Autowired
    SiteInfoWeeklyRepo siteInfoWeeklyRepo

    /**
     * 内容排行
     * @param userId
     * @param rankType sumRead:累计阅读  sumLike:累计点赞  sumReprint:累计转载
     * @return
     */
    List<EvaluateNews> getNewsRankByrankType(EvaluateReport evaluateReport, String rankType) {
        Date startTime = evaluateReport.startTime
        Date endTime = evaluateReport.endTime
        HashMap<Integer, List<String>> websiteMap = evaluateService.groupWebSitesByType(evaluateReport.channels)
        List<String> weChatList = websiteMap.get(Site.SITE_TYPE_WECHAT) ?: []
        List<String> weiboList = websiteMap.get(Site.SITE_TYPE_WEIBO) ?: []
        List<String> webSiteList = websiteMap.get(Site.SITE_TYPE_WEBSITE) ?: []
        List<EvaluateNews> evaluateNews
        if ("sumRead".equals(rankType)) {
            evaluateNews = evaluateNewsRepo.getNewsRankByChannel([], weChatList, [], startTime, endTime,"readCount")
        } else if ("sumLike".equals(rankType)) {
            evaluateNews = evaluateNewsRepo.getNewsRankByChannel([], weChatList, weiboList, startTime, endTime,"likeCount")
        } else if ("sumReprint".equals(rankType)) {
            evaluateNews = evaluateNewsRepo.getNewsRankByChannel(webSiteList, weChatList, weiboList, startTime, endTime,"reprintCount")
        }
        return evaluateNews
    }

    //词云
    List getKeywords(EvaluateReport evaluateReport) {
        Date startTime = evaluateReport.startTime
        Date endTime = evaluateReport.endTime
        HashMap<Integer, List<String>> websiteMap = evaluateService.groupWebSitesByType(evaluateReport.channels)
        List<String> weChatList = websiteMap.get(Site.SITE_TYPE_WECHAT)
        List<String> weiBoList = websiteMap.get(Site.SITE_TYPE_WEIBO)
        List<String> webSiteList = websiteMap.get(Site.SITE_TYPE_WEBSITE)
        List resultLsit = keywordsWeeklyRepo.getKeywordsWeeklyBySite(webSiteList ?: [], weChatList ?: [], weiBoList ?: [], startTime, endTime)
        return resultLsit
    }

    //平均阅读点赞分布
    Map getAvgReadAndLike(EvaluateReport evaluateReport) {
        Map result = [:]
        Date startTime = evaluateReport.startTime
        Date endTime = evaluateReport.endTime
        HashMap<Integer, List<String>> websiteMap = evaluateService.groupWebSitesByType(evaluateReport.channels)
        if (websiteMap.get(Site.SITE_TYPE_WECHAT)){
            Map readAndLikeMap = siteInfoWeeklyRepo.getAvgReadAndLike(websiteMap.get(Site.SITE_TYPE_WECHAT), startTime
                    , endTime, Site.SITE_TYPE_WECHAT)
            List<SiteInfoWeekly> siteInfoWeeklyList = siteInfoWeeklyRepo.getIndexRankBySite(Site.SITE_TYPE_WECHAT, startTime,
                    endTime, websiteMap.get(Site.SITE_TYPE_WECHAT) ?: [], "createTime", null, null)
            for (SiteInfoWeekly siteInfo : siteInfoWeeklyList) {
                siteInfo.avgLike = siteInfo.avgLike as int
                siteInfo.avgRead = siteInfo.avgRead as int
            }
            result.putAll(readAndLikeMap)
            result.put("data", siteInfoWeeklyList)
        }else {
            result.put("weChat","no")
        }
        return result
    }
}
