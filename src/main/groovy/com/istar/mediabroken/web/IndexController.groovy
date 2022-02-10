package com.istar.mediabroken.web

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

/**
 * Author : YCSnail
 * Date   : 2017-04-11
 * Email  : liyancai1986@163.com
 */
@Controller
class IndexController {

    @RequestMapping(value = '/')
    public String index(){
        return "redirect:/index.html"
    }
}
