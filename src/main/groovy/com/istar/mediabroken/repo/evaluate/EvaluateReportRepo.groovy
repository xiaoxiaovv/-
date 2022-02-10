package com.istar.mediabroken.repo.evaluate

import com.istar.mediabroken.entity.evaluate.EvaluateReport
import com.istar.mediabroken.utils.MongoHolder
import com.mongodb.QueryBuilder
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import java.util.regex.Pattern

import static com.istar.mediabroken.utils.MongoHelper.toObj

/**
 * @author hanhui
 * @date 2018/6/25 10:23
 * @desc
 * */
@Repository
@Slf4j
class EvaluateReportRepo {
    @Autowired
    MongoHolder mongo

    def getEvaluateReportById(long userId, List status, String reportId, List valid) {
        def collection = mongo.getCollection("evaluateReport")
        def find = collection.findOne(toObj([_id: reportId, status: ['$in': status], userId: userId, valid: ['$in': valid]]))
        return find
    }


    def getEvaluateReportById(String reportId) {
        def collection = mongo.getCollection("evaluateReport")
        def find = collection.findOne(toObj([_id: reportId]))
        return find
    }

    def delEvaluateReportById(long userId, String reportId) {
        def collection = mongo.getCollection("evaluateReport")
        def remove = collection.remove(toObj([_id: reportId, userId: userId]))
        return remove.updateOfExisting
    }

    def modifyEvaluateReportByValid(long userId, List status, String reportId, boolean valid) {
        def collection = mongo.getCollection("evaluateReport")
        def update = collection.update(toObj([_id: reportId, userId: userId, status: ['$in': status]]), toObj(['$set': [valid: valid, updateTime: new Date()]]))
        return update.updateOfExisting
    }

    def getEvaluateReportByStatusAndName(long userId, String evaluateName, int status, List valid, int pageSize, int pageNo) {
        def collection = mongo.getCollection("evaluateReport")
        QueryBuilder queryBuilder = QueryBuilder.start()
        queryBuilder.put("userId").is(userId)
        queryBuilder.put("valid").in(valid)
        if (StringUtils.isNoneBlank(evaluateName)) {
            Pattern pattern = Pattern.compile(".*?" + evaluateName + ".*")
            queryBuilder.put("evaluateName").regex(pattern)
        }
        if (status != 0) {
            queryBuilder.put("status").is(status)
        }
        QueryBuilder filed = QueryBuilder.start()
        filed.put("id").is(1)
        filed.put("userId").is(1)
        filed.put("evaluateName").is(1)
        filed.put("status").is(1)
        filed.put("channelsName").is(1)
        filed.put("startTime").is(1)
        filed.put("endTime").is(1)
        filed.put("createTime").is(1)
        filed.put("updateTime").is(1)
        filed.put("valid").is(1)
        def find = collection.find(queryBuilder.get(), filed.get()).sort(toObj([createTime: -1])).skip(pageSize * (pageNo - 1)).limit(pageSize)
        def result = []
        while (find.hasNext()) {
            def evaluateReport = find.next()
            result << new EvaluateReport(evaluateReport)
        }
        find.close()
        return result
    }

    def modifyEvaluateReportStatus(long userId, String evaluateReportId, int endStatus) {
        def collection = mongo.getCollection("evaluateReport")
        def find = collection.update(toObj([_id: evaluateReportId, userId: userId]), toObj(['$set': [status: endStatus, updateTime: new Date()]]))
        return find.updateOfExisting
    }

    def modifyEvaluateReportIndicator(long userId, String evaluateReportId, boolean indicator) {
        def collection = mongo.getCollection("evaluateReport")
        def find = collection.update(toObj([_id: evaluateReportId, userId: userId]), toObj(['$set': [indicator: indicator, updateTime: new Date()]]))
        return find.updateOfExisting
    }

