package com.istar.mediabroken.repo.compile

import com.istar.mediabroken.entity.compile.Chart
import com.istar.mediabroken.entity.compile.Report
import com.istar.mediabroken.utils.DateUitl
import com.istar.mediabroken.utils.EsHolder
import com.istar.mediabroken.utils.MongoHolder
import com.mongodb.QueryBuilder
import groovy.util.logging.Slf4j
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.index.query.RangeQueryBuilder
import org.elasticsearch.search.sort.SortOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository
import sun.util.calendar.LocalGregorianCalendar

import java.text.SimpleDateFormat

import static com.istar.mediabroken.utils.MongoHelper.toObj


/**
 * Author: zhaochen
 * Time: 2017/8/7
 */
@Repository
@Slf4j
class ReportRepo {
    @Autowired
    MongoHolder mongo

    String addUserReport(Report report) {
        def collection = mongo.getCollection("report")
        collection.insert(toObj(report.toMap()))
        return report.id;
    }

    int getUserReportCount(long userId) {
        Date date = DateUitl.getDayBegin()
        def collection = mongo.getCollection("report")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put('userId').is(userId)
        queryBuilder.put('status').in([1, 2, 3])
        queryBuilder.put("createTime").greaterThanEquals(date)
        def result = collection.count(queryBuilder.get())
        return result
    }

    Report getUserReport(long userId) {
        def collection = mongo.getCollection("report")
        def cursor = collection.find(toObj([userId: userId])).sort(toObj([createTime: -1])).limit(1)
        def result = null
        while (cursor.hasNext()) {
            def report = cursor.next()
            result = new Report(report)
        }
        cursor.close()
        return result
    }

    Report getUserReportById(long userId, String reportId) {
        def collection = mongo.getCollection("report")
        def report = collection.findOne(toObj([userId: userId, _id: reportId]))
        return report
    }

    Report getUserReportRecentlyFinished(long userId) {
        def collection = mongo.getCollection("report")
        def cursor = collection.find(toObj([userId: userId, status: 3])).sort(toObj([createTime: -1])).limit(1)
        def result = null
        while (cursor.hasNext()) {
            def report = cursor.next()
            result = new Report(report)
        }
        cursor.close()
        return result
    }

    Chart getTrendByChartType(String jobId, String chartType) {
        def result = null
        def collection = mongo.getCollection("chart")
        def chart = collection.findOne(toObj([jobId: jobId, chartType: chartType]))
        if (chart) {
            if (chart.createTime) {
                chart.createTime = new Date(chart.createTime)
            }
            if (chart.updateTime) {
                chart.updateTime = new Date(chart.updateTime)
            }
            result = new Chart(chart)
        }
        return result
    }

    List<Report> getReportByStatus(Integer status) {
        def collection = mongo.getCollection("report")
        QueryBuilder key = QueryBuilder.start()
        key.put("status").is(status)
        def cursor = collection.find(key.get())
        def result = []
        while (cursor.hasNext()) {
            def report = cursor.next()
            result << new Report(report)
        }
        cursor.close()
        return result
    }

    void modifyReportStatus(String reportId, Integer status,String description) {
        def collection = mongo.getCollection("report")
        def dataMap = [:]
        dataMap.put("updateTime",new Date())
        if (status){
            dataMap.put("status",status)
        }
        if (description){
            dataMap.put("description",description)
        }
        def result = collection.update(toObj(["_id":reportId]),toObj(["\$set":dataMap]), false, false)
    }
}
