package com.istar.mediabroken.api.system

import com.alibaba.fastjson.JSONObject
import com.istar.mediabroken.utils.DownloadUtils
import com.istar.mediabroken.utils.UploadUtil
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import static com.istar.mediabroken.api.ApiResult.apiResult

/**
 * Created by zc on 2018/3/9.w
 */
@Controller
@RequestMapping(value = "/api/wiki/download")
class ApiDockingInstructionApiController {

    @Value('${pdf.download.path}')
    public String downloadPath

    @RequestMapping(value = "/apiPdf", method = RequestMethod.GET)
    @ResponseBody
    Object downloadPdf(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String fileName = "编++公共接口协议标准版.pdf"
        DownloadUtils.downLoadFromUrl(downloadPath, fileName, response)
        return apiResult()
    }

}

