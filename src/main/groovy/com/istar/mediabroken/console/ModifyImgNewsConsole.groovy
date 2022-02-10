package com.istar.mediabroken.console

import com.istar.mediabroken.service.account.AccountService
import com.istar.mediabroken.service.admin.SystemAdminService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@Slf4j
public class ModifyImgNewsConsole implements Console {
    @Autowired
    SystemAdminService systemAdminSrv

    @Override
    public void execute(Map properties) {

// 以上代码不能修改
/////////////////////////////////////////////////////////////////
//设置的三条新闻ID
        String ids = properties.ids
/////////////////////////////////////////////////////////////////
// 以下代码不能修改
        def result = systemAdminSrv.modifySystemSetting(ids);
        println result
    }

    @Override
    public String getPropertyFileName() {
        return "modify_imgNews.properties";
    }
}