    List getEvaluateReportByStatus(int status) {
        def collection = mongo.getCollection("evaluateReport")
        def find = collection.find(toObj([status: status, valid: true]))
        def result = []
        while (find.hasNext()) {
            def evaluateReport = find.next()
            result << new EvaluateReport(evaluateReport)
        }
        find.close()
        return result
    }

    List getEvaluateReportByUser(long userId, List status) {
        def collection = mongo.getCollection("evaluateReport")
        def find = collection.find(toObj([userId: userId, status: ['$in': status], valid: true]))
        def result = []
        while (find.hasNext()) {
            def evaluateReport = find.next()
            result << new EvaluateReport(evaluateReport)
        }
        find.close()
        return result
    }

    def addEvaluate(EvaluateReport evaluateReport) {
        def collection = mongo.getCollection("evaluateReport")
        def insert = collection.insert(toObj([
                _id         : evaluateReport.id,
                userId      : evaluateReport.userId,
                evaluateName: evaluateReport.evaluateName,
                channelsName: evaluateReport.channelsName,
                isAuto      : evaluateReport.isAuto,
                channels    : evaluateReport.channels,
                status      : evaluateReport.status,
                startTime   : evaluateReport.startTime,
                endTime     : evaluateReport.endTime,
                createTime  : evaluateReport.createTime,
                updateTime  : evaluateReport.updateTime,
                valid       : evaluateReport.valid
        ]))
    }

    def getEvaluateByTime(long userId, boolean isAuto, Date createStr, Date createEnd) {
        def collection = mongo.getCollection("evaluateReport")
        def find = collection.findOne(toObj([userId: userId, isAuto: isAuto, createTime: [$gte: createStr, $lte: createEnd]]))
        return find
    }

    def getEvaluateByName(long userId, boolean valid, String evaluateName) {
        def collection = mongo.getCollection("evaluateReport")
        def find = collection.findOne(toObj([userId: userId, valid: valid, evaluateName: evaluateName]))
        return find
    }


    EvaluateReport getEvaluateReportByUserId(Long userId, List<Integer> status) {
        def collection = mongo.getCollection("evaluateReport")
        def cursor = collection.find(toObj([userId: userId, status: [$in: status], valid: true])).sort(toObj([createTime: -1])).limit(1)
        if (cursor.hasNext()) {
            def it = cursor.next()
            return new EvaluateReport(it)
        } else {
            return null
        }
    }

    EvaluateReport getValidEvaluateReportById(Long userId) {
        def collection = mongo.getCollection("evaluateReport")
        def cursor = collection.find(toObj([userId: userId, valid: true])).sort(toObj([createTime: -1])).limit(1)
        if (cursor.hasNext()) {
            def it = cursor.next()
            return new EvaluateReport(it)
        } else {
            return null
        }
    }


    EvaluateReport getReportByUserIdAndTime(Long userId, Date startTime, Date endTime) {
        def collection = mongo.getCollection("evaluateReport")
        def cursor = collection.find(toObj([userId: userId, status: ["\$in": [2, 3]], startTime: startTime, endTime: endTime]))
                .sort(toObj([createTime: -1])).limit(1)
        if (cursor.hasNext()) {
            def it = cursor.next()
            return new EvaluateReport(it)
        } else {
            return null
        }
    }

    def getUserEvaluateReport() {
        def collection = mongo.getCollection("evaluateReport")
        def aggregate = collection.aggregate(toObj([$match: [status: [$in: [2, 3]], isAuto: false, valid: true]]), toObj([$group: [_id: '\$userId']]))
        return aggregate.results()
    }

    def getUserLastEvaluateReportById(long userId) {
        def collection = mongo.getCollection("evaluateReport")
        def find = collection.find(toObj([userId: userId, status: [$in: [2, 3]], isAuto: false, valid: true])).sort(toObj([createTime: -1])).limit(1)
        if (find.hasNext()) {
            def evaluateReport = find.next()
            return new EvaluateReport(evaluateReport)
        } else {
            return null
        }

    }
}
