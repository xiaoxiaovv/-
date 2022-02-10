package com.istar.mediabroken.repo.capture

import com.istar.mediabroken.entity.capture.Site
import com.istar.mediabroken.utils.Md5Util
import com.istar.mediabroken.utils.MongoHolder
import com.mongodb.DBCursor
import com.mongodb.QueryBuilder
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import static com.istar.mediabroken.utils.MongoHelper.toObj
import static com.istar.mediabroken.utils.MongoHelper.toObj

/**
 * @author zxj
 * @create 2019/1/2
 */
@Repository
@Slf4j
class SiteDistinctRepo {
    @Autowired
    MongoHolder mongo

    boolean addSiteDistinct(Site site, boolean esHave, boolean subjectHave, boolean trace) {
        def collection = mongo.getCollection("siteDistinct")
        collection.save(
                toObj([
                        _id        : Md5Util.md5(site.siteType + site.websiteName + site.websiteDomain + ""),
                        siteName   : site.websiteName,
                        siteDomain : site.websiteDomain,
                        esHave     : esHave,
                        subjectHave: subjectHave,
                        trace      : trace,
                        siteType   : site.siteType,
                        updateTime : new Date(),
                        createTime : new Date(),
                ]))
        return true
    }


    List getSiteDisList(Boolean esHave, Boolean subjectHave, Boolean trace, int siteType) {
        def list = new ArrayList<>()
        QueryBuilder queryBuilder = QueryBuilder.start()
        if (siteType != null) {
            queryBuilder.put("siteType").is(siteType)
        }
        if (esHave != null) {
            queryBuilder.put("esHave").is(esHave)
        }
        if (subjectHave != null) {
            queryBuilder.put("subjectHave").is(subjectHave)
        }
        if (trace != null) {
            queryBuilder.put("trace").is(trace)
        }

        def collection = mongo.getCollection("siteDistinct")
        def find = collection.find(queryBuilder.get())
        while (find.hasNext()) {
            def site = find.next()
            def _id = site.get("_id")
            def siteName = site.get("siteName")
            def siteDomain = site.get("siteDomain")
            def es = site.get("esHave")
            def subject = site.get("subjectHave")
            def tra = site.get("trace")
            def type = site.get("siteType")
            def updateTime = (Date) site.get("updateTime")
            def createTime = (Date) site.get("createTime")
            list << [
                    _id        : _id,
                    siteName   : siteName,
                    siteDomain : siteDomain,
                    esHave     : es,
                    subjectHave: subject,
                    trace      : tra,
                    siteType   : type,
                    updateTime : updateTime,
                    createTime : createTime
            ]
        }
        return list
    }


    def updateSiteDisListToTrace(boolean trace, List ids,String jobId) {
        def collection = mongo.getCollection("siteDistinct")
        def update = collection.update(toObj([_id: ['$in': ids]]), toObj(['$set': [trace: trace,jobId:jobId]]), true, true)

    }

    def updateSiteDisListToSubjectHave(boolean subjectHave, List ids) {
        def collection = mongo.getCollection("siteDistinct")
        def update = collection.update(toObj([_id: ['$in': ids]]), toObj(['$set': [subjectHave: subjectHave]]),true, true)

    }
}
