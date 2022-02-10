package com.istar.mediabroken.api.evaluate

import com.istar.mediabroken.api.CurrentUser
import com.istar.mediabroken.entity.LoginUser
import com.istar.mediabroken.entity.account.Account
import com.istar.mediabroken.entity.evaluate.EvaluateReport
import com.istar.mediabroken.entity.evaluate.ReportStatusEnum
import com.istar.mediabroken.service.evaluate.EvaluateContentService
import com.istar.mediabroken.service.evaluate.EvaluateService
import groovy.util.logging.Slf4j
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import static com.istar.mediabroken.api.ApiResult.apiResult
import static org.springframework.web.bind.annotation.RequestMethod.GET

/**
 * @author hanhui
 * @date 2018/6/28 17:00
 * @desc 内容测评相关
 * */
@RestController
@Slf4j
@RequestMapping(value = "/api/evaluate/content")
class EvaluateContentApiController {

    @Autowired
    EvaluateContentService evaluateContentService
    @Autowired
    EvaluateService evaluateService


    @RequestMapping(value = "/rank", method = GET)
    Map getNewsRankByRankType(
            @CurrentUser LoginUser currentUser,
            @RequestParam(value = "rankType", required = true) String rankType
    ) {
        if ((Account.USERTYPE_TRIAL).equals(currentUser.userType)) {
            return apiResult([status: HttpStatus.SC_OK, msg: "升级正式版本以查看此内容，详情请联系客服或销售人员", list: []])
        }
        EvaluateReport evaluateReport = evaluateService.getValidEvaluateReportById(currentUser.userId)
        if (evaluateReport) {
            if (evaluateReport.status == ReportStatusEnum.failed.getKey()) {
                return apiResult([status: HttpStatus.SC_OK, msg: "测评创建失败，请检查渠道设置，如有问题请联系客服", list: []])
            } else if (evaluateReport.status == ReportStatusEnum.busy.getKey()) {
                return apiResult([status: HttpStatus.SC_OK, msg: "测评数据正在计算中...", list: []])
            } else {
                def resultList = evaluateContentService.getNewsRankByrankType(evaluateReport, rankType)
                if (resultList) {
                    return apiResult([status: HttpStatus.SC_OK, list: resultList, isDemo: false])
                } else {
                    return apiResult([status: HttpStatus.SC_OK, msg: "请在渠道管理页面设置需要测评的渠道", list: []])
                }
            }
        } else {
            EvaluateReport demoReport = evaluateService.getValidEvaluateReportById(0)
            def resultList = evaluateContentService.getNewsRankByrankType(demoReport, rankType)
            return apiResult([status: HttpStatus.SC_OK, list: resultList, isDemo: true])
        }
    }

    @RequestMapping(value = "/keywords", method = GET)
    Map getKeywords(
            @CurrentUser LoginUser currentUser,
            @RequestParam(value = "evaluateId", required = false, defaultValue = "") String evaluateId
    ) {
        if (evaluateId) {
            EvaluateReport evaluateReport = evaluateService.getEvaluateReportById(evaluateId)
            def resultList = evaluateContentService.getKeywords(evaluateReport)
            return apiResult([status: HttpStatus.SC_OK, list: resultList])
        } else {
            EvaluateReport evaluateReport = evaluateService.getValidEvaluateReportById(currentUser.userId)
            if (evaluateReport) {
                if (evaluateReport.status == ReportStatusEnum.failed.getKey()) {
                    return apiResult([status: HttpStatus.SC_OK, msg: "测评创建失败，请检查渠道设置，如有问题请联系客服", list: []])
                } else if (evaluateReport.status == ReportStatusEnum.busy.getKey()) {
                    return apiResult([status: HttpStatus.SC_OK, msg: "测评数据正在计算中...", list: []])
                } else {
                    def resultList = evaluateContentService.getKeywords(evaluateReport)
                    if (resultList) {
                        return apiResult([status: HttpStatus.SC_OK, list: resultList])
                    } else {
                        return apiResult([status: HttpStatus.SC_OK, msg: "请在渠道管理页面设置需要测评的渠道", list: []])
                    }
                }
            } else {
                EvaluateReport demoReport = evaluateService.getValidEvaluateReportById(0)
                def resultList = evaluateContentService.getKeywords(demoReport)
                return apiResult([status: HttpStatus.SC_OK, list: resultList])
            }
        }
    }

    @RequestMapping(value = "/readAndLike", method = GET)
    Map getAvgReadAndLike(
            @CurrentUser LoginUser currentUser,
            @RequestParam(value = "evaluateId", required = false, defaultValue = "") String evaluateId
    ) {
        if ((Account.USERTYPE_TRIAL).equals(currentUser.userType)) {
            return apiResult([status: HttpStatus.SC_OK, msg: "升级正式版本以查看此内容，详情请联系客服或销售人员", list: []])
        }
        if (evaluateId) {
            EvaluateReport evaluateReport = evaluateService.getEvaluateReportById(evaluateId)
            def resultList = evaluateContentService.getAvgReadAndLike(evaluateReport)
            if (resultList.get("weChat")) {
                return apiResult([status: HttpStatus.SC_OK, msg: "请在渠道管理页面设置需要测评的渠道", list: []])
            } else {
                return apiResult([status: HttpStatus.SC_OK, list: resultList])
            }
        } else {
            EvaluateReport evaluateReport = evaluateService.getValidEvaluateReportById(currentUser.userId)
            if (evaluateReport) {
                if (evaluateReport.status == ReportStatusEnum.failed.getKey()) {
                    return apiResult([status: HttpStatus.SC_OK, msg: "测评创建失败，请检查渠道设置，如有问题请联系客服", list: []])
                } else if (evaluateReport.status == ReportStatusEnum.busy.getKey()) {
                    return apiResult([status: HttpStatus.SC_OK, msg: "测评数据正在计算中...", list: []])
                } else {
                    def resultList = evaluateContentService.getAvgReadAndLike(evaluateReport)
                    if (resultList.get("weChat")) {
                        return apiResult([status: HttpStatus.SC_OK, msg: "请在渠道管理页面设置需要测评的渠道", list: []])
                    } else {
                        if (resultList.get("data")) {
                            return apiResult([status: HttpStatus.SC_OK, list: resultList])
                        } else {
                            return apiResult([status: HttpStatus.SC_OK, msg: "测评数据正在计算中...", list: []])
                        }
                    }
                }
            } else {
                EvaluateReport demoReport = evaluateService.getValidEvaluateReportById(0)
                def resultList = evaluateContentService.getAvgReadAndLike(demoReport)
                return apiResult([status: HttpStatus.SC_OK, list: resultList])
            }
        }
    }
}
