package com.istar.mediabroken.service

import com.istar.mediabroken.repo.admin.SettingRepo
import com.istar.mediabroken.repo.SystemNoticeRepo
import groovy.util.logging.Slf4j
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import static com.istar.mediabroken.api.ApiResult.apiResult

/**
 * Author : YCSnail
 * Date   : 2017-07-06
 * Email  : liyancai1986@163.com
 */
@Service
@Slf4j
class SystemNoticeService {

    @Autowired
    SystemNoticeRepo noticeRepo
    @Autowired
    SettingRepo settingRepo

    List getSystemNoticeList() {
        return noticeRepo.getNoticeList()
    }

    def getLatestNotice() {
        def list = getSystemNoticeList()
        return list ? list.get(0) : null
    }

    def createNotice(String cont, Date expireTime) {
        try {
            noticeRepo.add(cont, expireTime.time)
        }catch (Exception e) {
            log.error('{}', e)
        }
    }

    def delLatestNotice() {
        def notice = this.getLatestNotice()
        if(notice) {
            noticeRepo.update(notice.id)
        }
    }
    boolean isAdminAvailable(String adminUsername,String adminPassword){
        boolean isAdminAvailable =false;
        if ("".equals(adminUsername)||"".equals(adminPassword)){
            isAdminAvailable =false;
        }
        isAdminAvailable = settingRepo.isAdminAvailable(adminUsername,adminPassword)
        return isAdminAvailable;
    }

}
