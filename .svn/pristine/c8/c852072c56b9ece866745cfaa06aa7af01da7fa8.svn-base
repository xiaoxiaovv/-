package com.istar.mediabroken.api.account

import com.istar.mediabroken.api.CurrentUserId
import com.istar.mediabroken.entity.account.AccountOpenTypeEnum
import com.istar.mediabroken.entity.app.Agent
import com.istar.mediabroken.service.VerifyCodeService
import com.istar.mediabroken.service.account.AccountService
import com.istar.mediabroken.service.app.AgentService
import com.istar.mediabroken.service.wechat.WechatMinaService
import com.istar.mediabroken.utils.DateUitl
import com.istar.mediabroken.utils.StringUtils
import groovy.util.logging.Slf4j
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

import static com.istar.mediabroken.api.ApiResult.apiResult
import static org.apache.http.HttpStatus.*

@Controller
@Slf4j
@RequestMapping(value = "/api/account")
public class AccountWechatApiController {
    @Value("env")
    String env

    @Autowired
    AccountService accountService
    @Autowired
    VerifyCodeService verifyCodeSrv
    @Autowired
    AgentService agentService
    @Autowired
    WechatMinaService wechatMinaService


    int EXPIRY_INTERVAL = 10 * 60 * 1000

    /**
     * 微信小程序通过手机号码注册编++账号，点击获取验证码时，校验手机号码成功后发送验证码
     * 注册时将验证手机号码和验证码，再把用户名和密码通过发送短信给用户
     * 将原有手机号注册，更改为注册登录一体
     * 用户点击立即注册进入手机号注册/登录页面，用户输入手机号并获取验证码并输入，验证规则不变。如果系统已有此手机号且未绑定其他微信号，直接登陆。
     * 如果绑定过其他微信号提示已绑过其他微信号。如果系统没有此手机号则通过此手机号注册（同时短信发送pc端用户名密码）提示登陆成功。
     * @param phoneNumber
     * @param verifyCode
     * @return
     */
    @RequestMapping(value = "/wechat/regist", method = RequestMethod.POST)
    @ResponseBody
    public Map wechatRegist(
            @RequestParam(value = "phoneNumber", required = false, defaultValue = "") String phoneNumber,
            @RequestParam(value = "verifyCode", required = true) String verifyCode,
            @RequestParam(value = "wechatCode", required = true) String wechatCode
    ) {
        if (!phoneNumber) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, "手机号不能为空")
        }
        if (StringUtils.isMobileNumber(phoneNumber)) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, "手机号格式不正确")
        }

        //验证验证码是否有效
        def verifyCodeObj = verifyCodeSrv.getVerifyCode(phoneNumber)
        if (verifyCodeObj) {
            if (verifyCodeObj.verifyCode != verifyCode) {
                return apiResult(SC_INTERNAL_SERVER_ERROR, "验证码不正确")
            }
            def interval = System.currentTimeMillis() - verifyCodeObj.createTime.getTime()
            log.debug("interval: {}, EXPIRY_INTERVAL: {} ", interval, EXPIRY_INTERVAL)
            if (interval > EXPIRY_INTERVAL) {
                // todo very high 确定返回码
                return apiResult(SC_INTERNAL_SERVER_ERROR, "验证码已过期")
            }
        } else {
            return apiResult(SC_INTERNAL_SERVER_ERROR, "验证码不存在")
        }

        //验证码正确,验证手机号是否已存在，验证是否有以此手机号作为用户名的用户
        def user = accountService.getUserByPhoneNumber(phoneNumber)
        String uName = phoneNumber
        def userRegistWithPhone = accountService.getUserByUserName(uName)//以此手机号作为用户名注册过的用户
        if (userRegistWithPhone && uName != userRegistWithPhone.phoneNumber){
            return apiResult(SC_INTERNAL_SERVER_ERROR, "用户名已存在！")
        }
        if (user || (userRegistWithPhone && uName == userRegistWithPhone.phoneNumber)) {
            //此手机号存在系统中，执行登录操作
            def res = accountService.getUserById(user ? user.userId : (userRegistWithPhone ? userRegistWithPhone.userId : 0L))
            if (res.status == HttpStatus.SC_OK){
                user = res.msg
                return login(user.userName, user.password, wechatCode)
            }else {
                return res
            }
        }

        // 注册账户 角色类型“互联网用户”开通方式“移动端注册”用户数据统计记入新增注册用户
        String userName = phoneNumber
        String password = StringUtils.getCharAndNumr(6)
        try {
            accountService.accountRegister(userName, password, phoneNumber, "试用", accountService.getDefAppVersion().key, DateUitl.addDay(new Date(), 90),
                    "", Agent.AGENT_BJJ_ID, "0", "0", AccountOpenTypeEnum.wechatRegist.key, false)
            // 删除验证码
            verifyCodeSrv.removeVerifyCode(phoneNumber)
        }catch (Exception e){
            log.debug("registNumber: {}, exception: {} ", phoneNumber, "注册异常")
        }
        //开通账户后通过短信发送用户名和密码给用户
        verifyCodeSrv.sendLoginMsg(phoneNumber, userName, password)
        return apiResult([
                data : password,
                regMsg: "success"
        ])
    }

    /**
     * 微信小程序登录
     * @param userName
     * @param password
     * @return
     */
    @RequestMapping(value = "/wechat/login", method = RequestMethod.POST)
    @ResponseBody
    public Map wechatLogin(
            @RequestParam(value = "username", required = false, defaultValue = "") String username,
            @RequestParam(value = "password", required = false, defaultValue = "") String password,
            @RequestParam(value = "wechatCode", required = true) String wechatCode
    ) {
        return login(username, password, wechatCode)
    }

    Map login(String username, String password, String wechatCode){
        def res = wechatMinaService.getOpenInfo(wechatCode)
        if(res.is('errcode')) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, res.errmsg as String)
        }
        String openId = res.openid as String

        def user = accountService.wechatLogin(username, password)
        if (user) {

            if(!(user.valid)) {
                return apiResult(SC_NOT_FOUND, "账号不存在！")
            }
            if (user.expDate.getTime() < System.currentTimeMillis()) {
                return apiResult(SC_FORBIDDEN, "您的账号已到期，如需继续使用，请联系客服！")
            }


            if (user.openId && openId != user.openId){
                return apiResult(SC_FORBIDDEN, "此账号已绑定其他微信号，请注册新账号")
            }

            //绑定微信code，先要查询这个code是否已经被用户绑定过了
            if (!user.openId){
                def userWithOpenId = accountService.getUserByOpenId(openId)
                if (userWithOpenId){
                    return apiResult(SC_FORBIDDEN, "此账号已绑定其他微信号，请注册新账号")
                }
                accountService.bindWechatOpenId(user.userId, openId)
            }

            return apiResult([
                    userId           : user.userId,
                    orgId            : user.orgId,
                    teamId           : user.teamId,
                    sid              : user.sid,
                    userName         : user.userName,
                    nickName         : user.nickName,
                    avatar           : user.avatar,
                    isSitesInited    : user.isSitesInited,
                    expDate          : user.expDate
            ])

        } else {
            return apiResult(SC_NOT_FOUND, "用户名或密码不正确！")
        }
    }

    @RequestMapping(value = '/wechat/getUserInfo', method = RequestMethod.GET)
    @ResponseBody
    Object getOpenInfo(
            @RequestParam String code
    ) {
        def res = wechatMinaService.getOpenInfo(code)

        if(res.is('errcode')) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, res.errmsg as String)
        }
        String openId = res.openid as String

        def user = accountService.getUserByOpenId(openId)
        if (user){
            if(!(user.valid)) {
                return apiResult(SC_NOT_FOUND, "账号不存在！")
            }
            if (user.expDate.getTime() < System.currentTimeMillis()) {
                return apiResult(SC_FORBIDDEN, "您的账号已到期，如需继续使用，请联系客服！")
            }

            def session = accountService.getWechatUserSession(user.userId as long)
            if(!session) {
                return apiResult(SC_UNAUTHORIZED, "无效的会话！")
            }

            return apiResult([
                    userId           : user.id,
                    orgId            : user.orgId,
                    teamId           : user.teamId,
                    sid              : session.sid as String,
                    userName         : user.userName,
                    nickName         : user.nickName,
                    avatar           : user.avatar,
                    isSitesInited    : user.isSitesInited,
                    expDate          : user.expDate
            ])
        } else {
            return apiResult(SC_NOT_FOUND, "用户名或密码不正确！")
        }
    }

    /**
     * 小程序端退出登录，解除绑定
     * @param userId
     * @return
     */
    @RequestMapping(value = "wechat/logout", method = RequestMethod.POST)
    @ResponseBody
    public Map logout(
            @CurrentUserId userId
    ) {
        accountService.logoutWechat(userId)
        return apiResult([:])
    }

}
