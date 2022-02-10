package com.istar.mediabroken.service.compile

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.serializer.SerializerFeature
import com.istar.mediabroken.entity.compile.Report
import com.istar.mediabroken.repo.compile.ReportRepo
import com.istar.mediabroken.sender.ReportSender
import com.istar.mediabroken.utils.DateUitl
import com.mashape.unirest.http.Unirest
import groovy.util.logging.Slf4j
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

import static com.istar.mediabroken.api.ApiResult.apiResult

/**
 * Author: zhaochen
 * Time: 2017/8/7
 */
@Service
@Slf4j
class ReportService {
    @Autowired
    ReportRepo reportRepo
    @Autowired
    ReportSender reportSender
    @Value('${report.kafka.result.url}')
    String reportResultUrl
    @Value('${report.request.url}')
    String reportRequestUrl
    @Value('${report.status.url}')
    String reportStatusUrl
    @Value('${report.request.count}')
    String maxCount

    Map addUserReport(Long userId, String reportName, String keyWords, int timeRange) {
        //判断用户当天status in[1,2,3]有几个主题
        int count = reportRepo.getUserReportCount(userId)
        if (count >= 5) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "每个用户每天最多创建5个任务"])
        }
        //判断主题名称是否唯一
        Date now = new Date();
        Date startTime = DateUitl.addDay(now, -timeRange)
        def params = [
                "result_url" : reportResultUrl,
                "max_count"  : maxCount,
                "url_dedup"  : true,
                "user_logic" : keyWords.replace(" ", " AND "),
                "start_ctime": startTime.getTime(),
                "end_ctime"  : now.getTime(),
                "weibo_info" : 1
        ]
        log.info("addUserReportParams-userId:{},reportName:{},reportRequestUrl:{},Params:{}", userId, reportName, reportRequestUrl, params)
        def res = Unirest.post(reportRequestUrl).fields(params).asJson()
        log.info("addUserReportResponse-userId:{},reportName:{},result:{}", userId, reportName, res.body.object)
        def result = res.body.object
        String jobId = "";
        String description = ""
        int status = 1;
        if (result) {
            String result_code = result.result_code
            int resultCode = Integer.parseInt(result_code)
            if (resultCode < 0) {
                status = 4
            }
            jobId = result.job_id
            description = result.result_description
        }
        def report = new Report(
                _id: UUID.randomUUID().toString(),
                userId: userId,
                name: reportName,
                keyWords: keyWords,
                startTime: startTime,
                endTime: now,
                timeRange: timeRange,
                status: status,
                jobId: jobId,
                description: description,
                updateTime: now,
                createTime: now
        )
        String reportId = reportRepo.addUserReport(report)
        if (4 == status) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: description])
        } else {
            reportSender.send(JSON.toJSONString(report, SerializerFeature.WriteMapNullValue))
        }
        if (reportId) {
            return apiResult([reportId: reportId, status: HttpStatus.SC_OK, msg: "添加事件成功"])
        } else {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "添加事件失败"])
        }

    }

    Map getUserReport(Long userId) {
        Report report = reportRepo.getUserReport(userId)
        if (!report) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "没有设置事件信息"])
        }
        int count = reportRepo.getUserReportCount(userId)
        return apiResult([status: HttpStatus.SC_OK, msg: report, count: count])
    }

    def mixChartData(int timeRange, def chartData) {
        def chartDataFinal = []
        int chartSize = chartData.size();
        Date begin = DateUitl.addDay(DateUitl.getBeginDayOfParm(new Date()), -timeRange);
        Map dateMap = new HashMap();
        for (int i = 0; i < chartSize; i++) {
            def dataIndex = chartData.get(i);
            dateMap.put(dataIndex.date, dataIndex);
        }
        for (int j = 0; j < timeRange; j++) {
            Date mid = DateUitl.addDay(begin, j)
            def data = dateMap.get(mid.getTime())
            if (data) {
                chartDataFinal << data
            } else {
                chartDataFinal << ["date": mid.getTime(), "website": 0, "socialMedia": 0]
            }
        }
        return chartDataFinal
    }

    Map getTrendByChartType(Long userId, String reportId, String chartType) {
        def report = reportRepo.getUserReportById(userId, reportId)
        String notice = ""
        if (report && (3 != report.status)) {//去拿上一个status=3的chart值
            report = reportRepo.getUserReportRecentlyFinished(userId)
            notice = "当前请求的报表正在生成中，已为您返回上一次结果"
        }
        if (report) {
            def chart = reportRepo.getTrendByChartType(report.jobId, chartType)
            int timeRange = report.timeRange;
            if (chart) {
                if ("spreadTrend".equals(chartType)) {
                    if (chart.chartData) {
                        chart.chartData = mixChartData(timeRange, chart.chartData);
                    }
                }
                if (!"".equals(notice)) {
                    return apiResult([status: HttpStatus.SC_OK, msg: chart, notice: notice])
                } else {
                    return apiResult([status: HttpStatus.SC_OK, msg: chart])
                }
            } else {
                return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "查询无数据"])
            }
        } else {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "查询无数据"])
        }

    }

    Map getProgressRate(Long userId, String reportId) {
        int progress = 0;
        int total = 20000;
        def report = reportRepo.getUserReportById(userId, reportId)
        if (report) {
            if (report.status == 3) {
                progress = 100
            } else {
                Date createTime = report.createTime//创建时间
                Date date = new Date()//当前时间
                int minusTime = date.getTime() - createTime.getTime()//已经度过的时间
                if (minusTime < 18000) {//小于18s
                    progress = 100.0 * (minusTime) / total
                } else {
                    if (report.status != 3 && report.status != 4) {
                        progress = 90
                    }
                }
            }
        }
        return apiResult([status: HttpStatus.SC_OK, msg: report.description, reportStatus: report.status, progressRate: progress])
    }

    void syncReportJobStatus() {
        log.info("syncReportJobStatus start:{}", new Date())
        //处理刚刚新建的job
        def newReports = reportRepo.getReportByStatus(1)
        newReports.each { report ->
            try {
                def res = Unirest.post(reportStatusUrl)
                        .field("job_id", report.jobId)
                        .asJson()
                if (res.getStatus() != 200) {
                    return
                }
                def result = res.body.object
                log.info("report:{}", report)
                log.info("result:{}", result)
                def statusCode = null
                def blockingTaskLength = null
                if ((!result.has("result_code")) || (result.result_code as int) < 0) {
                    return
                }
                if (result.has("job_status")) {
                    def jobStatus = result.job_status
                    if (jobStatus.has("status_code")) {
                        statusCode = jobStatus.status_code
                    }
                    if (jobStatus.has("blocking_task_length")) {
                        blockingTaskLength = jobStatus.blocking_task_length
                    }
                }
                if (statusCode != null) {
                    if ((statusCode as int) == -1) {
                        //更新job到完成数据处理完成状态
                        modifyReportStatus2Complete(report)
                    }
                } else {
                    if (blockingTaskLength != null && (blockingTaskLength as int) == 0) {
                        modifyReportStatus2Complete(report)
                        return
                    }
                }
                if ((new Date().getTime() - report.updateTime.getTime()) > 120000) {
                    modifyReportStatus2Complete(report)
                    log.error("强行完成任务：{}", report)
                    return
                }
            } catch (Exception e) {
                log.error("处理状态信息失败：{}：{}", report, e.getMessage())
            }
        }
        //处理状态是新建的job
        def currDate = new Date()
        def processingReports = reportRepo.getReportByStatus(2)
        processingReports.each { report ->
            if ((currDate.getTime() - report.updateTime.getTime()) > 180000) {
                //更新状态到处理失败
                modifyReportStatus2Error(report)
            }
        }
    }

    private modifyReportStatus2Complete(Report report) {
        reportRepo.modifyReportStatus(report.id, 2, null)
        report.status = 2
        log.info("modifyReportStatus2Complete:{}", report)
        reportSender.send(JSON.toJSONString(report, SerializerFeature.WriteMapNullValue))
        log.info("modifyReportStatus2Complete:reportSender",)
    }

    private modifyReportStatus2Error(Report report) {
        reportRepo.modifyReportStatus(report.id, 4, "查询暂无数据，请稍后再试，或尝试其他关键字")
        report.status = 4
        reportSender.send(JSON.toJSONString(report, SerializerFeature.WriteMapNullValue))
    }
}

