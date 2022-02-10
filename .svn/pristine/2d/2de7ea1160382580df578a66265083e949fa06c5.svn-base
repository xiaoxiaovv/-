package com.istar.mediabroken.service.system

import com.istar.mediabroken.entity.SystemSetting
import com.istar.mediabroken.repo.admin.SettingRepo
import groovy.util.logging.Slf4j
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import static com.istar.mediabroken.api.ApiResult.apiResult

@Service("SystemSettingService")
@Slf4j
class SystemSettingService {
    @Autowired
    SettingRepo settingRepo

    Map getSystemSettingByTypeAndKey(String type, String key) {
        SystemSetting systemSetting = settingRepo.getSystemSetting(type, key)
        def content = systemSetting ? systemSetting.content : []
        return apiResult([status: HttpStatus.SC_OK, content: content])
    }

}