package com.istar.mediabroken.service.InformationStatistics

import com.istar.mediabroken.entity.PushTypeEnum
import com.istar.mediabroken.entity.capture.Site
import com.istar.mediabroken.repo.InformationStatistics.StatisticsNewsRepo
import com.istar.mediabroken.repo.capture.SiteRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Created by zxj on   2018/3/16
 */
@Service
class StatisticsNewsService {

    @Autowired
    SiteRepo siteRepo
    @Autowired
    StatisticsNewsRepo statisticsNewsRepo

    void getInformationDataInfluxes(Date startTime, Date endTime) {

        //查询网站
        long web = siteRepo.getNewCountByTypeAndTime(Site.SITE_TYPE_WEBSITE, startTime, endTime);
        //微信总数
        long weChat = siteRepo.getNewCountByTypeAndTime(Site.SITE_TYPE_WECHAT, startTime, endTime);
        //微博总数
        long weibo = siteRepo.getNewCountByTypeAndTime(Site.SITE_TYPE_WEIBO, startTime, endTime);
        //查询总数
        long amount = siteRepo.getNewCountByTypeAndTime(0, startTime, endTime);

        //
        Map map = new HashMap();
        map.put("webCount", web);
        map.put("weChatCount", weChat);
        map.put("weiboCount", weibo);
        map.put("amount", amount);
        map.put("statisticalTime", startTime);
        statisticsNewsRepo.addDataToInfluxes(map)
    }

    void getInformationDataOutflow(Date startTime, Date endTime) {

        //网站推送数
        def webCount = statisticsNewsRepo.getDataByTypeAndTimeAndNewsType(1, PushTypeEnum.NEWS_PUSH.index, [1], startTime, endTime)
        //微信推送数
        def weChatCount = statisticsNewsRepo.getDataByTypeAndTimeAndNewsType(1, PushTypeEnum.NEWS_PUSH.index, [6], startTime, endTime)
        //微博推送数
        def weiBoCount = statisticsNewsRepo.getDataByTypeAndTimeAndNewsType(1, PushTypeEnum.NEWS_PUSH.index, [401, 401], startTime, endTime)
        //文稿推送数
        def articlePush = statisticsNewsRepo.getDataByTypeAndTime(1, PushTypeEnum.ARTICLE_PUSH.index, startTime, endTime)
        //文稿同步
        def articleSyc = statisticsNewsRepo.getArticleSycSum(5, PushTypeEnum.ARTICLE_PUSH.index, startTime, endTime)

        def amount = webCount + weChatCount + weiBoCount + articlePush + articleSyc
        Map map = new HashMap();
        map.put("webCount", webCount)
        map.put("weChatCount", weChatCount)
        map.put("weiBoCount", weiBoCount)
        map.put("articlePush", articlePush)
        map.put("articleSyc", articleSyc)
        map.put("amount", amount)
        map.put("statisticalTime", startTime)
        statisticsNewsRepo.addDataToStatisticsOperation(map)
    }

}
