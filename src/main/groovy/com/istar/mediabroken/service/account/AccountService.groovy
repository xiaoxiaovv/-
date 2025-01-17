package com.istar.mediabroken.service.account

import com.alibaba.fastjson.JSONObject
import com.istar.mediabroken.entity.AccountProfile
import com.istar.mediabroken.entity.LoginUser
import com.istar.mediabroken.entity.SystemSetting
import com.istar.mediabroken.entity.account.*
import com.istar.mediabroken.entity.app.Agent
import com.istar.mediabroken.entity.capture.KeywordsScopeEnum
import com.istar.mediabroken.entity.capture.Site
import com.istar.mediabroken.entity.capture.SiteDetail
import com.istar.mediabroken.repo.PrimaryKeyRepo
import com.istar.mediabroken.repo.account.AccountRepo
import com.istar.mediabroken.repo.account.AccountVsRoleRepo
import com.istar.mediabroken.repo.account.TeamRepo
import com.istar.mediabroken.repo.account.WechatSessionRepo
import com.istar.mediabroken.repo.admin.SettingRepo
import com.istar.mediabroken.repo.capture.SiteRepo
import com.istar.mediabroken.service.app.AgentService
import com.istar.mediabroken.service.capture.SiteService
import com.istar.mediabroken.service.capture.SubjectService
import com.istar.mediabroken.service.ecloud.EcloudService
import com.istar.mediabroken.utils.DateUitl
import com.istar.mediabroken.utils.UrlUtils
import groovy.util.logging.Slf4j
import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import org.apache.commons.lang3.RandomStringUtils
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import javax.servlet.http.HttpServletRequest
import java.text.SimpleDateFormat

import static com.istar.mediabroken.api.ApiResult.apiResult

@Service
@Slf4j
class AccountService {
    @Autowired
    AccountRepo accountRepo
    @Autowired
    SettingRepo settingRepo
    @Autowired
    SiteRepo siteRepo
    @Autowired
    SubjectService subjectService
    @Autowired
    SiteService siteService
    @Autowired
    UserRoleService userRoleService
    @Autowired
    AgentService agentService
    @Autowired
    TeamRepo teamRepo
    @Autowired
    AccountVsRoleRepo accountVsRoleRepo
    @Autowired
    PrimaryKeyRepo primaryKeyRepo
    @Autowired
    OrganizationService organizationService
    @Autowired
    EcloudService ecloudService
    @Autowired
    WechatSessionRepo wechatSessionRepo
    @Autowired
    SimulateLoginSessionService simulateLoginSessionService



    def getAccountRole(long userId){
        return accountVsRoleRepo.getRoleIdByUserId(userId)
    }

    Map login(HttpServletRequest request, String userName, String password) {
        Agent agent = agentService.getAgent(request)

        if (!agent) return null
        // 使用md5加密
        def user = accountRepo.getUser(userName, password)

        if (!user) return null

        if (!(user.agentId as String).equals(agent.id as String)) return null
        if (user.valid) {
            accountRepo.disableSession(user.userId)
            def sid = accountRepo.addSession(user.userId)
            String roleName = userRoleService.getRoleNameById(user.userId)
            boolean phoneNumberTips = false
            String phoneNumber = user.phoneNumber
            if (!"".equals(phoneNumber) && phoneNumber != null && phoneNumber.startsWith("160000")){
                phoneNumberTips = true
            }
            //记录登录日志到loginLog并更新用户最后登录时间
            accountLogin(userName,password)
            return [
                    userId           : user.userId,
                    agentId          : user.agentId,
                    orgId            : user.orgId,
                    teamId           : user.teamId,
                    sid              : sid,
                    userName         : user.userName,
                    phoneNumberTips : phoneNumberTips,
                    nickName         : user.nickName,
                    avatar           : user.avatar,
                    isSitesInited    : user.isSitesInited,
                    isRecommandInited: user.isRecommandInited,
                    isCompileInited  : user.isCompileInited,
                    isPwdModified    : user.expDate < new Date() ? true : user.isPwdModified,
                    valid            : true,
                    roleName         : roleName,
                    expDate          : user.expDate
            ]
        } else {
            return [valid: user.valid]
        }
    }

    def accountLogin(String userName, String password){
        def user = accountRepo.getUser(userName, password)
        Date accountLoginTime = new Date()
        Date expDate = user.expDate
        if (accountLoginTime.before(expDate)){
            accountRepo.addLoginLog(user.userId)
            accountRepo.updateAccountLoginTime(user.userId)
        }
    }

