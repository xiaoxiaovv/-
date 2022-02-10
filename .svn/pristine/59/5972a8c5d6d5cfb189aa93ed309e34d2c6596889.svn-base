package com.istar.mediabroken.service.admin

import com.istar.mediabroken.entity.*
import com.istar.mediabroken.repo.account.AccountRepo
import com.istar.mediabroken.repo.admin.SettingRepo
import com.istar.mediabroken.repo.capture.NewsRepo
import groovy.util.logging.Slf4j
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import static com.istar.mediabroken.api.ApiResult.apiResult

@Service
@Slf4j
class SystemAdminService {
    @Autowired
    AccountRepo accountRepo
    @Autowired
    SettingRepo settingRepo
    @Autowired
    NewsRepo newsRepo

    Map modifyAccountProfile(String adminUsername,String adminPassword,String name,String mediaSiteCount,String wechatSiteCount,String professionalSiteCount){
        //验证管理员用户名密码
        boolean isAdminAvailable =false;
        int maxMediaSiteCount = 0;
        int maxWechatSiteCount = 0;
        int maxProfessionalSiteCount = 0;

        if ("".equals(adminUsername)||"".equals(adminPassword)){
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '用户名或密码为必填项！')
        }
        if ("".equals(name)){
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '请输入需要设置的用户名')
        }
        try{
            maxMediaSiteCount = Integer.parseInt(mediaSiteCount);
            maxWechatSiteCount = Integer.parseInt(wechatSiteCount);
            maxProfessionalSiteCount = Integer.parseInt(professionalSiteCount);
        }catch (Exception e){
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '请输入正确的网站数量')
        }
        if (maxMediaSiteCount<0||maxWechatSiteCount<0||maxProfessionalSiteCount<0){
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '请输入正确的网站数量')
        }else{
            isAdminAvailable = settingRepo.isAdminAvailable(adminUsername,adminPassword)
        }
        if (!isAdminAvailable){
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '用户名或密码错误！')
        }
        //通过name获取userId
        Map user = accountRepo.getUserByName(name);
        long userId =user.userId;
        Date now = new Date();
        AccountProfile accountProfile = accountRepo.getAccountProfileByUser(userId)
        def captureSite=[maxMediaSiteCount:0,maxWechatSiteCount:0,maxProfessionalSiteCount:0];

        if(accountProfile == null){
            accountProfile = new AccountProfile()
            accountProfile.userId = userId
            accountProfile.createTime = now
        }
        captureSite.maxMediaSiteCount=maxMediaSiteCount;
        captureSite.maxWechatSiteCount=maxWechatSiteCount;
        captureSite.maxProfessionalSiteCount=maxProfessionalSiteCount;
        accountProfile.captureSite =captureSite
        accountProfile.updateTime = now
        accountRepo.saveAccountProfile(accountProfile)

        return apiResult([status: HttpStatus.SC_OK,msg: "修改成功"])
    }

    Map modifySystemSetting(String ids){
        String[] content = ids?ids.split(","):[]
        //验证是否为有效id
        for (int i = 0; i < content.size(); i++) {
            Map map = newsRepo.getNewsWithoutContentById(content[i])
            if (!map){
                return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "非有效ID"])
            }
            if (map && !map.imgUrls){
                return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "非有效ID"])
            }
        }
        boolean result = settingRepo.modifySystemSetting(content)
        if (result){
            return apiResult([status: HttpStatus.SC_OK, msg: "修改成功"])
        }else{
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "修改失败"])
        }
    }
}
