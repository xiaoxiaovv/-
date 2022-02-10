package com.istar.mediabroken.api.tags

import com.alibaba.fastjson.JSONObject
import com.istar.mediabroken.api.CurrentUser
import com.istar.mediabroken.entity.LoginUser
import com.istar.mediabroken.service.tags.TagsManageService
import com.istar.mediabroken.utils.PmsApiUtils
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import static com.istar.mediabroken.api.ApiResult.apiResult
import static org.springframework.web.bind.annotation.RequestMethod.DELETE
import static org.springframework.web.bind.annotation.RequestMethod.GET
import static org.springframework.web.bind.annotation.RequestMethod.POST
import static org.springframework.web.bind.annotation.RequestMethod.PUT

/**
 * @description: 标签管理
 * @author: hexushuai
 * @date: 2019/2/27 16:52
 */
@RestController
@RequestMapping("/api/tags")
class TagsManageController {

    @Value('${pms.api.url}')
    private String url
    @Value('${pms.api.key}')
    private String appKey
    @Value('${pms.api.secret}')
    private String appSecret
    @Autowired
    private TagsManageService tagsManageService

    /**
     * 查询标签列表
     */
    @RequestMapping(value = "/list", method = GET)
    public Map getTagsList(@CurrentUser LoginUser user) {
        List list = tagsManageService.getTagsByOrgId(user.getOrgId())
        return apiResult(status: HttpStatus.SC_OK, list: list)
    }

    /**
     * 新增标签
     */
    @RequestMapping(value = "/add", method = POST)
    public Map addTag(@CurrentUser LoginUser user,
                      @RequestParam(value = "tagName", required = true) String tagName,
                      @RequestParam(value = "tagType", required = true) String tagType,
                      @RequestParam(value = "tagValue", required = true) List tagValue) {
        boolean result = tagsManageService.addTag(user.getOrgId(), tagName, tagType, tagValue)
        if(!result) {
            return apiResult(status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "该标签已存在")
        }
        return apiResult(status: HttpStatus.SC_OK, msg: "创建成功")
    }

    /**
     * 获取标签详情
     */
    @RequestMapping(value = "/detail/{id}", method = GET)
    public Map getTagInfo(@PathVariable(value = "id") String id) {
        Map map = tagsManageService.getTagInfoById(id)
        return apiResult(status: HttpStatus.SC_OK, tag: map)
    }

    /**
     * 修改标签
     */
    @RequestMapping(value = "/update/{id}", method = PUT)
    public Map updateTag(@PathVariable(value = "id") String id,
                         @RequestParam(value = "tagName", required = true) String tagName,
                         @RequestParam(value = "tagType", required = true) String tagType,
                         @RequestParam(value = "tagValue", required = true) List tagValue) {
        tagsManageService.updateTag(id, tagName, tagType, tagValue)
        return apiResult(status: HttpStatus.SC_OK, msg: "修改成功")
    }

    /**
     * 删除标签
     */
    @RequestMapping(value = "/delete/{id}", method = DELETE)
    public Map deleteTag(@PathVariable(value = "id") String id) {
        tagsManageService.deleteTagById(id)
        return apiResult(status: HttpStatus.SC_OK, msg: "删除成功")
    }

    /**
     * 获取地域信息
     */
    @RequestMapping(value = "/area", method = GET)
    public Map getArea(@RequestParam(value = "parentId", required = false, defaultValue = "0") String parentId) {
        Map<String, Object> paramMap = new HashMap<>()
        List<Map> areaList = new ArrayList<>()
        paramMap.put("pid", parentId)
        Map<String, Object> areaResult = PmsApiUtils.get(url + "/district", appKey, appSecret, paramMap)
        if ("0".equals(parentId)){
            Map<String, String> all = new HashMap<>()
            all.put("name", "全国")
            all.put("id", "-1")
            all.put("parentId", "-3")
            areaList.add(all)
        }
        areaList.addAll((List<Map>) ((JSONObject) areaResult.get("body")).get("list"))
        if ("0".equals(parentId)){
            Map<String, String> other = new HashMap<>()
            other.put("name", "其它")
            other.put("id", "-2")
            other.put("parentId", "-3")
            areaList.add(other)
        }
        return apiResult(status: HttpStatus.SC_OK, list: areaList)
    }
}
