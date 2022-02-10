package com.istar.mediabroken.api

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletRequest
import static com.istar.mediabroken.api.ApiResult.apiResult

/**
 * @author YCSnail
 * @date 2018-05-17
 * @email liyancai1986@163.com
 */
@RestController
@RequestMapping(value = "/api/test")
class TestApiController {

    @RequestMapping(value = "", method = RequestMethod.GET)
    Object test(
            HttpServletRequest request
    ){

        return apiResult([
                getScheme       : request.getScheme(),
                getServerName   : request.getServerName(),
                getServerPort   : request.getServerPort(),
                getLocalPort    : request.getLocalPort(),
                getRemotePort   : request.getRemotePort(),
                isSecure        : request.isSecure(),
                getRemoteAddr   : request.getRemoteAddr(),
                getRequestURL   : request.getRequestURL(),
                getRequestURI   : request.getRequestURI()
        ])
    }

}
