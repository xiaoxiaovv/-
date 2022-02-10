package com.istar.mediabroken.console

import com.istar.mediabroken.service.capture.SiteService
import groovy.util.logging.Slf4j
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import static com.istar.mediabroken.api.ApiResult.apiResult

@Component
@Slf4j
public class ImportSiteVsWeiboConsole implements Console {
    @Autowired
    SiteService siteService

    @Override
    public void execute(Map properties) {
        //路径
        String pathname = properties.pathname
        if ("".equals(pathname)){
            println(apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "请设置导入路径"]))
        }
        println(pathname)
        siteService.importSiteVsWeibo(pathname)
    }

    @Override
    public String getPropertyFileName() {
        return "import_siteVsWeibo.properties";
    }
}
