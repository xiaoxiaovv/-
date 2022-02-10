package com.istar.mediabroken.service.statistics

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.istar.mediabroken.entity.account.Account
import com.istar.mediabroken.entity.capture.NewsOperation
import com.istar.mediabroken.repo.capture.NewsRepo
import com.istar.mediabroken.repo.statistics.StatisticsManageRepo
import com.istar.mediabroken.service.account.AccountService
import com.istar.mediabroken.utils.DateUitl
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * author:hh
 * date:2018/3/21
 */
@Service
@Slf4j
class DataDeleteService {
    @Value('${data.backup.path}')
    String backUpPath
    @Value('${env}')
    String env
    @Autowired
    StatisticsManageRepo statisticsManageRepo
    @Autowired
    NewsRepo newsRepo
    @Autowired
    AccountService accountService

    void dataDelete(int day) {
        Date date = DateUitl.addDay(new Date(), -day)
        long deleteCount = 0L


        List<Account> accountList = accountService.getAllAccountList();

        for (int i = 0; i < accountList.size(); i++) {
            Account account = accountList.get(i)

            def flag = true

            int pageNo = 1;
            int pageSize = 50;
            while (flag) {
                List<NewsOperation> operationList = newsRepo.getOrgPushNewsList(account.getId(),date, pageNo, pageSize)
                if (operationList) {
                    if (operationList.size() > 0){
                        println(account.getId()+"：本次删除条数："+operationList.size())
                    }
                    if (operationList.size() < pageSize){
                        flag = false;
                    }
                    if ("online".equals(env)) {
                        handleAndWriteTimeOutData(operationList)
                    }
                    deleteTimeOutData(operationList)
                    deleteCount += operationList.size()
                } else {
                    flag = false
                    continue;
                }

                pageNo += 1;
            }

        }
        log.info(new Date().toString()+"删除newsOperation表中数据"+deleteCount+"条!")

    }

    private void deleteTimeOutData(List<NewsOperation> operationList) {
        def operationIds = []
        operationList.each { operation ->
            operationIds.add(operation.get_id())
        }
        newsRepo.deleteTimeOutData(operationIds)
    }

    private Boolean handleAndWriteTimeOutData(List<NewsOperation> operationList) {
        List<NewsOperation> backupList = []
        operationList.each {
            if (it.getOperationType() == 1 && it.isAutoPush == true){
                //自动推送的不备份
            }else {
                backupList.add(it)
            }
        }
        def listCount = backupList.size()
        Boolean isEquals = DateUitl.isEquals(backupList.get(0).getCreateTime(),
                backupList.get(listCount - 1).getCreateTime())
        if (isEquals) {
            String date = DateUitl.convertFormatDate(backupList.get(0).getCreateTime(), "yyyyMMdd")
            String fileName = backUpPath + "newsOperation" + date + ".txt"
            WriteTimeOutData(backupList, fileName)
        } else {
            Map operationMap = groupByCreateTime(backupList)
            operationMap.each { operation ->
                String fileName = backUpPath + "newsOperation" + operation.key + ".txt"
                WriteTimeOutData(operation.value, fileName)
            }
        }
    }

    private Map groupByCreateTime(List<NewsOperation> operationList) {
        Map<String, List<NewsOperation>> resultMap = new HashMap<String, List<NewsOperation>>()
        for (NewsOperation newsOperation : operationList) {
            String key = DateUitl.convertFormatDate(newsOperation.getCreateTime(), "yyyyMMdd")
            if (resultMap.containsKey(key)) {
                resultMap.get(key).add(newsOperation)
            } else {
                List<NewsOperation> tmpList = new ArrayList<NewsOperation>()
                tmpList.add(newsOperation)
                resultMap.put(key, tmpList)
            }
        }
        return resultMap
    }

    private void WriteTimeOutData(List<NewsOperation> operationList, fileName) {
        JSON json = JSONObject.toJSON(operationList)
        try {
            FileWriter file = new FileWriter(fileName, true)
            BufferedWriter out = new BufferedWriter(file);
            out.write(json.toString())
            out.newLine()
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
