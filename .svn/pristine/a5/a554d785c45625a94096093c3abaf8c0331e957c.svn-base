package com.istar.mediabroken.api

import com.alibaba.fastjson.JSONObject
import com.istar.mediabroken.utils.UploadUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import javax.servlet.http.HttpServletRequest

import static com.istar.mediabroken.api.ApiResult.apiResult

/**
 * Created by ZHXG on 2017/4/10.w
 */
@Controller
@RequestMapping(value = "/api/upload")
class FileUploadApiController {

    @Value('${image.upload.path}')
    public String uploadPath

    @RequestMapping(value = "/img", method = RequestMethod.POST, produces = ["text/plain;charset=UTF-8"])
    @ResponseBody
    String uploadImage(
            HttpServletRequest request,
            @RequestParam(value = "type") String type
    ) {
        def res = UploadUtil.uploadImg(request, uploadPath, type ?: '/img')
        return JSONObject.toJSONString(apiResult(res))
    }

}

