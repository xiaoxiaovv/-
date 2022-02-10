package com.istar.mediabroken.api.compile

import com.istar.mediabroken.api.CurrentUserId
import com.istar.mediabroken.service.compile.StyleService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.*

import javax.servlet.http.HttpServletRequest
import static com.istar.mediabroken.api.ApiResult.apiResult
import static org.springframework.web.bind.annotation.RequestMethod.*

/**
 * Author: zc
 * Time: 2017/8/3
 */
@RestController
@Slf4j
@RequestMapping(value = "/api/compile/")
class StyleApiController {
    @Autowired
    StyleService styleService
    @Value('${image.upload.path}')
    String UPLOAD_PATH

    /**
     * 样式上传
     * @param userId
     * @param request
     * @return
     */
    @RequestMapping(value = "style/import", method = POST)
    public Map addStyle(
            @CurrentUserId Long userId,
            @RequestParam String content
    ) {
        def result = styleService.addUserStyle(userId, content)
        return result
    }

    /**
     * 样式列表
     * @param userId
     * @return
     */
    @RequestMapping(value = "styles", method = GET)
    public Map getStyles(
            @CurrentUserId Long userId
    ) {
        def result = styleService.getUserStyles(userId);
        return result;
    }

    /**
     * 删除样式
     * @param userId
     * @param request
     * @param id
     * @return
     */
    @RequestMapping(value = "style/{id}", method = DELETE)
    public Map removeStyle(
            @CurrentUserId Long userId,
            HttpServletRequest request,
            @PathVariable("id") String id
    ) {
        styleService.removeStyle(userId, request, id)
        return apiResult();
    }
}
