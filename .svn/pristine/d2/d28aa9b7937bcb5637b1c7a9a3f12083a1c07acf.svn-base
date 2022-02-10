package com.istar.mediabroken.api3rd

import com.istar.mediabroken.Const
import com.istar.mediabroken.entity.Focus
import com.istar.mediabroken.entity.News
import com.istar.mediabroken.entity.YqmsSubject
import com.istar.mediabroken.utils.JaccardDistanceUtils
import com.istar.mediabroken.utils.Paging
import com.istar.mediabroken.utils.SolrMsgUtil
import com.istar.mediabroken.utils.SolrPaging
import com.istar.mediabroken.utils.StringUtils
import com.istar.mediabroken.utils.wordseg.WordSegUtil
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.time.DateUtils
import org.apache.http.HttpStatus
import org.springframework.stereotype.Repository

import java.text.SimpleDateFormat

import static com.istar.mediabroken.entity.News.caculateHeat
import static com.istar.mediabroken.utils.StringUtils.stripHtml

@Repository
@Slf4j
class CaptureApi3rd extends BaseApi3rd {

//    KV_SOURCETYPE：信息来源  // "" 全部  01 新闻  02 论坛  03 博客  04 微博 05 平媒  06 微信  07 视频  08 长微博  09 APP  10 评论  99 其他（可选）

    static String SOURCE_TYPE_NEWS = '01'
    static String SOURCE_TYPE_PRINT_MEDIA = '05'
    static String SOURCE_TYPE_WEXIN = '06'


    boolean isErrorTitle(Map news) {
        return (news.title.indexOf('ERROR: Not Found') > -1
                || news.title.indexOf('404 - File or directory not found') > -1
                || news.title.indexOf('Server Error') > -1
                || news.title.indexOf('Error 404') > -1
                || news.title.indexOf('404') > -1
                || news.title.indexOf('Unauthorized') > -1
                || news.title.trim() == '无'
                || news.title.trim() == '广告'
                || news.title.trim() == 'Application Blocked!'
        )
    }

    boolean isErrorTitle(News news) {
        return (news.title.indexOf('ERROR: Not Found') > -1
                || news.title.indexOf('404 - File or directory not found') > -1
                || news.title.indexOf('Server Error') > -1
                || news.title.indexOf('Error 404') > -1
                || news.title.indexOf('404') > -1
                || news.title.indexOf('Unauthorized') > -1
                || news.title.trim() == '无'
                || news.title.trim() == '广告'
                || news.title.trim() == 'Application Blocked!'
        )
    }

    Map getSubjectList(String sid, long userId) {
        def result = [status: HttpStatus.SC_OK, list: []]
        for (int i = 0; i < 2; i++) {
            def rep = doGet("KeyWordSet!listKeyWordSet.do", [
                    session : sid,
                    userid  : userId,
                    KK_TYPE : '01',
                    pageSize: 1000,
                    pageNo  : 1
            ])
            if(rep.size() == 0 ){
                sleep(500)
                continue
            }
            rep.list.mydata.each {
                result.list << [
                        subjectId  : it.ks_id,
                        subjectName: it.KK_NAME,
                        categoryId : it.KK_PID,
                        domain     : it.KK_DINGXIANG_DOMAIN,
                        account    : it.KK_DINGXIANG_ACCOUNT
                ]
            }
            break
        }
        return result
    }

    Paging<Map> getPagingSubjectList(YqmsSession session, int pageNo, int limit) {
        def paging = new Paging<Map>(pageNo, limit, 100000)

        def rep = doGet("KeyWordSet!listKeyWordSet.do", session, [
                KK_TYPE : '01',
                pageSize: limit,
                pageNo  : pageNo
        ])

        if (!rep) return paging

        rep.list.mydata.each {
            paging.list << [
                    subjectId  : it.ks_id,
                    subjectName: it.KK_NAME,
                    categoryId : it.KK_PID,
                    domain     : it.KK_DINGXIANG_DOMAIN,
                    account    : it.KK_DINGXIANG_ACCOUNT
            ]
        }
        return paging
    }

    Map removeSubject(String sid, long userId, String subjectId) {
        def rep = doGet("KeyWordSet!delEditKeyWordSet.do", [
                session: sid,
                userid : userId,
                KK_ID  : subjectId,
                KK_TYPE: '01',
        ])



        return rep
    }

//    Map addWebsiteSubject(String sid, long userId) {
//        def result = doGet("UserClassify!listUserClassify.do", [
//                session: sid,
//                userid: userId
//        ])
//
//        return result
//
//

    Map getNewsList(String sid, long userId, String subjectId, int pageNo, int limit) {
        getNewsList(sid, userId, subjectId, Const.SMT_WEB_SITE, pageNo, limit)
    }

    Map getNewsList(String sid, long userId, String subjectId, int siteType, int pageNo, int limit) {
//        String filter = 'jing'
        String filter = '1'
        if (siteType == Const.SMT_WEBCHAT_PUBLCI_ACCOUNT) {
            filter = '1'
        }
        def yqmsSdf = new SimpleDateFormat("yyyyMMddHHmmss")
        def rep = doGet("YqllIframe!list.do", [
                session     : sid,
                userid      : userId,
                KR_KEYWORDID: subjectId,
                KR_INFOTYPE : '01',
                KR_STATE    : filter,
                pageSize    : limit,
                pageNo      : pageNo
        ])

        def result = [status: HttpStatus.SC_OK, list: []]
        rep.page.list.each {
            def news = [
                    newsId         : it.kvUuid,
                    title          : stripHtml(it.kvTitle),
                    contentAbstract: stripHtml(it.kvAbstract),
                    source         : it.kvSite,
                    time           : yqmsSdf.parse(it.kvCtime).getTime(),
                    imgUrl         : it.KV_IMGURL,
                    heat           : caculateHeat(it.kvHot),
                    reprintCount   : it.kvHot
            ]
            if (isErrorTitle(news)) {
                return
            }

            result.list << news
        }

        return result
    }

    Paging<News> queryNewsList(String sid, long userId, String subjectId, String keywords, int siteType, int pageNo = 1, int limit = 10) {
        String filter = 'jing'
        if (siteType == Const.SMT_WEBCHAT_PUBLCI_ACCOUNT) {
            filter = '1'
        }
        def yqmsSdf = new SimpleDateFormat("yyyyMMddHHmmss")
        def rep = doGet("YqllIframe!list4Solr.do", [
                session                : sid,
                userid                 : userId,
                KR_KEYWORDID           : subjectId,
                KR_INFOTYPE            : '01',
                KR_STATE               : filter,
                message                : keywords,
                searchBy_titleORcontent: 'title',   // title
                pageSize               : limit,
                pageNo                 : pageNo
        ])

        def paging = new Paging(pageNo, limit, rep.page.totalRows)
        rep.page.list.each {
            def news = new News(
                    newsId: it.kvUuid,
                    title: stripHtml(it.kvTitle),
                    source: it.kvSite,
                    createTime: yqmsSdf.parse(it.kvCtime),
                    contentAbstract: stripHtml(it.kvAbstract),
                    reprintCount: 0
//                    heat: caculateHeat(it.kvHot),
//                    reprintCount: it.kvHot
            )
            if (isErrorTitle(news)) {
                return
            }

            paging.list << news
        }

        return paging
    }