    def simulateLogin(HttpServletRequest request, long userId, long adminUserId, String adminUsername){
        Agent agent = agentService.getAgent(request)
        if (!agent) return null
        def user = accountRepo.getUserById(userId)
        if (!user) return null
        if (!(user.agentId as String).equals(agent.id as String)) return null
        if (user.valid) {
            simulateLoginSessionService.disableSimulateSession(userId)
            def sid = simulateLoginSessionService.addSimulateLoginSession(userId,adminUserId,adminUsername)
            String roleName = userRoleService.getRoleNameById(userId)
            return [
                    userId           : userId,
                    agentId          : user.agentId,
                    orgId            : user.orgId,
                    teamId           : user.teamId,
                    sid              : sid,
                    userName         : user.userName,
                    nickName         : user.nickName,
                    avatar           : user.avatar,
                    isSitesInited    : user.isSitesInited,
                    isRecommandInited: user.isRecommandInited,
                    isCompileInited  : user.isCompileInited,
                    isPwdModified    : user.expDate < new Date() ? true : user.isPwdModified,
                    valid            : true,
                    roleName         : roleName,
                    expDate          : user.expDate
            ]
        } else {
            return [valid: user.valid]
        }
    }

    def wechatLogin(String userName, String password) {

        def user = null
        if (userName && password) {
            user = accountRepo.getUser(userName, password)
        }

        if (!user) return null

        if (!(user.agentId as String).equals(Agent.AGENT_BJJ_ID)) return null

        if (user.valid) {
            wechatSessionRepo.disableSession(user.userId as long)
            def sid = wechatSessionRepo.addSession(user.userId as long)
            return [
                    userId           : user.userId,
                    agentId          : user.agentId,
                    orgId            : user.orgId,
                    teamId           : user.teamId,
                    sid              : sid,
                    userName         : user.userName,
                    nickName         : user.nickName,
                    avatar           : user.avatar,
                    isSitesInited    : user.isSitesInited,
                    valid            : true,
                    expDate          : user.expDate,
                    openId           : user.openId
            ]
        } else {
            return [valid: user.valid]
        }
    }

    def getWechatUserSession(long userId){
        wechatSessionRepo.getByUserId(userId)
    }

    void bindWechatOpenId(long userId, String openId) {
        boolean result = accountRepo.bindWechatOpenId(userId, openId)
        log.info(result ? "绑定成功" : "绑定失败")
    }

    def getUserByOpenId(String openId){
        return accountRepo.getUserByOpenId(openId)
    }

    Long getUserIdBySessionId(String sid) {
        return accountRepo.getUserIdBySessionId(sid)
    }

    Map getUserBySessionId(String sid) {
        return accountRepo.getUserBySessionId(sid)
    }

    Map getUserByWechatSessionId(String sid) {
        return wechatSessionRepo.getUserBySessionId(sid)
    }

    void logoutWechat(long userId) {
        accountRepo.removeWechatOpenId(userId)
        wechatSessionRepo.disableSession(userId)
    }

    void logout(String sid, int loginSource) {
        if (loginSource == LoginSourceEnum.userLogin.key) {
            accountRepo.deleteSession(sid)
        } else if (loginSource == LoginSourceEnum.adminLogin.key) {
            simulateLoginSessionService.disableSimulateSession(sid)
        }
    }

    Map getUserByUserName(String userName) {
        return accountRepo.getUserByName(userName)
    }

    void register(String userName, String password, String phoneNumber) {
        accountRepo.addUser(userName, password, phoneNumber)
    }

    Map getUserByPhoneNumber(String phoneNumber) {
        return accountRepo.getUserByPhoneNumber(phoneNumber)
    }

    Map modifyPhoneNumber(long userId, String phoneNumber) {
        boolean result = accountRepo.modifyPhoneNumber(userId, phoneNumber)
        if (result) {
            return apiResult(status: HttpStatus.SC_OK, msg: '修改成功')
        } else {
            return apiResult(status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: '修改失败')
        }
    }

    Map modifyPassword(long userId, String password) {
        boolean result = accountRepo.modifyPassword(userId, password)
        if (result) {
            return apiResult(status: HttpStatus.SC_OK, msg: '密码修改成功')
        } else {
            return apiResult(status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: '密码修改失败')
        }
    }

    Map modifyPasswordStatus(long userId) {
        boolean result = accountRepo.modifyPasswordStatus(userId)
        if (result) {
            return apiResult(status: HttpStatus.SC_OK, msg: '密码状态修改成功')
        } else {
            return apiResult(status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: '密码状态修改失败')
        }
    }

    boolean validatePassword(long userId, String password2) {
        // todo 密码和用户信息是否不存在一块为好?
        def password = accountRepo.getPassword(userId)
        return password == password2
    }

    Account getUserInfoById(long userId) {
        //todo-liyc 增加用户状态的处理   //正式、使用、停用、到期、免费、等
        return accountRepo.getUserById(userId)
    }

    Map getUserById(long userId) {
        def account = accountRepo.getUserById(userId)
        if (!account) {
            return apiResult(status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: '没有找到对应的用户信息')
        } else {
            return apiResult(status: HttpStatus.SC_OK, msg: account)
        }
    }

