package com.istar.mediabroken.api3rd

import com.alibaba.fastjson.JSON
import com.istar.mediabroken.entity.ICompileSummary
import com.istar.mediabroken.entity.News
import groovy.util.logging.Slf4j
import org.apache.http.HttpStatus
import org.springframework.stereotype.Repository

import java.text.SimpleDateFormat

import static com.istar.mediabroken.api3rd.Api3rdUtil.toDate
import static com.istar.mediabroken.api3rd.Api3rdUtil.toType
import static com.istar.mediabroken.utils.StringUtils.stripHtml

/**
 * 话题相关API接口调用
 */
@Repository
@Slf4j
class TopicApi3rd extends BaseApi3rd {

    Map addTopic(YqmsSession session, String topicName, String keywords, String ambiguous, Date startTime, Date endTime) {
        def sdf = new SimpleDateFormat("yyyyMMddHHmmss")
        def rep = doGet("FrontTopic!saveAddTopic.do", session, [
                userstatus: 0,
                kt_name   : topicName,
                kk_name1  : keywords,
                kk_not: ambiguous,
                kt_begin: sdf.format(startTime),
                closetime: sdf.format(endTime),
        ])

        if (rep.result == 0) {
            return [status: HttpStatus.SC_INTERNAL_SERVER_ERROR]
        }

        def topics = getTopicList(session).list
        def topicId = null
        for (int i = 0; i < topics.size(); i++ ) {
            if (topics[i].topicName == topicName) {
                topicId = topics[i].topicId
                break
            }
        }

        def result = [status: HttpStatus.SC_OK, topicId: topicId]
        return result
    }

    Map modifyTopic(YqmsSession session, String topicId, String topicName, String keywords, String ambiguous, Date startTime, Date endTime) {
        def sdf = new SimpleDateFormat("yyyyMMddHHmmss")
        def rep = doGet("FrontTopic!saveEditTopic.do", session, [
                userstatus: 0,
                kt_uuid: topicId,
                kt_name   : topicName,
                kk_name1  : keywords,
                kk_not: ambiguous,
                kt_begin: sdf.format(startTime),
                closetime: sdf.format(endTime),
                kt_status: 1
        ])

        def result = [status: HttpStatus.SC_OK, rep: rep]
        return result
    }

    Map removeTopic(YqmsSession session, String topicId) {
        def rep = doGet("FrontTopic!delTopic.do", session, [
                topicid: topicId
        ])

        def result = [status: HttpStatus.SC_OK]
        return result
    }

    Map getTopicList(YqmsSession session) {
        def rep = doGet("FrontTopic!listTopic.do", session, [:])

        def list = []
        rep.list.mydata.each {
            list << [topicId: it.KT_UUID, topicName: it.KT_NAME]
        }


        def result = [status: HttpStatus.SC_OK, list: list]
        return result
    }

    List<News> getNewsList(ICompileSummary summary, int pageNo, int limit) {
        def sdf = new SimpleDateFormat('yyyyMMddHHmmss')
        def rep = doGet("FrontTopicAllInfo!listAllInfo.do", summary.session, [
                topicid : summary.yqmsTopicId,
                bTime   : sdf.format(summary.startTime),
                eTime   : sdf.format(summary.endTime),
                order   : 1,
                pageNo  : pageNo,
                pageSize: limit
        ])

        def newsList = []
        rep.list.mydata.each {
            newsList << new News(
                    newsId: it.KT_UUID,
                    title: stripHtml(it.KN_TITLE),
                    url: it.KN_URL,
                    createTime: toDate(it.KN_CTIME),
                    contentAbstract: stripHtml(it.KN_ABSTRACT),
                    source: it.KN_SOURCE,
                    site: it.KN_SITE,
                    newsType: toType(it.KN_TYPE),
            )
        }
        return newsList
    }

    // todo 和getNewsList合并一下
    List<News> getDiscussionList(ICompileSummary summary, int pageNo, int limit) {
        def sdf = new SimpleDateFormat('yyyyMMddHHmmss')
        def rep = doGet("FrontTopicAllInfo!listAllInfo.do", summary.session, [
                topicid : summary.yqmsTopicId,
                bTime   : sdf.format(summary.startTime),
                eTime   : sdf.format(summary.endTime),
                order   : 1,
                sourcetype: '02',
                pageNo  : pageNo,
                pageSize: limit
        ])

        def newsList = []
        rep.list.mydata.each {
            newsList << new News(
                    newsId: it.UUID,
                    title: stripHtml(it.KN_TITLE),
                    url: it.KN_URL,
                    createTime: toDate(it.KN_CTIME),
                    contentAbstract: stripHtml(it.KN_ABSTRACT),
                    source: it.KN_SOURCE,
                    site: it.KN_SITE,
                    newsType: toType(it.KN_TYPE)
            )
        }
        return newsList
    }