    // 精准查询
    Map getNewsList2(String sid, long userId, String subjectId, int pageNo = 1, int limit = 10) {
        def yqmsSdf = new SimpleDateFormat("yyyyMMddHHmmss")
        def rep = doGet("YqllIframe!list.do", [
                session     : sid,
                userid      : userId,
                KR_KEYWORDID: subjectId,
                KR_INFOTYPE : '01',
                KR_STATE    : 'jing',
                pageSize    : limit,
                pageNo      : pageNo
        ])

        def result = [status: HttpStatus.SC_OK, list: []]
        rep.page.list.each {
            def news = [
                    newsId         : it.kvUuid,
                    title          : stripHtml(it.kvTitle),
                    contentAbstract: stripHtml(it.kvAbstract),
                    source         : it.kvSite,
                    time           : yqmsSdf.parse(it.kvCtime).getTime(),
                    heat           : caculateHeat(it.kvHot)
            ]
            if (isErrorTitle(news)) {
                return
            }

            result.list << news
        }

        return result
    }

    Map getNewNewsCount(String sid, long userId, String subjectId, Date updateTime) {
        def sdf = new SimpleDateFormat('yyyyMMddHHmmss')

        def rep = doGet("YqllIframe!checkNewInfoNum.do", [
                session     : sid,
                userid      : userId,
                KR_KEYWORDID: subjectId,
                KR_INFOTYPE : '01',
                KR_STATE    : 'jing',
                btime       : sdf.format(updateTime)
        ])
        return [status: HttpStatus.SC_OK, count: rep.num]
    }

    Map getNews(String sid, long userId, String newsId) {
        def yqmsSdf = new SimpleDateFormat("yyyyMMddHHmmss")
        def rep = doGet("InfoDetail!listInfoDetail.do", [
                session: sid,
                userid : userId,
                KV_UUID: newsId,
//                INFOTYPE: "01",
        ])

        def result
        if (rep) {
            def news = rep.list
            result = [status          : HttpStatus.SC_OK,
                      title           : stripHtml(news.kvTitle),
                      publishTime     : yqmsSdf.parse(news.firstTime),
                      source          : news.kvSite,
                      author          : news.kvAuthor,
                      firstPublishTime: yqmsSdf.parse(news.kvCtime),
                      firstPublishSite: news.firstWeb,
                      reprintCount    : news.kvTransport,
                      keyword         : news.kvKeyword,
                      url             : news.kvUrl,
                      abstract        : stripHtml(news.kvAbstract),
                      content         : stripHtml(rep.listcnt)
            ]
        } else {
            result = null
        }

        return result
    }

    Map getHotNewsToday(String sid, long userId, String yqmsSubjectId) {
        def yqmsSdf = new SimpleDateFormat("yyyyMMddHHmmss")

        def rep = doGet("DayHot!listDayHot.do", [
                session     : sid,
                userid      : userId,
                KR_KEYWORDID: yqmsSubjectId,
                KR_INFOTYPE : '01',
                KR_STATE    : 'jing',
        ])

        def result = [status: HttpStatus.SC_OK, list: []]
        rep.list.each {
            def news = [
                    newsId: it.kvUuid,
                    title : stripHtml(it.kvTitle),
                    source: it.kvSite,
                    time  : yqmsSdf.parse(it.kvCtime).getTime()
            ]
            if (isErrorTitle(news)) {
                return
            }
            result.list << news
        }

        return result
    }

    Map getRelatedNews(String sid, long userId, String newsId) {
        def yqmsSdf = new SimpleDateFormat("yyyyMMddHHmmss")
        def rep = doGet("InfoDetail!listInfoDetail.do", [
                session: sid,
                userid : userId,
                KV_UUID: newsId,
//                INFOTYPE: "01",
        ])

        def newsLists = []
        rep.listxt.each {
            newsLists << [
                    newsId: it.kvUuid,
                    title : stripHtml(it.kvTitle),
                    source: it.kvSite,
                    time  : yqmsSdf.parse(it.kvCtime).getTime()
            ]
        }

        return [status: HttpStatus.SC_OK, list: newsLists]
    }

//    // todo vvv high 删除
//    Map addFocusSubject(String sid, long userId, String websiteDomain, String channelDomain, String focusKeywords) {
//        def sdf = new SimpleDateFormat("yyyyMMddHHmmss")
//        def subjectName = "st" + sdf.format(new Date())
//        def params = [
//                session  : sid,
//                userid   : userId,
//                KK_NAME: subjectName,
//                KK_TYPE: '01',
////                KK_MUST: '西城区 平谷区 门头沟区 北京市 顺义区 密云区 朝阳区 怀柔区 房山区 大兴区 丰台区 石景山区 东城区 昌平区 海淀区 延庆区 通州区',
////                KK_MUST: '北京 上海 天津 重庆 安徽 福建 甘肃 广东 广西 贵州 海南 河北 河南 黑龙江 湖北 湖南 吉林 江苏 江西 辽宁 内蒙古 宁夏 青海 山东 云南 浙江 台湾 香港 澳门',
//                KK_TUISONG: 0,
//                KK_TUISONG_ORIENTATION: '1,2,3',
//                KK_TUISONG_SOURCETYPE: '01,03,05',  // "" 全部 01 新闻  02 论坛  03 博客  04 微博 05 平媒  06 微信  07 视频 08长微博  09 APP  10 评论  99 其他 （可选）
//                KK_DINGXIANG_SOURCETYPE: '01,03,05',
////                KK_DATASOURCETYPE: "1,2,3,4"   // 数据来源类型 默认1综合 2行业 3境外 4属地 多选1,2,3,4（可选）, 没有感觉出有什么作用
//        ]
//
//        if (focusKeywords) {
//            params.KK_SHOULD = focusKeywords
//        }
//        if (websiteDomain) {
//            params.KK_DINGXIANG_DOMAIN = websiteDomain
//        }
//        if (channelDomain) {
//            params.KK_DINGXIANG_CHANNEL = channelDomain
//        }
//        def rep = doGet("KeyWordSet!saveAddKeyWordSet.do", params)
//
//        if (rep.result != 1) {
//            return [status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "创建主题失败"]
//        }
//
//        def subjects = getSubjectList(sid, userId)
//        def subjectId
//        subjects.list.each {
//            if (it.subjectName == subjectName) {
//                subjectId = it.subjectId
//            }
//        }
//        return [status: HttpStatus.SC_OK, subjectId: subjectId, subjectName: subjectName]
//    }

    Map addFocusSubject(YqmsSession session, Focus focus) {
        def sdf = new SimpleDateFormat("yyyyMMddHHmmss")
        def subjectName = "fc" + sdf.format(new Date())
        def params = [
                KK_NAME               : subjectName,
                KK_TYPE               : '01',
                KK_TUISONG            : 0,
                KK_TUISONG_ORIENTATION: '1,2,3',
                KK_MUST               : focus.mustKeywords,
                KK_SHOULD             : focus.shouldKeywords,
                KK_EVENT              : focus.eventKeywords,
                KK_NOT                : focus.notKeywords,
                KK_LABEL              : focus.expression,
                KK_ISHIGH             : focus.isAdvanced() ? 1 : 0
        ]

        def rep = doGet("KeyWordSet!saveAddKeyWordSet.do", session, params)

        if (rep.result != 1) {
            return [status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "创建主题失败"]
        }

        def subjects = getSubjectList(session.sid, session.userId)
        def subjectId = null
        subjects.list.each {
            if (it.subjectName == subjectName) {
                subjectId = it.subjectId
            }
        }
        return [status: HttpStatus.SC_OK, subjectId: subjectId, subjectName: subjectName]
    }

