package console

import com.istar.mediabroken.service.account.AccountService
import com.istar.mediabroken.service.admin.SystemAdminService
import org.springframework.context.support.ClassPathXmlApplicationContext

def ctx = new ClassPathXmlApplicationContext("classpath*:spring*.xml");
def systemAdminSrv = ctx.getBean(SystemAdminService)

// 以上代码不能修改
/////////////////////////////////////////////////////////////////
String ids="167198e7bfe3f633de0e5ca3a2185a81,58e421e84ed3b2224c9790d49681ab62,83f201f67e367abb377cf1580fd83a63"
/////////////////////////////////////////////////////////////////
// 以下代码不能修改
Map result = systemAdminSrv.modifySystemSetting(ids);
println(result)