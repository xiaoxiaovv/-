package com.istar.mediabroken.api

import com.istar.mediabroken.entity.account.Privilege
import com.istar.mediabroken.service.ShareChannelService
import com.istar.mediabroken.service.weibo.WeiboOAuthService
import groovy.util.logging.Slf4j
import org.apache.http.protocol.HTTP
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import static com.istar.mediabroken.api.ApiResult.apiResult

/**
 * Author : YCSnail
 * Date   : 2017-03-30
 * Email  : liyancai1986@163.com
 */
@Controller
@Slf4j
@RequestMapping(value = '/api/weibo/oauth')
class WeiboOAuthApiController {

    @Autowired
    WeiboOAuthService weiboOAuthSrv
    @Autowired
    ShareChannelService channelSrv

    /**
     * 获取新浪微博授权页url
     * @param userId
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
                url : weiboOAuthSrv.getOAuthRequestUrl(request)
        ])
    }

    /**
     * 新浪微博授权成功后的回调页
     * @param response
     * @param userId
     * @param code
     * @return
     */
    @RequestMapping(value = "/notify", method = RequestMethod.GET)
    @ResponseBody
    String notify(
            HttpServletRequest request,
            HttpServletResponse response,
            @CurrentUserId Long userId,
            @RequestParam(value = 'code', required = false, defaultValue = '') String code
    ) {
        def res = channelSrv.addWeiboShareChannel(request, userId, code)

        boolean success = false
        String msg = ''

        if(code == ''){
            msg = '-1'
        }else if(res == null){
            msg = '0'
        }else {
            success = true
            msg = '1'
        }

        String html = """
<!DOCTYPE html>
<html>
<body>
<script>
window.opener.oauthCallback(${success}, '${msg}');
window.close();
</script>
</body>
</html>
"""
        response.setCharacterEncoding(HTTP.UTF_8)
        response.setHeader("contentType", "text/html; charset=${HTTP.UTF_8}")
        response.getOutputStream().write(html.getBytes())
    }
}
