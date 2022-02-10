package com.istar.mediabroken.api

import com.istar.mediabroken.service.wechat.WechatMPService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Author : YCSnail
 * Date   : 2017-11-22
 * Email  : liyancai1986@163.com
 */
@Controller
@Slf4j
@RequestMapping(value = '/api/wechat/mp')
class WechatMPApiController {

    @Autowired
    private WechatMPService wechatMPService

    /**
     * 该方法主要是提供微信公众号中js-sdk开发的配置信息
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value='/config', method = RequestMethod.GET)
    public Objects config(
            HttpServletRequest request,
            HttpServletResponse response
    ){

        def wechatMPConfig = wechatMPService.getWechatConfig(request)

        response.setContentType('application/javascript')
        response.writer.print("""
            wx.config({
                debug: false,
                appId: '${wechatMPConfig.appId}',
                timestamp: ${wechatMPConfig.timestamp},
                nonceStr: '${wechatMPConfig.noncestr}',
                signature: '${wechatMPConfig.signature}',
                jsApiList: [
                    'checkJsApi',
                    'onMenuShareTimeline',
                    'onMenuShareAppMessage',
                    'onMenuShareQQ',
                    'onMenuShareWeibo',
                    'chooseWXPay',
                ]
            });
        """)
    }

}
