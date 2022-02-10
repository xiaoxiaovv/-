package com.istar.mediabroken.openapi

import com.istar.mediabroken.service.ICompileService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import static com.istar.mediabroken.api.ApiResult.apiResult
import static org.springframework.web.bind.annotation.RequestMethod.DELETE
import static org.springframework.web.bind.annotation.RequestMethod.GET

/**
 * Author : YCSnail
 * Date   : 2017-04-19
 * Email  : liyancai1986@163.com
 */
@RequestMapping(value = "/openapi/compile")
@RestController
class ICompileOApiController {

    @Autowired
    private ICompileService iCompileSrv

    /*
    *
    * 查询未推送的新闻摘要数据,最多10条, 按创建时间正序排序
    *
    *
    * 返回:
    *
    * * 摘要列表
    String id
    String title
    String source
    String picUrl
    String content
    List newsDetail
    long createTime
    */
    @RequestMapping(value = "/abstractPush", method = GET)
    Map getAbstractPushList(
            @RequestParam("orgId") String orgId
    ) {
        def list = []
        iCompileSrv.getOpenAbstractPushList(orgId)?.each {
            list << [
                    id          : it.abstractId,
                    title       : it.title as String,
                    source      : it.source as String,
                    picUrl      : it.picUrl,
                    newsDetail  : it.newsDetail,
                    createTime  : it.createTime
            ]
        }
        return apiResult([list: list])
    }

    /*
    *
    * 确认已处理完的摘要, abstractIds是一个逗号分隔的字符串列表
    *
    * 把状态从未推送修改成已推送
    *
    * /openapi/compile/abstractPush/2017-04-18,2017-04-19?_method=delete&orgId=test_org
    *
    * 返回: {"status": 200}
    */
    @RequestMapping(value = "abstractPush/{abstractIds:.*}", method = DELETE)
    Map deleteAbstractPush(
            @RequestParam("orgId") String orgId,
            @PathVariable("abstractIds") String abstractIds
    ) {
        iCompileSrv.updateAbstractPush2Pushed(orgId, abstractIds)
        return apiResult()
    }

    /*
    *
    * 查询未推送的综述数据,最多10条, 按创建时间正序排序
    *
    *
    * 返回:
    *
    * * 综述列表
    String id
    String title
    String source
    String data
    long createTime
    */
    @RequestMapping(value = "/summaryPush", method = GET)
    Map getSummaryPushList(
            @RequestParam("orgId") String orgId
    ) {
        def list = []
        iCompileSrv.getOpenSummaryPushList(orgId)?.each {
            list << [
                    id          : it.summaryId,
                    title       : it.title as String,
                    source      : it.source as String,
                    data        : it.data,
                    createTime  : it.createTime
            ]
        }
        return apiResult([list: list])
    }

    /*
    *
    * 确认已处理完的新闻, summaryIds是一个逗号分隔的字符串列表
    *
    * 把状态从未推送修改成已推送
    *
    * /openapi/compile/summaryPush/723463874qrqwer,82u3wuriuqerweur,i3eruweoiruweoriuw?_method=delete&orgId=test_org
    *
    * 返回: {"status": 200}
    */
    @RequestMapping(value = "summaryPush/{summaryIds:.*}", method = DELETE)
    Map deleteSummaryPush(
            @RequestParam("orgId") String orgId,
            @PathVariable("summaryIds") String summaryIds
    ) {
        iCompileSrv.updateSummaryPush2Pushed(orgId, summaryIds)
        return apiResult()
    }
}