    Map getSprendTrend(ICompileSummary summary) {
        def sdf = new SimpleDateFormat('yyyyMMddHHmmss')
        def timeSdf = new SimpleDateFormat('yyyyMM')

        def rep = doGet("FrontTopicInfo_fmt!listZST.do", summary.session, [
                topicid : summary.yqmsTopicId,
                bTime   : sdf.format(summary.startTime),
                eTime   : sdf.format(summary.endTime),
        ])

        def list = rep.list
        def result = [:]
        result.list = []
        def zst_time = list.zst_time.split(",")
        def zst_fm = list.zst_fm.split(",")
        def zst_zm = list.zst_zm.split(",")
        def zst_zx = list.zst_zx.split(",")
        def zst_sy = list.zst_sy.split(",")
        for (int i = 0; i < zst_time.size(); i++) {
            result.list << [
                    negativeNews: getIntValue(zst_fm[i]),  // 负面消息数
                    positiveNews: getIntValue(zst_zm[i]),  // 正面消息数
                    neutralNews: getIntValue(zst_zx[i]), // 中性消息数
                    news: getIntValue(zst_sy[i]),              // 全部消息数
                    time: timeSdf.parse(zst_time[i].substring(1, zst_time[i].length() - 1)),
            ]
        }

        result.totalNews = rep.list.allcount
        return result
    }

    int getIntValue(String value) {
        Integer.valueOf(value.substring(1, value.length() - 1))
    }

    List<News> getWeiboList(ICompileSummary summary) {
        def sdf = new SimpleDateFormat('yyyyMMddHHmmss')
        def rep = doGet("FrontTopicMblog!listMblog.do", summary.session, [
                topicid : summary.yqmsTopicId,
                bTime   : sdf.format(summary.startTime),
                eTime   : sdf.format(summary.endTime)
        ])

        def newsList = []
        rep.list.list.each {
            newsList << new News(
                    newsId: it.UUID,
                    title: stripHtml(it.KN_TITLE),
                    url: it.KN_URL,
                    createTime: toDate(it.KN_CTIME),
                    site: it.KN_SITE,
                    source: it.KN_SOURCE,
                    author: it.KN_AUTHOR,
                    newsType: toType(it.KN_TYPE)
            )
        }
        return newsList
    }

    Map getTopN(ICompileSummary summary) {
        def sdf = new SimpleDateFormat('yyyyMMddHHmmss')
        def rep = doGet("FrontTopicInfo_ftr!listFTR.do", summary.session, [
                topicid : summary.yqmsTopicId,
                bTime   : sdf.format(summary.startTime),
                eTime   : sdf.format(summary.endTime),
        ])

        def topAuthors = []
        rep.list.listAuthor.each {
            topAuthors << [
                    author: it.name,
                    publishCount: it.count
            ]
        }
        def topSites = []
        rep.list.listWebname.each {
            topSites << [
                    site: it.name,
                    publishCount: it.count
            ]
        }

        return [topSites: topSites, topAuthors: topAuthors]
    }

    Map getEventEvolution(ICompileSummary summary) {
        def sdf = new SimpleDateFormat('yyyyMMddHHmmss')
        def rep = doGet("FrontTopicInfo_sjz!listSjz.do", summary.session, [
                topicid : summary.yqmsTopicId,
                bTime   : sdf.format(summary.startTime),
                eTime   : sdf.format(summary.endTime),
        ])

        def result = [:]
        result.hotKeywords = []
        rep.list.hotkeys.each {
            it.each { entity ->
                result.hotKeywords << [
                        keyword: entity.key,
                        count: entity.value
                ]
            }
        }

        result.news = []
        rep.list.newList.each {
//            if (it.first == '1') {
                result.news << new News(
                        newsId: it.UUID,
                        title: stripHtml(it.KN_TITLE),
                        url: it.KN_URL,
                        createTime: toDate(it.KN_CTIME),
                        source: it.KN_SOURCE,
                        site: it.KN_SITE,
                        author: it.KN_AUTHOR,
                        newsType: toType(it.KN_TYPE),
                        simhash: it.simhash

                )
//            }

        }

        result.firstPublish = []
        rep.list.list.each {
            if (it.first == '1') {
                result.firstPublish << new News(
                        newsId: it.UUID,
                        title: stripHtml(it.KN_TITLE),
                        url: it.KN_URL,
                        createTime: toDate(it.KN_CTIME),
                        source: it.KN_SOURCE,
                        site: it.KN_SITE,
                        author: it.KN_AUTHOR,
                        newsType: toType(it.KN_TYPE)
                )
            }

        }



        return result
    }

    // todo summary 暂时没有用
    List<News> getWeibo(ICompileSummary summary) {
        def sdf = new SimpleDateFormat('yyyyMMddHHmmss')
        def rep = doGet("FrontTopicMblog!listMblog_bozhu.do", summary.session, [
                topicid : summary.yqmsTopicId,
                bTime   : sdf.format(summary.startTime),
                eTime   : sdf.format(summary.endTime),
        ])
        return []
    }

