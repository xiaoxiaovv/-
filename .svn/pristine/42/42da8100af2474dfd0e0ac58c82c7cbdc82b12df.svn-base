package com.istar.mediabroken.console

import com.istar.mediabroken.service.account.AccountService;
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class AddUserConsole implements Console {
    @Autowired
    AccountService accountSrv

    @Override
    public void execute(Map properties) {

// 以上代码不能修改
/////////////////////////////////////////////////////////////////
//账号类别 试用、正式
        String userType = properties.userType
//用户名
        String userName = properties.userName
//手机号
        String phoneNumber = properties.phoneNumber
//密码
        String password = properties.password
//账号有效期 ？天
        String expDate = properties.expDate
//所属媒体全称
        String company = properties.company
//用户真实姓名
        String realName = properties.realName
//用户所在地区
        String userArea = properties.userArea
//职务 1\2\3
        int duty = Integer.valueOf(properties.duty)

//设置默认站点数目 1
        int maxMediaSiteCount = Integer.valueOf(properties.maxMediaSiteCount)
        int maxWechatSiteCount = Integer.valueOf(properties.maxWechatSiteCount)
/////////////////////////////////////////////////////////////////

        if (!userName) {
            println 'userName不能为空'
            return
        }
// 以下代码不能修改
        def result = accountSrv.addUser(userName, password, phoneNumber, userType, expDate, company, realName, userArea, duty, maxMediaSiteCount, maxWechatSiteCount)
        println result
    }

    @Override
    public String getPropertyFileName() {
        return "add_user.properties";
    }
}
