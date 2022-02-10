package com.istar.mediabroken.entity.account

import com.istar.mediabroken.entity.app.Agent

/**
 * Author: zc
 * Time: 2017/8/8
 */
class Account {
    public static final String USERTYPE_TRIAL = "试用";
    public static final String USERTYPE_OFFICIAL = "正式";
    public static final String OPENTYPE_ADMIN = "后台开通";
    public static final String OPENTYPE_REGIST = "前台注册";


    public static final String ECLOUD_USERNAME_PREFIX = "ecloud_"

    long id
    String userName
    String phoneNumber
    String nickName
    Integer gender
    String birthday
    String company
    Integer duty
    String qqNumber
    String weChatNumber
    String email
    String avatar
    String password
    Date updateTime
    Date createTime
    String userType
    Date expDate
    String realName
    String productType
    String userArea
    boolean isSitesInited
    boolean isRecommandInited
    boolean isCompileInited
    Integer expMsgSendDay
    boolean valid
    String teamId
    String agentId
    String orgId
    boolean isManager

    long salesId
    Integer openType //1:后台开通 2:前台注册
    boolean isPwdModified

    Map<String, Object> toMap() {
        return [
                _id              : id ?: System.currentTimeMillis(),
                userName         : userName ?: "",
                phoneNumber      : phoneNumber ?: "",
                nickName         : nickName ?: "",
                gender           : gender ?: 1,
                birthday         : birthday ?: "",
                company          : company ?: "",
                duty             : duty ?: 1,
                qqNumber         : qqNumber ?: "",
                weChatNumber     : weChatNumber ?: "",
                email            : email ?: "",
                avatar           : avatar ?: "",
                updateTime       : updateTime,
                createTime       : createTime,
                userType         : userType ?: "",
                expDate          : expDate,
                realName         : realName ?: "",
                productType      : productType ?: "",
                userArea         : userArea ?: "",
                isSitesInited    : isSitesInited,
                isRecommandInited: isRecommandInited,
                isCompileInited  : isCompileInited,
                expMsgSendDay    : expMsgSendDay,
                teamId           : teamId ?: "",
                agentId          : agentId ?: "",
                orgId            : orgId ?: "",
                isManager        : isManager,
                salesId          : salesId ?: 0l,
                openType         : openType,
                isPwdModified    : isPwdModified
        ]
    }

    Account() {
        super
    }

    Account(Map map) {
        id = map._id ?: map.id ?: 0
        userName = map.userName ?: ""
        phoneNumber = map.phoneNumber ?: ""
        password = map.password ?: ""
        nickName = map.nickName ?: ""
        gender = map.gender ?:0
        birthday = map.birthday ?: ""
        company = map.company ?: ""
        duty = map.duty ?:1
        qqNumber = map.qqNumber ?: ""
        weChatNumber = map.weChatNumber ?: ""
        email = map.email ?: ""
        avatar = map.avatar ?: ""
        updateTime = map.updateTime
        createTime = map.createTime
        userType = map.userType ?: ""
        expDate = map.expDate
        realName = map.realName ?: ""
        productType = map.productType ?: ""
        userArea = map.userArea ?: ""
        isSitesInited = map.isSitesInited
        isRecommandInited = map.isRecommandInited
        isCompileInited = map.isCompileInited
        expMsgSendDay = map.expMsgSendDay
        valid = map.valid == false ? false : true
        teamId = map.teamId ?: ""
        agentId = map.agentId ?: Agent.AGENT_BJJ_ID
        orgId = map.orgId ?: ""
        isManager = map.isManager == false ? false : true
        salesId = map.salesId ?: 0l
        openType = map.openType
        isPwdModified = map.isPwdModified
    }


    public Account(long id, String userName, String phoneNumber, String password, Date createTime, String userType,
                   String productType, Date expDate, String realName, String userArea, String orgId, String qqNumber,
                   String weChatNumber, String email, boolean valid, String teamId, String agentId, boolean isManager) {
        this.id = id;
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.createTime = createTime;
        this.userType = userType;
        this.productType = productType;
        this.expDate = expDate;
        this.realName = realName;
        this.userArea = userArea;
        this.orgId = orgId;
        this.qqNumber = qqNumber;
        this.weChatNumber = weChatNumber;
        this.email = email;
        this.valid = true;
        this.teamId = teamId;
        this.agentId = agentId;
        this.isManager = isManager;

    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", nickName='" + nickName + '\'' +
                ", gender=" + gender +
                ", birthday='" + birthday + '\'' +
                ", company='" + company + '\'' +
                ", duty=" + duty +
                ", qqNumber='" + qqNumber + '\'' +
                ", weChatNumber='" + weChatNumber + '\'' +
                ", email='" + email + '\'' +
                ", avatar='" + avatar + '\'' +
                ", password='" + password + '\'' +
                ", updateTime=" + updateTime +
                ", createTime=" + createTime +
                ", userType='" + userType + '\'' +
                ", expDate=" + expDate +
                ", realName='" + realName + '\'' +
                ", productType='" + productType + '\'' +
                ", userArea='" + userArea + '\'' +
                ", isSitesInited=" + isSitesInited +
                ", isRecommandInited=" + isRecommandInited +
                ", isCompileInited=" + isCompileInited +
                ", expMsgSendDay=" + expMsgSendDay +
                ", valid=" + valid +
                ", teamId='" + teamId + '\'' +
                ", agentId='" + agentId + '\'' +
                ", orgId='" + orgId + '\'' +
                ", isManager=" + isManager +
                ", salesId=" + salesId +
                ", openType=" + openType +
                ", isPwdModified=" + isPwdModified +
                '}';
    }

    public Account(long id, String userName, String phoneNumber, String password, Date createTime, String userType,
                   String productType, Date expDate, String realName, String userArea, String orgId, String qqNumber,
                   String weChatNumber, String email, boolean valid, String teamId, String agentId, boolean isManager,
                   long salesId, Integer openType, boolean isPwdModified) {
        this.id = id;
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.createTime = createTime;
        this.userType = userType;
        this.productType = productType;
        this.expDate = expDate;
        this.realName = realName;
        this.userArea = userArea;
        this.orgId = orgId;
        this.qqNumber = qqNumber;
        this.weChatNumber = weChatNumber;
        this.valid = valid
        this.email = email;
        this.valid = true;
        this.teamId = teamId;
        this.agentId = agentId;
        this.isManager = isManager;
        this.salesId = salesId;
        this.openType = openType;
        this.isPwdModified = isPwdModified;
    }

    static String getEcloudUsername (int ecloudUserId) {
        return ECLOUD_USERNAME_PREFIX + ecloudUserId;
    }

}

enum AccountOpenTypeEnum {
    adminRegist(1, '后台开通'), //后台管理员开通账户
    register(2, '前台注册'), //用户自注册
    orgAdminRegist(3,'机构管理员注册'), //机构管理员在前台注册机构用户
    ecloud(4,'移动云接入'), //中国移动云平台接入方式开通
    wechatRegist(5,'移动端注册') //手机微信小程序方式开通

    private Integer key
    private String desc

    AccountOpenTypeEnum(Integer key, String desc) {
        this.key = key
        this.desc = desc
    }

    Integer getKey() {
        return key
    }

    String getDesc() {
        return desc
    }
}