    def getHotTopic(ICompileSummary summary) {
        def sdf = new SimpleDateFormat('yyyyMMddHHmmss')
        def rep = doGetArray("FrontTopic!viewPointAnalysis.do", summary.session, [
                topicid : summary.yqmsTopicId,
                bTime   : sdf.format(summary.startTime),
                eTime   : sdf.format(summary.endTime),
                count: 10
        ])
        def newsList = []
        rep.each {
            newsList << new News(
                    title: stripHtml(it.view)
            )
        }
        return newsList
    }

    def getWeiboAuthorRelations(ICompileSummary summary) {
        def sdf = new SimpleDateFormat('yyyyMMddHHmmss')
        def rep = doGet("FrontTopicMblog!listMblog_bozhuAuthor.do", summary.session, [
                topicid : summary.yqmsTopicId,
//                topicid : 'd9fc393b8c3641b894091fb52da74ce5',
                bTime   : sdf.format(summary.startTime),
                eTime   : sdf.format(summary.endTime),
                num     : 30
        ])

        def authors = [:]
        def relations = []
        rep.list.each {
            authors.put(it.AUTHOR, 1)
            if (it.AUTHORACTIVE) {
                it.AUTHORACTIVE.split(/\s+/).each { it2 ->
                    authors.put(it2, 2)
                    relations.add([it2, it.AUTHOR, 2])
                }
            }
            if (it.AUTHORPASSIVE) {
                it.AUTHORPASSIVE.split(/\s+/).each { it2 ->
                    authors.put(it2, 3)
                    relations.add([it.AUTHOR, it2, 3])
                }
            }
        }
        return [authors: authors, relations: relations]
    }

    List<News> getFirstPublishMedias(ICompileSummary summary) {
        def sdf = new SimpleDateFormat('yyyyMMddHHmmss')
        def rep = doGet("FrontTopicInfo_sjz!listSjz.do", summary.session, [
                topicid : summary.yqmsTopicId,
                bTime   : sdf.format(summary.startTime),
                eTime   : sdf.format(summary.endTime),
        ])

        def result = []
        rep.list.list.each {
            result << new News(
                    newsId: it.UUID,
                    title: stripHtml(it.KN_TITLE),
                    url: it.KN_URL,
                    createTime: toDate(it.KN_CTIME),
                    source: it.KN_SOURCE,
                    site: it.KN_SITE,
                    author: it.KN_AUTHOR,
                    newsType: toType(it.KN_TYPE)
            )
        }

        return result
    }

    Map getWeiboAnalysis(ICompileSummary summary) {
        def sdf = new SimpleDateFormat('yyyyMMddHHmmss')
        def rep = doGet("FrontTopicMblog!listMblog_bozhu.do", summary.session, [
                topicid: summary.yqmsTopicId,
                bTime  : sdf.format(summary.startTime),
                eTime  : sdf.format(summary.endTime),
        ])

        def result = [distribution: [], bloggerRank: []]
        rep.list.location.each {
            it.each { it2 ->
                result.distribution << [
                        region : it.key,
                        persons: it.value
                ]
            }

        }

        rep.list.bozhu.each {
            it.each { it2 ->
                result.bloggerRank << [
                        name           : it2.key,
                        personsAffected: 0
                ]
            }

        }

        return result
    }

    List getSourceTypeDistribution(ICompileSummary summary) {
        def sdf = new SimpleDateFormat('yyyyMMddHHmmss')
        def rep = doGet("FrontTopicInfo_fbt!listFbt.do", summary.session, [
                topicid: summary.yqmsTopicId,
                bTime  : sdf.format(summary.startTime),
                eTime  : sdf.format(summary.endTime),
        ])
        rep = rep.list
        return [
                [source: '网站', count: rep.qbxwcount],
                [source: '论坛', count: rep.qbltcount],
                [source: '博客', count: rep.qbbkcount],
                [source: '微博', count: rep.qbwbcount],
                [source: '平媒', count: rep.qbpmcount],
                [source: '微信', count: rep.qbwxcount],
                [source: '长微博', count: rep.qblwcount],
                [source: 'APP', count: rep.qbappcount]
        ]
    }
    /**
     * 返回传播的详细信息
     * @param summary
     * @return
     */
    Map getSummaryDistribution(ICompileSummary summary) {
        def sdf = new SimpleDateFormat('yyyyMMddHHmmss')
        def rep = doGet("FrontTopicInfo_fbt!listFbt.do", summary.session, [
                topicid: summary.yqmsTopicId,
                bTime  : sdf.format(summary.startTime),
                eTime  : sdf.format(summary.endTime),
        ])
        rep = rep.list
        def result = [:]
        def infoTotal = 0
        rep.each {
            if(it.key.startsWith("qb")){
                infoTotal += (it.value as int)
                result.put(it.key,it.value)
            }

        }
        result.put("infoTotal",infoTotal)
        return result
    }
}
