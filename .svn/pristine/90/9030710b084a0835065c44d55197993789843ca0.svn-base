package console

import com.istar.mediabroken.service.account.AccountService
import com.istar.mediabroken.service.capture.SiteService
import org.springframework.context.support.ClassPathXmlApplicationContext

def ctx = new ClassPathXmlApplicationContext("classpath*:spring*.xml");
def siteService = ctx.getBean(SiteService)

// 以上代码不能修改
/////////////////////////////////////////////////////////////////
//在导出的excel中最后加一列message，一下路径为需要导入的excel的路径

/////////////////////////////////////////////////////////////////
// 以下代码不能修改
def result =siteService.importSiteMessage()
println result