package com.istar.mediabroken.api.simulateLogin

import com.istar.mediabroken.entity.account.LoginSourceEnum
import com.istar.mediabroken.service.account.AccountService
import com.istar.mediabroken.service.account.SimulateUserSecretKeyService
import com.istar.mediabroken.service.system.UserBehaviorService
import com.istar.mediabroken.utils.CookieUtil
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import static com.istar.mediabroken.api.ApiResult.apiResult
import static org.apache.http.HttpStatus.SC_FORBIDDEN
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR
import static org.apache.http.HttpStatus.SC_NOT_FOUND

@Controller
@Slf4j
@RequestMapping(value = "/api/simulate")
public class SimulateLoginApiController {
    @Autowired
    AccountService accountService
    @Autowired
    SimulateUserSecretKeyService simulateUserSecretKeyService
    @Autowired
    UserBehaviorService userBehaviorSrv

    /**
     * 仅供后台模拟用户登录调用
     * @param request
     * @param userId
     * @param adminUserId
     * @param adminUsername
     * @param secretKey
     * @return
     */
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    @ResponseBody
    public Map simulateLogin(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "userId", required = false, defaultValue = "0") long userId,
            @RequestParam(value = "adminUserId", required = false, defaultValue = "0") long adminUserId,
            @RequestParam(value = "adminUsername", required = false, defaultValue = "") String adminUsername,
            @RequestParam(value = "secretKey", required = false, defaultValue = "") String secretKey
    ) {
//        response.setHeader("Access-Control-Allow-Origin", "http://admintest.zhihuibian.com")
        if (userId == 0 || adminUserId == 0 || "".equals(secretKey) || "".equals(adminUsername)) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, "缺少必要的参数信息！")
//            response.sendRedirect("/index.html")
        }
        // 通过secretKey验证访问维护登录接口的安全
        boolean security = simulateUserSecretKeyService.securityVisitByUserIdAndSecretKey(userId, secretKey)
        if (!security) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, "不安全的维护操作！")
//            response.sendRedirect("/index.html")
        }
        def user = accountService.simulateLogin(request, userId, adminUserId, adminUsername)
        if (user) {
            if (user.valid) {
                if (user.expDate.getTime() < System.currentTimeMillis()) {
                    return apiResult(SC_FORBIDDEN, "此账号已到期，如需继续使用，请联系客服！")
//                    response.sendRedirect("/index.html")
                } else {
                    //记录用户行为数据收集接口，因为后台统计没有时间修改程序，暂不记录后台登录行为的统计数据
                    userBehaviorSrv.collectUserBehavior(userId, "login", "homepage", "login", "login", LoginSourceEnum.adminLogin.key, adminUsername)
                    CookieUtil.addCookies(response, request, [
                            sid              : user.sid,
                            userName         : user.userName,
                            nickName         : user.nickName,
                            avatar           : user.avatar,
                            isSitesInited    : user.isSitesInited.toString(),
                            isRecommandInited: user.isRecommandInited.toString(),
                            isCompileInited  : user.isCompileInited.toString(),
                            roleName         : user.roleName,
                            loginSource      : LoginSourceEnum.adminLogin.key.toString(),
                            adminUsername    : adminUsername
                    ])
                    response.sendRedirect("/main.html")
                    return apiResult()
                }
            } else {
                return apiResult(SC_NOT_FOUND, "账号不存在！")
//                response.sendRedirect("/index.html")
            }

        } else {
            return apiResult(SC_NOT_FOUND, "用户名或密码不正确！")
//            response.sendRedirect("/index.html")
        }
    }

}
