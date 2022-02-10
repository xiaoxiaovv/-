package com.istar.mediabroken.api.analysis

import com.alibaba.fastjson.JSON
import com.istar.mediabroken.api.CurrentUserId
import com.istar.mediabroken.service.analysis.ChannelService
import groovy.util.logging.Slf4j
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import static com.istar.mediabroken.api.ApiResult.apiResult
import static org.springframework.web.bind.annotation.RequestMethod.GET
import static org.springframework.web.bind.annotation.RequestMethod.POST
import static org.springframework.web.bind.annotation.RequestMethod.PUT

/**
 * Author: Luda
 * Time: 2017/8/2
 */
@RestController
@Slf4j
@RequestMapping(value = "/api/analysis/")
class ChannelApiController {
    @Autowired
    ChannelService channelService
    @Value('${4power.demoUserId}')
    long demoUserId
    /**
     * 获取渠道设置
     * @param userId
     * @return
     */
    @RequestMapping(value = "channel", method = GET)
    public Map getUserChannel(
            @CurrentUserId Long userId
    ) {
        def channel = channelService.getAnalysisSites(userId)
        if (channel.status != HttpStatus.SC_OK || null == channel.msg) {
            userId = demoUserId
            channel = channelService.getAnalysisSites(userId)
        }
        if (channel.status != HttpStatus.SC_OK || null == channel.msg) {
            return null
        }
        return channel;
    }

    /**
     * 添加渠道设置
     * @param userId
     * @param orgName
     * @param sites
     * @return
     */
    @RequestMapping(value = "channel", method = POST)
    public Map addAnalysisSites(
            @CurrentUserId Long userId,
            @RequestParam(value = "orgName") String orgName,
            @RequestParam(value = "sites") String sites
    ) {
        def sitesArray = JSON.parseArray(sites)
        def result = channelService.modifyAnalysisSites([
                userId : userId,
                orgName: orgName,
                sites  : sitesArray,
                updateTime: new Date()
        ])
        return result;
    }

    /**
     * 更新渠道设置
     * @param userId
     * @param orgName
     * @param sites
     * @return
     */
    @RequestMapping(value = "channel", method = PUT)
    public Map modifyAnalysisSites(
            @CurrentUserId Long userId,
            @RequestParam(value = "orgName") String orgName,
            @RequestParam(value = "sites") String sites
    ) {
        def sitesArray = JSON.parseArray(sites)
        def result = channelService.modifyAnalysisSites([
                userId    : userId,
                orgName   : orgName,
                sites     : sitesArray,
                updateTime: new Date()
        ])
        return result;
    }

    @RequestMapping(value = "channel/weeklySummary", method = GET)
    public Map getWeeklySummary(
            @CurrentUserId Long userId
    ) {
        def result = [:]
        result.list = channelService.getWeeklyNewsSummary(userId)
        result.sum = channelService.getWeeklyNewsSummaryTotal(userId)

//        list.add([siteTypeName: '网站', siteName: '劳动午报', psi: 327.83, mii: 34.26, bsi: 76.57, tsi: 254.89])
//        list.add([siteTypeName: '微博', siteName: '劳动午报', psi: 327.83, mii: 34.26, bsi: 76.57, tsi: 254.89])
//        list.add([siteTypeName: '微信', siteName: '劳动午报', psi: 327.83, mii: 34.26, bsi: 76.57, tsi: 254.89])
//        list.add([siteTypeName: '网站', siteName: '劳动午报', psi: 327.83, mii: 34.26, bsi: 76.57, tsi: 254.89])
//        list.add([siteTypeName: '微博', siteName: '劳动午报', psi: 327.83, mii: 34.26, bsi: 76.57, tsi: 254.89])
//        def sum = [psi: 106.22, mii: 615.66, bsi: 446.77, tsi: 731.33]
//        result.list = list
//        result.sum = sum
        return apiResult(result)
    }

    /**
     * 一周稿件排行
     * @param userId
     * @return
     */
    @RequestMapping(value = "channel/weeklyNews", method = GET)
    public Map getWeeklyNews(
            @CurrentUserId Long userId
    ) {
        def result = channelService.getWeeklyNews(userId)
        result.list = result.msg
        return apiResult(result)
    }
    /**
     * 渠道发稿统计
     * @param userId
     * @return
     */
    @RequestMapping(value = "channel/weeklyNewsSummary", method = GET)
    public Map getWeeklyNewsSummary(
            @CurrentUserId Long userId
    ) {
        def result = channelService.getWeeklyNewsSummary1(userId)
        return result
    }

    /**
     * 最新稿件
     * @param userId
     * @return
     */
    @RequestMapping(value = "channel/latestNews", method = GET)
    public Map getLatestNews(
            @CurrentUserId Long userId
    ) {
        def result = [:]
        def list = channelService.getLatestNewsByChannel(userId)
        result.list = list
        return apiResult(result)
    }

    @RequestMapping(value = "channel/wordsCloud", method = GET)
    public Map getWordsCloud(
            @CurrentUserId Long userId
    ) {
        def result = [:]
        def list = channelService.getWordsCloudByChannel(userId)
        //todo need to delete it
        list.each {
            if ("九大".equals(it.word)) {
                it.word = "十九大"
            } else {
                if ("十九".equals(it.word)) {
                    it.word = "人民"
                }
            }
        }
        result.list = list
        return apiResult(result)
    }
}
