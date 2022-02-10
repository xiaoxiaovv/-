package com.istar.mediabroken.console

import com.istar.mediabroken.service.capture.NewsService
import com.istar.mediabroken.service.compile.MaterialService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@Slf4j
public class NewsAbstractExcelConsole implements Console {
    @Autowired
    MaterialService materialService

    @Override
    public void execute(Map properties) {
        List materials = materialService.queryUserMaterials();
        materialService.excelOutMaterials(materials)
        println "导出我的文稿明细"
    }

    @Override
    public String getPropertyFileName() {
        return "";
    }
}
