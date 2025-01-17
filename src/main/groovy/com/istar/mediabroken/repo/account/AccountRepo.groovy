package com.istar.mediabroken.repo.account

import com.istar.mediabroken.api3rd.YqmsSession
import com.istar.mediabroken.entity.AccountProfile
import com.istar.mediabroken.entity.account.Account
import com.istar.mediabroken.entity.account.AccountApply
import com.istar.mediabroken.entity.capture.SiteDetail
import com.istar.mediabroken.utils.DateUitl
import com.istar.mediabroken.utils.MongoHolder
import com.mongodb.BasicDBObject
import com.mongodb.QueryBuilder
import com.mongodb.WriteResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import static com.istar.mediabroken.utils.MongoHelper.toObj

@Repository
class AccountRepo {
    String DEFAULT_AVATAR = "/theme/default/images/default-avatar.png"

    @Autowired
    MongoHolder mongo;

    Map getUser(String userName, String password) {
        def collection = mongo.getCollection("account")
        def cursor = collection.find(toObj([userName: userName, password: password]))
        def user = null;
        if (cursor.hasNext()) {
            def userObj = cursor.next()
            Boolean valid = false
            if (userObj.get("valid") == null) {
                valid = true
            } else {
                valid = userObj.get("valid") as Boolean
            }
            user = [
                    userId           : userObj.get("_id") as Long,
                    agentId          : userObj.agentId ?: "1",
                    orgId            : userObj.orgId ?: "0",
                    teamId           : userObj.teamId ?: "0",
                    userName         : userObj.userName,
                    phoneNumber      : userObj.phoneNumber,
                    nickName         : userObj.nickName,
                    avatar           : userObj.avatar ?: DEFAULT_AVATAR,
                    isSitesInited    : userObj.isSitesInited ?: false,
                    isRecommandInited: userObj.isRecommandInited ?: false,
                    isCompileInited  : userObj.isCompileInited ?: false,
                    expDate          : userObj.expDate,
                    isPwdModified    : userObj.isPwdModified == false ? false : true,//老用户不做修改密码提示，isPwdModified为null和true时返回true，为false时返回false（isPwdModified为null时，isPwdModified as Boolean=null , isPwdModified as boolean=false）
                    valid            : valid,
                    openId           : userObj.openId ?: ""
            ]
        }
        cursor.close()
        return user
    }

    def getUserByOpenId(String openId) {
        def collection = mongo.getCollection("account")
        def cursor = collection.find(toObj([openId: openId]))
        def user = null
        if (cursor.hasNext()) {
            def userObj = cursor.next()
            Boolean valid = false
            if (userObj.get("valid") == null) {
                valid = true
            } else {
                valid = userObj.get("valid") as Boolean
            }
            user = [
                    userId           : userObj.get("_id") as Long,
                    agentId          : userObj.agentId ?: "1",
                    orgId            : userObj.orgId ?: "0",
                    teamId           : userObj.teamId ?: "0",
                    userName         : userObj.userName,
                    nickName         : userObj.nickName,
                    avatar           : userObj.avatar ?: DEFAULT_AVATAR,
                    isSitesInited    : userObj.isSitesInited ?: false,
                    isRecommandInited: userObj.isRecommandInited ?: false,
                    isCompileInited  : userObj.isCompileInited ?: false,
                    expDate          : userObj.expDate,
                    isPwdModified    : userObj.isPwdModified == false ? false : true,//老用户不做修改密码提示，isPwdModified为null和true时返回true，为false时返回false（isPwdModified为null时，isPwdModified as Boolean=null , isPwdModified as boolean=false）
                    valid            : valid,
                    openId           : userObj.openId
            ]
        }
        cursor.close()
        return user
    }

    String addSession(Long userId) {
        def collection = mongo.getCollection("session")
        def sid = UUID.randomUUID().toString()
        // todo 需要验证是否插入成功
        collection.insert(toObj([_id: sid, userId: userId, createTime: new Date(), enable: true, updateTime: new Date()]))
        return sid
    }

    /**
     * 记录用户登录
     * @return
     */
    String addLoginLog(Long userId) {
        def collection = mongo.getCollection("loginLog")
        def sid = UUID.randomUUID().toString()
        collection.insert(toObj([_id: sid, userId: userId, createTime: new Date(), enable: true, updateTime: new Date()]))
        return sid
    }

    void updateAccountLoginTime(Long userId) {
        def collection = mongo.getCollection("account")
        collection.update(toObj([_id: userId]), toObj(['$set': [lastLoginTime: new Date(), updateTime: new Date()]]))
    }

