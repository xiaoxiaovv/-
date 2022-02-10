package com.istar.mediabroken.api.statistics

import com.istar.mediabroken.api.CheckPrivilege
import com.istar.mediabroken.api.CurrentUser
import com.istar.mediabroken.entity.LoginUser
import com.istar.mediabroken.entity.account.Privilege
import com.istar.mediabroken.service.statistics.StatisticsManageService
import com.istar.mediabroken.utils.DateUitl
import groovy.util.logging.Slf4j
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

import static com.istar.mediabroken.api.ApiResult.apiResult
import static org.springframework.web.bind.annotation.RequestMethod.GET
import static org.springframework.web.bind.annotation.RequestMethod.PUT

/**
 * Author: zc
 * Time: 2018/3/15
 */
@RestController
@Slf4j
@RequestMapping(value = "/api/statistics")
class StatisticsManageApiController {
    @Autowired
    StatisticsManageService statisticsManageService
    /**
     * 查询今日发布
     * @param user
     * @return
     */
    @CheckPrivilege(privileges = [Privilege.DATASTATISTICS])
    @RequestMapping(value = "/publish/today", method = GET)
    @ResponseBody
    public Map getTodayPublished(
            @CurrentUser LoginUser user
    ) {
        Map result = statisticsManageService.getTodayPublished(user.orgId);
        return result;
    }

    /**
     * 发布明细
     * @param user
     * @param startDate
     * @param endDate
     * @param channelType pushChannel,weiboChannel,wechatChannel,toutiaoChannel,qqomChannel
     * @param teamName
     * @param pageNo
     * @param pageSize
     * @return
     */
    @CheckPrivilege(privileges = [Privilege.DATASTATISTICS])
    @RequestMapping(value = "/publish/publishDetail", method = GET)
    @ResponseBody
    public Map getPublishDetail(
            @CurrentUser LoginUser user,
            @RequestParam(value = "startDate", required = true) String startDate,    // 开始时间
            @RequestParam(value = "endDate", required = true) String endDate,    // 结束时间
            @RequestParam(value = "channelType", required = false, defaultValue = "") String channelType,
            @RequestParam(value = "teamName", required = false, defaultValue = "") String teamName,
            @RequestParam(value = "publisher", required = false, defaultValue = "") String publisher,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize
    ) {
        Map result = [:]
        Date startTime = null
        Date endTime = null
        try {
            startTime = DateUitl.convertFormatDate(startDate, "yyyy-MM-dd HH:mm:ss")
            endTime = DateUitl.convertFormatDate(endDate, "yyyy-MM-dd HH:mm:ss")

            if (!startTime && !endTime){
                endTime = new Date()
                startTime = DateUitl.getBeginDayOfParm(endTime)
            }
            if (startTime && !endTime){
                endTime = new Date()
            }
            if (!startTime && endTime){
                startTime = DateUitl.getBeginDayOfParm(endTime)
            }

            if (startTime > endTime){
                return apiResult(status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "开始时间需小于结束时间")
            }
            if ((DateUitl.getDistance(startTime, endTime) > 31)){
                return apiResult(status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "时间区间请选择31天内")
            }
        } catch (Exception e) {
            return apiResult(status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "请输入正确的时间格式")
        }
        result = statisticsManageService.getPublishDetailInfo(user.orgId, startTime, endTime, channelType, teamName, publisher, pageNo, pageSize);
        return result;
    }

    /**
     * 明细页组别列表
     * @param user
     * @return
     */
    @CheckPrivilege(privileges = [Privilege.DATASTATISTICS])
    @RequestMapping(value = "/publish/teamNameList", method = GET)
    @ResponseBody
    public Map getTeamNameList(
            @CurrentUser LoginUser user
    ) {
        Map result = statisticsManageService.getTeamNameList(user.orgId);
        return result;
    }

    /**
     * 查询昨日发布
     * @param user
     * @return
     */
    @CheckPrivilege(privileges = [Privilege.DATASTATISTICS])
    @RequestMapping(value = "/publish/yesterday", method = GET)
    @ResponseBody
    public Map getYesterdayPublished(
            @CurrentUser LoginUser user
    ) {
        Map result = statisticsManageService.getYesterdayPublished(user.orgId);
        return result;
    }

    /**
     * 发布趋势
     * @param user
     * @return
     */
    @CheckPrivilege(privileges = [Privilege.DATASTATISTICS])
    @RequestMapping(value = "/publish/publishTrend", method = GET)
    @ResponseBody
    public Map getPublishTrend(
            @CurrentUser LoginUser user,
            @RequestParam(value = "trendType", required = false, defaultValue = "7") int trendType
    ) {
        def result = statisticsManageService.getPublishTrend(user.orgId, trendType)
        return result;
    }

    /**
     * 发布统计查询
     * @param user
     * @param startDate
     * @param endDate
     * @param teamName
     * @param publisher
     * @return
     */
    @CheckPrivilege(privileges = [Privilege.DATASTATISTICS])
    @RequestMapping(value = "/publish/publishStatistics", method = GET)
    @ResponseBody
    public Map getPublishStatistics(
            @CurrentUser LoginUser user,
            @RequestParam(value = "startDate", required = true) String startDate,    // 开始时间
            @RequestParam(value = "endDate", required = true) String endDate,    // 结束时间
            @RequestParam(value = "teamName", required = false, defaultValue = "") String teamName,
            @RequestParam(value = "publisher", required = false, defaultValue = "") String publisher
    ) {
        Map result = [:]
        Date startTime = null
        Date endTime = null
        try {
            startTime = DateUitl.convertFormatDate(startDate, "yyyy-MM-dd")
            endTime = DateUitl.convertFormatDate(endDate, "yyyy-MM-dd")
            if (endTime){
                endTime = DateUitl.addDay(endTime, 1)
            }
            if (!startTime && !endTime){
                return apiResult(status: HttpStatus.SC_OK, list: [])
            }
            if (startTime && !endTime){
                endTime = DateUitl.addDay(startTime, 1)
            }
            if (!startTime && endTime){
                startTime = endTime
                endTime = DateUitl.addDay(endTime, 1)
            }
            if (startTime > endTime){
                return apiResult(status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "开始时间需小于结束时间")
            }
        } catch (Exception e) {
            return apiResult(status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "请输入正确的时间格式")
        }
        result = statisticsManageService.getPublishStatistics(user.orgId, startTime, endTime, teamName, publisher);
        return result;
    }
}