    private void addProvinceKeywords(LinkedHashMap<String, Serializable> params) {
        if (params.KK_MUST) {
            params.KK_MUST = params.KK_MUST + ' 北京 上海 天津 重庆 安徽 福建 甘肃 广东 广西 贵州 海南 河北 河南 黑龙江 湖北 湖南 吉林 江苏 江西 辽宁 内蒙古 宁夏 青海 山东 云南 浙江 台湾 香港 澳门'
        } else {
            params.KK_MUST = '北京 上海 天津 重庆 安徽 福建 甘肃 广东 广西 贵州 海南 河北 河南 黑龙江 湖北 湖南 吉林 江苏 江西 辽宁 内蒙古 宁夏 青海 山东 云南 浙江 台湾 香港 澳门'
        }
    }

    Map modifyFocusSubject(YqmsSession session, Focus focus) {
        log.debug('{}', focus.yqmsSubjectName)
        def params = [
                KK_NAME               : focus.yqmsSubjectName,
                KK_ID                 : focus.yqmsSubjectId,
                KK_TYPE               : '01',
                KK_TUISONG            : 0,
                KK_TUISONG_ORIENTATION: '1,2,3',
                KK_MUST               : focus.mustKeywords,
                KK_SHOULD             : focus.shouldKeywords,
                KK_EVENT              : focus.eventKeywords,
                KK_NOT                : focus.notKeywords,
                KK_LABEL              : focus.expression,
                KK_ISHIGH             : focus.isAdvanced() ? 1 : 0
        ]

        def rep = doGet("KeyWordSet!saveEditKeyWordSet.do", session, params)

        if (rep.result != 1) {
            return [status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "修改主题失败"]
        }

        return [status: HttpStatus.SC_OK]
    }

    Map modifyWebsiteSubject(YqmsSession session, String subjectId, String subjectName, String websiteDomain, String channelDomain, String focusKeywords) {
        def params = [
                KK_ID                  : subjectId,
                KK_NAME                : subjectName,
                KK_TYPE                : '01',
                KK_TUISONG             : 0,
                KK_DATASOURCETYPE      : "1,3,4,8,9,10,11,13,14,15,17,18",
                KK_TUISONG_ORIENTATION : '1,2,3',
                KK_TUISONG_SOURCETYPE  : '01,05',  // "" 全部 01 新闻  02 论坛  03 博客  04 微博 05 平媒  06 微信  07 视频 08长微博  09 APP  10 评论  99 其他 （可选）
                KK_DINGXIANG_SOURCETYPE: "01,02,03,05,06,09,99",
                KK_SHOULD              : focusKeywords,
                KK_DINGXIANG_DOMAIN    : websiteDomain,
                KK_DINGXIANG_CHANNEL   : channelDomain,
        ]

//        addProvinceKeywords(params)
        def rep = doGet("KeyWordSet!saveEditKeyWordSet.do", session, params)

        if (rep.result != 1) {
            return [status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "修改主题失败"]
        }
    }

    Map modifyWechatSubject(YqmsSession session, String subjectId, String subjectName, String account, String focusKeywords) {
        def params = [
                KK_ID                  : subjectId,
                KK_NAME                : subjectName,
                KK_TYPE                : '01',
                KK_TUISONG             : 0,
                KK_TUISONG_ORIENTATION : '1,2,3',
                KK_TUISONG_SOURCETYPE  : "01,02,03,04,05,06,07,08,09,99",  // "" 全部 01 新闻  02 论坛  03 博客  04 微博 05 平媒  06 微信  07 视频 08长微博  09 APP  10 评论  99 其他 （可选）
                KK_DINGXIANG_SOURCETYPE: '06,09',
                KK_TUISONG_STATE       : 'xx',
                KK_SHOULD              : focusKeywords,
                KK_DINGXIANG_DOMAIN    : 'mp.weixin.qq.com',
                KK_DINGXIANG_ACCOUNT   : account
        ]

//        addProvinceKeywords(params)
        def rep = doGet("KeyWordSet!saveEditKeyWordSet.do", session, params)

        if (rep.result != 1) {
            return [status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "修改主题失败"]
        }

        return [status: HttpStatus.SC_OK]
    }

    Map addWebsiteSubject(YqmsSession session, String subjectName, String websiteDomain, String channelDomain, String focusKeywords) {
        def params = [
                KK_NAME                : subjectName,
                KK_TYPE                : '01',
                KK_DATASOURCETYPE      : "1,3,4,8,9,10,11,13,14,15,17,18",
//                KK_MUST: '西城区 平谷区 门头沟区 北京市 顺义区 密云区 朝阳区 怀柔区 房山区 大兴区 丰台区 石景山区 东城区 昌平区 海淀区 延庆区 通州区',
//                KK_MUST: '北京 上海 天津 重庆 安徽 福建 甘肃 广东 广西 贵州 海南 河北 河南 黑龙江 湖北 湖南 吉林 江苏 江西 辽宁 内蒙古 宁夏 青海 山东 云南 浙江 台湾 香港 澳门',
                KK_TUISONG             : 0,
                KK_TUISONG_ORIENTATION : '1,2,3',
                KK_TUISONG_SOURCETYPE  : '01,05',  // "" 全部 01 新闻  02 论坛  03 博客  04 微博 05 平媒  06 微信  07 视频 08长微博  09 APP  10 评论  99 其他 （可选）
                KK_DINGXIANG_SOURCETYPE: "01,02,03,05,06,09,99"
//                KK_DATASOURCETYPE: "1,2,3,4"   // 数据来源类型 默认1综合 2行业 3境外 4属地 多选1,2,3,4（可选）, 没有感觉出有什么作用
        ]

        if (focusKeywords) {
            params.KK_SHOULD = focusKeywords
        }
        if (websiteDomain) {
            params.KK_DINGXIANG_DOMAIN = websiteDomain
        }
        if (channelDomain) {
            params.KK_DINGXIANG_CHANNEL = channelDomain
        }

//        addProvinceKeywords(params)
        def rep = doGet("KeyWordSet!saveAddKeyWordSet.do", session, params)

        if (rep.result != 1) {
            return [status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "创建主题失败"]
        }

        for (def pageNo = 1; true; pageNo++) {
            def paging = getPagingSubjectList(session, pageNo, 50)
            if (!paging.list) break
            for (int i = 0; i < paging.list.size(); i++) {
                def it = paging.list[i]
                if (it.subjectName == subjectName) {
                    def subjectId = it.subjectId
                    return [status: HttpStatus.SC_OK, subjectId: subjectId, subjectName: subjectName]
                }
            }
        }
        return [status: HttpStatus.SC_INTERNAL_SERVER_ERROR]
    }

