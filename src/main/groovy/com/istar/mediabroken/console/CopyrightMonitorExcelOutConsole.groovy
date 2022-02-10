package com.istar.mediabroken.console

import com.istar.mediabroken.service.copyright.MonitorService
import com.istar.mediabroken.utils.ExportExcel
import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils
import org.apache.http.HttpStatus
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.text.SimpleDateFormat

@Component
@Slf4j
public class CopyrightMonitorExcelOutConsole implements Console {
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
        List list = monitorService.queryAllMonitors(startDate, endDate);
        monitorService.excelOut(list)
        println "导出版权监控表"
    }

    @Override
    public String getPropertyFileName() {
        return "copyrightMonitor_date.properties";
    }

}
