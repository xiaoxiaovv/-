package com.istar.mediabroken.api.account

import com.istar.mediabroken.api.CheckPrivilege
import com.istar.mediabroken.api.CurrentUser
import com.istar.mediabroken.api.CurrentUserId
import com.istar.mediabroken.api.NotCheckExpiry
import com.istar.mediabroken.entity.LoginUser
import com.istar.mediabroken.entity.account.Account
import com.istar.mediabroken.entity.account.AccountOpenTypeEnum
import com.istar.mediabroken.entity.account.LoginSourceEnum
import com.istar.mediabroken.entity.account.Privilege
import com.istar.mediabroken.entity.account.Role
import com.istar.mediabroken.entity.app.Agent
import com.istar.mediabroken.service.CaptureService
import com.istar.mediabroken.service.VerifyCodeService
import com.istar.mediabroken.service.account.AccountService
import com.istar.mediabroken.service.account.SimulateUserSecretKeyService
import com.istar.mediabroken.service.account.UserRoleService
import com.istar.mediabroken.service.app.AgentService
import com.istar.mediabroken.service.captcha.CaptchaService
import com.istar.mediabroken.service.captcha.LineCaptcha
import com.istar.mediabroken.service.system.UserBehaviorService
import com.istar.mediabroken.utils.DateUitl
import com.istar.mediabroken.utils.StringUtils
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.time.DateFormatUtils
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import static com.istar.mediabroken.api.ApiResult.apiResult
import static org.apache.http.HttpStatus.*
import static org.springframework.web.bind.annotation.RequestMethod.PUT

@Controller
@Slf4j
@RequestMapping(value = "/api/account")
public class AccountApiController {
    @Value("env")
    String env
    @Autowired
    AccountService accountService
    @Autowired
    CaptureService captureSrv
    @Autowired
    VerifyCodeService verifyCodeSrv
    @Autowired
    CaptchaService captchaSrv
    @Autowired
    AgentService agentService
    @Autowired
    SimulateUserSecretKeyService simulateUserSecretKeyService
    @Autowired
    private UserBehaviorService userBehaviorSrv
    @Autowired
    UserRoleService userRoleService


    int EXPIRY_INTERVAL = 10 * 60 * 1000
    int CAPTCHA_CODE_COUNT = 4
    int CODE_VALIDITY = 1 * 60 * 1000


    @RequestMapping(value = "/authority", method = RequestMethod.GET)
    @ResponseBody
    public Map getAccountAuthority(
            @CurrentUser LoginUser user
    ) {
        int img = 0;
        int video = 0;
        int tag = 0;
        Map map = new HashMap();
        def role = accountService.getAccountRole(user.userId)
        def name = userRoleService.getRoleByAgentIdAndRoleName(user.agentId, "全媒体机构用户")
        def nameMag = userRoleService.getRoleByAgentIdAndRoleName(user.agentId, "全媒体机构管理员")
        String roleId = name.getId()
        String roleIdMag = nameMag.getId()
        if (role) {
            //视频的机构管理员和视频机构用户都有权限
            if (org.apache.commons.lang3.StringUtils.equals(role.roleId, roleId) || org.apache.commons.lang3.StringUtils.equals(role.roleId, roleIdMag)) {
                img = 1;
                video = 1;
                tag = 1;
                map.put("img", img)
                map.put("video", video)
                map.put("tag", tag)
                return apiResult(map)
            }
        }
        map.put("img", img)
        map.put("video", video)
        map.put("tag", tag)
        return apiResult(map)
    }
    /*
     *
     * 获取手机验证码
     *
     * 服务器返回
     *  <pre>
     *  {
     *      status: 200,   // 500, 验证码已存在,未到过期时间
     *      verifyCode: '手机验证码'
     *  }
     *  </pre>
     */

