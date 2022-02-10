package com.istar.mediabroken.ecloud

import com.alibaba.fastjson.JSONObject
import com.istar.mediabroken.entity.account.Account
import com.istar.mediabroken.repo.account.AccountRepo
import com.istar.mediabroken.service.account.AccountService
import com.istar.mediabroken.service.account.OrganizationService
import com.istar.mediabroken.service.account.UserRoleService
import com.istar.mediabroken.service.ecloud.EcloudErrorCodeEnum
import com.istar.mediabroken.service.ecloud.EcloudOAuthService
import com.istar.mediabroken.service.ecloud.EcloudService
import com.istar.mediabroken.service.ecloud.EcloudUserService
import com.istar.mediabroken.utils.CookieUtil
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Author : YCSnail
 * Date   : 2018-04-18
 * Email  : liyancai1986@163.com
 */
@Controller(value = "ecloud/indexController")
@RequestMapping(value = "/api/ecloud")
@Slf4j
class IndexController {

    @Value('${ecloud.app_id}')
    String appId

    @Autowired
    private EcloudOAuthService ecloudOAuthSrv
    @Autowired
    private EcloudUserService ecloudUserSrv
    @Autowired
    private EcloudService ecloudSrv
    @Autowired
    private AccountService accountSrv
    @Autowired
    private AccountRepo accountRepo
    @Autowired
    private UserRoleService userRoleService
    @Autowired
    private OrganizationService organizationSrv

    /**
     * 单点登录首页   /main.html
     * /api/ecloud/index?appId={appId}&packagecode={PACKAGECODE}&code={CODE}
     * @param code
     * @param appId
     * @param packagecode
     * @return
     */
    @RequestMapping(value = "/index", method = [RequestMethod.GET, RequestMethod.POST])
    public String index(
            ModelMap model,
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam String code,
            @RequestParam String appId,
            @RequestParam(value = "packagecode") String packageCode    //资费编码
    ){

        log.info(['ecloud', 'index', code, appId, packageCode].join(':::') as String)

        //验证是否是移动云应用
        if(!this.appId.equals(appId)) {
            model.addAttribute('msg', '应用ID配置错误！')
            return "ecloud/index"
        }

        //获取访问令牌
        def tokenInfo = ecloudOAuthSrv.getOAuthToken(code)
        if(!tokenInfo.access_token){
            model.addAttribute('msg', tokenInfo.msg + '(' + tokenInfo.code +')')
            return "ecloud/index"
        }


        //根据令牌和用户ID，请求出完整的用户信息
        def ecloudUserInfo = ecloudUserSrv.getEcloudUserInfo(tokenInfo.access_token as String, tokenInfo.uid as int)

        log.info(['ecloud', 'index', 'ecloudUserInfo', ecloudUserInfo].join(':::') as String)

        //根据移动云用户信息获取bjj用户信息
        def accountInfo = accountSrv.getUserByUserName(Account.getEcloudUsername(ecloudUserInfo.userid as int))

        if(!accountInfo) {
            model.addAttribute('msg', '账号无效或未授权！')
            return "ecloud/index"
        }

        Account account = accountSrv.getUserInfoById(accountInfo.userId)

        if(!account?.valid) {
            model.addAttribute('msg', '账号无效！')
            return "ecloud/index"
        }

        //登录操作
        accountRepo.disableSession(account.id)
        def sid = accountRepo.addSession(account.id)
        String roleName = userRoleService.getRoleNameById(account.id)

        CookieUtil.addCookies(response, request, [
                "sid"               : sid,
                "userName"          : account.userName,
                "nickName"          : account.nickName,
                "avatar"            : account.avatar,
                "isSitesInited"     : account.isSitesInited.toString(),
                "isRecommandInited" : account.isRecommandInited.toString(),
                "isCompileInited"   : account.isCompileInited.toString(),
                "roleName"          : roleName
        ])

        if(!account.isSitesInited) {
            return "redirect:/account/init.html";
        }

        return "redirect:/main.html";
    }