    Map addWechatSubject(YqmsSession session, String subjectName, String account, String focusKeywords) {
        def params = [
                KK_NAME                : subjectName,
                KK_TYPE                : '01',
//                KK_MUST: '西城区 平谷区 门头沟区 北京市 顺义区 密云区 朝阳区 怀柔区 房山区 大兴区 丰台区 石景山区 东城区 昌平区 海淀区 延庆区 通州区',
//                KK_MUST: '北京 上海 天津 重庆 安徽 福建 甘肃 广东 广西 贵州 海南 河北 河南 黑龙江 湖北 湖南 吉林 江苏 江西 辽宁 内蒙古 宁夏 青海 山东 云南 浙江 台湾 香港 澳门',
                KK_TUISONG             : 0,
                KK_TUISONG_ORIENTATION : '1,2,3',
                KK_TUISONG_SOURCETYPE  : "01,02,03,04,05,06,07,08,09,99",  // "" 全部 01 新闻  02 论坛  03 博客  04 微博 05 平媒  06 微信  07 视频 08长微博  09 APP  10 评论  99 其他 （可选）
                KK_DINGXIANG_SOURCETYPE: '06,09',
                KK_TUISONG_STATE       : 'xx',
//                KK_DATASOURCETYPE: "1,2,3,4"   // 数据来源类型 默认1综合 2行业 3境外 4属地 多选1,2,3,4（可选）, 没有感觉出有什么作用
        ]


        if (focusKeywords) {
            params.KK_TUISONG_SEARCH = focusKeywords
        }

        params.KK_DINGXIANG_DOMAIN = 'mp.weixin.qq.com'
        params.KK_DINGXIANG_ACCOUNT = account

//        addProvinceKeywords(params)
        def rep = doGet("KeyWordSet!saveAddKeyWordSet.do", session, params)

        if (rep.result != 1) {
            return [status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "创建主题失败"]
        }

        for (def pageNo = 1; true; pageNo++) {
            def paging = getPagingSubjectList(session, pageNo, 50)
            if (!paging.list) break
            for (int i = 0; i < paging.list.size(); i++) {
                def it = paging.list[i]
                if (it.subjectName == subjectName) {
                    def subjectId = it.subjectId
                    return [status: HttpStatus.SC_OK, subjectId: subjectId, subjectName: subjectName]
                }
            }
        }
        return [status: HttpStatus.SC_INTERNAL_SERVER_ERROR]
    }

    // 通过solr查询相关新闻
    // 单选 //专题浏览默认显示来源类型 0 全部  01 新闻  02 论坛  03 博客  04 微博 05 平媒  06 微信  07 视频  09 APP  10评论  99 其他
    Paging<TortNews> getPagingCopyrightMonitorNews(String keywords, int pageNo, int limit) {
        // todo 把地址放到config.properties
        keywords = keywords.split(/\s+/).join(',')
        def paging = new Paging<TortNews>(pageNo, limit, 0)
        def result = doGetSolr([
                q    : "kvTitle: \"${keywords}\" AND (kvSourcetype: 1 OR kvSourcetype: 6)" as String,
                start: paging.offset,
                rows : limit,
                fl   : 'kvTitle,kvSource,kvAuthor,kvUrl,kvUuid,kvContent,kvAbstract,kvSite,kvCtime,kvSourcetype,score,kvSourcetype',
                sort : 'kvDkTime desc'
        ])

        paging.total = result.response?.numFound ?: 0
        def docs = result.response?.docs
        docs = (docs != null) ? docs : []
        def yqmsSdf = new SimpleDateFormat("yyyyMMddHHmmss")
        docs.each {
            paging.list << new TortNews(
                    title: stripHtml(it.kvTitle),
                    source: it.kvSource,
                    author: it.kvAuthor,
                    url: it.kvUrl,
                    newsId: it.kvUuid,
//                content     : stripHtml(it.kvContent),
                    contentAbstract: stripHtml(it.kvAbstract),
                    site: it.kvSite,
                    createTime: yqmsSdf.parse(it.kvCtime),
                    newsType: it.kvSourcetype as int
            )
        }
        return paging
    }

    int getReprintCount(String keywords) {
        keywords = keywords.split(/\s+/).join('" AND kvTitle: "')
        def result = doGetSolr([
                q    : "kvTitle: \"${keywords}\" AND (kvSourcetype: 1 OR kvSourcetype: 6)" as String,
                start: 0,
                rows : 1,
                fl   : 'kvUuid',
                sort : 'kvDkTime desc'
        ])
        if (result&&result.reponse){
            return result.response.numFound
        }else{
            return 0
        }
    }

    News getNewsByUrl(String url) {
        def result = doGetSolr([
            q    : "kvUrl: \"${url}\"" as String,
            start: 0,
            rows : 1,
            fl   : 'kvTitle,kvSource,kvAuthor,kvUrl,kvUuid,kvContent,kvAbstract,kvSite,kvCtime,kvSourcetype,score,kvSourcetype',
            sort : 'kvDkTime desc'
        ])

        def docs = result.response?.docs
        docs = (docs != null) ? docs : []
        def yqmsSdf = new SimpleDateFormat("yyyyMMddHHmmss")
        def obj=docs?docs[0]:[]
        News news = null
        if (obj){
            news = new TortNews(
                    title: stripHtml(obj.kvTitle),
                    source: obj.kvSource,
                    author: obj.kvAuthor,
                    url: obj.kvUrl,
                    newsId: obj.kvUuid,
                    contentAbstract: stripHtml(obj.kvAbstract),
                    site: obj.kvSite,
                    createTime: yqmsSdf.parse(obj.kvCtime),
                    newsType: obj.kvSourcetype as int
            )
        }
        return news
    }

    Map getNewsListByTime(String sid, long userId, String subjectId, int pageNo = 1, int limit = 10, String btime, String etime) {
        def yqmsSdf = new SimpleDateFormat("yyyyMMddHHmmss")
        def rep = doGet("YqllIframe!list.do", [
                session     : sid,
                userid      : userId,
                KR_KEYWORDID: subjectId,
                KR_INFOTYPE : '01',
                KR_STATE    : 'jing',
                pageSize    : limit,
                pageNo      : pageNo,
                btime       : btime,
                etime       : etime
        ])

        def result = [status: HttpStatus.SC_OK, list: []]
        rep.page.list.each {
            def news = [
                    newsId      : it.kvUuid,
                    title       : stripHtml(it.kvTitle),
                    source      : it.kvSite,
                    time        : yqmsSdf.parse(it.kvCtime).getTime(),
                    heat        : caculateHeat(it.kvHot),
                    reprintCount: it.kvHot
            ]
            if (isErrorTitle(news)) {
                return
            }

            result.list << news
        }

        return result
    }

