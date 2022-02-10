package com.istar.mediabroken.api.system

import com.istar.mediabroken.api.CurrentUserId
import com.istar.mediabroken.service.system.MessageService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import static com.istar.mediabroken.api.ApiResult.apiResult
import static org.springframework.web.bind.annotation.RequestMethod.GET


@RestController("MessageApiController")
@Slf4j
@RequestMapping(value = "/api/system")
class MessageApiController {
    @Autowired
    MessageService messageService

    @RequestMapping(value = "/messages/newCount", method = GET)
    public Map getNewMesageCount(
            @CurrentUserId Long userId) {
        int newCount = 0
        newCount = messageService.getNewMesageCount(userId)
        return apiResult([count: newCount])
    }

    /**
     * 按createTime倒序排列的消息列表
     * @param userId
     * @param prevTime
     * @param lastTime
     * @param pageSize
     * @param loginSource 1 后台模拟登录 2 前台用户登录
     * @return
     */
    @RequestMapping(value = "/messages", method = GET)
    public Map getMesages(
            @CurrentUserId Long userId,
            @RequestParam(value = "prevTime", required = false) Long prevTime,
            @RequestParam(value = "lastTime", required = false) Long lastTime,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
            @RequestParam(value = "loginSource", required = false, defaultValue = "2") int loginSource
    ) {
        Date prevDate = null
        Date lastDate = null
        if (prevTime) {
            prevDate = new Date(prevTime)
        }
        if (lastTime) {
            lastDate = new Date(lastTime)
        }
        def result = messageService.getMessage(userId, prevDate, lastDate, pageSize, loginSource)
        return result
    }
}