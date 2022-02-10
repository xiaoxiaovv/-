package com.istar.mediabroken.console

import com.istar.mediabroken.service.account.AccountService
import com.istar.mediabroken.service.capture.SubjectService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@Slf4j
public class SubjectExcelConsole implements Console {
    @Autowired
    SubjectService subjectService

    @Override
    public void execute(Map properties) {
        List list = subjectService.queryAllSubjects();
        subjectService.excelOut(list)
        println "导出全网监控列表"
    }

    @Override
    public String getPropertyFileName() {
        return "";
    }
}