    // 通过solr查询相关新闻按时间
    // 单选 //专题浏览默认显示来源类型 0 全部  01 新闻  02 论坛  03 博客  04 微博 05 平媒  06 微信  07 视频  09 APP  10评论  99 其他
    Paging<TortNews> getPagingCopyrightMonitorNewsByTime(String keywords, int pageNo, int limit, String start, String end) {
        keywords = keywords.split(/\s+/).join(',')
        def paging = new Paging<TortNews>(pageNo, limit, 100000)
/*        def result = doGetSolr([
                q    : "kvTitle: \"${keywords}\" AND (kvSourcetype: 1 OR kvSourcetype: 6 OR kvSourcetype: 4 OR kvSourcetype: 401 OR kvSourcetype: 402 OR kvSourcetype: 8) AND kvDkTime:[ \"${start}\" TO \"${end}\" ]" as String,
                start: paging.getOffset(),
                rows : limit,
                fl   : 'kvDkTime,kvTitle,kvSource,kvAuthor,kvUrl,kvUuid,kvAbstract,kvSite,kvCtime,kvSourcetype,score',
                sort : 'kvDkTime asc'
        ])*/

        Map params = new HashMap()
        params.put("q", "kvTitle: \"${keywords}\" AND (kvSourcetype: 1 OR kvSourcetype: 6 OR kvSourcetype: 4 OR kvSourcetype: 401 OR kvSourcetype: 402 OR kvSourcetype: 8) AND kvDkTime:[ \"${start}\" TO \"${end}\" ]" as String)
        params.put("start", paging.getOffset())
        params.put("rows", limit?:10)
        params.put("fl", "kvDkTime,kvTitle,kvSource,kvAuthor,kvUrl,kvUuid,kvAbstract,kvSite,kvCtime,kvSourcetype,score")
        params.put("sort", "kvDkTime asc")

        def result = SolrMsgUtil.doPost(params)
        if (!result){
            paging.list = []
            return paging
        }
        com.alibaba.fastjson.JSON jsonResult = com.alibaba.fastjson.JSONObject.parse(result)
        def docs = jsonResult.response?.docs
        def numFound = (jsonResult.response?.numFound) as Integer
        docs = (docs != null) ? docs : []
        def yqmsSdf = new SimpleDateFormat("yyyyMMddHHmmss")
        for (int i = 0; i < limit && (i + paging.offset) < numFound;) {
            def it = docs[i]
            paging.list << new TortNews(
                    title: stripHtml(it.kvTitle),
                    source: it.kvSource,
                    author: it.kvAuthor,
                    url: it.kvUrl,
                    newsId: it.kvUuid,
                    contentAbstract: stripHtml(it.kvAbstract),
                    site: it.kvSite,
                    createTime: yqmsSdf.parse(it.kvCtime),
                    newsType: it.kvSourcetype as int,
                    dkTime: it.kvDkTime
            )
            i++
        }
        return paging
    }

    Map queryPagingNew(String q, int start, int rows, String fl, String sort) {
        // todo 把地址放到config.properties
        def result = doGetSolr([
                q    : q,
                start: start,
                rows : rows,
                fl   : fl,
                sort : sort
        ])


        def docs = result.response?.docs
        def total = result.response?.numFound ?: 0
        def rep = [total: total, list: []]
        docs = (docs != null) ? docs : []
        docs.each {
            def news = it

            if (it.kvTitle != null)
                news.kvTitle = stripHtml(it.kvTitle ?: "")
            if (it.kvAbstract != null)
                news.kvAbstract = stripHtml(it.kvAbstract ?: "")
            if (it.kvContent != null)
                news.kvContent = stripHtml(it.kvContent ?: "")
            rep.list << news
        }
        return rep
    }

    Paging<News> getNewsListBySite(String field,String site, Date start, Date end, int  PageNo, int limit) {
        def sdf = new SimpleDateFormat("yyyyMMddHHmmss")
        def paging = new Paging<TortNews>(PageNo, limit)
        def result = doGetSolr([
                q    : "kvSite: \"${site}*\"  AND kvDkTime:[ \"${sdf.format(start)}\" TO \"${sdf.format(end)}\" ]" as String,
                start: paging.getOffset(),
                rows: limit,
                fl: 'kvDkTime,kvTitle,kvSource,kvAuthor,kvUrl,kvUuid,kvContent,kvAbstract,kvSite,kvCtime,kvSourcetype,kvOrientation',
                sort: 'kvUuid asc,kvDkTime desc',
                rows : limit,
                fl   : 'kvDkTime,kvTitle,kvSource,kvAuthor,kvUrl,kvUuid,kvContent,kvAbstract,kvSite,kvCtime,kvSourcetype,kvOrientation',
                sort : 'kvDkTime asc'
        ])
        def docs = result.response?.docs
        def numFound = 0
        if ((result.response?.numFound) != null)
            numFound = (result.response?.numFound) as Integer
        paging.total = numFound
        docs = (docs != null) ? docs : []
        def yqmsSdf = new SimpleDateFormat("yyyyMMddHHmmss")
        docs.each {
            def title = stripHtml(it.kvTitle)
            def content = stripHtml(it.kvContent)
            paging.list << new News(
                    title: title,
                    source: it.kvSource,
                    author: it.kvAuthor,
                    url: it.kvUrl,
                    newsId: it.kvUuid,
                    contentAbstract: stripHtml(it.kvAbstract),
                    site: it.kvSite,
                    createTime: yqmsSdf.parse(it.kvCtime),
                    newsType: it.kvSourcetype as int,
                    orientation: it.kvOrientation,
                    keywords: WordSegUtil.extractKeywords(title, content, 5)
            )
        }
        return paging
    }

    Paging<News> getNewsListByTitle(String title, Date start, Date end, int pageNo, int limit) {
        def paging = new Paging<TortNews>(pageNo, limit)
        def sdf = new SimpleDateFormat("yyyyMMddHHmmss")
        def result = doGetSolr([
                q    : "kvTitle: \"${title}\" AND kvDkTime:[ \"${sdf.format(start)}\" TO \"${sdf.format(end)}\" ]" as String,
                start: paging.getOffset(),
                rows : limit,
                fl   : 'kvDkTime,kvTitle,kvSource,kvAuthor,kvUrl,kvUuid,kvContent,kvAbstract,kvSite,kvCtime,kvSourcetype,kvOrientation',
                sort : 'kvDkTime asc'
        ])

        if (!result) {
            return paging
        }

        def docs = result.response?.docs
        def numFound = (result.response?.numFound) as Integer
        paging.total = numFound
        docs = (docs != null) ? docs : []
        def yqmsSdf = new SimpleDateFormat("yyyyMMddHHmmss")
        docs.each {
            def title2 = stripHtml(it.kvTitle)
            def content = stripHtml(it.kvContent)
            paging.list << new News(
                    title: title2,
                    source: it.kvSource,
                    author: it.kvAuthor,
                    url: it.kvUrl,
                    newsId: it.kvUuid,
                    contentAbstract: stripHtml(it.kvAbstract),
                    site: it.kvSite,
                    createTime: yqmsSdf.parse(it.kvCtime),
                    newsType: it.kvSourcetype as int,
                    orientation: it.kvOrientation,
                    keywords: WordSegUtil.extractKeywords(title, content, 5)
            )
        }
        return paging
    }



