package console

import com.istar.mediabroken.service.account.AccountService
import org.springframework.context.support.ClassPathXmlApplicationContext

def ctx = new ClassPathXmlApplicationContext("classpath*:spring*.xml");
def accountSrv = ctx.getBean(AccountService)

// 以上代码不能修改
/////////////////////////////////////////////////////////////////
//账号类别 试用、正式
def userType="试用"
//用户名
def userName = "zcc2"
//手机号
def phoneNumber = '12345678910'
//密码
def password = 'pw'
//账号有效期 ？天
def expDate="2017-08-31"
//所属媒体全称
def company="testcom2"
//用户真实姓名
def realName="张小花"
//用户所在地区
def userArea="北京"
//职务 1\2\3
def duty=1

//设置默认站点数目 1
def maxMediaSiteCount=15
def maxWechatSiteCount=15
/////////////////////////////////////////////////////////////////

if (!userName) {
    println 'userName不能为空'
    return
}
// 以下代码不能修改
def result =accountSrv.addUser(userName, password, phoneNumber,userType,expDate,company,realName,userArea,duty,maxMediaSiteCount,maxWechatSiteCount)
println result