    Long getUserIdBySessionId(String sid) {
        def collection = mongo.getCollection("session")
        def cursor = collection.find(toObj([_id: sid, enable: true]))
        def userId = null;
        if (cursor.hasNext()) {
            def userObj = cursor.next()
            userId = userObj.get("userId") as Long
        }
        cursor.close()
        return userId
    }

    Map getUserBySessionId(String sid) {
        def collection = mongo.getCollection("session")
        def rep = collection.findOne(toObj([_id: sid]))
        def userId = null;
        if (rep) {
            return [
                    userId    : rep.userId,
                    enable    : rep.enable,
                    updateTime: rep.updateTime]
        } else {
            null
        }
        return userId
    }

    void deleteSession(String sid) {
        def collection = mongo.getCollection("session")
        collection.remove(toObj([_id: sid]))
    }

    Map getUserByName(String userName) {
        def collection = mongo.getCollection("account")
        def rep = collection.findOne(toObj([userName: userName, valid: true]))
        if (rep) {
            [userId: rep.get("_id") as Long, phoneNumber: rep.phoneNumber]
        } else {
            null
        }
    }

    void addUser(String userName, String password, String phoneNumber) {
        def collection = mongo.getCollection("account")
        collection.insert(toObj([
                _id        : System.currentTimeMillis(),
                userName   : userName,
                password   : password,
                phoneNumber: phoneNumber,
                orgId      : '0',
                createTime : new Date()
        ]))
    }

    boolean addUser(Account account) {
        def collection = mongo.getCollection("account")
        def result = collection.insert(toObj([
                _id        : account.id,
                userName   : account.userName,
                password   : account.password,
                phoneNumber: account.phoneNumber,
                userType   : account.userType,
                expDate    : account.expDate,
                company    : account.company,
                realName   : account.realName,
                userArea   : account.userArea,
                duty       : account.duty,
                orgId      : '0',
                createTime : account.createTime
        ]))
        println(result)
        def findOne = collection.findOne(toObj([_id: account.id]))
        println(findOne)
        return true
    }

    List<Account> getUserList() {
        def collection = mongo.getCollection("account")
        def cursor = collection.find()
        def result = []
        while (cursor.hasNext()) {
            def account = cursor.next()
            result << new Account(account)
        }
        cursor.close()
        return result
    }

    Map getUserByPhoneNumber(String phoneNumber) {
        def collection = mongo.getCollection("account")
        def rep = collection.findOne(toObj([phoneNumber: phoneNumber, valid: true]))
        if (rep) {
            [userId: rep.get("_id") as Long]
        } else {
            null
        }
    }

    boolean modifyPhoneNumber(long userId, String phoneNumber) {
        def collection = mongo.getCollection("account")
        collection.update(toObj([_id: userId]), toObj(['$set': [phoneNumber: phoneNumber, updateTime: new Date()]]))
        return true
    }

    boolean modifyPassword(long userId, String password) {
        def collection = mongo.getCollection("account")
        collection.update(toObj([_id: userId]), toObj(['$set': [password: password, isPwdModified: true, updateTime: new Date()]]))
        return true
    }

    boolean bindWechatOpenId(long userId, String openId) {
        def collection = mongo.getCollection("account")
        collection.update(toObj([_id: userId]), toObj(['$set': [openId: openId, updateTime: new Date()]]))
        return true
    }

    boolean removeWechatOpenId(long userId) {
        def collection = mongo.getCollection("account")
        collection.update(toObj([_id: userId]), toObj(['$set': [openId: "", updateTime: new Date()]]))
        return true
    }

    boolean modifyPasswordStatus(long userId) {
        def collection = mongo.getCollection("account")
        collection.update(toObj([_id: userId]), toObj(['$set': [isPwdModified: true, updateTime: new Date()]]))
        return true
    }

    boolean updateAccountDuty(long userId, int duty) {
        def collection = mongo.getCollection("account")
        collection.update(toObj([_id: userId]), toObj(['$set': [duty: duty, updateTime: new Date()]]))
        return true
    }

    boolean updateAccountSendDay(long userId, int expMsgSendDay) {
        def collection = mongo.getCollection("account")
        collection.update(toObj([_id: userId]), toObj(['$set': [expMsgSendDay: expMsgSendDay, updateTime: new Date()]]))
        return true
    }

    boolean updateAccountGuidance(long userId) {
        def collection = mongo.getCollection("account")
        collection.update(toObj([_id: userId]), toObj(['$set': [isSitesInited: true, updateTime: new Date()]]))
        return true
    }

    String getPassword(long userId) {
        def collection = mongo.getCollection("account")
        def user = collection.findOne(toObj([_id: userId]))
        return user ? user.password : null
    }