    SolrPaging<News> getNewsListBySite(String field,String site, Date start, Date end, String  CurrentCursor, int limit) {
        def sdf = new SimpleDateFormat("yyyyMMddHHmmss")
        def paging = new SolrPaging<TortNews>(0, limit,CurrentCursor)
        def result = doGetSolr([
                q: "${field}: \"${site}*\"  AND kvDkTime:[ \"${sdf.format(start)}\" TO \"${sdf.format(end)}\" ]" as String,
                cursorMark:CurrentCursor,
                rows: limit,
                fl: 'kvDkTime,kvTitle,kvSource,kvAuthor,kvUrl,kvUuid,kvContent,kvAbstract,kvSite,kvCtime,kvSourcetype,kvOrientation',
                sort: 'kvUuid asc,kvDkTime desc'
        ])
        def docs = result.response?.docs
        docs = (docs != null) ? docs : []
        def cursor = result.nextCursorMark as String
        paging.CurrentCursor = cursor

        def yqmsSdf = new SimpleDateFormat("yyyyMMddHHmmss")
        docs.each {
            def title = stripHtml( it.kvTitle as String )
            def content = stripHtml( it.kvContent as String )
            if( it.kvSourcetype==1 || it.kvSourcetype==5 || it.kvSourcetype==4 || it.kvSourcetype==6 || it.kvSourcetype==9){
                paging.list << new News(
                        title       : title,
                        source      : it.kvSource,
                        author      : it.kvAuthor,
                        url         : it.kvUrl,
                        newsId      : it.kvUuid,
                        contentAbstract : stripHtml( it.kvAbstract as String ),
                        site        : it.kvSite,
                        createTime  : yqmsSdf.parse(it.kvCtime as String),
                        newsType: it.kvSourcetype as int,
                        orientation: it.kvOrientation,
                        keywords: WordSegUtil.extractKeywords(title, content, 5)
                )
            }
        }
        return paging
    }

    SolrPaging<News> getNewsListByTitle(String title, Date start, Date end, String  CurrentCursor, int limit) {
        def paging = new SolrPaging<TortNews>( 0, limit ,CurrentCursor )
        def sdf = new SimpleDateFormat("yyyyMMddHHmmss")
        def result = doGetSolr([
                q: "kvTitle: \"${title}\" AND kvDkTime:[ \"${sdf.format(start)}\" TO \"${sdf.format(end)}\" ]" as String,
                cursorMark:CurrentCursor,
                rows: limit,
                fl: 'kvDkTime,kvTitle,kvSource,kvAuthor,kvUrl,kvUuid,kvContent,kvAbstract,kvSite,kvCtime,kvSourcetype,kvOrientation',
                sort: 'kvUuid asc,kvDkTime desc'
        ])

        if (!result) {
            return paging
        }

        def docs = result.response?.docs
        docs = (docs != null) ? docs : []

        def cursor = result.nextCursorMark as String
        paging.CurrentCursor = cursor

        def yqmsSdf = new SimpleDateFormat("yyyyMMddHHmmss")
        docs.each {
            def title2 = stripHtml( it.kvTitle as String )
            def content = stripHtml( it.kvContent as String )
            paging.list << new News(
                    title       : title2,
                    source      : it.kvSource,
                    author      : it.kvAuthor,
                    url         : it.kvUrl,
                    newsId      : it.kvUuid,
                    contentAbstract : stripHtml(it.kvAbstract as String),
                    site        : it.kvSite,
                    createTime  : yqmsSdf.parse(it.kvCtime as String ),
                    newsType: it.kvSourcetype as int,
                    orientation: it.kvOrientation,
                    keywords: WordSegUtil.extractKeywords(title, content, 5)
            )
        }
        return paging
    }


    void removeFocusSubject(YqmsSession session, Focus focus) {
        removeSubject(session.sid, session.userId, focus.yqmsSubjectId)
    }

    Paging getPagingFocusNewsList(YqmsSession session, Focus focus, int pageNo, int limit) {
        def yqmsSdf = new SimpleDateFormat("yyyyMMddHHmmss")
        def rep = doGet("YqllIframe!list.do", session, [
                KR_KEYWORDID: focus.yqmsSubjectId,
                KR_INFOTYPE : '01',
                KR_STATE    : 'jing',
                pageSize    : limit,
                pageNo      : pageNo
        ])

        def paging = new Paging<News>(pageNo, limit, 3000)
        rep.page.list.each {
            def news = [
                    newsId         : it.kvUuid,
                    title          : stripHtml(it.kvTitle),
                    contentAbstract: stripHtml(it.kvAbstract),
                    source         : it.kvSite,
                    time           : yqmsSdf.parse(it.kvCtime).getTime(),
                    imgUrl         : it.KV_IMGURL,
                    heat           : caculateHeat(it.kvHot),
                    reprintCount   : it.kvHot
            ]
            if (isErrorTitle(news)) {
                return
            }

            paging.list << news
        }

        return paging
    }

    Paging<News> queryPagingFocusNewsList(YqmsSession session, Focus focus, String keywords, int pageNo, int limit) {
        def yqmsSdf = new SimpleDateFormat("yyyyMMddHHmmss")
        def rep = doGet("YqllIframe!list4Solr.do", session, [
                KR_KEYWORDID           : focus.yqmsSubjectId,
                KR_INFOTYPE            : '01',
                KR_STATE               : 'jing',
                message                : keywords,
                searchBy_titleORcontent: 'title',   // title
                pageSize               : limit,
                pageNo                 : pageNo
        ])

        def paging = new Paging(pageNo, limit, rep.page.totalRows)
        rep.page.list.each {
            def news = new News(
                    newsId: it.kvUuid,
                    title: stripHtml(it.kvTitle),
                    source: it.kvSite,
                    createTime: yqmsSdf.parse(it.kvCtime),
                    contentAbstract: stripHtml(it.kvAbstract),
                    reprintCount: 0
            )
            if (isErrorTitle(news)) {
                return
            }

            paging.list << news
        }

        return paging
    }

    Paging<News> getNewsList(YqmsSession session, String yqmsSubjectId, Map params, int pageNo, int limit) {
        def yqmsSdf = new SimpleDateFormat("yyyyMMddHHmmss")

        def request = [
                KR_KEYWORDID: yqmsSubjectId,
                KR_INFOTYPE : '01',
                KR_STATE    : 'jing',
                pageSize    : limit,
                pageNo      : pageNo
        ]
        if (params) {
            request.putAll(params)
        }
        def rep = doGet("YqllIframe!list.do", session, request)

        def result = new Paging<News>(pageNo, limit);
        rep.page.list.each {
            def news = new News(
                    newsId: it.kvUuid,
                    title: stripHtml(it.kvTitle),
                    contentAbstract: stripHtml(it.kvAbstract),
                    source: it.kvSite,
                    createTime: yqmsSdf.parse(it.kvCtime),
                    imgUrl: it.KV_IMGURL,
                    reprintCount: it.kvHot
            )
            if (isErrorTitle(news)) {
                return
            }
            result.list << news
        }

        return result
    }

    private Paging<News> getNewsListByParams(YqmsSession session, String yqmsSubjectId, Map params, int pageNo, int limit) {
        def yqmsSdf = new SimpleDateFormat("yyyyMMddHHmmss")

        def request = [
                KR_KEYWORDID: yqmsSubjectId,
                KR_INFOTYPE : '01',
//                KR_STATE: 'jing',
                pageSize    : limit,
                pageNo      : pageNo
        ]

        if (params.sourceType) {
            request.KV_SOURCETYPE = params.sourceType.join(',')
        }

        def rep = doGet("YqllIframe!list.do", session, request)
        def result = new Paging<News>(pageNo, limit, 3000);
        rep.page.list.each {
            def news = new News(
                    newsId: it.kvUuid,
                    title: stripHtml(it.kvTitle),
                    contentAbstract: stripHtml(it.kvAbstract),
                    source: it.kvSite,
                    createTime: yqmsSdf.parse(it.kvCtime),
                    imgUrl: it.KV_IMGURL,
                    reprintCount: it.kvHot,
                    newsType: it.kvSourcetype as int
            )
            if (isErrorTitle(news)) {
                return
            }
            result.list << news
        }
        log.debug('{}', result.list)

        return result
    }

