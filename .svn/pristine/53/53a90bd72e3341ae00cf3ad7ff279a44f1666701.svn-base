package com.istar.mediabroken.api

import com.istar.mediabroken.service.CaptureService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

import static com.istar.mediabroken.api.ApiResult.apiResult

@Controller
@Slf4j
public class NewsApiController {
    @Autowired
    CaptureService captureSrv

    /**
     * 请求:
     * sid: "会话id"
     * pageNo: int   // 页号
     * limit: int
     *
     * 返回:
     * <pre>
     * {
     *    status: int           // 状态,200为成功
     *    total: int,           // 新闻总数,第一页为1
     *    totalPage: int,       // 总页数
     *    list: [{
     *      newsId: "新闻Id"
     *      title: "标题",
     *      source: "新浪",      // 来源
     *      time: long,
     *      heat: int
     *    }, ...]
     * }
     * </pre>
     *
     * @deprecated /api/capture/news
     */
	@RequestMapping(value = "/news")
	@ResponseBody
    @Deprecated
	public Map news(String sid, int pageNo, int limit) {
        def random = new Random()

        def rep = [:]
        rep.status = 200
        rep.total = 3001
        rep.totalPage = Math.round(rep.total / limit)
        rep.list = []
        def offset = pageNo * limit
        for (int i = 0; i < limit; i++) {
            rep.list << [
                    newsId: "${offset + i}".toString(),
                    title: "标题${offset + i}".toString(),
                    source: "来源一般是网站${offset + i}".toString(),
                    time: System.currentTimeMillis(),
                    heat: random.nextInt(10)
            ]
        }

		return rep;
	}

    /*
     * 请求:
     * sid: "会话id"
     * limit: int
     *
     * 返回:
     * {
     *    status: int           // 状态,200为成功
     *    list: [{
     *      newsId: "新闻Id"
     *      title: "标题",
     *      source: "新浪",      // 来源
     *      time: long,
     *      heat: int
     *    }, ...]
     * }
     */
    @RequestMapping(value = "/focusNews")
    @ResponseBody
    public Map focusNews(String sid, int limit) {
        def random = new Random()

        def rep = [:]
        rep.status = 200
        rep.total = 3001
        rep.limit = []
        for (int i = 0; i < limit; i++) {
            rep.limit << [
                    newsId: "${i}".toString(),
                    title: "标题${i}同样很长，有多长就不知道了".toString(),
                    source: "来源${i}".toString(),
                    time: System.currentTimeMillis(),
                    heat: random.nextInt(10)
            ]
        }

        return rep;
    }

    /*
    * 请求:
    * sid: "会话id"
    * limit: int
    *
    * 返回:
    * {
        *    status: int           // 状态,200为成功
        *    list: [{
                        *      newsId: "新闻Id"
            *      title: "标题",
            *      source: "新浪",      // 来源
            *      time: long,
            *      heat: int
            *    }, ...]
        * }
    */
    @RequestMapping(value = "/hotWeibo")
    @ResponseBody
    public Map hotWeibo(String sid, int limit) {
        limit = 5
        def newsList = captureSrv.getHotWeibo(0)
        limit = limit > newsList.size() ? newsList.size() : limit
        return apiResult([limit: newsList.subList(0, limit)]);
    }


    /*
     * 请求:
     * sid: "会话id"
     * pageNo: int   // 页号
     * limit: int
     *
     * 返回:
     * {
     *    status: int           // 状态,200为成功
     *    list: [{
     *      siteName: "站点名称"
     *      siteDomain: "站点域名",
     *      channelName: "频道名称",
     *      channelDomain: "频道域名",
     *      navigationName: "导航名称",
     *      isShow: boolean, //导航显示
     *      categoryName: "" // 所属类别名称
     *    }, ...]
     * }
     */
    @RequestMapping(value = "/captureSettings")
    @ResponseBody
    @Deprecated
    public Map captureSettings(String sid, int pageNo, int limit) {
        def rep = [:]
        rep.status = 200
        rep.total = 3001
        rep.totalPage = Math.round(rep.total / limit)
        rep.list = []
        def offset = pageNo * limit

        def categoryNameList = ['微信公众号', '媒体网站', '专业网站']
        def captureTimeSpaceList = ['5分钟', '6分钟', '20分钟']
        for (int i = 0; i < limit; i++) {
            rep.list << [
                    navigationId: "${offset + i}".toString(),
                    siteName: "站点名称${offset + i}".toString(),
                    siteDomain: "站点域名${offset + i}".toString(),
                    channelName: "频道名称${offset + i}".toString(),
                    channelDomain: "频道域名${offset + i}".toString(),
                    navigationName: "导航名称${offset + i}".toString(),
                    isShow: true,
                    categoryName: categoryNameList[i%3].toString(),
                    captureTimeSpace: captureTimeSpaceList[i%3].toString()
            ]
        }

        return rep;
    }


    /*
     * 请求:
     * sid: "会话id"
     * newsId: "新闻ID"
     * limit: int
     *
     * 返回:
     * {
     *    status: int           // 状态,200为成功
     *    list: [{
     *      newsId: "新闻Id"
     *      title: "标题",
     *      source: "新浪",      // 来源
     *      time: long,
     *      heat: int
     *    }, ...]
     * }
     * @Deprecated see /api/news/{newsId:.*}/relatedNews}
     */
    @RequestMapping(value = "/relatedNews")
    @ResponseBody
    @Deprecated
    public Map relatedNews(String sid, String newsId, int imit) {
        def random = new Random()

        def rep = [:]
        rep.status = 200
        rep.total = 3001
        rep.list = []
        for (int i = 0; i < limit; i++) {
            rep.list << [
                    newId: "${i}".toString(),
                    title: "标题${i}".toString(),
                    source: "来源${i}".toString(),
                    time: System.currentTimeMillis(),
                    heat: random.nextInt(10)
            ]
        }

        return rep;
    }
}
