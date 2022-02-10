package com.istar.mediabroken.api.app

import com.istar.mediabroken.api.CurrentUser
import com.istar.mediabroken.entity.LoginUser
import com.istar.mediabroken.service.app.AgentService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletRequest
import static com.istar.mediabroken.api.ApiResult.apiResult

/**
 * Author : YCSnail
 * Date   : 2018-01-03
 * Email  : liyancai1986@163.com
 */
@RestController
@RequestMapping(value = '/api/app')
class AppInfoApiController {

    @Autowired
    private AgentService agentService

    @RequestMapping(value = '/info', method = RequestMethod.GET)
    Object getAppInfo(
            HttpServletRequest request
    ) {

        def agent = agentService.getAgent(request)
        def footer = agent.footer
        footer = footer.replaceAll("\n", "<br/>").replaceAll(" ", "&nbsp;");
        def appInfo = [
                "id"        : agent.id,
                "appName"   : agent.siteName,
                "appKey"    : agent.agentKey,
                "telephone" : agent.telephone,
                "qrcode"    : agent.qrcode,
                "logo"      : agent.logo,
                "pureLogo"  : agent.pureLogo,
                "bgImage"   : agent.bgImage,
                "icon"      : agent.icon,
                "slogen"    : agent.slogen,
                "desc"      : agent.desc,
                "footer"    : footer,
                "key"       : UUID.randomUUID().toString()
        ]

        return apiResult([appInfo : appInfo])
    }

    @RequestMapping(value = '/clearAllCache', method = RequestMethod.DELETE)
    Object clearAppInfo(
            @CurrentUser LoginUser user
    ) {
        agentService.clearAgentCache()
        return apiResult()
    }

    @RequestMapping(value = '/clearCache', method = RequestMethod.DELETE)
    Object clearAppInfo(
            @RequestParam("domain") String domain
    ) {
        agentService.clearAgentCacheByDomain(domain)
        return apiResult()
    }

}
