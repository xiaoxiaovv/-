package com.istar.mediabroken.service.system

import com.alibaba.fastjson.JSON
import com.istar.mediabroken.entity.account.Account
import com.istar.mediabroken.sender.UserBehaviorSender
import com.istar.mediabroken.service.account.AccountService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Author : YCSnail
 * Date   : 2018-04-04
 * Email  : liyancai1986@163.com
 */
@Service
@Slf4j
class UserBehaviorService {


    public static final String[] user_behavior_type_array = ['login', 'view', 'click']
    public static final String[] user_behavior_module_array = ['capture', 'compile', 'analysis', 'copyright', 'homepage', 'usercenter']
    public static final String[] user_behavior_func_array = ['login', 'site', 'subject']
    public static final String[] user_behavior_action_array = ['login', 'addSite', 'addSubject']

    @Autowired
    private UserBehaviorSender userBehaviorSender
    @Autowired
    private AccountService accountSrv

    def collectUserBehavior(long userId, String type, String module, String func, String action) {

        Account account = accountSrv.getUserInfoById(userId)

        //整理数据结构
        def userBehavior = [
                userId  : userId,
                type    : type,
                module  : module,
                func    : func,
                action  : action,
                time    : System.currentTimeMillis(),
                param   : '',
                accountType : 0     //todo-liyc
        ]
        try {
            //发送到消息队列kafka
            userBehaviorSender.send(JSON.toJSONString([userBehavior]))
        } catch (Exception e){
            log.error(e)
        }
    }

    def collectUserBehavior(long userId, String type, String module, String func, String action, int loginSource, String adminUsername) {

        Account account = accountSrv.getUserInfoById(userId)

        //整理数据结构
        def userBehavior = [
                userId  : userId,
                type    : type,
                module  : module,
                func    : func,
                action  : action,
                time    : System.currentTimeMillis(),
                param   : '',
                accountType : 0,     //todo-liyc
                loginType : loginSource,
                loginUser : adminUsername
        ]
//        log.info('userBehavior: {}', userBehavior)
        try {
            //发送到消息队列kafka
            userBehaviorSender.send(JSON.toJSONString([userBehavior]))
        } catch (Exception e){
            log.error(e)
        }
    }

}
