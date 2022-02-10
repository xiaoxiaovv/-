package com.istar.mediabroken.api

import com.istar.mediabroken.entity.account.Privilege
import com.istar.mediabroken.service.ShareChannelService
import com.istar.mediabroken.service.qqom.QqomOAuthService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

import javax.servlet.http.HttpServletRequest

import static com.istar.mediabroken.api.ApiResult.apiResult

/**
 * Author : YCSnail
 * Date   : 2017-09-11
 * Email  : liyancai1986@163.com
 */
@Controller
@Slf4j
@RequestMapping(value = '/api/qqom/oauth')
class QqomOAuthApiController {

    @Autowired
    private QqomOAuthService qqomOAuthSrv
    @Autowired
    private ShareChannelService channelSrv

    /**
     * 获取企鹅媒体平台授权页url
     * @param userId
     * @return { url : $string }
     */
    @RequestMapping(value = "/url", method = RequestMethod.GET)
    @ResponseBody
    @CheckExpiry
    @CheckPrivilege(privileges = [Privilege.SHARE_CHANNEL])
    Object qqomOAuthUrl(
            HttpServletRequest request,
            @CurrentUserId Long userId
    ) {
        return apiResult([
                url : qqomOAuthSrv.getOAuthRequestUrl(request)
        ])
    }

    @RequestMapping(value = "/callback", method = RequestMethod.GET)
    Object callback(
            HttpServletRequest request,
            @CurrentUserId Long userId,
            @RequestParam(value = 'code', required = false, defaultValue = '') String code
    ) {

        if(code == ""){
            String errorCode = request.getParameter('error_code')
            String errorMsg = request.getParameter('error_msg')
            log.error(['qqom', '企鹅号授权回掉', errorCode, errorMsg].join(':::') as String)
        } else {
            def res = channelSrv.addQqomShareChannel(request, userId, code)
        }

        return "redirect:/main.html#!/account/channel"
    }

}
