package com.istar.mediabroken.console

import com.istar.mediabroken.entity.account.AccountApply
import com.istar.mediabroken.service.account.AccountService
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
public class AccountApplyExcelOutConsole implements Console {
    @Autowired
    AccountService accountService

    @Override
    public void execute(Map properties) {
//查询所有申请用户
        def sdf = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss')
        String startTime = "1970-01-01 00:00:00"
        Date startDate = sdf.parse(startTime)
        List<AccountApply> accountApplyList = accountService.getAccountApplyList();
        def hasDataMap = [:]
        List returnList = new ArrayList()
        println("新申请用户查询中，请稍候……")
        for (int i = 0; i < accountApplyList.size(); i++) {
            AccountApply accountApply = accountApplyList.get(i)
            def obj = [:]
            obj.put("_id", accountApply.id)
            obj.put("name", accountApply.name)
            obj.put("company", accountApply.company)
            obj.put("position", accountApply.position)
            obj.put("mobile", accountApply.mobile)
            obj.put("email", accountApply.email)
            obj.put("qq", accountApply.qq)
            obj.put("message", accountApply.message)
            obj.put("applyTime", accountApply.applyTime)
            obj.put("createTime", DateUitl.convertStrDate(accountApply.createTime))

            returnList.add(obj)
        }
            String outfileName = UUID.randomUUID();
            ExportExcel ex = new ExportExcel();
            String sheetName = "新申请用户";//下载文件的默认名字,sheet页名字
            String fileName = outfileName
            String headers = "_id,name,company,position,mobile,email,qq,message,applyTime,createTime";
//表头
            String selname = "_id,name,company,position,mobile,email,qq,message,applyTime,createTime,o"
//标题对应key值

            HSSFWorkbook wb = ex.exportExcel(sheetName, headers, returnList, selname);
//返回一个workbook即excel实例。datetime是sheet页的名称
            String excelFolder = "D:\\新申请用户";
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
        println("默认生成目录在 D:\\新申请用户")
    }

    @Override
    public String getPropertyFileName() {
        return "";
    }
}
