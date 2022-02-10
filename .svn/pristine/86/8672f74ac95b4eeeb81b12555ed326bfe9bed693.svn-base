package com.istar.mediabroken.api.capture

import com.istar.mediabroken.api.CheckPrivilege
import com.istar.mediabroken.api.CurrentUser
import com.istar.mediabroken.entity.LoginUser
import com.istar.mediabroken.entity.account.Privilege
import com.istar.mediabroken.service.capture.InstantNewsService
import com.istar.mediabroken.utils.DateUitl
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

import static com.istar.mediabroken.api.ApiResult.apiResult
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR
import static org.apache.http.HttpStatus.SC_OK
import static org.springframework.web.bind.annotation.RequestMethod.*

/**
 * Author: zc
 * Time: 2018/5/9
 */
@RestController("InstantNewsApiController")
@Slf4j
@RequestMapping(value = "/api/capture/")
class InstantNewsApiController {
    @Autowired
    InstantNewsService instantNewsService

    /**
     * 查询时间段内的新闻数量（查询一段时间之内的新进新闻数量）
     * @param user
     * @return
     */
    @CheckPrivilege(privileges = [Privilege.INSTANTNEWS_VIEW])
    @RequestMapping(value = "instantNews/count", method = GET)
    public Map getInstantNewsCount(
            @CurrentUser LoginUser user,
            @RequestParam(value = "startTime", required = false, defaultValue = "0") long startTime,
            @RequestParam(value = "endTime", required = false, defaultValue = "0") long endTime
    ) {
        long count = 0
        def result = [status: SC_OK, count: count]
        if (startTime > endTime || startTime == 0 || endTime == 0) {
            return result
        }
        try {
            Date endDate = new Date(endTime)//new Date()
            Date startDate = new Date(startTime)//DateUitl.addHour(endTime, -8)
            count = instantNewsService.getInstantNewsCount(user.userId, startDate, endDate)
            result.count = count
        } catch (Exception e) {
            return result
        }
        return result
    }

    /**
     * 查询资讯新闻列表：(每次查询当前时间往前八个小时的新闻数据)
     * @param user
     * @param pageSize
     * @param pageNo
     * @return
     */
    @CheckPrivilege(privileges = [Privilege.INSTANTNEWS_VIEW])
    @RequestMapping(value = "instantNews", method = GET)
    public Map getInstantNews(
            @CurrentUser LoginUser user,
            @RequestParam(value = "startTime", required = false, defaultValue = "0") long startTime,
            @RequestParam(value = "endTime", required = false, defaultValue = "0") long endTime,
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") int pageSize,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo,
            @RequestParam(value = "queryId", required = false, defaultValue = "") String queryId,
            @RequestParam(value = "queryName", required = false, defaultValue = "") String queryName
    ) {
        def result = [status: SC_OK, list: []]
        if (startTime > endTime) {
            return result
        }
        try {
            Date endDate = null
            Date startDate = null
            if (startTime == 0 || endTime == 0) {
                endDate = new Date()
                startDate = DateUitl.addHour(endDate, -8)
            } else {
                endDate = new Date(endTime)
                startDate = new Date(startTime)
            }
            Map map = instantNewsService.getInstantNews(user.userId, startDate, endDate, pageSize, queryId, queryName)
            if (pageNo == 1 && !map.list){
                map.msg = "所选站点暂时没有数据"
            }
            return apiResult(map)
        } catch (Exception e) {
            log.info(e.printStackTrace())
            return result
        }
    }
    /**
     * 查询资讯快选标记新闻列表
     * @param user
     * @param pageSize
     * @param pageNo
     * @return
     */
    @CheckPrivilege(privileges = [Privilege.INSTANTNEWS_VIEW])
    @RequestMapping(value = "instantNewsMark", method = GET)
    public Map getMarkedNews(
            @CurrentUser LoginUser user,
            @RequestParam(value = "queryName", required = false, defaultValue = "") String queryName,
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") int pageSize,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo
    ) {
        def list = instantNewsService.getInstantMarkedNews(user.userId, queryName, pageSize, pageNo)
        def result = [status: SC_OK, list: list]
        if (pageNo == 1 && !result.list){
            result.msg = "未搜索到相关内容"
        }
        return result
    }
    /**
     * 标记资讯快选新闻
     * @param user
     * @param newsId
     * @return
     */
    @CheckPrivilege(privileges = [Privilege.INSTANTNEWS_VIEW])
    @RequestMapping(value = "instantNewsMark/{newsId}", method = POST)
    public Map addMarkedNews(
            @CurrentUser LoginUser user,
            @PathVariable("newsId") String newsId
    ) {
        def result = instantNewsService.addInstantNewsMarked(user.userId, newsId)
        if (result) {
            return [status: SC_OK, msg: "标记成功"]
        } else {
            return [status: SC_INTERNAL_SERVER_ERROR, msg: "标记异常"]
        }

    }

    /**
     * 取消标记资讯快选新闻
     * @param user
     * @param newsId
     * @return
     */
    @CheckPrivilege(privileges = [Privilege.INSTANTNEWS_VIEW])
    @RequestMapping(value = "instantNewsMark/{newsId}", method = DELETE)
    public Map removeMarkedNews(
            @CurrentUser LoginUser user,
            @PathVariable("newsId") String newsId
    ) {
        try {
            instantNewsService.removeInstantNewsMarked(user.userId, newsId)
            return [status: SC_OK, msg: "取消标记"]
        } catch (Exception e) {
            return [status: SC_INTERNAL_SERVER_ERROR, msg: "取消标记异常"]
        }

    }

}
