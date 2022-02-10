package com.istar.mediabroken.api.compile

import com.alibaba.fastjson.JSONObject
import com.istar.mediabroken.api.CheckExpiry
import com.istar.mediabroken.api.CurrentUser
import com.istar.mediabroken.entity.LoginUser
import com.istar.mediabroken.service.compile.TagService
import groovy.util.logging.Slf4j
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletRequest

import static com.istar.mediabroken.api.ApiResult.apiResult
import static org.springframework.web.bind.annotation.RequestMethod.GET
import static org.springframework.web.bind.annotation.RequestMethod.POST

/**
 * @author zxj
 * @create 2019/1/17
 */
@RestController
@Slf4j
@RequestMapping(value = "/api/tag/")
class TagApiController {

    @Autowired
    TagService tagService;

    @Value('${image.upload.path}')
    public String uploadPath

    @RequestMapping(value = "import", method = RequestMethod.POST, produces = ["text/plain;charset=UTF-8"])
    @CheckExpiry
    public Object sitesImport(
            HttpServletRequest request,
            @RequestParam(value = "orgId", required = true) String orgId
    ) {
//        def res = UploadUtil.uploadExcel(request, uploadPath, '/xls')
//        if (!(res.status == HttpStatus.SC_OK)) {
//            return JSONObject.toJSONString(apiResult(res))
//        }
//        File f1 = new File(uploadPath + File.separator + res.msg)
//        def result = [status: HttpStatus.SC_OK, msg: '']
//        if (null == f1) {
//            result.msg = "模板文件为空,请选择文件";
//            result.status = HttpStatus.SC_BAD_REQUEST;
//            return JSONObject.toJSONString(apiResult(result))
//        } else {
//            result = siteService.manySitesImport(userId, f1);
//        }
        def time = new Date().time
        log.info("导入开始：{}", time)
        tagService.orgTagImport(orgId, "D:/tagTemplate.xls")
        def time1 = new Date().time
        log.info("导入结束：{}", time1)
        return JSONObject.toJSONString("导入成功，耗时：" + (time1 - time))
    }

    @RequestMapping(value = "list", method = GET)
    public Object getTagList(
            @CurrentUser LoginUser user

    ) {
        def list = tagService.getUerTagList(user)
        return apiResult(["status": HttpStatus.SC_OK, "list": list]);
    }


    @RequestMapping(value = "/history", method = POST)
    public Object addTagHistory(
            @CurrentUser LoginUser user,
            @RequestParam(value = "tagIds", required = true, defaultValue = "") String tagIds
    ) {
        def list =[]
        if (tagIds.length() == 0) {
            list =[]
        }else {
            def split = tagIds.split(",")
            if (split.size() > 50) {
                return apiResult(["status": HttpStatus.SC_INTERNAL_SERVER_ERROR, "msg": "栏目最多选择50个"]);
            }
            list = Arrays.asList(split)
        }


        tagService.saveTagHistory(user.userId, list)
        return apiResult(["status": HttpStatus.SC_OK,"msg":"修改成功"]);
    }

}
