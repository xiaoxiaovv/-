package com.istar.mediabroken.api.alioss

import com.istar.mediabroken.service.alioss.CallbackService
import com.istar.mediabroken.service.alioss.OssApiUrlService
import com.istar.mediabroken.service.alioss.OssMaterialService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @description: ossApi回调api
 * @author: hexushuai
 * @date: 2019/1/22 13:58
 */

@RestController
@RequestMapping(value = 'api/oss')
class CallbackController {

    @Autowired
    private CallbackService callbackService
    @Autowired
    private OssApiUrlService ossApiUrlService
    @Autowired
    private OssMaterialService materialService

    @RequestMapping(value = '/callback', method = RequestMethod.POST)
    public void doCallback(HttpServletRequest request, HttpServletResponse response) {
        Map callbackMap = callbackService.doCheckCallback(request)
        boolean check = callbackMap.get("result")
        long userId = Long.parseLong(callbackMap.get("userId"))
        String orgId = callbackMap.get("orgId")
        if (check) {
            String object = callbackMap.get("fileName")
            String toLower = object.toLowerCase()
            def videoType = ['.mp4', '.flv', '.mov', '.avi', '.rm', '.rmvb', '.wmv', '.mpg', '.mpeg' , '.mpeg2', '.mpeg3', '.mpeg4'] as String[]
            String material = "img"
            videoType.each { it ->
                if (toLower.endsWith(it)) {
                    material = "video"
                }
            }
            if (material.equals("video")) {
                String materialId
                try {
                    materialId = materialService.addMaterial(orgId, userId , object)
                    //视频截帧
                    ossApiUrlService.doCutFrameToImg(orgId, object)
                    //获取提交转码url
                    List urlList = ossApiUrlService.getSubmitTransCodeUrl(orgId, object)
                    //提交转码
                    urlList.each { it ->
                        materialService.submitTransCode(it, materialId, orgId, userId)
                    }
                } catch (Exception e) {
                    e.printStackTrace()
                }
            } else {
                materialService.addImg(orgId, userId, object)
            }

            callBackResponse(request, response, "{\"Status\":\"OK\"}", HttpServletResponse.SC_OK)
        } else {
            callBackResponse(request, response, "{\"Status\":\"verdify not ok\"}", HttpServletResponse.SC_BAD_REQUEST)
        }
    }

    private void callBackResponse(HttpServletRequest request, HttpServletResponse response, String results, int status)
            throws IOException {
        String callbackFunName = request.getParameter("callback")
        response.addHeader("Content-Length", String.valueOf(results.length()))
        if (callbackFunName == null || callbackFunName.equalsIgnoreCase(""))
            response.getWriter().println(results)
        else
            response.getWriter().println(callbackFunName + "( " + results + " )")
        response.setStatus(status)
        response.flushBuffer()
    }

    private void callBackResponse(HttpServletRequest request, HttpServletResponse response, String results) throws IOException {
        String callbackFunName = request.getParameter("callback")
        if (callbackFunName == null || callbackFunName.equalsIgnoreCase(""))
            response.getWriter().println(results)
        else
            response.getWriter().println(callbackFunName + "( " + results + " )")
        response.setStatus(HttpServletResponse.SC_OK)
        response.flushBuffer()
    }
}
