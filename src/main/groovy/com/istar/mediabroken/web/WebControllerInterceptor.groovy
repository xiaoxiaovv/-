package com.istar.mediabroken.web

import com.alibaba.fastjson.JSONObject
import com.istar.mediabroken.api.CurrentUserId
import com.istar.mediabroken.service.account.AccountService
import groovy.util.logging.Slf4j
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import static com.istar.mediabroken.api.ApiResult.apiResult

@Slf4j
public class WebControllerInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    AccountService accountSrv

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!(handler instanceof HandlerMethod)) return true
        HandlerMethod method = handler;
        def params = method.getMethodParameters()
        def isFound = false
        for (int i = 0; i < params.length; i++) {
            if (params[i].getParameterAnnotation(CurrentUserId.class)) {
                isFound = true
                break;
            }
        }

        if (!isFound) {
            return true
        }

        // todo 可以通过判断handler的是否有currentUserId注解来判断是否要拦截
//        if (request.getServletPath() != "/api/account/sidValidation") {
//            return true
//        }

        def cookies = request.getCookies()
        def sid = request.getParameter("sid")
        for (int i = 0; i < cookies.size(); i++) {
            def it = cookies[i]
            if (it.getName() == "sid") {
                sid = it.getValue()
            }
        }

        def userId = accountSrv.getUserIdBySessionId(sid)
        log.debug("userId: {}", userId)
        if (userId) {
            request.setAttribute("__userId", userId)
            return true
        }

        response.setContentType("application/json;charset=UTF-8")
        def result = JSONObject.toJSONString(apiResult(HttpStatus.SC_UNAUTHORIZED, '登录过期，请重新登录'))
        response.getOutputStream().print(result)
        return false
    }
}
