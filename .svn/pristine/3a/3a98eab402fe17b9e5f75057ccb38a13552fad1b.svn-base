package com.istar.mediabroken.openapi

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import static com.istar.mediabroken.api.ApiResult.apiResult
import static org.springframework.web.bind.annotation.RequestMethod.GET

@RequestMapping(value = "/openapi")
@RestController
class DemoOApiController {

    @RequestMapping(value = "/demo", method = GET)
    Map getDemo(@OrgId String orgId, @Params MapWraper data) {
        def params = data.getMap()
        return apiResult([orgId: orgId, x: params.x, y: params.y])
    }

}