    Paging<News> queryNewsListByParams(YqmsSession session, String yqmsSubjectId, String keywords, Map params, int pageNo, int limit) {
        def yqmsSdf = new SimpleDateFormat("yyyyMMddHHmmss")

        log.debug('params: {}', params)

        if (!keywords) {
            return getNewsListByParams(session, yqmsSubjectId, params, pageNo, limit)
        }

        def request = [
                KR_KEYWORDID           : yqmsSubjectId,
                KR_INFOTYPE            : '01',
//                KR_STATE: 'jing',
                message                : keywords,
                searchBy_titleORcontent: 'title',
                pageSize               : limit,
                pageNo                 : pageNo
        ]

        if (params.sourceType) {
            request.KV_SOURCETYPE = params.sourceType.join(',')
        }

        def rep = doGet("YqllIframe!list4Solr.do", session, request)

        def paging = new Paging(pageNo, limit, rep.page.totalRows)
        rep.page.list.each {
            def news = new News(
                    newsId: it.kvUuid,
                    title: stripHtml(it.kvTitle),
                    source: it.kvSite,
                    createTime: yqmsSdf.parse(it.kvCtime),
                    contentAbstract: stripHtml(it.kvAbstract),
                    reprintCount: 0,
                    newsType: it.kvSourcetype as int
            )
            if (isErrorTitle(news)) {
                return
            }

            paging.list << news
        }

        log.debug('{}', paging.list)
        return paging
    }

    // todo vvv high 需要从solr中拿数据
    Paging<News> getLatestNewsList(YqmsSession session, String yqmsSubjectId, int pageNo, int limit) {
        def params = [KV_SOURCETYPE: '01,05,06']
        return getNewsList(session, yqmsSubjectId, params, pageNo, limit)
    }

    List<News> getFocusNewsList(YqmsSession session, String yqmsSubjectId, int limit) {
        def params = [KV_SOURCETYPE: '01,05']
        return getTopNewsList(session, yqmsSubjectId, params, limit)
    }

    List<News> getHotWeixinList(YqmsSession session, String yqmsSubjectId, int limit) {
        def params = [KV_SOURCETYPE: '06']
        return getTopNewsList(session, yqmsSubjectId, params, limit)
    }

    List<News> getTopNewsList(YqmsSession session, String yqmsSubjectId, Map params, int limit) {
        def yqmsSdf = new SimpleDateFormat("yyyyMMddHHmmss")


        def endTime = new Date()
        def startTime = DateUtils.addDays(new Date(), -1)

        def request = [
                KR_KEYWORDID: yqmsSubjectId,
                KR_INFOTYPE : '01',
                KR_STATE    : 'jing',
                btime       : yqmsSdf.format(startTime),
                etime       : yqmsSdf.format(endTime),

        ]
        if (params) {
            request.putAll(params)
        }
        def rep = doGet("DayHot!listDayHot.do", session, request)

        def list = []
        int i = 0;
        rep.list.each {
            def news = new News(
                    newsId: it.kvUuid,
                    title: stripHtml(it.kvTitle),
                    source: it.kvSite,
                    createTime: yqmsSdf.parse(it.kvCtime),
                    reprintCount: Integer.valueOf(it.count)
            )
            if (isErrorTitle(news)) {
                return
            }
            if (i++ < limit) {
                list << news
            }
        }

        return list
    }

    // todo vv high 把添加站点方法合并成一个
    YqmsSubject addAllSiteSubject(YqmsSession session, String domains, String wecharAccounts) {
        def sdf = new SimpleDateFormat("yyyyMMddHHmmss")
        def subjectName = "as" + sdf.format(new Date())

        if (wecharAccounts) {
            if (domains.indexOf('mp.weixin.qq.com') < 0) {
                domains = 'mp.weixin.qq.com,' + domains
            }
        }

        def params = [
                KK_NAME                : subjectName,
                KK_TYPE                : '01',
                KK_TUISONG             : 0,
                KK_TUISONG_ORIENTATION : '1,2,3',
                KK_TUISONG_SOURCETYPE  : '01,05,06',  // "" 全部 01 新闻  02 论坛  03 博客  04 微博 05 平媒  06 微信  07 视频 08长微博  09 APP  10 评论  99 其他 （可选）
                KK_DINGXIANG_SOURCETYPE: '01,05,06',
                KK_DINGXIANG_DOMAIN    : domains,
                KK_DINGXIANG_ACCOUNT   : wecharAccounts
        ]

//        addProvinceKeywords(params)
        def rep = doGet("KeyWordSet!saveAddKeyWordSet.do", session, params)

        if (rep.result != 1) {
            return null
        }

        def subjects = getSubjectList(session.sid, session.userId)
        def subjectId = null
        subjects.list.each {
            if (it.subjectName == subjectName) {
                subjectId = it.subjectId
            }
        }
        return new YqmsSubject(yqmsSubjectId: subjectId, yqmsSubjectName: subjectName)
    }


    long getNewsCount(YqmsSession session, long userId, String yqmsSubjectId, String startDate, String endDate) {

        def request = [
                userid      : userId,
                KR_KEYWORDID: yqmsSubjectId,
                KR_INFOTYPE : '01',
//                KR_STATE: 'jing',
                btime       : startDate,
                etime       : endDate,
        ]

        def rep = doGet("YqllIframe!getTotalRows.do", session, request)

        return rep.totalrows
    }

    long getNewsCount2(String sid, long userId, String yqmsSubjectId, String startDate, String endDate) {

        def request = [
                session     : sid,
                userid      : userId,
                KR_KEYWORDID: yqmsSubjectId,
                KR_INFOTYPE : '01',
//                KR_STATE: 'jing',
                btime       : startDate,
                etime       : endDate,
        ]

        def rep = doGet("YqllIframe!getTotalRows.do", request)

        return rep.totalrows
    }

