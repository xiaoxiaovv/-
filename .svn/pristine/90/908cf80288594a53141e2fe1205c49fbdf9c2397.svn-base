package com.istar.mediabroken.console

import com.istar.mediabroken.service.copyright.MonitorService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.text.SimpleDateFormat

@Component
@Slf4j
public class ArticleOperationExcelOutConsole implements Console {
    @Autowired
    MonitorService monitorService

    @Override
    public void execute(Map properties) {
        String start = properties.startDate
        String end = properties.endDate
        boolean searchData = false;
        searchData = Boolean.valueOf(properties.searchData)

        if (!start) {
            println '开始时间startDate不能为空'
            return
        }
        if (!end) {
            println '结束时间endDate不能为空'
            return
        }

        def sdf = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss')
        Date startDate = sdf.parse(start)
        Date endDate = sdf.parse(end)
        if (!startDate) {
            println '开始时间startDate不合法'
            return
        }
        if (!endDate) {
            println '结束时间endDate不合法'
            return
        }
        List list = monitorService.queryAllArticleOperation(startDate, endDate);
        //記得改
        monitorService.excelOutArticleOperation(list)
        println "导出同步稿件表"
    }

    @Override
    public String getPropertyFileName() {
        return "articleOperation_date.properties";
    }

}
