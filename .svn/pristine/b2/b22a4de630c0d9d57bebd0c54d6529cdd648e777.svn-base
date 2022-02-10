package com.istar.mediabroken.api

import com.istar.mediabroken.entity.LoginUser
import com.istar.mediabroken.service.SystemNoticeService
import org.apache.commons.lang3.time.DateUtils
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import static com.istar.mediabroken.api.ApiResult.apiResult

/**
 * Author : YCSnail
 * Date   : 2017-07-06
 * Email  : liyancai1986@163.com
 */
@RestController
@RequestMapping(value = ['/api/notice'])
class SystemNoticeApiController {

    @Autowired
    private SystemNoticeService noticeSrv

    @RequestMapping(value = '/addNotice', method = RequestMethod.PUT)
    Object createNotice(
            @RequestParam String cont,
            @RequestParam String expireTime,
            @RequestParam String username,
            @RequestParam String password
    ){

        if('bjj' != username || 'imedia2017' != password) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '用户名或密码错误！')
        }

        try {
            Date expireDate = DateUtils.parseDate(expireTime, 'yyyy-MM-dd HH:mm:ss')
            noticeSrv.createNotice(cont, expireDate)

            return apiResult()
        }catch (Exception e) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '过期时间格式(yyyy-MM-dd HH:mm:ss)')
        }
    }

    @RequestMapping(value = '/currentNotice', method = RequestMethod.GET)
    Object getLatestNotice(
            @CurrentUser LoginUser user
    ){
        def notice = noticeSrv.getLatestNotice()
        return notice ? apiResult([notice: notice]) : apiResult(HttpStatus.SC_NOT_FOUND, '')
    }

    @RequestMapping(value = '/currentNotice', method = RequestMethod.DELETE)
    Object delLatestNotice(
            @RequestParam String username,
            @RequestParam String password
    ){
        //验证管理员用户名密码
        boolean isAdminAvailable = noticeSrv.isAdminAvailable(username,password);
        if (!isAdminAvailable){
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '用户名或密码错误！')
        }

        noticeSrv.delLatestNotice()
        return apiResult()
    }
}