    Paging<News> getNewsListByKeywords(List<String> keyWords, List<Integer> siteTypes, List siteNames, List wechatAccounts, Date startTime, int pageNo, int limit,
                                       Integer heat, Integer time, String startTime4Solr, String endTime4Solr, Integer  orientation, Integer order) {
        def siteNamesCause = []
        siteNames.each {
            siteNamesCause << 'kvSite: "' + it + '*"'
        }

        def wechatAccountsCause = []
        log.debug('{}', wechatAccounts)
        wechatAccounts.each {
            wechatAccountsCause << 'kvSource: "' + it + '*"'
        }

        def keywordsCause = []
        keyWords.each {
            keywordsCause << 'kvTitle: "' + it + '"'
        }

        def siteCause = []
        if (siteNamesCause) {
            siteCause << '((' + siteNamesCause.join(' OR ') + ') AND (kvSourcetype: 1 OR kvSourcetype: 5))'
        }
        if (wechatAccountsCause) {
            siteCause << '((' + wechatAccountsCause.join(' OR ') + ') AND kvSourcetype: 6)'
        }
        def cause = []
        cause << '(' + siteCause.join(' OR ') + ' )'
        if (keywordsCause){
            cause << '(' + keywordsCause.join(' AND ') + ')'
        }

        if(time != 0){
            cause << 'kvCtime:[ "' + startTime4Solr + '" TO "' + endTime4Solr + '" ]'
        }

        if(orientation == 1 || orientation == 2 || orientation == 3 ){
            cause << 'kvOrientation: "' + orientation + '"'
        }

        def paging = new Paging<TortNews>(pageNo, limit)
        def queryMap = [:]
        queryMap.q = cause.join(' AND ')
        queryMap.start = paging.getOffset()
        queryMap.rows = limit
        queryMap.fl = 'kvDkTime,kvCtime,kvTitle,kvSource,kvAuthor,kvUrl,kvUuid,kvContent,kvAbstract,kvSite,kvCtime,kvSourcetype,score,kvSourcetype,kvOrientation'
        if(order  == 1){
            queryMap.sort = 'kvDkTime desc'
        }

        def result = doGetSolr(queryMap)
        if(result.size() == 0){
            paging.total = 0
            return paging
        }
        def numFound = (result.response?.numFound) as Integer
        paging.total = numFound
        def docs = result.response?.docs
        docs = (docs != null) ? docs : []
        def yqmsSdf = new SimpleDateFormat("yyyyMMddHHmmss")
        docs.each {
            def title2 = stripHtml(it.kvTitle)
            paging.list << new News(
                    title: title2,
                    source: it.kvSource,
                    author: it.kvAuthor,
                    url: it.kvUrl,
                    newsId: 'solr:' + it.kvUuid,
                    contentAbstract: StringUtils.solrHtml2text(it.kvAbstract),
                    site: it.kvSite,
                    createTime: yqmsSdf.parse(it.kvDkTime),
                    newsType: it.kvSourcetype as int,
                    orientation: it.kvOrientation,
            )
        }
        return paging
    }

    News getNewsBySolr(String newsId) {
        def yqmsSdf = new SimpleDateFormat("yyyyMMddHHmmss")
        def result = doGetSolr([
                q : "kvUuid:${newsId}",
                fl: 'kvDkTime,kvTitle,kvSource,kvAuthor,kvUrl,kvUuid,kvContent,kvAbstract,kvSite,kvCtime,kvSourcetype,score,kvSourcetype,kvOrientation',
        ])
        if (result) {
            def it = result.response?.docs[0]
            def title = stripHtml(it.kvTitle)
            def content = StringUtils.solrText2Html(it.kvContent)
            return new News(
                    title: title,
                    source: it.kvSource,
                    author: it.kvAuthor,
                    url: it.kvUrl,
                    newsId: 'solr:' + it.kvUuid,
                    contentAbstract: StringUtils.solrHtml2text(it.kvAbstract),
                    site: it.kvSite,
                    createTime: yqmsSdf.parse(it.kvCtime),
                    newsType: it.kvSourcetype as int,
                    orientation: it.kvOrientation,
                    content: content,
            )
        } else {
            return null
        }
    }
    /**
     * solr相关新闻信息
     * @param newsId
     * @param title
     * @param pageNo
     * @param limit
     * @return
     */
    Paging<News> getRelatedNewsBySolr(String newsId, String title, int pageNo, int limit) {
        def paging = new Paging<TortNews>(pageNo, limit)
        def sdf = new SimpleDateFormat("yyyyMMddHHmmss")
        def result = doGetSolr([
                q    : "kvTitle: \"${title}\" NOT kvUuid : \"${newsId}\" " as String,
                start: paging.getOffset(),
                rows : limit,
                fl   : 'kvDkTime,kvTitle,kvSource,kvAuthor,kvUrl,kvUuid,kvContent,kvAbstract,kvSite,kvCtime,kvSourcetype,score,kvSourcetype,kvOrientation',
                sort : 'kvDkTime desc'
        ])

        if (!result) {
            return paging
        }

        def docs = result.response?.docs
        def numFound = (result.response?.numFound) as Integer
        paging.total = numFound
        docs = (docs != null) ? docs : []
        def yqmsSdf = new SimpleDateFormat("yyyyMMddHHmmss")
        docs.each {
            def title2 = stripHtml(it.kvTitle)
            def content = stripHtml(it.kvContent)
            paging.list << new News(
                    title: title2,
                    source: it.kvSource,
                    author: it.kvAuthor,
                    url: it.kvUrl,
                    newsId: "solr:" + it.kvUuid,
                    contentAbstract: stripHtml(it.kvAbstract),
                    site: it.kvSite,
                    createTime: yqmsSdf.parse(it.kvCtime),
                    newsType: it.kvSourcetype as int,
                    orientation: it.kvOrientation,
                    keywords: WordSegUtil.extractKeywords(title, content, 5)
            )
        }
        return paging
    }

    Map getTotalToday(YqmsSession session) {
        def rep = doGet("OneWeekTrend!listOneWeekTrend.do", session, [:])

        def latest = rep.list[rep.list.size() - 1]

        return [total: latest.COUNT_ZONG]
    }

    Paging getCopyrightMonitorNews(String title,String abstract1, Date start, Date end, int pageNo, int limit,int orderType) {
        def paging = new Paging<TortNews>(1, 300)
        def sdf = new SimpleDateFormat("yyyyMMddHHmmss")
        def query = [:]
        query.q = "kvContent: \"${abstract1}*\"" as String

//        if(title.equals("")){
//            query = [
//                    q: "kvAbstract: \"${abstract1}*\"  AND kvDkTime:[ \"${sdf.format(start)}\" TO \"${sdf.format(end)}\" ]" as String,
//            ]
//        }else if(abstract1.equals("")){
//            query = [
//                    q: "(kvTitle: \"${title}*\" AND kvDkTime:[ \"${sdf.format(start)}\" TO \"${sdf.format(end)}\" ]" as String,
//            ]
//        }else {
//            query = [
//                    q: "(kvTitle: \"${title}*\"  OR kvAbstract: \"${abstract1}*\")  AND kvDkTime:[ \"${sdf.format(start)}\" TO \"${sdf.format(end)}\" ]" as String,
//            ]
//        }
        query.start = paging.getOffset()
        query.rows = limit
//        if(orderType == 1){
//            query.sort = 'kvDkTime desc'
//        }else {
//            query.sort = 'score desc'
//        }
        def result = doGetSolr(query)
        def docs = result.response?.docs
        if( (result.response?.numFound)!=null )
            docs = (docs != null) ? docs : []
        def yqmsSdf = new SimpleDateFormat("yyyyMMddHHmmss")
        docs.each {
            def newsTitle = stripHtml(it.kvTitle)
            def newsAbstract = stripHtml(it.kvAbstract)
            def content = stripHtml(it.kvContent)
            def titleSimilarity = JaccardDistanceUtils.computeJaccardDistance(title, newsTitle)
            def abstractSimilarity = JaccardDistanceUtils.computeJaccardDistance(abstract1, newsAbstract)
            if(titleSimilarity > 0.3 || abstractSimilarity > 0.3){
                paging.list << [
                        title       : newsTitle,
                        url         : it.kvUrl,
                        source      : it.kvSource,
                        createTime  : yqmsSdf.parse(it.kvDkTime).getTime(),
                        publishTime : yqmsSdf.parse(it.kvCtime).getTime(),
                        isOriginal  : 0,
                        abstract    : newsAbstract,
                        content           : content,
                        titleSimilarity   : titleSimilarity,
                        abstractSimilarity: abstractSimilarity,
                        keywords          : WordSegUtil.extractKeywords(title, newsAbstract, 5)
                ]
            }

        }
        paging.total = paging.list.size()
        return paging
    }
}
