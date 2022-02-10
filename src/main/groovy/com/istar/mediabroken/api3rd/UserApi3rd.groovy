package com.istar.mediabroken.api3rd

import groovy.util.logging.Slf4j
import org.apache.http.HttpStatus
import org.springframework.stereotype.Repository

@Repository
@Slf4j
class UserApi3rd extends BaseApi3rd {

    Map login(String userId, String password) {
        def result = doGet('LoginOK!login.do', [
                userid: userId,
                passwd: password
        ])
        if (!result.error) {
            return [status: HttpStatus.SC_OK, session: result.session, userId: result.ZhxgYqmsUser.KU_ID as long]
        } else if (result.error == "1") {
            return [status: HttpStatus.SC_UNAUTHORIZED, msg: "用户名和密码错误" ]
        } else {
            return [status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "未知错误, 错误码:${result.error}" ]
        }

    }

}
