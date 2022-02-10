package com.istar.mediabroken.api.compile

import com.alibaba.fastjson.JSONObject
import com.istar.mediabroken.api.CurrentUser
import com.istar.mediabroken.api.CurrentUserId
import com.istar.mediabroken.entity.LoginUser
import com.istar.mediabroken.service.compile.ReportService
import com.istar.mediabroken.service.compile.ReportService
import com.istar.mediabroken.utils.UploadUtil
import com.istar.mediabroken.utils.Word2Html
import groovy.util.logging.Slf4j
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.*

import javax.servlet.http.HttpServletRequest

import static com.istar.mediabroken.api.ApiResult.apiResult
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR
import static org.springframework.web.bind.annotation.RequestMethod.*

/**
 * Author: zc
 * Time: 2017/8/7
 */
@RestController
@Slf4j
@RequestMapping(value = "/api/compile/")
class ReportApiController {
    @Autowired
    ReportService reportService
    /**
     * 数据-生成事件
     * @param userId
     * @param reportName
     * @param keyWords
     * @param timeRange
     * @return
     */
    @RequestMapping(value = "report", method = POST)
    public Map addUserReport(
            @CurrentUserId Long userId,
            @RequestParam(value = "name", required = true) String reportName,
            @RequestParam(value = "keywords", required = true) String keyWords,
            @RequestParam(value = "timeRange", required = true, defaultValue = "7") int timeRange
    ) {
        //判断事件名称是否为空
        if (reportName.equals("") || reportName.equals("")) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, '请填写事件名称');
        }
        //关键词是否至少3个
        if (!"".equals(keyWords)) {
            keyWords=keyWords.replaceAll("[' ']+"," ");
            def keyWord = keyWords.split(" ");
            if (keyWord.size() < 3) {
                return apiResult(SC_INTERNAL_SERVER_ERROR, '关键词至少设置3个');
            }
        }else{
            return apiResult(SC_INTERNAL_SERVER_ERROR, '关键词至少设置3个');
        }
        if (!timeRange) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, '请选择时间范围');
        }
        def result = reportService.addUserReport(userId, reportName.trim(), keyWords.trim(), timeRange)
        return result
    }
    /**
     * 获取用户事件
     * @param userId
     * @return
     */
    @RequestMapping(value = "report", method = GET)
    public Map getUserReport(
            @CurrentUserId Long userId
    ) {
        def result = reportService.getUserReport(userId)
        return result;
    }

    /**
     * 获取chart数据
     * @param userId
     * @param reportId
     * @param chartType
     * @return
     */
    @RequestMapping(value = "report/{reportId}/{chartType}", method = GET)
    public Map getEventTrend(
            @CurrentUserId Long userId,
            @PathVariable String reportId,
            @PathVariable String chartType

    ) {
        def result = reportService.getTrendByChartType(userId, reportId, chartType)
        return result;
    }

    /**
     * 获取进度条
     * @param userId
     * @param reportId
     * @return
     */
    @RequestMapping(value = "report/{reportId}/progressRate", method = GET)
    public Map getProgressRate(
            @CurrentUserId Long userId,
            @PathVariable String reportId
    ) {
        def result = reportService.getProgressRate(userId, reportId)
        return apiResult(result);
    }

}
