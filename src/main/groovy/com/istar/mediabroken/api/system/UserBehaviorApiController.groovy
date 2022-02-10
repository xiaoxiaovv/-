package com.istar.mediabroken.api.system

import com.istar.mediabroken.api.CurrentUser
import com.istar.mediabroken.entity.LoginUser
import com.istar.mediabroken.entity.account.LoginSourceEnum
import com.istar.mediabroken.service.system.UserBehaviorService
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import static com.istar.mediabroken.api.ApiResult.apiResult

/**
 * Author : YCSnail
 * Date   : 2018-04-04
 * Email  : liyancai1986@163.com
 */
@RestController
@RequestMapping(value = "/api/user/behavior")
class UserBehaviorApiController {

    @Autowired
    private UserBehaviorService userBehaviorSrv

    /**
     * 用户行为数据收集接口
     * @param user
     * @param type      login|view|click
     * @param module    capture|compile|analysis|copyright|homepage|usercenter
     * @param func
     * @param action
     * @param loginSource
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public Object collectUserBehavior(
            @CurrentUser LoginUser user,
            @RequestParam(value = "type", defaultValue = "") String type,
            @RequestParam(value = "module", defaultValue = "") String module,
            @RequestParam(value = "func", defaultValue = "") String func,
            @RequestParam(value = "action", defaultValue = "") String action
    ) {
        //验证接收参数

        if(!UserBehaviorService.user_behavior_type_array.contains(type)) {
            return apiResult(status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "type参数值不正确！")
        }
        if(!UserBehaviorService.user_behavior_module_array.contains(module)) {
            return apiResult(status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "module参数值不正确！")
        }
        if(!UserBehaviorService.user_behavior_func_array.contains(func)) {
            return apiResult(status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "func参数值不正确！")
        }
        if(!UserBehaviorService.user_behavior_action_array.contains(action)) {
            return apiResult(status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "action参数值不正确！")
        }


        //收集用户行为
        userBehaviorSrv.collectUserBehavior(user.userId, type, module, func, action, LoginSourceEnum.userLogin.key, "")

        return apiResult()
    }
}
