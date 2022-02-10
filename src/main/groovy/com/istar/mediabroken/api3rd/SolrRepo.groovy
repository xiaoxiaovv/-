package com.istar.mediabroken.api3rd

import com.istar.mediabroken.entity.News
import com.istar.mediabroken.utils.Paging
import com.istar.mediabroken.utils.SolrPaging
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Repository

import java.text.SimpleDateFormat

import static com.istar.mediabroken.utils.StringUtils.stripHtml

/**
 * Created by luda on 2018/3/29.
 */
@Repository
@Slf4j
class SolrRepo extends BaseApi3rd {

    // 通过solr查询相关新闻按时间
    // 单选 //专题浏览默认显示来源类型 0 全部  01 新闻  02 论坛  03 博客  04 微博 05 平媒  06 微信  07 视频  09 APP  10评论  99 其他
    SolrPaging<TortNews> getPagingNewsByTitle(String title, int pageNo, int limit,String cursor) {
//        keywords = keywords.split(/\s+/).join(',')
        def paging = new SolrPaging<TortNews>(pageNo, limit, 100000)
        def result = doGetSolr([
                q    : "kvTitle: \"${title}\"",
                cursorMark:cursor,
                rows : "100",
                sort: 'kvUuid asc,kvDkTime desc'
        ])

        def docs = result.response?.docs
        def numFound = (result.response?.numFound) as Integer
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
                    dkTime: it.kvDkTime,
                    content: it.kvContent
            )
            i++
        }
        return paging
    }

    SolrPaging<TortNews> getPagingNewsByContent(String content, int pageNo, int limit, String cursor) {
        List keywords = content.split(" ")
        List queryKey = []
        keywords.each {
            queryKey << "kvContent: ${it}"
        }
        def paging = new SolrPaging<TortNews>(pageNo, limit, 100000)
        def result = doGetSolr([
                q         : queryKey.join(" AND "),
                cursorMark:cursor,
//                start     : paging.getOffset(),
                rows      : "100",
//                fl: 'kvDkTime,kvTitle,kvSource,kvAuthor,kvUrl,kvUuid,kvContent,kvAbstract,kvSite,kvCtime,kvSourcetype,kvOrientation',
                sort: 'kvUuid asc,kvDkTime desc'
        ])

        def docs = result.response?.docs
        def numFound = (result.response?.numFound) as Integer
        docs = (docs != null) ? docs : []
        def yqmsSdf = new SimpleDateFormat("yyyyMMddHHmmss")
        def cursorStr = result.nextCursorMark as String
        paging.CurrentCursor = cursorStr
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
                    dkTime: it.kvDkTime,
                    content: it.kvContent
            )
            i++
        }
        return paging
    }

    News getNewsByUrl(String url) {
        def result = doGetSolr([
                q    : "kvUrl: \"${url}\"" as String,
                start: 0,
                rows : 1,
        ])

        def docs = result.response?.docs
        docs = (docs != null) ? docs : []
        def yqmsSdf = new SimpleDateFormat("yyyyMMddHHmmss")
        def obj = docs ? docs[0] : []
        News news = null
        if (obj) {
            news = new TortNews(
                    title: stripHtml(obj.kvTitle),
                    source: obj.kvSource,
                    author: obj.kvAuthor,
                    url: obj.kvUrl,
                    newsId: obj.kvUuid,
                    contentAbstract: stripHtml(obj.kvAbstract),
                    site: obj.kvSite,
                    createTime: yqmsSdf.parse(obj.kvCtime),
                    newsType: obj.kvSourcetype as int,
                    content: obj.kvContent
            )
        }
        return news
    }

    public static void main(String[] args) {
        String url = ""
        String title = ""
        String content = ""
        println(getNewsByUrl("http://www.sohu.com/a/225921495_119586"))
    }
}
