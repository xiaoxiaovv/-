package com.istar.mediabroken.service.system

import com.alibaba.fastjson.JSONArray
import com.istar.mediabroken.entity.SystemSetting
import com.istar.mediabroken.entity.account.Account
import com.istar.mediabroken.entity.account.LoginSourceEnum
import com.istar.mediabroken.entity.system.Message
import com.istar.mediabroken.repo.account.AccountRepo
import com.istar.mediabroken.repo.admin.SettingRepo
import com.istar.mediabroken.repo.system.MessageRepo
import com.istar.mediabroken.utils.*
import groovy.util.logging.Slf4j
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import javax.servlet.http.Cookie
import java.text.SimpleDateFormat

import static com.istar.mediabroken.api.ApiResult.apiResult

@Service("MessageService")
@Slf4j
class MessageService {
    @Autowired
    MessageRepo messageRepo
    @Autowired
    AccountRepo accountRepo
    @Autowired
    SettingRepo settingRepo

    int getNewMesageCount(long userId) {
        int newMessageCount = 0
        Date lastTime = messageRepo.getLastTimeByUserId(userId);
        if (!lastTime) {
            lastTime = messageRepo.getMinCreateTime(userId)
            if (lastTime) {
                lastTime = DateUitl.addDay(lastTime, -1)
            }
        }
        newMessageCount = messageRepo.getNewMesageCount(userId, lastTime)
        return newMessageCount
    }

    Map getMessage(long userId, Date prevTime, Date lastTime, int pageSize, int loginSource) {
        List<Message> list = []
        boolean updateStatus = false
        if (!prevTime) {//第一次查询
            updateStatus = true
            lastTime = messageRepo.getLastTimeByUserId(userId);//用于判断是否标记红点
        }
        if (!lastTime) {
            lastTime = messageRepo.getMinCreateTime(userId)
            if (lastTime) {
                lastTime = DateUitl.addDay(lastTime, -1)
            }
        }
        list = messageRepo.getMessage(userId, prevTime, pageSize)
        list.each {
            if (it.createTime > lastTime) {
                it.isRead = false
            } else {
                it.isRead = true
            }
        }
        if (loginSource == LoginSourceEnum.userLogin.key) {//用户前台登录走正常流程，模拟用户登录不走更新红点提示的流程
            if (updateStatus) {//第一次查询
                if (list) {//更新messageStatus中的lastTime
                    Message message = list.get(0)
                    messageRepo.modifyMessageStatus(userId, message ? message.createTime : lastTime)
                }
            }
        }
        return apiResult([status: HttpStatus.SC_OK, list: list, lastTime: lastTime])
    }

    void addMessageForAccount() {
        Date dateNow = new Date()
        //查询过期用户的信息
        List accountList = accountRepo.getUserList()
        SystemSetting systemSetting = settingRepo.getSystemSetting("accountTip", "expMsgSendDay")
        def sendDay = [] //系统配置天数
        if (systemSetting) {
            sendDay = systemSetting.content
        }
        if (sendDay) {
            sendDay = sendDay.sort { a, b ->
                (int) a.value - (int) b.value
            }//升序
        } else {
            sendDay = [1]
        }

        for (int i = 0; i < accountList.size(); i++) {
            Account account = accountList.get(i)
            Date expDate = account.expDate;
            String userTypeStr = ""
            if ("试用".equals(account.userType)) {
                userTypeStr = "试用"
            }
            if (dateNow > expDate) {//已过期
                if ((null == account.expMsgSendDay) || account.expMsgSendDay > 0) {
                    messageRepo.addMessage(account.id, "账号过期提示", "您的" + userTypeStr + "账号已到期，如需继续使用，请联系客服（客服电话：{appTelephone}），感谢您的试用")
                    accountRepo.updateAccountSendDay(account.id, 0)
                }
            } else {//尚未过期,根据配置天数提示
                int distance = DateUitl.getDistance(expDate, dateNow)//计算过期时间距当前时间的天数
                if (sendDay.contains(distance)) {
                    if (distance != account.expMsgSendDay) {
                        messageRepo.addMessage(account.id, "账号即将过期提示", "您的" + userTypeStr + "账号将在" + distance + "天后到期，如需继续使用，请联系客服（客服电话：{appTelephone}），感谢您的试用")
                        accountRepo.updateAccountSendDay(account.id, distance)

                    }
                } else {
                    if (distance > sendDay[sendDay.size() - 1]) {//表示distance未到配置天数的最大值
                        continue
                    } else {
                        def prevDay = 0
                        for (int j = 0; j < sendDay.size(); j++) {
                            if (distance > prevDay && distance < sendDay.get(j)) {
                                if (distance != account.expMsgSendDay) {
                                    messageRepo.addMessage(account.id, "账号即将过期提示", "您的" + userTypeStr + "账号将在" + distance + "天后到期，如需继续使用，请联系客服（客服电话：{appTelephone}），感谢您的试用")
                                    accountRepo.updateAccountSendDay(account.id, distance)
                                }
                            }
                            prevDay = sendDay.get(j)
                        }
                    }
                }
            }
        }

    }

}