    Map getDutyList() {
        def account = accountRepo.getDutyList()
        if (!account) {
            return apiResult(status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: '没有找到对应的用户职责')
        } else {
            return apiResult(status: HttpStatus.SC_OK, msg: account)
        }
    }

    Map modifyUser(long userId, String avatar, String nickName, int gender, String birthday, String company, int duty, String qqNumber, String weChatNumber, String email) {
        def isAccountExist = accountRepo.getUserById(userId)
        if (!isAccountExist) {
            return apiResult(status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: '没有找到对应的用户信息')
        }
        def account = new Account(
                _id: userId,
                avatar: avatar,
                nickName: nickName,
                gender: gender,
                birthday: birthday,
                company: company,
                duty: duty,
                qqNumber: qqNumber,
                weChatNumber: weChatNumber,
                email: email,
                updateTime: new Date()
        )

        boolean result = accountRepo.modifyUser(userId, account)
        if (result) {
            return apiResult(status: HttpStatus.SC_OK, msg: '修改成功')
        } else {
            return apiResult(status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: '修改失败')
        }
    }

    void modifyIsSitesInited(long userId, boolean isSitesInited) {
        accountRepo.modifyIsSitesInited(userId, isSitesInited)
    }

    Map modifyRecommandInited(long userId) {
        boolean result = accountRepo.modifyRecommandInited(userId)
        if (result) {
            return apiResult(status: HttpStatus.SC_OK, msg: '修改成功')
        } else {
            return apiResult(status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: '修改失败')
        }
    }

    Map modifyCompileInited(long userId) {
        boolean result = accountRepo.modifyCompileInited(userId)
        if (result) {
            return apiResult(status: HttpStatus.SC_OK, msg: '修改成功')
        } else {
            return apiResult(status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: '修改失败')
        }
    }
    /**
     * 保存申请信息
     * @param name
     * @param company
     * @param position
     * @param mobile
     * @param email
     * @param qq
     * @param message
     * @return
     */
    boolean saveApplyInfo(String name, String company, String position,
                          String mobile, String email, String qq, String message) {

        def currTime = new Date();

        def applyInfo = [
                name      : name,
                company   : company,
                position  : position,
                mobile    : mobile,
                email     : email,
                qq        : qq,
                message   : message,
                applyTime : (currTime.getTime() / 1000L) as Long,
                createTime: currTime,
        ]
        return accountRepo.saveApplyInfo(applyInfo)
    }

    Map getCaptureSiteAccountProfile(long userId) {
        AccountProfile accountProfile = accountRepo.getAccountProfileByUser(userId)
        //是否存在，如果不存在则新增一个，如果存在则修改
        SystemSetting map = settingRepo.getSystemSetting("captureSite", "maxSiteCount")
        if (accountProfile != null && accountProfile.captureSite.size() >= map.content.size()) {
            return accountProfile.captureSite
        }
        if (accountProfile == null) {
            accountProfile = new AccountProfile()
            accountProfile.userId = userId
            accountProfile.createTime = new Date();
            accountProfile.captureSite = map.content
            accountProfile.updateTime = new Date();
            accountRepo.saveAccountProfile(accountProfile)
        }
        //从systemSetting中获取
        def account = accountRepo.getUserById(userId)
        String productType = account.productType
        if (!productType) {
            productType = getDefAppVersion().key
        }
        SystemSetting setting = settingRepo.getSystemSetting("appVersion", productType)
        if (setting == null) {
            return [:]
        }
        def captureSite = accountProfile.captureSite
        if (!accountProfile.captureSite.get("maxMediaSiteCount")) {
            def maxMediaSiteCount = setting.content.captureSite.maxMediaSiteCount
            captureSite.maxMediaSiteCount = maxMediaSiteCount
        }
        if (!accountProfile.captureSite.get("maxWechatSiteCount")) {
            def maxWechatSiteCount = setting.content.captureSite.maxWechatSiteCount
            captureSite.maxWechatSiteCount = maxWechatSiteCount
        }
        if (!accountProfile.captureSite.get("maxWeiboSiteCount")) {
            def maxWeiboSiteCount = setting.content.captureSite.maxWeiboSiteCount
            captureSite.maxWeiboSiteCount = maxWeiboSiteCount
        }

        return accountProfile.getCaptureSite()
    }

