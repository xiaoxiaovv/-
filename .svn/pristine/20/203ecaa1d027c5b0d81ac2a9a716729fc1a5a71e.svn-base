package com.istar.mediabroken.api.rubbish

import com.istar.mediabroken.api.CurrentUser
import com.istar.mediabroken.entity.LoginUser
import com.istar.mediabroken.service.rubbish.RubbishNewsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import static com.istar.mediabroken.api.ApiResult.apiResult

/**
 * Author : YCSnail
 * Date   : 2017-11-09
 * Email  : liyancai1986@163.com
 */
@RestController
@RequestMapping(value = "/api/rubbish/news")
class RubbishNewsApiController {

    @Autowired
    private RubbishNewsService rubbishNewsService

    @RequestMapping(value = "/{newsId}", method = RequestMethod.PUT)
    Map addGroup(
            @CurrentUser LoginUser user,
            @PathVariable String newsId
    ){
        rubbishNewsService.addNews2Rubbish(user.userId, newsId)
        return apiResult()
    }



}
