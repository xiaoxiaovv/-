package com.istar.mediabroken.api.alioss

import com.alibaba.fastjson.JSONObject
import com.istar.mediabroken.api.CurrentUser
import com.istar.mediabroken.entity.LoginUser
import com.istar.mediabroken.service.alioss.OssApiUrlService
import com.istar.mediabroken.service.alioss.OssMaterialService
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import static com.istar.mediabroken.api.ApiResult.apiResult

/**
 * @description: 阿里云对象存储api
 * @author: hexushuai
 * @date: 2019/1/18 9:23
 */
@RestController
@RequestMapping(value = '/api/oss')
class OssApiController {

    @Autowired
    private OssMaterialService materialService
    @Autowired
    private OssApiUrlService ossApiUrlService

    @RequestMapping(value = '/video', method = RequestMethod.GET)
    void videoUpload(@CurrentUser LoginUser user, HttpServletRequest request, HttpServletResponse response) {
        try {
            JSONObject ja1 = materialService.upload(user.getOrgId(), user.getUserId(), "video")
            response.setHeader("Access-Control-Allow-Origin", "*")
            response.setHeader("Access-Control-Allow-Methods", "GET, POST")
            uploadResponse(request, response, ja1.toString())
            println("获取地址处理完成")
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    @RequestMapping(value = '/img', method = RequestMethod.GET)
    void imageUpload(@CurrentUser LoginUser user, HttpServletRequest request, HttpServletResponse response) {
        try {
            JSONObject ja1 = materialService.upload(user.getOrgId(), user.getUserId(), "img")
            response.setHeader("Access-Control-Allow-Origin", "*")
            response.setHeader("Access-Control-Allow-Methods", "GET, POST")
            uploadResponse(request, response, ja1.toString())
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    @RequestMapping(value = '/repeatTransCode', method = RequestMethod.GET)
    Map submitTransCode(@CurrentUser LoginUser user, @RequestParam("id") String id,
                        @RequestParam("videoSourceUrl") String videoSourceUrl) {
        try {
            String object = videoSourceUrl.substring(videoSourceUrl.indexOf(".com") + 5, videoSourceUrl.length())
            List urlList = ossApiUrlService.getSubmitTransCodeUrl(user.getOrgId(), object)
            urlList.each { it ->
                materialService.submitTransCode(it, id, user.getOrgId(), user.getUserId())
            }
        } catch (Exception e) {
            e.printStackTrace()
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "提交转码异常"])
        }

        return apiResult([status: HttpStatus.SC_OK, msg: "提交转码成功"])
    }

    private void uploadResponse(HttpServletRequest request, HttpServletResponse response, String results) throws IOException {
        String callbackFunName = request.getParameter("callback")
        if (callbackFunName == null || callbackFunName.equalsIgnoreCase(""))
            response.getWriter().println(results)
        else
            response.getWriter().println(callbackFunName + "( " + results + " )")
        response.setStatus(HttpServletResponse.SC_OK)
        response.flushBuffer()
    }

}