    Map addUser(String userName, String password, String phoneNumber, String userType, String expDate, String company, String realName, String userArea, int duty, int maxMediaSiteCount, int maxWechatSiteCount) {
        def isAccountExist = accountRepo.getUserByName(userName)
        if (isAccountExist) {
            return apiResult(status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: '已有此用户注册信息')
        }
        def isAccountExist2 = accountRepo.getUserByPhoneNumber(phoneNumber)
        if (isAccountExist2) {
            return apiResult(status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: '已有此手机号注册信息')
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd")
        Date exp = sdf.parse(expDate)
        if (!exp) {
            exp = DateUitl.addDay(new Date(), 7)
        }
        long userId = System.currentTimeMillis()
        def account = new Account(
                _id: userId,
                userName: userName,
                password: password,
                phoneNumber: phoneNumber,
                userType: userType,
                expDate: exp,
                company: company,
                realName: realName,
                userArea: userArea,
                duty: duty,
                orgId: '0',
                createTime: new Date()
        )

        boolean result = accountRepo.addUser(account)
        if (result) {
            //添加角色信息
            userRoleService.addUserRole(userId, "0c4d9b85-d37f-4188-b6e7-ce3970bb819c", userName, userName, new Date(), new Date())
            //添加默认网站数目
            if (maxMediaSiteCount == 0) {
                maxMediaSiteCount = 10
            }
            if (maxWechatSiteCount == 0) {
                maxWechatSiteCount = 10
            }
            Date now = new Date();
            AccountProfile accountProfile = new AccountProfile()
            accountProfile.userId = userId
            accountProfile.createTime = now
            accountProfile.captureSite = ["maxMediaSiteCount" : maxMediaSiteCount,
                                          "maxWechatSiteCount": maxWechatSiteCount]
            accountProfile.updateTime = now
            accountRepo.saveAccountProfile(accountProfile)
            return apiResult(status: HttpStatus.SC_OK, msg: '添加成功')
        } else {
            return apiResult(status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: '添加失败')
        }
    }

    Map getUserList() {
        def result = accountRepo.getUserList()
        return apiResult(status: HttpStatus.SC_OK, msg: result)
    }

    /**
     * 更新用户身份duty
     * @param userId
     * @param duty
     * @return
     */
    boolean modifyAccountDuty(long userId, int duty) {
        return accountRepo.updateAccountDuty(userId, duty)
    }

    void addDefaultSubject(long userId) {
        //添加默认全网监控主题
        List<Map> subjectList = new ArrayList<Map>()
//        subjectList.add(["subjectName": "共享经济", "keyWords": "[{\"subOperator\":\"and\",\"keywords\":[\"共享单车\",\"绿色出行\",\"共享经济\"]}]", "areaWords": ""])
        subjectList.add(["subjectName": "人工智能", "keyWords" : "[{\"subOperator\":\"or\",\"keywords\":[\"腾讯AI\",\"谷歌AI\"],\"editMode\":false},{\"operator\":\"and\"},{\"subOperator\":\"or\",\"keywords\":[\"推出\",\"诞生\",\"掀起\",\"突破\"],\"editMode\":false},{\"operator\":\"and\"},{\"subOperator\":\"or\",\"keywords\":[\"技术\",\"领域\",\"应用\"]}]", "areaWords": "",  "excludeWords" : "信用卡 网贷"])
        for (int i = 0; i < subjectList.size(); i++) {
            String subjectName = subjectList.get(i).get("subjectName")
            String keyWords = subjectList.get(i).get("keyWords")
            String areaWords = subjectList.get(i).get("areaWords")
            String excludeWords = subjectList.get(i).get("excludeWords")
            subjectService.addUserSubject(userId, subjectName, KeywordsScopeEnum.contentScope.key, "", keyWords, areaWords, excludeWords)
        }
    }

    void addDefaultSite(long userId) {
        //添加默认站点
        List<Map> siteList = new ArrayList<Map>()
        siteList.add(["siteName": "汽车之家", "websiteDomain": "autohome.com.cn"])
        siteList.add(["siteName": "36氪", "websiteDomain": "36kr.com"])
        siteList.add(["siteName": "虎扑体育", "websiteDomain": "hupu.com"])
        siteList.add(["siteName": "芥末堆", "websiteDomain": "www.jiemodui.com"])
        siteList.add(["siteName": "虎嗅", "websiteDomain": "www.huxiu.com"])
        for (int i = 0; i < siteList.size(); i++) {
            String siteName = siteList.get(i).get("siteName")
            String websiteDomain = siteList.get(i).get("websiteDomain")

            HanyuPinyinOutputFormat upperCaseFormat = new HanyuPinyinOutputFormat();
            upperCaseFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE)
            char word = siteName.substring(0, 1) as char
            def pinYin = PinyinHelper.toHanyuPinyinStringArray(word, upperCaseFormat)
            def pinYinPrefix = (pinYin ? pinYin[0] : word) as String
            def site = new Site(
                    userId: userId,
                    siteName: siteName,
                    websiteName: siteName,
                    websiteDomain: UrlUtils.webSiteDomainUrl(websiteDomain),
                    siteType: 1,
                    isAutoPush: false,
                    domainReverse: websiteDomain.equals("") ? "" : UrlUtils.getReverseDomainFromUrl(websiteDomain),
                    pinYinPrefix: pinYinPrefix,
                    updateTime: new Date(),
                    createTime: new Date(),
            )
            siteRepo.addSite(site)
        }
        //添加默认公众号
        List<Map> siteList2 = new ArrayList<Map>()
        siteList2.add(["siteName": "环球旅行"])
        siteList2.add(["siteName": "果壳"])
        for (int i = 0; i < siteList2.size(); i++) {
            String siteName = siteList2.get(i).get("siteName")

            HanyuPinyinOutputFormat upperCaseFormat = new HanyuPinyinOutputFormat();
            upperCaseFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE)
            char word = siteName.substring(0, 1) as char
            def pinYin = PinyinHelper.toHanyuPinyinStringArray(word, upperCaseFormat)
            def pinYinPrefix = (pinYin ? pinYin[0] : word) as String
            def site = new Site(
                    userId: userId,
                    siteName: siteName,
                    websiteName: siteName,
                    websiteDomain: "",
                    siteType: 2,
                    isAutoPush: false,
                    domainReverse: "",
                    pinYinPrefix: pinYinPrefix,
                    updateTime: new Date(),
                    createTime: new Date(),
            )
            siteRepo.addSite(site)
        }
        //添加默认微博
        List<Map> siteList3 = new ArrayList<Map>()
        siteList3.add(["siteName": "明星娱乐头条"])
        for (int i = 0; i < siteList3.size(); i++) {
            String siteName = siteList3.get(i).get("siteName")

            HanyuPinyinOutputFormat upperCaseFormat = new HanyuPinyinOutputFormat();
            upperCaseFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE)
            char word = siteName.substring(0, 1) as char
            def pinYin = PinyinHelper.toHanyuPinyinStringArray(word, upperCaseFormat)
            def pinYinPrefix = (pinYin ? pinYin[0] : word) as String
            def site = new Site(
                    userId: userId,
                    siteName: siteName,
                    websiteName: siteName,
                    websiteDomain: "",
                    siteType: 3,
                    isAutoPush: false,
                    domainReverse: "",
                    pinYinPrefix: pinYinPrefix,
                    updateTime: new Date(),
                    createTime: new Date(),
            )
            siteRepo.addSite(site)
        }
    }

    Map initializeRecommandData(long userId, String[] classification, String area, int duty, int subjectFlag) {
        //判断是否已经历新手引导
        Account account = accountRepo.getUserById(userId)
        if (account.isSitesInited) {
            return apiResult(status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "已经设置新手引导")
        }
        if (!classification && "".equals(area)) {
            //跳过新手引导添加默认的站点和主题
            accountRepo.updateAccountGuidance(userId)
            addDefaultSite(userId)
//            addDefaultSubject(userId)
//            modifyAccountDuty(userId, duty)//更新新手用户的身份duty
            return apiResult(status: HttpStatus.SC_OK, msg: "跳过新手引导")
        }
        addAccountSites(classification, userId)
        //添加默认主题
        if (subjectFlag == 1) {
            addDefaultSubject(userId)
        }
        //更新新手用户的身份duty
        def result = []
//        result = modifyAccountDuty(userId, duty)
        result = accountRepo.updateAccountGuidance(userId)
        if (result) {
            return apiResult(status: HttpStatus.SC_OK, msg: "修改成功")
        } else {
            return apiResult(status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "修改失败")
        }

    }

    void addAccountSites(String[] classification, long userId) {
        classification.eachWithIndex { elem, inedex ->
            def siteList = accountRepo.getSiteListByclassification(elem)
            def siteMap = siteList.groupBy { it.siteType }
            siteMap.each { key, value ->
                for (int t = 0; t < (3 - inedex); t++) {
                    def list = value as ArrayList
                    try {
                        SiteDetail siteDetail = list.get(t)
                        if (!siteDetail.siteName) {
                            continue
                        }
                        addAccountSite(siteDetail, userId)
                    } catch (Exception e) {
                        println(elem + key + "初始化推荐站点数量不足,请联系运维人员添加!")
                    }

                }
            }
        }
    }

    public void addAccountSite(SiteDetail siteDetail, long userId) {
        String siteName = siteDetail.siteName
        String websiteDomain = siteDetail.siteDomain
        String siteTypeStr = siteDetail.siteType
        int siteType = Site.SITE_TYPE_WEBSITE
        if ("微信公众号".equals(siteTypeStr)) {
            siteType = Site.SITE_TYPE_WECHAT
        }
        if ("微博".equals(siteTypeStr)) {
            siteType = Site.SITE_TYPE_WEIBO
        }
        HanyuPinyinOutputFormat upperCaseFormat = new HanyuPinyinOutputFormat();
        upperCaseFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE)
        char word = siteName.substring(0, 1) as char
        def pinYin = PinyinHelper.toHanyuPinyinStringArray(word, upperCaseFormat)
        def pinYinPrefix = (pinYin ? pinYin[0] : word) as String
        def site = new Site(
                userId: userId,
                siteName: siteName,
                websiteName: siteName,
                websiteDomain: UrlUtils.webSiteDomainUrl(websiteDomain),
                siteType: siteType,
                isAutoPush: false,
                domainReverse: websiteDomain.equals("") ? "" : UrlUtils.getReverseDomainFromUrl(websiteDomain),
                pinYinPrefix: pinYinPrefix,
                updateTime: new Date(),
                createTime: new Date(),
        )
        siteRepo.addSite(site)
    }

    List<AccountApply> getAccountApplyList() {
        List<AccountApply> accountApplyList = accountRepo.getAccountApplyList()
        return accountApplyList
    }

    List<Account> getAllAccountList(){
        List<Account> accountList = accountRepo.getAccountList()
        return accountList;
    }

    def getAccountList(LoginUser user) {
        def list = []
        def teamList = teamRepo.getTeamListByOrgIdAndAnegtId(user.agentId, user.orgId)
        if (!teamList) {
            def accountList = accountRepo.getAccountListByTeamIdAndAgentId(user.orgId, user.agentId, "0", true)
            def map = [
                    id         : "0",
                    orgId      : user.orgId,
                    agentId    : user.agentId,
                    teamName   : "未分组",
                    accountList: accountList
            ]
            list.add(map)
            return list
        }
        teamList.each { team ->
            def teamMap = [:]
            def accountList = accountRepo.getAccountListByTeamIdAndAgentId(user.orgId, user.agentId, team.id, true)
            def map = [
                    id         : team.id,
                    orgId      : team.orgId,
                    agentId    : team.agentId,
                    teamName   : team.teamName,
                    accountList: accountList
            ]
            teamMap.put("team", map)
            list.add(map)
        }
        def accountList = accountRepo.getAccountListByTeamIdAndAgentId(user.orgId, user.agentId, "0", true)
        if (accountList) {
            def map = [
                    id         : "0",
                    orgId      : user.orgId,
                    agentId    : user.agentId,
                    teamName   : "未分组",
                    accountList: accountList
            ]
            list.add(map)
        }
        return list
    }


    Account addOrgUser(long loginUserId, String userName, String password, String phoneNumber, String userType, String productType,
                       Date expDate, String realName, String agentId, String orgId, String teamId, long salesId, String company) {
        def userId = System.currentTimeMillis()
        Account account = new Account(userId, userName, phoneNumber, password, new Date(), userType,
                productType, expDate, realName, '', orgId, '', '', '', true,
                teamId, agentId, false, salesId, AccountOpenTypeEnum.orgAdminRegist.key, false)

        account.setCompany(company)
        def result = accountRepo.addOrgUser(account)
        if (result) {
            //判断是什么机构用户
            Role role = userRoleService.getRoleById(loginUserId)
            def name = "机构用户"
            if (role.subRoleName) {
                name = role.subRoleName
            }
            addAccountVsRole(result, name)
            addAccountSetting(productType, userId)
            return result
        } else {
            return null
        }
    }

    Map accountIsExist(String userName) {
        return accountRepo.getUserByName(userName);
    }

    Map phoneNumberIsExist(String phoneNumber) {
        return accountRepo.getUserByPhoneNumber(phoneNumber)
    }

    private void addAccountVsRole(Account result, String roleName) {
        //向用户角色对应表中添加内容
        Role role = userRoleService.getRoleByAgentIdAndRoleName(result.getAgentId(), roleName);
        def roleId = role.getId()
        String accountVsRoleId = primaryKeyRepo.createId("accountVsRole");
        def userId = result.getId()
        AccountVsRole accountVsRole = new AccountVsRole(accountVsRoleId, roleId, userId,);
        accountVsRoleRepo.saveAccountVsRole(accountVsRole);
    }
  
    private def addAccountVsRole(long userId, String roleId) {
        String accountVsRoleId = primaryKeyRepo.createId("accountVsRole");
        AccountVsRole accountVsRole = new AccountVsRole(accountVsRoleId, roleId, userId,);
        return accountVsRoleRepo.saveAccountVsRole(accountVsRole);
    }

    private void addAccountSetting(String productType, long userId) {
        //添加用户配置信息（可配置站点数量）
        AccountProfile accountProfile = new AccountProfile();
        SystemSetting appVersion = settingRepo.getSystemSetting("appVersion", productType);
        if (appVersion == null) {
            appVersion = settingRepo.getSystemSetting("appVersion", "Std.Version");
        }
        if (appVersion != null) {
            Map map = appVersion.getContent();
            accountProfile.setCaptureSite((Map) map.get("captureSite"));
        }
        accountProfile.setId(UUID.randomUUID().toString());
        accountProfile.setUserId(userId);
        accountProfile.setCreateTime(new Date());
        accountProfile.setUpdateTime(new Date());
        accountRepo.addAccountProfile(accountProfile);
    }

    Account getOrgUserById(long userId) {
        def orgUser = accountRepo.getUserById(userId)
        if (orgUser) {
            return orgUser
        } else {
            return null
        }

    }

    Boolean updateOrgUser(long userId, String userName, String password, String realName, String phoneNumber, String teamId) {
        Account account = new Account()
        account.setId(userId)
        account.setPassword(password)
        account.setRealName(realName)
        account.setPhoneNumber(phoneNumber)
        account.setTeamId(teamId)
        account.setUpdateTime(new Date())
        account.setIsPwdModified(false);
        def result = accountRepo.updateOrgUser(userId, account)
        return result
    }

    Boolean deleteOrgUser(long userId) {
        def result = accountRepo.deleteOrgUser(userId)
        return result
    }

    Boolean modifyOrgUserTeam(String orgUserIds, String teamId) {
        def orgUserIdsMap = orgUserIds.split(",")
        def orgUserIdsList = []
        orgUserIdsMap.each { userId ->
            orgUserIdsList.add(Long.parseLong(userId))
        }
        try {
            accountRepo.modifyTeamIdById(orgUserIdsList, teamId)
        } catch (Exception e) {
            return false
            e.printStackTrace()
        }
        return true
    }

    def getUsersByOrgIdAndAngetId(String orgId, String agentId) {
        def list = accountRepo.getAccountList(agentId, orgId)
        return list
    }

    boolean isOverstepOrgUserCount(String agentId, String orgId) {
        def userCount = getOrgUserCount(agentId, orgId)
        def maxUserCount = getOrgMaxUserCount(orgId, agentId)
        if (userCount < maxUserCount) {
            return false
        } else {
            return true
        }
    }

    int getOrgUserCount(String agentId, String orgId) {
        def orgUserList = accountRepo.getAccountList(agentId, orgId)
        if (orgUserList) {
            return orgUserList.size()
        } else {
            return 0
        }
    }

    int getOrgMaxUserCount(String orgId, String agentId) {
        Organization organization = organizationService.getOrgInfoByOrgIdAndAgentId(orgId, agentId)
        return organization.maxUserCount
    }

    Account accountRegister(String userName, String password, String phoneNumber, String userType, String productType,
                            Date expDate, String realName, String agentId, String orgId, String teamId, int openType, boolean isPwdModified) {
        def userId = System.currentTimeMillis()
        Account account = new Account(userId, userName, phoneNumber, password, new Date(), userType,
                productType, expDate, realName, '', orgId, '', '', '', true,
                teamId, agentId, false, 0l, openType, isPwdModified)
        def result = accountRepo.addOrgUser(account)
        if (result) {
            addAccountVsRole(result, "互联网用户")
            addAccountSetting(productType, userId)
            return result
        } else {
            return null
        }
    }

    String getOpenType(Integer openType) {
        if (openType == AccountOpenTypeEnum.adminRegist.getKey() ||
                openType == AccountOpenTypeEnum.orgAdminRegist.getKey()) {
            return Account.OPENTYPE_ADMIN
        } else if (openType == AccountOpenTypeEnum.register.getKey()||
                openType == AccountOpenTypeEnum.wechatRegist.getKey()) {
            return Account.OPENTYPE_REGIST
        }
        return null
    }

    def createOrgUser4Ecloud(def data) {
/*
        {
            "applyno":"CIDC-O-DDF12ADB96874484BF2FA8DD8F06111B",
            "ecordercode":"121243544",
            "custid":12312312,
            "productcode":"2121212",
            "users":[
                {
                    "opttype":0,
                    "userid":1001,
                    "username":"admin",
                    "useralias":"测试用户",
                    "mobile":"13800138000",
                    "email":"13800138000@139.com",
                    "begintime":1411718510123,
                    "endtime":1411718510123,
                    "paras":[
                        {"key":"sad",
                            "value":"111"},
                        {"key":"qweqw",
                            "value":"222"}
                    ]
                },
                {
                    "opttype":0,
                    "userid":1002,
                    "username":"admin1",
                    "useralias":"测试用户1",
                    "mobile":"13800138002",
                    "email":"13800138002@139.com"
                    "begintime":1411718510123,
                    "endtime":1411718510123,
                    "paras":[
                        {"key":"sad",
                            "value":"111"},
                        {"key":"qweqw",
                            "value":"222"}
                    ]
                }
        ]
        }
*/

        String orgId = Organization.getEcloudOrgId(data.custid as int)

        data.users?.each { userInfo ->
            int ecloudUserId = userInfo.userid as int

            long userId = System.currentTimeMillis()
            String userName = Account.getEcloudUsername(ecloudUserId)

            //首先获取移动云订购时，增加过的机构
            Organization org = organizationService.getOrgInfoByOrgIdAndAgentId(orgId, Agent.AGENT_BJJ_ID)

            //根据添加的机构信息，获取需要添加的用户版本
            String version = ecloudService.convertProductType4EcloudOrg(org?.ecloudOrgInfo)
            String userType = ecloudService.convertUserType4EcloudOrg(org?.ecloudOrgInfo)

            //如果用户存在则更新用户信息
            def accountObj = accountRepo.getUserByName(userName)
            if(accountObj) {

                //如果用户已经存在，有可能是用户重新授权，则需要更新用户信息(此处只更新了用户到期时间，用户基本信息没有更新)
                accountRepo.modifyAccountExpDate(accountObj.userId as Long, new Date(userInfo.endtime as long))
                accountRepo.modifyAccountProductType(accountObj.userId as Long, version)

                log.info(['ecloud', 'createOrgUser', "用户已经存在，不再创建, 只更新用户过期时间！"].join(':::') as String)
                return
            }

            //根据添加的机构信息，判断是不是管理员用户
            boolean isManager = ecloudService.isEcloudOrgManager(org?.ecloudOrgInfo, userName)

            Account account = new Account([
                    _id              : userId,
                    userName         : userName,
                    password         : RandomStringUtils.randomAlphanumeric(10),
                    phoneNumber      : userInfo.mobile ?: "",
                    nickName         : userInfo.useralias ?: "",  //真实姓名
                    realName         : userInfo.useralias ?: "",
                    avatar           : "",

                    agentId          : Agent.AGENT_BJJ_ID,
                    orgId            : orgId,
                    teamId           : "",
                    salesId          : 0l,

                    gender           : 1,
                    birthday         : "",
                    company          : "",
                    duty             : 1,
                    qqNumber         : "",
                    weChatNumber     : "",
                    email            : userInfo.email ?: "",
                    userArea         : "",

                    userType         : userType,
                    openType         : AccountOpenTypeEnum.ecloud.key,
                    productType      : version,


                    isSitesInited    : false,
                    isRecommandInited: false,
                    isCompileInited  : false,
                    isPwdModified    : true,
                    isManager        : isManager,
                    expMsgSendDay    : 1,
                    expDate          : new Date(userInfo.endtime as long),
                    createTime       : new Date()
            ])

            def result = accountRepo.addOrgUser(account)
            log.info(['ecloud', 'createOrgUser', '新创建用户', JSONObject.toJSONString(result)].join(':::') as String)
            if (result) {
                //为用户分配普通的机构用户角色，因为不能使用该账号新开机构用户，所以不能分配机构管理员角色
                addAccountVsRole(userId, Role.ROLE_BJJ_ORG_USER)
                addAccountSetting(version, userId)
            } else {
                return null
            }
        }

    }

    def cancelOrgUser4Ecloud(def data) {

        data.users?.each { userInfo ->
            int ecloudUserId = userInfo.userid as int
            String userName = Account.getEcloudUsername(ecloudUserId)

            if(userInfo.opttype as int == 0) {    //取消授权
                def accountObj = accountRepo.getUserByName(userName)
                if(accountObj) {
                    //取消用户授权
                    accountRepo.modifyAccountExpDate(accountObj.userId, new Date())
                    log.info(['ecloud', 'cancel', '修改用户过期时间', JSONObject.toJSONString(accountObj)].join(':::') as String)
                    return
                }
            }
        }

    }

    Map getEvaluateChannelAccountProfile(long userId) {
        AccountProfile accountProfile = accountRepo.getAccountProfileByUser(userId)
        //是否存在，如果不存在则新增一个，如果存在则修改
        SystemSetting map = this.getDefAppVersion()
        if (accountProfile != null && (accountProfile.evaluateChannel.size() >= map.content.captureSite.size())) {
            return accountProfile.evaluateChannel
        }
        if (accountProfile == null) {
            accountProfile = new AccountProfile()
            accountProfile.userId = userId
            accountProfile.createTime = new Date();
            accountProfile.evaluateChannel = map.content.captureSite
            accountProfile.updateTime = new Date();
            accountRepo.saveAccountProfile(accountProfile)
        }
        //从systemSetting中获取
        def account = accountRepo.getUserById(userId)
        String productType = account.productType
        if (!productType) {
            productType = getDefAppVersion().key
        }
        SystemSetting setting = settingRepo.getSystemSetting("appVersion", productType)

        def channelSite = accountProfile.evaluateChannel
        if (!channelSite) {
            channelSite = setting.content.captureSite
        }

        return channelSite
    }
    SystemSetting getDefAppVersion(){
        return settingRepo.getSystemSetting("appVersion", "Std.Version")
    }

}
