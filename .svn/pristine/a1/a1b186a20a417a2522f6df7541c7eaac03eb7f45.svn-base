package com.istar.mediabroken.api

import com.istar.mediabroken.entity.account.Privilege
import com.istar.mediabroken.service.ShareChannelService
import com.istar.mediabroken.service.toutiao.ToutiaoOAuthService
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
 * Date   : 2017-05-31
 * Email  : liyancai1986@163.com
 */
@Controller
@Slf4j
@RequestMapping(value = '/api/toutiao/oauth')
class ToutiaoOAuthApiController {

    @Autowired
    private ToutiaoOAuthService toutiaoOAuthSrv
    @Autowired
    private ShareChannelService channelSrv

    /**
     * 获取头条号授权页url
     * @return { url : $string }
     */
    @RequestMapping(value = "/url", method = RequestMethod.GET)
    @ResponseBody
    @CheckExpiry
    @CheckPrivilege(privileges = [Privilege.SHARE_CHANNEL])
    Object weiboOAuthUrl(
            HttpServletRequest request,
            @CurrentUserId Long userId
    ) {
        return apiResult([
                url : toutiaoOAuthSrv.getOAuthRequestUrl(request)
        ])
    }

    @RequestMapping(value = "/callback", method = RequestMethod.GET)
    Object callback(
            HttpServletRequest request,
            @CurrentUserId Long userId,
            @RequestParam(value = 'code', required = false, defaultValue = '') String code
    ) {

        def res = channelSrv.addToutiaoShareChannel(request, userId, code)

        return "redirect:/main.html#!/account/channel"
    }
}
