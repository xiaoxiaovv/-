package com.istar.mediabroken.api.evaluate

import com.alibaba.fastjson.JSONObject
import com.istar.mediabroken.api.CurrentUser
import com.istar.mediabroken.entity.LoginUser
import com.istar.mediabroken.entity.evaluate.EvaluateReport
import com.istar.mediabroken.entity.evaluate.ReportStatusEnum
import com.istar.mediabroken.service.evaluate.EvaluateChannelService
import com.istar.mediabroken.service.evaluate.EvaluateReportService
import com.istar.mediabroken.service.evaluate.EvaluateService
import com.istar.mediabroken.utils.DateUitl
import com.istar.mediabroken.utils.DownloadUtils
import groovy.util.logging.Slf4j
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

import javax.servlet.http.HttpServletResponse

import static com.istar.mediabroken.api.ApiResult.apiResult

/**
 * @author zxj
 * @create 2018/7/2
 */
@RestController
@Slf4j
@RequestMapping(value = "/api/evaluate/")
class EvaluateReportApiController {

    @Autowired
    EvaluateReportService evaluateReportService
    @Autowired
    EvaluateChannelService evaluateChannelService
    @Autowired
    EvaluateService evaluateService

    @RequestMapping(value = "status", method = RequestMethod.GET)
    Map getEvaluateReportStatus(
            @CurrentUser LoginUser user

    ) {
        List list = new ArrayList();
        for (ReportStatusEnum reportStatusEnum : ReportStatusEnum.values()) {
            list.add(reportStatusEnum.toMap());
        }
        Map<String, Object> m = new HashMap<>();
        m.put("list", list)
        return apiResult(m)
    }

    @RequestMapping(value = "time", method = RequestMethod.GET)
    Map getEvaluateReportTime(
            @CurrentUser LoginUser user

    ) {
        def starTime = DateUitl.getLastWeekMondayBegin(new Date())
        def endTime = DateUitl.addSeconds(DateUitl.getThisWeekMondayBegin(new Date()), -1)
        return apiResult([starTime: starTime, endTime: endTime])
    }

    @RequestMapping(value = "report", method = RequestMethod.POST)
    Map addEvaluateReport(
            @CurrentUser LoginUser user,
            @RequestParam(value = "evaluateName", required = true) String evaluateName,
            @RequestParam(value = "channelsName", required = true) String channelsName
    ) {
        if (evaluateName.length() > 20) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '测评名称长度20个字符以内！')
        }
        if (channelsName.length() > 20) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '总渠道名称长度20个字符以内！')
        }
        def channel = evaluateChannelService.findAllChannel(user.userId)
        if (channel.size() <= 0 || !channel) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '请您添加要测评的渠道！')
        }

        def time = evaluateReportService.getEvaluateByTime(user.userId, false, DateUitl.getDayBegin(), DateUitl.getBeginDayOfTomorrow())
        if (time) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '您今天已经创建过测评了，请明天再试！')
        }
        def name = evaluateReportService.getEvaluateByName(user.userId, evaluateName)
        if (name) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '评测名称已存在！')
        }

        try {
            evaluateReportService.addEvaluateReport(user.userId, evaluateName, channelsName, false, DateUitl.getLastWeekMondayBegin(new Date()), DateUitl.addSeconds(DateUitl.getThisWeekMondayBegin(new Date()), -1))
            return apiResult(HttpStatus.SC_OK, '添加成功！')
        } catch (e) {
            print(e)
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '添加失败！')
        }

    }

    /**
     * 渠道管理的查询
     * @param user
     * @param status
     * @param evaluateName
     * @param pageSize
     * @param pageNo
     * @return
     */
    @RequestMapping(value = "report", method = RequestMethod.GET)
    Map getEvaluateReports(
            @CurrentUser LoginUser user,
            @RequestParam(value = "status", required = false, defaultValue = "0") int status,
            @RequestParam(value = "evaluateName", required = false, defaultValue = "") String evaluateName,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo
    ) {
        def name = evaluateReportService.getEvaluateReportByStatusAndName(user.userId, evaluateName, status, pageSize, pageNo)
        return apiResult([list: name])
    }

    /**
     * 删除测评
     * @param user
     * @param reportId
     * @return
     */
    @RequestMapping(value = "report/{reportId}", method = RequestMethod.DELETE)
    Map delEvaluateReport(
            @CurrentUser LoginUser user,
            @PathVariable(value = "reportId") String reportId
    ) {
        def id = evaluateReportService.delEvaluateReportById(user.userId, reportId)
        if (id) {
            return apiResult(HttpStatus.SC_OK, '删除成功！')
        }
        return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '删除失败！')
    }

    /**
     * 重试
     * @param user
     * @param reportId
     * @return
     */
    @RequestMapping(value = "report/{reportId}", method = RequestMethod.PUT)
    Map retryEvaluateReport(
            @CurrentUser LoginUser user,
            @PathVariable(value = "reportId") String reportId
    ) {
        def id = evaluateReportService.retryEvaluateReportById(user.userId, reportId)
        if (id) {
            return apiResult(HttpStatus.SC_OK, '操作成功！')
        }
        return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '操作失败！')
    }

    /**
     * 生成word
     * @param user
     * @param reportId
     * @return
     */
    @RequestMapping(value = "report/{reportId}", method = RequestMethod.POST)
    Map createEvaluateReport(
            @CurrentUser LoginUser user,
            @PathVariable(value = "reportId") String reportId,
            @RequestBody String data
    ) {
        JSONObject dataJson = JSONObject.parseObject(data);
        List imgList = dataJson.get("imgList");
        if (imgList.size() < 8) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '生成失败！')
        }

        def rep = evaluateService.getEvaluateReportById(reportId)
        if (rep.indicator) {
            return apiResult(HttpStatus.SC_OK, '报告生成中请稍后！')
        }
        evaluateReportService.modifyEvaluateReportIndicator(user.userId, reportId, true)
        try {
            def report = evaluateReportService.createReport(user, reportId, imgList)
            return apiResult(HttpStatus.SC_OK, report)
        } catch (e) {
            print(e.printStackTrace())
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '生成失败！')
        } finally {
            evaluateReportService.modifyEvaluateReportIndicator(user.userId, reportId, false)
        }
        return apiResult(HttpStatus.SC_OK, '生成成功！')
    }

    /**
     * 下载模板
     * @param user
     * @param reportId
     * @return
     */
    @RequestMapping(value = "report/{reportId}", method = RequestMethod.GET)
    Map downLoadEvaluateReport(
            @CurrentUser LoginUser user,
            HttpServletResponse response,
            @PathVariable(value = "reportId") String reportId
    ) {
        EvaluateReport id = evaluateReportService.getEvaluateReportById(user.userId, reportId)
        def path = evaluateReportService.downLoadEvaluateReport(user.userId, reportId);
        if (path.equals("") || id == null) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '没有找到相关下载文件！')
        }
        try {
            DownloadUtils.download(path, response, id.evaluateName + ".doc")
        } catch (IOException e) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '下载报告失败！')
        }
        return apiResult(HttpStatus.SC_OK, '下载成功！')
    }

}
