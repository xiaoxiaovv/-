package com.istar.mediabroken.service.app

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.istar.mediabroken.entity.app.Agent
import com.istar.mediabroken.repo.app.AgentRepo
import com.istar.mediabroken.utils.UrlUtils
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import javax.servlet.http.HttpServletRequest
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

/**
 * Author : YCSnail
 * Date   : 2018-01-19
 * Email  : liyancai1986@163.com
 */
@Service
@Slf4j
class AgentService {

    @Autowired
    private AgentRepo agentRepo

    static Agent defaultAgent = [
            "id" : "1",
            "agentName" : "智慧星光传媒事业部",
            "siteName"  : "编++",
            "agentKey"  : "default",
            "telephone" : "010-83051272",
            "qrcode"    : "http://www.zhihuibian.com/theme/default/images/qrcode.png",
            "logo"      : "/theme/default/images/logo.png",
            "pureLogo"  : "/theme/default/images/logo-pure.png",
            "bgImage"   : "/theme/default/images/index-bg.jpg",
            "icon"      : "/theme/default/images/favicon.ico",
            "slogen"    : "告别繁琐 创造便是快乐",
            "desc"      : "智能内容创作辅助工具，基于大数据·人工智能技术，轻松完成素材采集、智能选题、机器组稿、多端发布，为内容编辑分忧。",
            "footer"    : "北京智慧星光信息技术有限公司 &nbsp;&nbsp;&nbsp;&nbsp; 地址：北京市海淀区北四环西路56号辉煌时代大厦15层<br/>Copyright 2001-2018 istarshine.com&nbsp;&nbsp;&nbsp;&nbsp; All rights reserved.<br/>京ICP备12009876号-1  京公网安备11010802008588号"
    ]
    private static final Cache<String, String> cache = CacheBuilder.newBuilder()
            .maximumSize(100)    // 最多可以缓存1000个key
            .expireAfterWrite(86400, TimeUnit.SECONDS)  // 过期时间 24小时
            .build()


    /**
     * 获取app应用的相关信息
     * @param request
     * @return
     */
    Agent getAgent (HttpServletRequest request) {

        String domain = UrlUtils.getBjjDomain(request)

        log.info('request-serverName: ' + request.getServerName())
        log.info('request-request-url: ' + request.requestURL)

        if(!domain){
            return defaultAgent
        }

        Agent agent = cache.get("AGENT_${domain}", new Callable<Agent>() {
            @Override
            Agent call() throws Exception {
                def list = agentRepo.getAgentList(domain)
                return (list && list.size() > 0) ? list.get(0) : defaultAgent
            }
        })
        return agent
    }

    void clearAgentCache () {
        cache.invalidateAll()
    }

    void clearAgentCacheByDomain(String domain) {
        cache.invalidate("AGENT_${domain}")
    }

}
