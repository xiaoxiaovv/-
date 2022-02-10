package com.istar.mediabroken.api

import com.alibaba.fastjson.JSONObject
import com.istar.mediabroken.entity.LoginUser
import com.istar.mediabroken.entity.account.Account
import com.istar.mediabroken.entity.account.LoginSourceEnum
import com.istar.mediabroken.service.account.AccountService
import com.istar.mediabroken.service.account.PrivilegeService
import com.istar.mediabroken.service.account.SimulateLoginSessionService
import groovy.util.logging.Slf4j
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter

import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.lang.annotation.Annotation

import static com.istar.mediabroken.api.ApiResult.apiResult
import static org.apache.http.HttpStatus.*

@Slf4j
public class ApiControllerInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    AccountService accountSrv
    @Autowired
    PrivilegeService privilegeService
    @Autowired
    SimulateLoginSessionService simulateLoginSessionService

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!(handler instanceof HandlerMethod)) return true

        HandlerMethod method = handler;

        def userAnnotation = getUserAnnotation(method)
        if (userAnnotation == null) {
            return true
        }

        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
        response.setHeader("Pragma", "no-cache"); // HTTP 1.0

        def loginUser = null
        def adminUsername = ""
        if(isMinaRequest(request)) {

            String msid = request.getHeader('msid') as String

            loginUser = accountSrv.getUserByWechatSessionId(msid)
            if (!loginUser) {
                printErrorInfo(response, '无效的会话!')
                return false
            }
        } else {

            def cookies = request.getCookies()
            if (!cookies) {
                printErrorInfo(response)
                return false
            }

            String sid = getSid(request, cookies)
            if (!sid) {
                printErrorInfo(response)
                return false
            }

            String loginSource = getParamValue(request, cookies, "loginSource")//判断登录来源，后台模拟登录还是前台用户登录
            if (String.valueOf(LoginSourceEnum.adminLogin.key).equals(loginSource)) {
                adminUsername = getParamValue(request, cookies, "adminUsername")
                loginUser = simulateLoginSessionService.getUserBySimulateSessionId(sid)//后台模拟登录
            }else {
                loginUser = accountSrv.getUserBySessionId(sid)//前台用户登录
            }
            if (!loginUser) {
                printErrorInfo(response, '无效的会话!')
                return false
            } else if (!loginUser.enable) {
                def errorId = UUID.randomUUID().toString()
                printErrorInfo(response, SC_LOCKED, '用户已在其他设备登录!', errorId)
                return false
            }
        }

        log.info('user:{}, url:{}, method:{}, admin:{}', loginUser.userId, request.getRequestURI(), request.getMethod().toLowerCase(), adminUsername)

        def user = accountSrv.getUserInfoById(loginUser.userId)

        boolean isExpired = checkExpired(user, method)
        if (isExpired) {
            printErrorInfo(response, SC_FORBIDDEN, '您的账号已到期，如需继续使用，请联系客服！')
            return false
        }

        boolean hasPrivilege = checkPrivilege(user, method)
        if (!hasPrivilege) {
            printErrorInfo(response, SC_FORBIDDEN, '您的账号暂时没有相应操作权限！')
            return false
        }

        if (userAnnotation == CurrentUserId.class) {
            request.setAttribute("__userId", loginUser.userId)
            return true
        } else if (userAnnotation == CurrentUser.class) {
            request.setAttribute("__user", new LoginUser(
                    userId: loginUser.userId,
                    agentId: user.agentId ?: '1',
                    orgId: user.orgId ?: '0',
                    teamId: user.teamId ?: '0',
                    userType: user.userType ?: Account.USERTYPE_TRIAL
            ))
            return true
        }

        printErrorInfo(response)
        return false
    }

    private boolean checkExpired(Account user, HandlerMethod method) {
        def isExpired = false

        if (user.expDate.getTime() < System.currentTimeMillis()) {
            if (method.getMethodAnnotation(CheckExpiry.class)) {
                isExpired = true
            } else if (method.getMethodAnnotation(NotCheckExpiry.class)) {
                isExpired = false
            } else {
                def methodName = method.getMethod().getName()
                if (methodName.startsWith('add') || methodName.startsWith('modify') || methodName.startsWith('remove')) {
                    isExpired = true
                }
            }
        }

        return isExpired
    }

    /**
     * 用户鉴权
     */
    private boolean checkPrivilege(Account user, HandlerMethod method) {

        def hasPrivilege = true

        Annotation privilegeAnnotation = method.getMethodAnnotation(CheckPrivilege.class)

        //只对增加了验证权限注解的方法做鉴权
        if (privilegeAnnotation) {
            //该api需要验证的权限
            def privileges = privilegeAnnotation.privileges()

            if (privileges.length <= 0) return false

            //用户拥有的权限
            def userPrivileges = privilegeService.getPrivilegesByUserId(user.id)
            if (!userPrivileges) return false

            //取该API权限与用户权限的交集
            def privilegeList = userPrivileges.grep(privileges)
            hasPrivilege = (privilegeList.size() > 0)
        }
        return hasPrivilege
    }

    private String getSid(HttpServletRequest request, Cookie[] cookies) {
        def sid = request.getParameter("sid")
        for (int i = 0; i < cookies.size(); i++) {
            def it = cookies[i]
            if (it.getName() == "sid") {
                sid = it.getValue()
            }
        }
        return sid
    }

    private def getParamValue(HttpServletRequest request, Cookie[] cookies, String key) {
        def value = request.getParameter(key)
        for (int i = 0; i < cookies.size(); i++) {
            def it = cookies[i]
            if (it.getName() == key) {
                value = it.getValue()
            }
        }
        return value
    }

    private Class<? extends Annotation> getUserAnnotation(HandlerMethod method) {
        def params = method.getMethodParameters()
        def userAnnotation = null
        for (int i = 0; i < params.length; i++) {
            if (params[i].getParameterAnnotation(CurrentUserId.class)) {
                userAnnotation = CurrentUserId.class
                break;
            } else if (params[i].getParameterAnnotation(CurrentUser.class)) {
                userAnnotation = CurrentUser.class
                break;
            }
        }
        return userAnnotation
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        if (ex) {
            def errorId = UUID.randomUUID().toString()
            log.error('unexpected error: ' + errorId, ex)
            printErrorInfo(response, SC_INTERNAL_SERVER_ERROR, '未知的错误!', errorId)
        }
    }

    private void printErrorInfo(HttpServletResponse response) {
        response.setHeader("Content-Type", "application/json;charset=UTF-8")
        response.setCharacterEncoding("UTF-8");
        def result = JSONObject.toJSONString(apiResult(HttpStatus.SC_UNAUTHORIZED, '登录超时!'))
        response.getWriter().write(result);
        response.getWriter().close();
    }

    private void printErrorInfo(HttpServletResponse response, String message) {
        response.setHeader("Content-Type", "application/json;charset=UTF-8")
        response.setCharacterEncoding("UTF-8")
        def result = JSONObject.toJSONString(apiResult(HttpStatus.SC_UNAUTHORIZED, message))
        response.getWriter().write(result);
        response.getWriter().close();
    }

    private void printErrorInfo(HttpServletResponse response, int status, String message, String errorId) {
        response.setHeader("Content-Type", "application/json;charset=UTF-8")
        response.setCharacterEncoding("UTF-8")
        def result = JSONObject.toJSONString(apiResult(status, message, errorId))
        response.getWriter().write(result);
        response.getWriter().close();
    }

    private void printErrorInfo(HttpServletResponse response, int status, String message) {
        response.setHeader("Content-Type", "application/json;charset=UTF-8")
        response.setCharacterEncoding("UTF-8")
        def result = JSONObject.toJSONString(apiResult(status, message))
        response.getWriter().write(result);
        response.getWriter().close();
    }

    /*
    * 校验是否是小程序发出的请求
    */

    private static boolean isMinaRequest(HttpServletRequest request) {

        String ua = request?.getHeader('user-agent')
        String source = request?.getHeader('source')
        if (!ua) {
            return false
        }
        log.debug(ua)
        return (ua.contains('MicroMessenger') && 'mina' == source)
    }

}