    String getYqmsSession(long yqmsUserId) {
        def collection = mongo.getCollection("yqmsSession")
        def session = collection.findOne(toObj([yqmsUserId: yqmsUserId]))
        return session ? session.session : null
    }

    YqmsSession getYqmsSession2(long yqmsUserId) {
        def collection = mongo.getCollection("yqmsSession")
        def session = collection.findOne(toObj([yqmsUserId: yqmsUserId]))
        return session ? new YqmsSession(sid: session.session, userId: yqmsUserId) : null
    }

    YqmsSession getYqmsSession2() {
        def collection = mongo.getCollection("yqmsSession")
        def session = collection.findOne()
        return session ? new YqmsSession(sid: session.session, userId: session.yqmsUserId) : null
    }

    Map getYqmsSession() {
        // todo accountRepo.getYqmsSession() 要改成可以获取占用资源最少的用户
        def collection = mongo.getCollection("yqmsSession")
        def session = collection.findOne()
        return session ? [session: session.session, yqmsUserId: session.yqmsUserId] : null
    }

    Account getUserById(long userId) {
        def collection = mongo.getCollection("account")
        def account = collection.findOne(toObj([_id: userId]))
        if (account) {
            return new Account(account)
        } else {
            return null
        }
    }

    List<String> getDutyList() {
        def collection = mongo.getCollection("account")
        def result = []
        def cursor = collection.find()//.distinct("duty")
        while (cursor.hasNext()) {
            def it = cursor.next()
            if (it.duty) {
                result << it.duty
            }
        }
        cursor.close()
        if (result) {
            result = result.unique()
        }
        return result;
    }

    boolean modifyUser(long userId, Account account) {
        def collection = mongo.getCollection("account")
        collection.update(
                toObj([
                        _id: userId
                ]),
                toObj(['$set': [
                        nickName    : account.nickName,
                        avatar      : account.avatar,
                        gender      : account.gender,
                        birthday    : account.birthday,
                        company     : account.company,
                        duty        : account.duty,
                        qqNumber    : account.qqNumber,
                        weChatNumber: account.weChatNumber,
                        email       : account.email,
                        updateTime  : account.updateTime
                ]]))
        return true;
    }

    void modifyAccountExpDate(Long  userId, Date expDate) {
        def collection = mongo.getCollection("account")
        collection.update(
                toObj([
                        _id: userId
                ]),
                toObj(['$set': [
                        expDate : expDate
                ]]))
    }

    void modifyAccountProductType(Long  userId, String productType) {
        def collection = mongo.getCollection("account")
        collection.update(
                toObj([
                        _id: userId
                ]),
                toObj(['$set': [
                        productType : productType
                ]]))
    }


    void modifyIsSitesInited(long userId, boolean isSitesInited) {
        def collection = mongo.getCollection("account")
        collection.update(toObj([_id: userId]), toObj(['$set': [isSitesInited: isSitesInited]]))
    }

    boolean modifyRecommandInited(long userId) {
        def collection = mongo.getCollection("account")
        collection.update(toObj([_id: userId]), toObj(['$set': [isRecommandInited: true]]))
        return true
    }

    boolean modifyCompileInited(long userId) {
        def collection = mongo.getCollection("account")
        collection.update(toObj([_id: userId]), toObj(['$set': [isCompileInited: true]]))
        return true
    }

    List getYqmsUserIds() {
        def collection = mongo.getCollection("yqmsSession")
        def res = collection.find()
        def yqmsUserIds = []
        while (res.hasNext()) {
            yqmsUserIds << (res.next().yqmsUserId as long)
        }
        res.close()
        return yqmsUserIds
    }

    boolean saveApplyInfo(Map applyInfo) {
        def collection = mongo.getCollection("accountApply")
        // todo 需要验证是否插入成功
        def result = collection.insert(toObj(applyInfo))
        return true
    }

    void disableSession(long userId) {
        def collection = mongo.getCollection("session")
        collection.update(toObj([userId: userId]), toObj(['$set': [enable: false, updateTime: new Date()]]), false, true)

    }

    void addAccountProfile(AccountProfile accountProfile) {
        def collection = mongo.getCollection("accountProfile")
        def result = collection.findOne(toObj([userId: accountProfile.userId]))
        if (!result) {
            collection.insert(toObj(accountProfile.toMap()))
        }
    }

    void saveAccountProfile(AccountProfile accountProfile) {
        def collection = mongo.getCollection("accountProfile")
        collection.save(toObj(accountProfile.toMap()))
    }

