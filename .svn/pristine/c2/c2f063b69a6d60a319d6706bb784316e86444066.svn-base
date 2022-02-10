package com.istar.mediabroken.api

import com.istar.mediabroken.entity.account.Privilege
import com.istar.mediabroken.service.ShareChannelService
import com.istar.mediabroken.service.wechat.WechatOAuthService
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

import javax.servlet.http.HttpServletRequest

import static com.istar.mediabroken.api.ApiResult.apiResult

/**
 * Author : YCSnail
 * Date   : 2017-05-17
 * Email  : liyancai1986@163.com
 */
@Controller
@Slf4j
@RequestMapping(value = '/api/wechat/oauth')
class WechatOAuthApiController {

    @Autowired
    private WechatOAuthService wechatOAuthService
    @Autowired
    private ShareChannelService channelSrv


    @RequestMapping(value = '/push', method = [RequestMethod.GET, RequestMethod.POST])
    @ResponseBody
    Object push(
            HttpServletRequest request
    ){

        String nonce        = request.getParameter("nonce")
        String timestamp    = request.getParameter("timestamp")
        String signature    = request.getParameter("signature")
        String msgSignature = request.getParameter("msg_signature")

        if (StringUtils.isBlank(msgSignature))
            return

        //todo 在解析xml之前，首先应验证url

        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line)
        }
        String xml = sb.toString();

        log.info('wechat:::原始xml:::'.concat(xml))

        try {
            wechatOAuthService.wechatNotify(request, nonce, timestamp, msgSignature, xml)
            return 'success'
        }catch (Exception e) {
            return 'failure'
        }
    }

    /**
     * 获取微信公众号授权页url
     * @return { url : $string }
     */
    @RequestMapping(value = "/url", method = RequestMethod.GET)
    @ResponseBody
    @CheckExpiry
    @CheckPrivilege(privileges = [Privilege.SHARE_CHANNEL])
    Object wechatOAuthUrl(
            HttpServletRequest request,
            @CurrentUserId Long userId
    ) {
        return apiResult([
                url : wechatOAuthService.getOAuthRequestUrl(request)
        ])
    }

    /**
     * 授权成功回调页
     * @param userId
     * @param authCode
     * @param expiresIn
     * @return
     */
    @RequestMapping(value = "/callback", method = RequestMethod.GET)
    Object callback(
            HttpServletRequest request,
            @CurrentUserId Long userId,
            @RequestParam(value = 'auth_code', required = false, defaultValue = '') String authCode,
            @RequestParam(value = 'expires_in', required = false, defaultValue = '600') long expiresIn
    ) {

        def res = channelSrv.addWechatShareChannel(request, userId, authCode)

        return "redirect:/main.html#!/account/channel"
    }

    /**
     * /api/wechat/oauth/mp/$APPID$/callback
     * 公众号消息与事件接收URL
     * @return
     */
    @RequestMapping(value = "/mp/{appId}/callback", method = RequestMethod.GET)
    @ResponseBody
    Object mpCallback(
            HttpServletRequest request,
            @PathVariable String appId
    ){
        log.info('wechat:::appId:::{}', appId)
        try {
            return 'success'
        }catch (Exception e) {
            return 'failure'
        }
    }


}
