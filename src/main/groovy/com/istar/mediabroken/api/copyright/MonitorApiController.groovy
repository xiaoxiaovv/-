package com.istar.mediabroken.api.copyright

import com.alibaba.fastjson.JSONObject
import com.istar.mediabroken.api.CurrentUserId
import com.istar.mediabroken.service.copyright.MonitorService
import com.istar.mediabroken.utils.DownloadUtils
import groovy.util.logging.Slf4j
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletResponse

import static com.istar.mediabroken.api.ApiResult.apiResult
import static org.springframework.web.bind.annotation.RequestMethod.DELETE
import static org.springframework.web.bind.annotation.RequestMethod.GET
import static org.springframework.web.bind.annotation.RequestMethod.POST
import static org.springframework.web.bind.annotation.RequestMethod.PUT

/**
 * Author: Luda
 * Time: 2017/8/8
 */
@RestController("MonitorApiController")
@Slf4j
@RequestMapping(value = "/api/copyright")
class MonitorApiController {
    @Autowired
    MonitorService monitorService

    /**
     * 添加版权监控设置
     * @param userId
     * @param title
     * @param url
     * @param author
     * @param date
     * @param media
     * @param whiteList
     * @param blackList
     * @param contentAbstract
     * @return
     */
    @RequestMapping(value = "/monitor", method = POST)
    public Map addCopyrightMonitor(
            @CurrentUserId Long userId,
            @RequestParam("title") String title,      // 标题
            @RequestParam("url") String url,
            @RequestParam(value = "author", required = false, defaultValue = "") String author,    // 作者
            @RequestParam(value = "startDate", required = false, defaultValue = "") String startDate,        // 监控开始日期
            @RequestParam(value = "media", required = false, defaultValue = "") String media,      // 媒体
            @RequestParam(value = "whiteList", required = false, defaultValue = "") String whiteList,      // 白名单,用逗号分隔
            @RequestParam(value = "blackList", required = false, defaultValue = "") String blackList,      // 黑白单,用逗号分隔
            @RequestParam(value = "contentAbstract", required = false, defaultValue = "") String contentAbstract
    ) {
        def result = monitorService.addCopyrightMonitor(userId, title, url, author, startDate, media, whiteList, blackList, contentAbstract)
        return result;
    }

    /**
     * 查询版权监控列表
     * @param userId
     * @return
     */
    @RequestMapping(value = "/monitors", method = GET)
    public Map getCopyrightMonitors(
            @CurrentUserId Long userId
    ) {
        def result = monitorService.getCopyrightMonitors(userId);
        return result;
    }

    /**
     * 查询版权监控详情
     * @param user
     * @param monitorId
     * @return
     */
    @RequestMapping(value = "/monitor/{monitorId:.*}", method = GET)
    public Map getCopyrightMonitor(
            @CurrentUserId Long userId,
            @PathVariable("monitorId") String monitorId
    ) {
        def result = monitorService.getCopyrightMonitor(userId, monitorId)
        return result;
    }

    /**
     * 修改版权监控设置
     * @param userId
     * @param monitorId
     * @param title
     * @param url
     * @param author
     * @param date
     * @param media
     * @param whiteList
     * @param blackList
     * @param contentAbstract
     * @return
     */
    @RequestMapping(value = "/monitor/{monitorId:.*}", method = PUT)
    public Map modifyCopyrightMonitor(
            @CurrentUserId Long userId,
            @PathVariable("monitorId") String monitorId,
            @RequestParam(value = "title", required = true) String title,
            @RequestParam(value = "url", required = false, defaultValue = "") String url,
            @RequestParam(value = "author", required = false, defaultValue = "") String author,    // 作者
            @RequestParam(value = "startDate", required = false, defaultValue = "") String startDate,        // 发布日期
            @RequestParam(value = "media", required = false, defaultValue = "") String media,      // 媒体
            @RequestParam(value = "whiteList", required = false, defaultValue = "") String whiteList,      // 白名单,用逗号分隔
            @RequestParam(value = "blackList", required = false, defaultValue = "") String blackList,      // 黑白单,用逗号分隔
            @RequestParam(value = "contentAbstract", required = false, defaultValue = "") String contentAbstract
    ) {
        def result = monitorService.modifyCopyrightMonitor(userId, monitorId, title, url, author, startDate, media, whiteList, blackList, contentAbstract)
        return result;
    }

    /**
     * 删除版权监控设置
     * @param userId
     * @param monitorId
     * @return
     */
    @RequestMapping(value = "/monitor/{monitorId:.*}", method = DELETE)
    public Map removeCopyrightMonitor(
            @CurrentUserId Long userId,
            @PathVariable("monitorId") String monitorId
    ) {
        def result = monitorService.removeCopyrightMonitor(userId, monitorId)
        return result;
    }

