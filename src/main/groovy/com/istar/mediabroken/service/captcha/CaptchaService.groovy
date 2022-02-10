package com.istar.mediabroken.service.captcha

import com.istar.mediabroken.repo.CaptchaRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Author : YCSnail
 * Date   : 2018-03-15
 * Email  : liyancai1986@163.com
 */
@Service
class CaptchaService {

    @Autowired
    private CaptchaRepo captchaRepo

    def createVerifyCode(String key, String verifyCode, String ua) {
        captchaRepo.update(key, verifyCode, ua)
    }

    def getCaptcha(String key) {
        def captcha = captchaRepo.get(key)
        if(captcha) {
            captcha.verifyCode = (captcha.verifyCode as String)?.toLowerCase()
        }
        return captcha
    }

    def deleteCaptcha(String key){
        return captchaRepo.remove(key)
    }

}
