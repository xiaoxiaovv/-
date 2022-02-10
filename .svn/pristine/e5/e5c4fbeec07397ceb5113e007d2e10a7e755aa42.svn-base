package com.istar.mediabroken.console

import com.istar.mediabroken.entity.account.AccountApply
import com.istar.mediabroken.service.account.AccountService
import com.istar.mediabroken.service.analysis.ChannelService
import com.istar.mediabroken.utils.DateUitl
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
public class AnalysisSitesExcelOutConsole implements Console {
    @Autowired
    ChannelService channelService

    @Override
    public void execute(Map properties) {
//查询所有申请用户
        def sdf = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss')
        String startTime = "1970-01-01 00:00:00"
        Date startDate = sdf.parse(startTime)
        List nodataSites=channelService.getNoDataSites()
        List returnList = new ArrayList()
        println("传播测评未采集站点查询中，请稍候……")
        for (int i = 0; i < nodataSites.size(); i++) {
            def obj = nodataSites.get(i)
            switch (obj.siteType){
                case 1:
                    obj.siteTypeName="网站"
                    break;
                case 2:
                    obj.siteTypeName="微博"
                    break;
                case 3:
                    obj.siteTypeName="微信"
                    break;
                default:
                    obj.siteTypeName="网站"
                    break;

            }

            returnList.add(obj)
        }
            String outfileName = UUID.randomUUID();
            ExportExcel ex = new ExportExcel();
            String sheetName = "传播测评未采集站点";//下载文件的默认名字,sheet页名字
            String fileName = outfileName
            String headers = "siteType,siteTypeName,siteName,siteDomain";
//表头
            String selname = "siteType,siteTypeName,siteName,siteDomain,o"
//标题对应key值

            HSSFWorkbook wb = ex.exportExcel(sheetName, headers, returnList, selname);
//返回一个workbook即excel实例。datetime是sheet页的名称
            String excelFolder = "D:\\传播测评未采集站点";
            def result = [
                    status: HttpStatus.SC_OK,
                    msg   : '',
            ];
            //如果文件夹不存在则创建，如果已经存在则删除
            File outPath = new File(excelFolder)
            if (!outPath.exists()) {
                FileUtils.forceMkdir(outPath)
            } else {
                FileUtils.forceDelete(outPath)
                FileUtils.forceMkdir(outPath)
            }
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try {
                wb.write(os);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            byte[] content = os.toByteArray();
            OutputStream fos = null;
            File file = new File(excelFolder + "/" + "excel.xls")

            try {
                fos = new FileOutputStream(file);
                fos.write(content);
                os.close();
                fos.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        println("默认生成目录在 D:\\传播测评未采集站点")
    }

    @Override
    public String getPropertyFileName() {
        return "";
    }
}