    /**
     * 保存copyRightFilter
     * @param user
     * @param whiteList
     * @param blackList
     * @return
     */
    @RequestMapping(value = "/monitor/filter", method = PUT)
    public Map modifyWhiteList(
            @CurrentUserId Long userId,
            @RequestParam("whiteList") String whiteList,
            @RequestParam("blackList") String blackList
    ) {
        def result = monitorService.modifyCopyRightFilter(userId, whiteList, blackList)
        return result;
    }

    /**
     * 疑似侵权列表
     * @param userId
     * @param monitorId
     * @param queryType 0全部 1白名单 2黑名单 3疑似侵权
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/monitor/{monitorId:.*}/news", method = GET)
    public Map getCopyrightMonitorNewsSuspected(
            @CurrentUserId long userId,
            @PathVariable(value = "monitorId") String monitorId,
            @RequestParam(value = "queryType", required = false, defaultValue = "0") int queryType,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize
    ) {
        def result = monitorService.getCopyrightMonitorNews(userId, monitorId, queryType, pageNo, pageSize)
        return result;
    }

    /**
     * 查询版权监控详情新闻列表-EXCEL导出
     * @param data
     * @return
     */
    @RequestMapping(value = "/monitor/{monitorId}/news/excel", method = POST)
    public Map getCopyrightMonitorNewsExcelOut(
            @PathVariable(value = "monitorId") String monitorId,
            @RequestBody String data
    ) {
        JSONObject dataJson = JSONObject.parseObject(data);
        String copyRightNewsIds = dataJson.get("copyRightNewsIds").toString();
        if (!copyRightNewsIds){
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "请选择要下载的新闻");
        }
        String[] copyRightNewsList = copyRightNewsIds.split(",")
        if (copyRightNewsList.length > 100){
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "最多下载100条内容");
        }
        def result = monitorService.copyrightMonitorNewsExcelOut(copyRightNewsList as List)
        return result
    }
    /**
     * 查询版权监控详情新闻列表-EXCEL导出
     * @param token
     * @param response
     * @return
     */
    @RequestMapping(value = "monitor/{monitorId}/news/excel/{excelId}", method = GET)
    public Map getCopyrightMonitorNewsExcelOutByToken(
            @PathVariable("excelId") String excelId,
            @PathVariable(value = "monitorId") String monitorId,
            HttpServletResponse response
    ) {
        def path = monitorService.getCopyrightMonitorNewsExcelByToken(excelId);
        if ("".equals(path)) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: '没有找到相关下载文件！'])
        }
        try {
            DownloadUtils.download(path, response,null)
        } catch (IOException e) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: '下载发生错误！'])
        }
        return apiResult([status: HttpStatus.SC_OK, msg: '下载完成！'])
    }
    /**
     * 删除疑似侵权列表数据
     * @param userId
     * @param monitorId
     * @param newsIds
     * @return
     */
    @RequestMapping(value = "/monitor/{monitorId:.*}/news/{newsIds:.*}", method = DELETE)
    public Map removeCopyrightMonitorNews(
            @CurrentUserId Long userId,
            @PathVariable("monitorId") String monitorId,
            @PathVariable("newsIds") String newsIds
    ) {
        def result = monitorService.removeCopyrightMonitorNews(userId, monitorId, newsIds)
        return result;
    }

    /**
     * 查询传播分析总数
     * @param monitorId
     * @return
     */
    @RequestMapping(value = "/monitor/{monitorId}/analysis/summary", method = GET)
    public Map getAnalysisSummary(
            @CurrentUserId Long userId,
            @PathVariable(value = "monitorId") String monitorId
    ) {
        def result = monitorService.getAnalysisSummary(userId, monitorId)
        return result
    }
    /**
     * 查询传播分析一周趋势
     * @param monitorId
     * @return
     */
    @RequestMapping(value = "/monitor/{monitorId}/analysis/reprintTrend", method = GET)
    public Map getAnalysisReprintTrend(
            @CurrentUserId Long userId,
            @PathVariable(value = "monitorId") String monitorId
    ) {
        def result = monitorService.getAnalysisReprintTrend(userId, monitorId)
        return result
    }
    /**
     * 查询传播分析各渠道转载分布
     * @param monitorId
     * @return
     */
    @RequestMapping(value = "/monitor/{monitorId}/analysis/channelReprintSummary", method = GET)
    public Map getAnalysisChannelReprintSummary(
            @CurrentUserId Long userId,
            @PathVariable(value = "monitorId") String monitorId
    ) {
        def result = monitorService.getAnalysisChannelReprintSummary(userId, monitorId)
        return result
    }
}
