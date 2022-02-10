package com.istar.mediabroken.api.admin

import com.istar.mediabroken.service.admin.SystemAdminService;
import groovy.util.logging.Slf4j;
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController

import static org.springframework.web.bind.annotation.RequestMethod.PUT;

/**
 * Author: Luda
 * Time: 2017/7/7
 */
@RestController
@Slf4j
@RequestMapping(value = "/api/admin")
class SystemAdminApiController {
    @Autowired
    SystemAdminService systemAdminSrv;
    /**
     * 保存用户站点数量设置
     * @param user
     * @return
     */
    @RequestMapping(value = "/accountProfile/captureSite", method = PUT)
    @ResponseBody
    public Map modifyAccountProfile(
            @RequestParam(value = "adminUsername", required = true) String adminUsername,
            @RequestParam(value = "adminPassword", required = true) String adminPassword,
            @RequestParam(value = "name", required = true) String name,
            @RequestParam(value = "maxMediaSiteCount", required = false) String maxMediaSiteCount,
            @RequestParam(value = "maxWechatSiteCount", required = false) String maxWechatSiteCount,
            @RequestParam(value = "maxProfessionalSiteCount", required = false) String maxProfessionalSiteCount
    ) {
        Map result = systemAdminSrv.modifyAccountProfile(adminUsername, adminPassword, name, maxMediaSiteCount, maxWechatSiteCount, maxProfessionalSiteCount);
        return result;
    }

    @RequestMapping(value = "/hotPicNews/{ids}", method = PUT)
    public Map modifySystemSetting(
            @PathVariable ids
    ) {
        Map result = systemAdminSrv.modifySystemSetting(ids);
        return result;
    }
}
