package com.istar.mediabroken.console

import com.istar.mediabroken.service.account.AccountService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@Slf4j
public class UserListConsole implements Console {
    @Autowired
    AccountService accountSrv

    @Override
    public void execute(Map properties) {
        def result=accountSrv.getUserList()
        println result
    }

    @Override
    public String getPropertyFileName() {
        return "";
    }
}
