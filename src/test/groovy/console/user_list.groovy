package console

import com.istar.mediabroken.service.account.AccountService
import org.springframework.context.support.ClassPathXmlApplicationContext

def ctx = new ClassPathXmlApplicationContext("classpath*:spring*.xml");
def accountSrv = ctx.getBean(AccountService)

// 以上代码不能修改
/////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////
// 以下代码不能修改
def result =accountSrv.getUserList()
println result