    /**
     * 企业业务开通与变更接口 (SaaS0001)
     * /api/ecloud/org/create?timestamp={TIMESTAMP}&sign={SIGN}
     * @param timestamp
     * @param sign
     * @param data
     * @return
     */
    @RequestMapping(value = "/org/create", method = [RequestMethod.POST])
    @ResponseBody
    Object createOrg(
            @RequestParam long timestamp,
            @RequestParam String sign,
            @RequestBody def data
    ){
        //验证sign
        if(!ecloudSrv.verifySign(timestamp, sign)) {
            return EcloudErrorCodeEnum.SIGN_ERROR.toMap()
        }

        log.info(['ecloud', 'SaaS0001', JSONObject.toJSONString(data)].join(':::') as String)

        try {
            // 开通机构并开通机构账号
            organizationSrv.createOrg4Ecloud(data)

            return [
                    result  : true,
                    errmsg  : ''
            ]
        } catch (Exception e) {
            log.error(e)
            return EcloudErrorCodeEnum.ERROR.toMap()
        }
    }

    /**
     * 企业业务状态变更接口(SaaS0002)
     * /api/ecloud/org/cancel?timestamp={TIMESTAMP}&sign={SIGN}
     * @param timestamp
     * @param sign
     * @param data
     * @return
     */
    @RequestMapping(value = "/org/cancel", method = RequestMethod.POST)
    @ResponseBody
    Object cancelOrg(
            @RequestParam long timestamp,
            @RequestParam String sign,
            @RequestBody def data
    ){

        //验证sign
        if(!ecloudSrv.verifySign(timestamp, sign)) {
            return EcloudErrorCodeEnum.SIGN_ERROR.toMap()
        }

        log.info(['ecloud', 'SaaS0002', JSONObject.toJSONString(data)].join(':::') as String)

        return [
                result  : true,
                errmsg  : ''
        ]
    }

    /**
     * 成员业务开通与变更接口 (SaaS0003)
     * /api/ecloud/account/create?timestamp={TIMESTAMP}&sign={SIGN}
     * @param timestamp
     * @param sign
     * @param data
     * @return
     */
    @RequestMapping(value = "/account/create", method = RequestMethod.POST)
    @ResponseBody
    Object createAccount(
            @RequestParam long timestamp,
            @RequestParam String sign,
            @RequestBody def data
    ){

        //验证sign
        if(!ecloudSrv.verifySign(timestamp, sign)) {
            return EcloudErrorCodeEnum.SIGN_ERROR.toMap()
        }

        log.info(['ecloud', 'SaaS0003', JSONObject.toJSONString(data)].join(':::') as String)

        try {
            // 开通账号
            accountSrv.createOrgUser4Ecloud(data)

            return [
                    success  : true,
                    errmsg  : ''
            ]
        } catch (Exception e) {
            log.error(e)
            return EcloudErrorCodeEnum.ERROR.toMap()
        }
    }

    /**
     * 成员业务状态变更接口(SaaS0004)
     * /api/ecloud/account/cancel?timestamp={TIMESTAMP}&sign={SIGN}
     * @param timestamp
     * @param sign
     * @param data
     * @return
     */
    @RequestMapping(value = "/account/cancel", method = RequestMethod.POST)
    @ResponseBody
    Object cancelAccount(
            @RequestParam long timestamp,
            @RequestParam String sign,
            @RequestBody def data
    ){

        //验证sign
        if(!ecloudSrv.verifySign(timestamp, sign)) {
            return EcloudErrorCodeEnum.SIGN_ERROR.toMap()
        }

        log.info(['ecloud', 'SaaS0004', JSONObject.toJSONString(data)].join(':::') as String)

        try {
            // 开通账号
            accountSrv.cancelOrgUser4Ecloud(data)

            return [
                    success  : true,
                    errmsg  : ''
            ]
        } catch (Exception e) {
            log.error(e)
            return EcloudErrorCodeEnum.ERROR.toMap()
        }
    }

    /**
     * 业务参数校验接口(SaaS0005)
     * @param timestamp
     * @param sign
     * @param data
     * @return
     */
    @RequestMapping(value = "/validate", method = RequestMethod.POST)
    @ResponseBody
    Object validateData(
            @RequestParam long timestamp,
            @RequestParam String sign,
            @RequestBody def data
    ){

        //验证sign
        if(!ecloudSrv.verifySign(timestamp, sign)) {
            return EcloudErrorCodeEnum.SIGN_ERROR.toMap()
        }

        return [
                result  : true,
                errmsg  : ''
        ]
    }

    /**
     * 数据变更通知接口 (SaaS3001)
     * @param timestamp
     * @param sign
     * @param data
     * @return
     */
    @RequestMapping(value = "/notify", method = RequestMethod.POST)
    @ResponseBody
    Object notify(
            @RequestParam long timestamp,
            @RequestParam String sign,
            @RequestBody def data
    ){


        return [
                result  : true,
                errmsg  : ''
        ]
    }


}