    @RequestMapping(value = "/verifyCode", method = RequestMethod.GET)
    @ResponseBody
    public Map getVerificationCode(
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam(value = "mina", required = false, defaultValue = "false") boolean mina
    ) {
        if (!phoneNumber) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, "手机号不能为空")
        }
        if (StringUtils.isMobileNumber(phoneNumber)) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, "手机号格式不正确")
        }
        if (!mina){
            def user = accountService.getUserByPhoneNumber(phoneNumber)
            if (user) {
                // todo very high 确定返回码
                return apiResult(SC_INTERNAL_SERVER_ERROR, "手机号已被注册！")
            }
        }
        def verifyCodeObj = verifyCodeSrv.getVerifyCode(phoneNumber)
        if (verifyCodeObj) {
            def interval = System.currentTimeMillis() - verifyCodeObj.createTime.getTime()
            log.debug("interval: {}", interval)
            if (interval < CODE_VALIDITY) {
                // todo very high 确定返回码
                return apiResult(SC_INTERNAL_SERVER_ERROR, "验证码已存在,请稍候再试")
            }
        }

        verifyCodeSrv.sendVerifyCode(phoneNumber)
        return apiResult()
    }

    /**
     * 查询验证码临时接口，为产品同事使用
     * @param phone
     * @param username
     * @return
     */
    @RequestMapping(value = "/verifyCode/query", method = RequestMethod.GET)
    @ResponseBody
    Object getVerifyCode(
            @RequestParam String phone,
            @RequestParam(value = 'username', required = false, defaultValue = '') String username
    ) {
        def verifyCodeObj = username ? verifyCodeSrv.getVerifyCode(phone, username) : verifyCodeSrv.getVerifyCode(phone)

        if (verifyCodeObj) {
            return apiResult([
                    query     : verifyCodeObj.phoneNumber,
                    verifyCode: verifyCodeObj.verifyCode,
                    createTime: DateFormatUtils.format(verifyCodeObj.createTime, 'yyyy-MM-dd HH:mm:ss')
            ])
        } else {
            return apiResult(HttpStatus.SC_NOT_FOUND, '验证码不存在！')
        }
    }

    /*
     *
     * 注册新用户
     *
     * 服务器返回
     *  <pre>
     *  {
     *      status: 200,
     *      sid: 'session id'
     *  }
     *  </pre>
     */

    @RequestMapping(value = "/registry", method = RequestMethod.POST)
    @ResponseBody
    public Map registry(
            HttpServletRequest request,
            @RequestParam("userName") String userName,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("company") String company,
            @RequestParam("verifyCode") String verifyCode
    ) {
        /* if (env == ENV_ONLINE) {
             return apiResult(SC_INTERNAL_SERVER_ERROR, "注册功能暂时不开放")
         }*/
        if (userName.length() <= 0) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "用户名不能为空")
        }
        if (userName.length() > 30) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "用户名过长")
        }
        if (!StringUtils.isLegitimate(userName)) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "用户名仅支持英文、数字、下划线")
        }
        // todo high 通过捕捉异常的方式来返回错误码
        def user = accountService.getUserByUserName(userName)
        if (user) {
            // todo very high 确定返回码
            return apiResult(SC_INTERNAL_SERVER_ERROR, "用户名已存在！")
        }
        if (!password || !confirmPassword) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "密码不能为空")
        }
        if (!StringUtils.securityPwd(password)) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "密码6-18个字符，支持英文、数字、下划线")
        }
        if (password != confirmPassword) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "密码和确认密码不一致")
        }

        if (!phoneNumber) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, "手机号不能为空")
        }
        if (StringUtils.isMobileNumber(phoneNumber)) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, "手机号格式不正确")
        }
        // todo very high 验证手机号已存在
        user = accountService.getUserByPhoneNumber(phoneNumber)
        if (user) {
            // todo very high 确定返回码
            return apiResult(SC_INTERNAL_SERVER_ERROR, "手机号已被注册！")
        }
        //公司名称校验
        if (company) {
            if (StringUtils.isCheckName(company)) {
                if (company.length() < 2 || company.length() > 30) {
                    return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "单位或媒体名称仅支持2-30位数字、字母、中文")
                }
            }
        }
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

        // todo 要考虑创建时已存在的情况(重新填写注册)
