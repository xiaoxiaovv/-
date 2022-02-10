package com.istar.mediabroken.api.account

import com.alibaba.fastjson.JSONObject
import com.istar.mediabroken.api.CurrentUser
import com.istar.mediabroken.entity.LoginUser
import com.istar.mediabroken.service.account.AccountCustomSettingService
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

import static com.istar.mediabroken.api.ApiResult.apiResult

/**
 * Author : YCSnail
 * Date   : 2017-12-07
 * Email  : liyancai1986@163.com
 */
@RestController
@RequestMapping(value = '/api/custom')
class CustomSettingApiController {

    @Autowired
    AccountCustomSettingService accountCustomSettingService
    /**
     * 用户需要在首页显示的功能模块（有顺序）
     * @param user
     * @return
     */
    @RequestMapping(value = '/dashboard/modules', method = RequestMethod.GET)
    Object getDashboardModules(
            @CurrentUser LoginUser user
    ) {
        def result = accountCustomSettingService.getDashboardModules(user.getUserId())
        return apiResult([list: result])
    }

    /**
     * 更新首页功能模块排序
     * @param user
     * @return
     */
    @RequestMapping(value = '/dashboard/modules', method = RequestMethod.PUT)
    Object modifyDashboardModules(
            @CurrentUser LoginUser user,
            @RequestParam(value = 'content', required = false, defaultValue = "") String[] content
    ) {
        try {
            accountCustomSettingService.modifyDashboardModules(user.getUserId(), content)
            return apiResult(HttpStatus.SC_OK, "修改成功")
        } catch (Exception e) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "修改失败")
        }
    }

    @RequestMapping(value = '/dashboard/module/setting', method = RequestMethod.GET)
    Object getModuleCustomSetting(
            @CurrentUser LoginUser user
    ) {
        try {
            def result = accountCustomSettingService.getDashboardModulesSetting(user.getUserId())
            return apiResult(result)
        } catch (Exception e) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "加载失败")
        }
    }

    @RequestMapping(value = '/dashboard/{module}/setting', method = RequestMethod.PUT)
    Object modifyModuleCustomSetting(
            @CurrentUser LoginUser user,
            @RequestBody String data,
            @PathVariable(value = "module") String module
    ) {
        try {
            JSONObject dataJson = JSONObject.parseObject(data);
            String ids  = dataJson.get("ids").toString();
            accountCustomSettingService.modifyAccountCustomDashboardSettingByType(user.userId, ids, module)
            return apiResult(HttpStatus.SC_OK, "修改成功")
        } catch (Exception e) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, "修改失败")
        }
    }

}