    void modifyAccountProfile(AccountProfile accountProfile) {
        def collection = mongo.getCollection("accountProfile")
        collection.update(toObj([userId: accountProfile.userId]), toObj(['$set': [captureSite: accountProfile.captureSite, updateTime: new Date()]]), false, false)
    }

    AccountProfile getAccountProfileByUser(long userId) {
        def collection = mongo.getCollection("accountProfile")
        def result = collection.findOne(toObj([userId: userId]))
        def accountProfile = null
        if (result) {
            accountProfile = new AccountProfile(result)
        }
        return accountProfile
    }

    Map getCompileSummaryProfile(long userId) {
        def collection = mongo.getCollection("accountProfile")
        def result = collection.findOne(toObj([userId: userId]), toObj([compileSummary: 1]))
        if (result) {
            return result.compileSummary ? result.compileSummary : [:]
        }
        return [:]
    }

    boolean isValidAppId(String appId) {
        def collection = mongo.getCollection("organization")
        def result = collection.findOne(toObj([appId: appId]))
        if (result) {
            return true
        }
        return false
    }

    List getSiteList(List classifications, List areas) {
        def collection = mongo.getCollection("siteDetail")
        QueryBuilder queryBuilder = QueryBuilder.start()
        if (classifications) {
            queryBuilder.put("classification").in(classifications)
        }
        if (!(areas.size() == 1 && "0".equals(areas.get(0)))) {
            queryBuilder.put("area").in(areas)
        }
        def cursor = collection.find(queryBuilder.get()).sort(toObj([level: 1])).limit(5)
        def result = []
        while (cursor.hasNext()) {
            def site = cursor.next()
            result << new SiteDetail(site)
        }
        cursor.close()
        return result
    }

    List getSiteListByclassification(String classification) {
        def collection = mongo.getCollection("siteDetail")
        def cursor = collection.find(toObj([classification: classification, isNoviceGuidance: "1"]))
        def result = []
        while (cursor.hasNext()) {
            def site = cursor.next()
            result << new SiteDetail(site)
        }
        return result
    }

    List<AccountApply> getAccountApplyList() {
        def collection = mongo.getCollection("accountApply")
        QueryBuilder queryBuilder = QueryBuilder.start()
        def cursor = collection.find(queryBuilder.get()).sort(toObj([createTime: -1]))
        List<AccountApply> result = new ArrayList<AccountApply>()
        while (cursor.hasNext()) {
            def accountApply = cursor.next()
            result << new AccountApply(toObj(accountApply))
        }
        cursor.close()
        return result
    }

    List<Account> getValidUsers() {
        def collection = mongo.getCollection("account")
        /*QueryBuilder queryBuilder = QueryBuilder.start()去掉有效用户的限制
        QueryBuilder query1 = QueryBuilder.start()
        query1.put("userType").is("正式")
        query1.put("valid").notEquals(false)
        QueryBuilder query2 = QueryBuilder.start()
        query2.put("userType").is("试用")
        query2.put("expDate").greaterThanEquals(new Date())
        query2.put("valid").notEquals(false)
        QueryBuilder query3 = QueryBuilder.start()
        query3.put("userType").is(null)
        query3.put("expDate").greaterThanEquals(new Date())
        query3.put("valid").notEquals(false)
        queryBuilder.or(query1.get()).or(query2.get()).or(query3.get())
        def cursor = collection.find(queryBuilder.get()).sort(toObj([_id: -1]))*/
        def cursor = collection.find().sort(toObj([_id: -1]))
        List<Account> result = new ArrayList<AccountApply>()
        while (cursor.hasNext()) {
            def account = cursor.next()
            result << new Account(toObj(account))
        }
        cursor.close()
        return result
    }

    def getAccountList(String agentId, String orgId) {
        def collection = mongo.getCollection("account")
        def cursor = collection.find(toObj([agentId: agentId, orgId: orgId, valid: true]))
        def result = []
        while (cursor.hasNext()) {
            def account = cursor.next()
            result << new Account(account)
        }
        cursor.close()
        return result
    }

    def getAccountList() {
        def collection = mongo.getCollection("account")
        def cursor = collection.find()
        def result = []
        while (cursor.hasNext()) {
            def account = cursor.next()
            result << new Account(account)
        }
        cursor.close()
        return result
    }

    def modifyTeamIdById(List accountIds, String teamId) {
        def collection = mongo.getCollection("account")
        collection.update(toObj(['_id': ['$in': accountIds]]), toObj(['$set': [teamId: teamId]]), false, true)
    }

