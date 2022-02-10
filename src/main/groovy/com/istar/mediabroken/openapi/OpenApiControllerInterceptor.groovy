package com.istar.mediabroken.openapi

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.istar.mediabroken.repo.account.OrganizationRepo
import com.istar.mediabroken.service.account.AccountService
import com.istar.mediabroken.utils.Md5Util
import groovy.util.logging.Slf4j
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import static com.istar.mediabroken.api.ApiResult.apiResult
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR

@Slf4j
public class OpenApiControllerInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    AccountService accountSrv

    @Autowired
    OrganizationRepo orgRepo

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!(handler instanceof HandlerMethod)) return true
        def appId = request.getParameter("appId")

        if(!appId) {
            printErrorInfo(response, HttpStatus.SC_BAD_REQUEST, '没有AppId')
            return false
        }

        def org = orgRepo.getOrganizationByAppId(appId)
        if (!org) {
            printErrorInfo(response, HttpStatus.SC_UNAUTHORIZED, '没有查找到指定的App信息')
            return false
        }
        log.info('start data')
        log.info('data: {}', request.getParameter('data'))
        def sign
        try {
            sign = Md5Util.md5(appId + org.secret + request.getParameter('data'))
        } catch (e) {
            log.error('unexpectged error', e)
            printErrorInfo(response, HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage())
            return false
        }

        log.info('sign: {}', sign)
        if (sign != request.getParameter('sign')) {
            printErrorInfo(response, HttpStatus.SC_UNAUTHORIZED, '数据校验错误')
            return false
        }

        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
        response.setHeader("Pragma", "no-cache"); // HTTP 1.0

        request.setAttribute("__orgId", org.id)
        request.setAttribute("__params", JSON.parse(request.getParameter('data')))

        return true
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
