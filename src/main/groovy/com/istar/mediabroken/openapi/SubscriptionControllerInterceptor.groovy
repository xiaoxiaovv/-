package com.istar.mediabroken.openapi

import com.alibaba.fastjson.JSONObject
import com.istar.mediabroken.api.CurrentUser
import com.istar.mediabroken.api.CurrentUserId
import com.istar.mediabroken.entity.LoginUser
import com.istar.mediabroken.entity.account.Organization
import com.istar.mediabroken.service.account.OrganizationService
import groovy.util.logging.Slf4j
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.lang.annotation.Annotation

import static com.istar.mediabroken.api.ApiResult.apiResult

@Slf4j
public class SubscriptionControllerInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    OrganizationService organizationService

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!(handler instanceof HandlerMethod)) return true
        HandlerMethod method = handler;
        def userAnnotation = getUserAnnotation(method)
        if (userAnnotation == null) {
            return true
        }
        def user = request.getAttribute("__user")
        if (!user.orgId || "0".equals(user.orgId)){
            printErrorInfo(response, HttpStatus.SC_UNAUTHORIZED, '非机构内用户')
            return false
        }
        Organization org = organizationService.getOrgInfoByOrgIdAndAgentId(user.orgId, user.agentId)
        if (!org){
            printErrorInfo(response, HttpStatus.SC_UNAUTHORIZED, '非机构内用户')
            return false
        }
        if (userAnnotation == CurrentUser.class) {
            request.setAttribute("__user", new LoginUser(
                    userId  : user.userId,
                    agentId : user.agentId,
                    orgId   : user.orgId,
                    teamId  : user.teamId,
                    org: [appId: org.appId, secret: org.secret]
            ))
            return true
        }

        return false
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
        }
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

}