//        accountService.register(userName, password, phoneNumber)
        //如何判断是如何注册的
        Agent agent = agentService.getAgent(request)
        def agentId = agent.id
        accountService.accountRegister(userName, password, phoneNumber, "试用",
                accountService.getDefAppVersion().key, DateUitl.addDay(new Date(), 90), "", agentId, "0", "0", AccountOpenTypeEnum.register.key,true)
        // 删除验证码
        verifyCodeSrv.removeVerifyCode(phoneNumber)

        // todo veryhigh 删除登录功能
        def newUser = accountService.login(request, userName, password)
        return apiResult([sid: newUser.sid])
    }

    /*
     *
     * 登陆
     *
     * 服务器返回
     *  <pre>
     *  {
     *      status: 200,
     *      sid: 'session id'
     *  }
     *  </pre>
     */

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public Map login(
            HttpServletRequest request,
            @RequestParam("userName") String userName,
            @RequestParam("password") String password,
            @RequestParam(value = "key", required = true) String key,
            @RequestParam("verifyCode") String verifyCode,
            @RequestParam(value = "debug", required = false, defaultValue = '0') int debug
    ) {
        //验证图片验证码
        if (org.apache.commons.lang3.StringUtils.isBlank(verifyCode) || verifyCode.trim().length() != CAPTCHA_CODE_COUNT) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, "验证码不正确！")
        }

        def captcha = captchaSrv.getCaptcha(key)

        if('online'.equals(env) || debug == 0) {        //生成环境 或者 debug模式不开启时，需要验证图片验证码
            if (!captcha ) {
                return apiResult(SC_INTERNAL_SERVER_ERROR, "请输入正确的验证码！")
            }

            if (!(verifyCode.toLowerCase().equals(captcha.verifyCode))) {
                //如果以上基本其他信息均验证通过，但是验证码输入不正确时，刷新页面key值，并重新生成验证码
                captchaSrv.deleteCaptcha(key)
                return apiResult(SC_INTERNAL_SERVER_ERROR, [
                        msg: "验证码不正确！",
                        key: UUID.randomUUID().toString()
                ])
            }
        }

        if(userName.startsWith(Account.ECLOUD_USERNAME_PREFIX)) {
            return apiResult(SC_NOT_FOUND, "用户不存在！")
        }

        def user = accountService.login(request, userName, password)
        if (user) {
            if (user.valid) {
                if (user.expDate.getTime() < System.currentTimeMillis()) {
                    return apiResult(SC_FORBIDDEN, "您的账号已到期，如需继续使用，请联系客服！")
                }else {
                    captchaSrv.deleteCaptcha(key)
                    return apiResult([
                            userId           : user.userId,
                            orgId            : user.orgId,
                            teamId           : user.teamId,
                            sid              : user.sid,
                            userName         : user.userName,
                            phoneNumberTips : user.phoneNumberTips,
                            nickName         : user.nickName,
                            avatar           : user.avatar,
                            isSitesInited    : user.isSitesInited,
                            isRecommandInited: user.isRecommandInited,
                            isCompileInited  : user.isCompileInited,
                            isPwdModified    : user.isPwdModified,
                            roleName         : user.roleName,
                            loginSource      : LoginSourceEnum.userLogin.key
                    ])
                }
            } else {
                return apiResult(SC_NOT_FOUND, "账号不存在！")
            }

        } else {
            return apiResult(SC_NOT_FOUND, "用户名或密码不正确！")
        }
    }

    /*
     *
     * 登陆
     *
     * 服务器返回
     *  <pre>
     *  {
     *      status: 200,
     *      sid: 'session id'
     *  }
     *  </pre>
     */

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    @ResponseBody
    public Map logout(
            @CookieValue("sid") String sid,
            HttpServletResponse response,
            @RequestParam(value = "loginSource", required = false, defaultValue = "2") int loginSource
    ) {
        Cookie newCookie = new Cookie("sid", null)
        newCookie.setMaxAge(0) //立即删除型
        newCookie.setPath('/')
        response.addCookie(newCookie); //重新写入，将覆盖之前的

        accountService.logout(sid, loginSource)
        return apiResult([:])
    }

    /*
     *
     * 登陆
     *
     * 服务器返回
     *  <pre>
     *  {
     *      status: 200,
     *      sid: 'session id'
     *  }
     *  </pre>
     */

    @RequestMapping(value = "/sidValidation", method = RequestMethod.POST)
    @ResponseBody
    public Map login(@CurrentUserId Long userId) {
        log.debug("{}", userId)
        return apiResult([userId: 1])
    }

    /*
     *
     * 修改密码
     *
     * 服务器返回
     *  <pre>
     *  {
     *      status: 200,
     *      msg: '错误消息'
     *  }
     *  </pre>
     */

    @RequestMapping(value = "/password", method = RequestMethod.PUT)
    @ResponseBody
    public Map modifyPassword(
            @CurrentUserId Long userId,
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword) {
        if (!oldPassword || !newPassword || !confirmPassword) {
            return apiResult(status: 1, msg: "密码不能为空")
        } else if (newPassword != confirmPassword) {
            return apiResult(status: 2, msg: "密码和确认密码不一致")
        }
        if (!StringUtils.securityPwd(newPassword)) {
            return apiResult(status: 5, msg: "密码6-18个字符，支持英文、数字、下划线")
        }
        def isValid = accountService.validatePassword(userId, oldPassword)
        if (!isValid) {
            return apiResult(status: 3, msg: "原始密码不正确")
        }

        def result = accountService.modifyPassword(userId, newPassword)
        return result
    }

    @RequestMapping(value = "/passwordStatus", method = RequestMethod.PUT)
    @ResponseBody
    public Map modifyPasswordStatus(
            @CurrentUserId Long userId
    ) {
        def result = accountService.modifyPasswordStatus(userId)
        return result
    }
    /*
    *
    * 获取基本信息
    *
    * 服务器返回
    *  <pre>
    *  {
    *      status: 200,
                userName: "用户名",
                gender: int, // 1: 男, 2: 女, 默认为0, 未知
                birthdayYear: 1970,   //默认为0
                birthdayMonth: 1,  //默认为0
                birthdayDate: 1,   //默认为0
                company: "所属媒体",  //可为空字符串
                duty: 1 //1. 普通编辑, 2. 新闻编辑   // 默认为1
    *
    *  }
    *  </pre>
    */

    @RequestMapping(value = "/user/basic", method = RequestMethod.GET)
    @ResponseBody
    public Map getBasicInfo(
            @CurrentUserId Long userId) {
        def result = accountService.getUserById(userId)
        return result
    }

    @RequestMapping(value = "/user/duty", method = RequestMethod.GET)
    public Map getDutyList(
            @CurrentUserId Long userId
    ) {
        def result = accountService.getDutyList()
        return result
    }

    /*
    *
    * 修改基本信息
    *
    */

    @RequestMapping(value = "/user/basic", method = RequestMethod.PUT)
    @ResponseBody
    public Map modifyBasicInfo(
            @CurrentUserId Long userId,
            @RequestParam(value = "avatar", required = false, defaultValue = "") String avatar,
            @RequestParam(value = "nickName", required = false, defaultValue = "") String nickName,
            @RequestParam(value = "gender", required = false, defaultValue = "0") int gender,
            @RequestParam(value = "birthday", required = false, defaultValue = "") String birthday,
            @RequestParam(value = "company", required = false, defaultValue = "") String company,
            @RequestParam(value = "duty", required = false, defaultValue = "1") int duty,
            @RequestParam(value = "qqNumber", required = false, defaultValue = "") String qqNumber,
            @RequestParam(value = "weChatNumber", required = false, defaultValue = "") String weChatNumber,
            @RequestParam(value = "email", required = false, defaultValue = "") String email
    ) {
        if (birthday && birthday.length() > 10) {
            return apiResult(status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: '出生日期格式如1990-01-01')
        }
        def result = accountService.modifyUser(userId, avatar, nickName, gender, birthday, company, duty, qqNumber, weChatNumber, email)
        return result
    }

    /*
    *
    * 获取基本信息
    *
    * 服务器返回
    *  <pre>
    *  {
    *      status: 200,
                userName: "用户名",
                nickName: "昵称",
                phoneNumber: "绑定的手机号码"
    *
    *  }
    *  </pre>
    */

    @RequestMapping(value = "/user/account", method = RequestMethod.GET)
    @ResponseBody
    public Map getAccountInfo(
            @CurrentUserId Long userId) {

        def result = accountService.getUserById(userId)
        return result
    }

    /*
     *
     * 忘记密码后,获取手机验证码
     *
     * 服务器返回
     *  <pre>
     *  {
     *      status: 200,   // 500, 验证码已存在,未到过期时间
     *  }
     *  </pre>
     */

    @RequestMapping(value = "/forgotPassword/verifyCode", method = RequestMethod.GET)
    @ResponseBody
    public Map getVerifyCodeForRetrievePassword(
            @RequestParam("userName") String userName,
            @RequestParam("phoneNumber") String phoneNumber
    ) {
        if (userName.length() <= 0) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "用户名不能为空")
        }
        if (userName.length() > 30) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "用户名过长")
        }
        if (!StringUtils.isLegitimate(userName)) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "用户名仅支持英文、数字、下划线")
        }
        def user = accountService.getUserByUserName(userName)
        if (!user) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, "用户不存在")
        }
        if (!phoneNumber) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, "手机号不能为空")
        }
        if (StringUtils.isMobileNumber(phoneNumber)) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, "手机号格式不正确")
        }
        if (user.phoneNumber != phoneNumber) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, "用户名和手机号不匹配")
        }

        def verifyCodeObj = verifyCodeSrv.getVerifyCode(phoneNumber, userName)
        if (verifyCodeObj) {
            def interval = System.currentTimeMillis() - verifyCodeObj.createTime.getTime()
            if (interval < CODE_VALIDITY) {
                return apiResult(SC_INTERNAL_SERVER_ERROR, "验证码已存在,请稍候再试")
            }
        }

        def verifyCode = verifyCodeSrv.generateVerifyCode(phoneNumber, userName)
        verifyCodeSrv.sendVerifyCode(phoneNumber, verifyCode.verifyCode)
        return apiResult()
    }

    /*
     *
     * 获取手机验证码后,点击下一步时执行
     *
     * 服务器返回
     *  <pre>
     *  {
     *      status: 200,   // 500, 验证码已存在,未到过期时间
     *  }
     *  </pre>
     */

    @RequestMapping(value = "/forgotPassword/validateVerifyCode", method = RequestMethod.GET)
    @ResponseBody
    public Map resetPasswordForRetrievePassword(
            @RequestParam("userName") String userName,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("verifyCode") String verifyCode) {
        if (userName.length() <= 0) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "用户名不能为空")
        }
        if (userName.length() > 30) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "用户名过长")
        }
        if (!StringUtils.isLegitimate(userName)) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "用户名仅支持英文、数字、下划线")
        }
        def user = accountService.getUserByUserName(userName)
        if (!user) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, "用户不存在")
        }
        if (!phoneNumber) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, "手机号不能为空")
        }
        if (StringUtils.isMobileNumber(phoneNumber)) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, "手机号格式不正确")
        }
        if (user.phoneNumber != phoneNumber) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, "登录名和手机号不匹配")
        }
        def verifyCodeObj = verifyCodeSrv.getVerifyCode(phoneNumber, userName)


        if (verifyCodeObj) {
            if (verifyCodeObj.verifyCode != verifyCode) {
                return apiResult(SC_INTERNAL_SERVER_ERROR, "验证码不正确")
            }

            def interval = System.currentTimeMillis() - verifyCodeObj.createTime.getTime()
            if (interval > EXPIRY_INTERVAL) {
                return apiResult(SC_INTERNAL_SERVER_ERROR, "验证码已过期")
            }
        } else {
            return apiResult(SC_INTERNAL_SERVER_ERROR, "验证码不存在")
        }

        return apiResult()
    }

    /*
     *
     * 修改密码,并登陆
     *
     * 服务器返回
     *  <pre>
     *  {
     *      status: 200,   // 500, 验证码已存在,未到过期时间
     *  }
     *  </pre>
     */

    @RequestMapping(value = "/forgotPassword/resetPassword", method = RequestMethod.PUT)
    @ResponseBody
    public Map resetPasswordForRetrievePassword(
            HttpServletRequest request,
            @RequestParam("userName") String userName,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("verifyCode") String verifyCode
    ) {
        if (userName.length() <= 0) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "用户名不能为空")
        }
        if (userName.length() > 30) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "用户名过长")
        }
        if (!StringUtils.isLegitimate(userName)) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "用户名仅支持英文、数字、下划线")
        }
        if (!password || !confirmPassword) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "密码不能为空")
        }
        if (!StringUtils.securityPwd(password)) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "密码6-18个字符，支持英文、数字、下划线")
        }
        if (password != confirmPassword) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "密码和确认密码不一致")
        }

        if (!phoneNumber) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, "手机号不能为空")
        }
        if (StringUtils.isMobileNumber(phoneNumber)) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, "手机号格式不正确")
        }
        // 验证码验证
        def verifyCodeObj = verifyCodeSrv.getVerifyCode(phoneNumber, userName)
        if (verifyCodeObj) {
            if (verifyCodeObj.verifyCode != verifyCode) {
                return apiResult(SC_INTERNAL_SERVER_ERROR, "验证码不正确")
            }

            def interval = System.currentTimeMillis() - verifyCodeObj.createTime.getTime()
            if (interval > EXPIRY_INTERVAL) {
                return apiResult(SC_INTERNAL_SERVER_ERROR, "验证码已过期")
            }
        } else {
            return apiResult(SC_INTERNAL_SERVER_ERROR, "验证码不存在")
        }

        // 修改密码
        def user = accountService.getUserByUserName(userName)
        accountService.modifyPassword(user.userId, password)

        // 删除验证码
        verifyCodeSrv.removeVerifyCode(phoneNumber, userName)

        // todo veryhigh 删除登录功能
        // 登陆
        def newUser = accountService.login(request, userName, password)
        return apiResult([sid: newUser.sid])
    }

    @RequestMapping(value = "/apply", method = RequestMethod.POST)
    @ResponseBody
    public Map saveApplyInfo(
            @RequestParam(value = "name", required = false, defaultValue = "") String name,
            @RequestParam(value = "company", required = false, defaultValue = "") String company,
            @RequestParam(value = "position", required = false, defaultValue = "") String position,
            @RequestParam(value = "mobile", required = false, defaultValue = "") String mobile,
            @RequestParam(value = "email", required = false, defaultValue = "") String email,
            @RequestParam(value = "qq", required = false, defaultValue = "") String qq,
            @RequestParam(value = "message", required = false, defaultValue = "") String message
    ) {
        if (name.equals("") || company.equals("") || mobile.equals("")) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, "信息输入不全")
        }
        def result = accountService.saveApplyInfo(name, company, position,
                mobile, email, qq, message)
        return apiResult([result: result ? "success" : "fail"])
    }

    /**
     * 新手引导完成初始化
     * @param userId
     * @param classification
     * @param area
     * @param duty
     * @return
     */
    @RequestMapping(value = "/initialization", method = [RequestMethod.GET, RequestMethod.POST])
    @ResponseBody
    @NotCheckExpiry
    public Map initializeRecommandData(
            @CurrentUserId Long userId,
            @RequestParam(value = "classification", required = false) String classification,
            @RequestParam(value = "area", required = false, defaultValue = "") String area,
            @RequestParam(value = "duty", required = false, defaultValue = "1") int duty,
            @RequestParam(value = "subjectFlag", required = false, defaultValue = "0") int subjectFlag
    ) {
        String[] classifications = []
        if (classification) {
            classification = URLDecoder.decode(classification,"utf-8")
            classifications = classification.split(",")
        }
        def result = accountService.initializeRecommandData(userId, classifications, area, duty, subjectFlag)
        return result
    }

    /**
     * 热点推荐 引导
     * @param userId
     * @return
     */
    @RequestMapping(value = "recommand/initialization", method = RequestMethod.POST)
    @ResponseBody
    @NotCheckExpiry
    public Map modifyRecommandInited(
            @CurrentUserId Long userId
    ) {
        def result = accountService.modifyRecommandInited(userId)
        return result
    }

    /**
     * 智能组稿 引导
     * @param userId
     * @return
     */
    @RequestMapping(value = "compile/initialization", method = RequestMethod.POST)
    @ResponseBody
    @NotCheckExpiry
    public Map modifyCompileInited(
            @CurrentUserId Long userId
    ) {
        def result = accountService.modifyCompileInited(userId)
        return result
    }

    @CheckPrivilege(privileges = [Privilege.TEAM_MANAGE])
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public Map getAgentAccountList(
            @CurrentUser LoginUser user
    ) {
        def result = accountService.getAccountList(user)
        return apiResult([list: result])
    }

    @CheckPrivilege(privileges = [Privilege.ACCOUNT_MANAGE])
    @RequestMapping(value = "/orgAccount", method = RequestMethod.POST)
    @ResponseBody
    public Map addOrgAccount(
            @CurrentUser LoginUser user,
            @RequestParam(value = "userName", required = true) String userName,
            @RequestParam(value = "password", required = true) String password,
            @RequestParam(value = "realName", required = false, defaultValue = "") String realName,
            @RequestParam(value = "phoneNumber", required = false, defaultValue = "") String phoneNumber,
            @RequestParam(value = "teamId", required = false, defaultValue = "0") String teamId
    ) {
        if (userName.length() <= 0) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "用户名不能为空")
        }
        if (userName.length() > 20) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "用户名过长")
        }
        if (!StringUtils.isLegitimate(userName)) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "用户名仅支持英文、数字、下划线")
        }
        def isAccountExist = accountService.accountIsExist(userName)
        if (isAccountExist) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "用户名已存在");
        }
        if (!password) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "密码不能为空")
        }
        if (!StringUtils.securityPwd(password)) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "密码6-18个字符，支持英文、数字、下划线")
        }
        if (!phoneNumber) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, "手机号不能为空")
        }
        if (StringUtils.isMobileNumber(phoneNumber)) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, "手机号格式不正确")
        }
        if (phoneNumber) {
            def isAccountExist2 = accountService.phoneNumberIsExist(phoneNumber)
            if (isAccountExist2) {
                return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "已有此手机号注册信息");
            }
        }
        def isOverstepOrgUserCount = accountService.isOverstepOrgUserCount(user.agentId, user.orgId)
        if (isOverstepOrgUserCount) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "已达到用户数量上限");
        }

        def loginUser = accountService.getUserInfoById(user.userId)
        def salesId = loginUser.salesId
        if (!salesId) {
            salesId = 0l
        }
        def productType = loginUser.getProductType()
        if (!productType){
            productType = accountService.getDefAppVersion().key
        }
        def addAccountResult = accountService.addOrgUser(loginUser.id,userName, password, phoneNumber, loginUser.getUserType(),
                productType, loginUser.getExpDate(), realName, loginUser.getAgentId(), loginUser.getOrgId(), teamId, salesId, loginUser.getCompany() ?: "")
        if (addAccountResult) {
            apiResult(HttpStatus.SC_OK, "添加成功")
        } else {
            apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "添加失败")
        }
    }

    @CheckPrivilege(privileges = [Privilege.ACCOUNT_MANAGE])
    @RequestMapping(value = "/orgAccount", method = RequestMethod.GET)
    @ResponseBody
    public Map getOrgAccount(
            @CurrentUser LoginUser user,
            @RequestParam(value = "userId", required = true) long userId
    ) {
        if (user.getUserId() == userId) {
            return apiResult(status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "获取帐号信息失败")
        }
        def result = accountService.getOrgUserById(userId)
        if (result) {
            return apiResult(status: HttpStatus.SC_OK, msg: result)
        } else {
            return apiResult(status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "获取帐号信息失败")
        }
    }

    @CheckPrivilege(privileges = [Privilege.ACCOUNT_MANAGE])
    @RequestMapping(value = "/orgAccount", method = RequestMethod.PUT)
    @ResponseBody
    public Map updateOrgAccount(
            @CurrentUser LoginUser user,
            @RequestParam(value = "userId", required = true) long userId,
            @RequestParam(value = "userName", required = true) String userName,
            @RequestParam(value = "password", required = true) String password,
            @RequestParam(value = "realName", required = false, defaultValue = "") String realName,
            @RequestParam(value = "phoneNumber", required = true) String phoneNumber,
            @RequestParam(value = "teamId", required = false, defaultValue = "0") String teamId
    ) {
        if (user.getUserId() == userId) {
            return apiResult(status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "管理员无法被修改")
        }
        if (userName.length() <= 0) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "用户名不能为空")
        }
        if (userName.length() > 20) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "用户名过长")
        }
        if (!StringUtils.isLegitimate(userName)) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "用户名仅支持英文、数字、下划线")
        }
        if (!password) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "密码不能为空")
        }
        if (!StringUtils.securityPwd(password)) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "密码6-18个字符，支持英文、数字、下划线")
        }
        if (!phoneNumber) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, "手机号不能为空")
        }
        if (StringUtils.isMobileNumber(phoneNumber)) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, "手机号格式不正确")
        }
        if (phoneNumber) {
            def isAccountExist2 = accountService.phoneNumberIsExist(phoneNumber)
            if (isAccountExist2 != null && !isAccountExist2.get("userId").equals(userId)) {
                return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "已有此手机号注册信息");
            }
        }
        def result = accountService.updateOrgUser(userId, userName, password, realName, phoneNumber, teamId)
        if (result) {
            return apiResult(status: HttpStatus.SC_OK, msg: "修改成功")
        } else {
            return apiResult(status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "修改失败")
        }
    }

    @CheckPrivilege(privileges = [Privilege.ACCOUNT_MANAGE])
    @RequestMapping(value = "/orgAccount/{userId}", method = RequestMethod.DELETE)
    @ResponseBody
    public Map deleteOrgAccount(
            @CurrentUser LoginUser user,
            @PathVariable long userId
    ) {
        if (user.getUserId() == userId) {
            return apiResult(status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "管理员无法被删除")
        }
        def result = accountService.deleteOrgUser(userId)
        if (result) {
            return apiResult(status: HttpStatus.SC_OK, msg: "删除成功")
        } else {
            return apiResult(status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "删除失败")
        }
    }

    @CheckPrivilege(privileges = [Privilege.ACCOUNT_MANAGE])
    @RequestMapping(value = "/orgUserTeam", method = RequestMethod.PUT)
    @ResponseBody
    public Map modifyOrgUserTeam(
            @CurrentUser LoginUser user,
            @RequestParam(value = "userIds", required = true) String userIds,
            @RequestParam(value = "teamId", required = true) String teamId
    ) {
        def result = accountService.modifyOrgUserTeam(userIds, teamId)
        if (result) {
            return apiResult(status: HttpStatus.SC_OK, msg: "修改成功")
        } else {
            return apiResult(status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "修改失败")
        }
    }

    @ResponseBody
    @RequestMapping(value = "/captcha", method = RequestMethod.GET)
    public Object captcha(
            @RequestParam(value = "w", required = false, defaultValue = "100") int width,
            @RequestParam(value = "h", required = false, defaultValue = "38") int height,
            @RequestParam(value = "k", required = true) String key,
            @RequestParam(value = "ua", required = true) String ua
    ) {

        //定义图形验证码的长、宽、验证码字符数、干扰元素个数
        LineCaptcha captcha = new LineCaptcha(width, height, CAPTCHA_CODE_COUNT, 20);
        String img = "data:image/png;base64," + captcha.getImageBase64()

        captchaSrv.createVerifyCode(key, captcha.code, ua)

        return apiResult([img: img])
    }

    @ResponseBody
    @RequestMapping(value = "/login/status", method = RequestMethod.GET)
    public Object loginStatus(
            HttpServletRequest request
    ) {

        boolean loginStatus = false

        def sid = request.getCookies()?.find { it.name == 'sid' }
        if(sid?.value) {
            def loginUser = accountService.getUserBySessionId(sid.value)
            if(loginUser?.enable) {
                def user = accountService.getUserInfoById(loginUser.userId)
                if(user.valid && user.expDate.getTime() > System.currentTimeMillis()) {
                    loginStatus = true
                }
            }
        }

        return loginStatus ? apiResult() : apiResult(status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "无效的会话！")
    }

    /**
     * 修改手机号码
     * @param userId
     * @param phoneNumber
     * @param verifyCode
     * @return
     */
    @RequestMapping(value = "/phoneNumber", method = PUT)
    @ResponseBody
    Map modifyPhoneNumber(
            @CurrentUserId Long userId,
            @RequestParam(value = "phoneNumber", required = true) String phoneNumber,
            @RequestParam(value = "verifyCode", required = true) String verifyCode
    ) {
        try {
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
            return accountService.modifyPhoneNumber(userId, phoneNumber)
        } catch (Exception e) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: '保存失败'])
        }
    }
}
