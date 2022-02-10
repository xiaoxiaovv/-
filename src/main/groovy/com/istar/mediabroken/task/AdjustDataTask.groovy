package com.istar.mediabroken.task

import com.istar.mediabroken.service.capture.SubjectService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@Slf4j
class AdjustDataTask implements Task {
    @Autowired
    SubjectService subjectService

    @Override
    void execute() {
        subjectService.adjustSubjectKeywords()
    }
}