    List getAccountListByTeamIdAndAgentId(String orgId, String agentId, String teamId, boolean valid) {
        def collection = mongo.getCollection("account")
        def cursor = collection.find(toObj([orgId: orgId, agentId: agentId, teamId: teamId, valid: valid]))
        def result = []
        while (cursor.hasNext()) {
            def account = cursor.next()
            result << new Account(account)
        }
        cursor.close()
        return result
    }


    Account addOrgUser(Account account) {
        def collection = mongo.getCollection("account")
        def result = collection.insert(toObj([
                _id          : account.id,
                userName     : account.userName,
                phoneNumber  : account.phoneNumber,
                password     : account.password,
                createTime   : account.createTime,
                userType     : account.userType,
                productType  : account.productType,
                expDate      : account.expDate,
                nickName     : account.nickName,
                realName     : account.realName,
                userArea     : account.userArea,
                orgId        : account.orgId,
                qqNumber     : account.qqNumber,
                weChatNumber : account.weChatNumber,
                email        : account.email,
                valid        : account.valid,
                teamId       : account.teamId,
                agentId      : account.agentId,
                isManager    : account.isManager,
                salesId      : account.salesId,
                openType     : account.openType,
                isPwdModified: account.isPwdModified,
                company      : account.company
        ]))
        def findOne = collection.findOne(toObj([_id: account.id]))
        if (findOne) {
            return new Account(findOne)
        } else {
            return null
        }
    }

    boolean updateOrgUser(long userId, Account account) {
        def collection = mongo.getCollection("account")
        WriteResult result = collection.update(
                toObj([
                        _id: userId
                ]),
                toObj(['$set': [
                        password   : account.password,
                        realName   : account.realName,
                        phoneNumber: account.phoneNumber,
                        teamId     : account.teamId,
                        updateTime : account.updateTime,
                        isPwdModified : account.isPwdModified
                ]]))
        return result.updateOfExisting;
    }

    boolean deleteOrgUser(long userId) {
        def collection = mongo.getCollection("account")
        collection.update(
                toObj([
                        _id: userId
                ]),
                toObj([
                        '$set': [
                                valid: false
                        ]
                ])
        )
        return true
    }

    List<String> getOrgAccountIdsByAgent(String agentId) {
        def collection = mongo.getCollection("account")
        def cursor = collection.find(toObj('agentId': agentId, 'valid': true, 'orgId': ['$ne': '0']))
        def accountIds = []
        while (cursor.hasNext()) {
            def account = cursor.next()
            accountIds << account._id
        }
        cursor.close()
        return accountIds
    }

    Map getAccountMapByAgent(String agentId) {
        def collection = mongo.getCollection("account")
        def cursor = collection.find(toObj('agentId': agentId, 'valid': true, 'orgId': ['$ne': '0']))
        def accountMap = [:]
        while (cursor.hasNext()) {
            def account = cursor.next()
            accountMap.put(account._id, new Account(toObj(account)))
        }
        return accountMap
    }

    def getAccountStatistic(Date startTime, Date endTime) {
        def collection = mongo.getCollection("account")
        BasicDBObject query = new BasicDBObject("valid", true).append("expDate", new BasicDBObject("\$gte",
                DateUitl.getBeginDayOfParm()))
        if (startTime && endTime) {
            query.append("createTime", new BasicDBObject("\$gte", startTime).append("\$lt", endTime))
        }
        BasicDBObject match = new BasicDBObject("\$match", query)
        BasicDBObject groupObj = new BasicDBObject("_id", new BasicDBObject("userType", "\$userType")
                .append("openType", "\$openType")).append("count", new BasicDBObject("\$sum", 1))
        BasicDBObject group = new BasicDBObject("\$group", groupObj)
        def aggregate = collection.aggregate(Arrays.asList(match, group))
        def list = aggregate.results()
        return list
    }

    Long getAccountCountByType(String userType, Date startTime, Date endTime) {
        def collection = mongo.getCollection("account")
        BasicDBObject query = new BasicDBObject("valid", true)
        if (userType) {
            query.append("userType", userType)
        }
        if (startTime) {
            query.append("expDate", new BasicDBObject("\$gte", startTime))
        }
        if (endTime) {
            query.append("expDate", new BasicDBObject("\$lt", endTime))
        }
        Long result = collection.count(query)
        return result
    }

    List<Account> getAccountListByExpDate( Date startTime, Date endTime) {
        def collection = mongo.getCollection("account")
        def cursor = collection.find(toObj(["valid": true, "expDate": [$gte: startTime, $lt: endTime]]))
        def result = []
        while (cursor.hasNext()) {
            def account = cursor.next()
            result << new Account(account)
        }
        return result
    }
}
