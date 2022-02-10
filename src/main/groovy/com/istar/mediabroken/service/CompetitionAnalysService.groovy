package com.istar.mediabroken.service

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.istar.mediabroken.api3rd.CaptureApi3rd
import com.istar.mediabroken.api3rd.TortNews
import com.istar.mediabroken.entity.copyright.CopyRightFilter
import com.istar.mediabroken.entity.CopyrightMonitor
import com.istar.mediabroken.entity.CopyrightMonitorLog
import com.istar.mediabroken.entity.CopyrightMonitorNews
import com.istar.mediabroken.entity.News
import com.istar.mediabroken.repo.account.AccountRepo
import com.istar.mediabroken.repo.CompetitionAnalysRepo
import com.istar.mediabroken.task.RemoveCopyrightMonitorNews
import com.istar.mediabroken.task.ThreadManager
import com.istar.mediabroken.utils.ExportExcel
import com.istar.mediabroken.utils.JaccardDistanceUtils
import com.istar.mediabroken.utils.Paging
import com.istar.mediabroken.utils.ZipUitl
import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

import java.text.SimpleDateFormat

import static com.istar.mediabroken.api.ApiResult.apiResult
import static com.istar.mediabroken.utils.UrlUtils.stripUrl

@Service
@Slf4j
class CompetitionAnalysService {

    @Autowired
    CompetitionAnalysRepo competitionAnalysRepo

    @Autowired
    AccountRepo accountRepo

    @Autowired
    CaptureApi3rd captureApi

    @Value('${image.upload.path}')
    String UPLOAD_PATH

    String addCopyrightMonitor(CopyrightMonitor copyrightMonitor) {
        def monitorId = UUID.randomUUID().toString()
        copyrightMonitor.monitorId = monitorId
        competitionAnalysRepo.addCopyrightMonitor(copyrightMonitor)
        return monitorId
    }

    void modifyCopyrightMonitor(CopyrightMonitor copyrightMonitor) {
        competitionAnalysRepo.modifyCopyrightMonitor(copyrightMonitor)
        removeCopyrightMonitorLogAndNews(copyrightMonitor.userId,copyrightMonitor.monitorId)
    }

    void removeCopyrightMonitor(long userId, String monitorId) {
        competitionAnalysRepo.removeCopyrightMonitor(userId, monitorId)
        ThreadManager.instance.addThread(new RemoveCopyrightMonitorNews(userId,monitorId))
    }

    List<CopyrightMonitor> getCopyrightMonitors(long userId) {
        return competitionAnalysRepo.getCopyrightMonitors(userId)
    }

    CopyrightMonitor getCopyrightMonitor(long userId, String monitorId) {
        return competitionAnalysRepo.getCopyrightMonitor(userId, monitorId)
    }

    int getCopyrightMonitorCount(long userId) {
        return competitionAnalysRepo.getCopyrightMonitorCount(userId)
    }

    Paging<TortNews> getPagingCopyrightMonitorNews(long userId, String monitorId, int pageNo, int limit) {
        def monitor = competitionAnalysRepo.getCopyrightMonitor(userId, monitorId)
//        def newsList = captureApi.getPagingCopyrightMonitorNews(seg(monitor.title), pageNo, limit)
        def newsList = null;
        def whiteList = []
        monitor.whiteList.each{
            whiteList << "." + stripUrl(it)
        }

        def blackList=[];
        monitor.blackList.each{
            blackList << "." + stripUrl(it)
        }
        if (monitor !=null){
            newsList=captureApi.getPagingCopyrightMonitorNews(monitor.title, pageNo, limit)
            newsList.list.each {
                def compare = JaccardDistanceUtils.computeJaccardDistance(monitor.title, it.title)
                log.debug('{}',compare)
                if (compare > 0.8) {
                    it.isTort = true
                } else {
                    it.isTort = false
                }
                it.isWhite=false;
                it.isBlack=false;
                String strUrl="."+stripUrl(it.url)
                boolean whiteFlag=false;
                boolean blackFlag=false;
                for (int i = 0; i < whiteList.size(); i++) {
                    if(strUrl.contains(whiteList.get(i))){
                        whiteFlag=true;
                        break
                    }
                }
                for (int i = 0; i < blackList.size(); i++) {
                    if(strUrl.contains(blackList.get(i))){
                        blackFlag=true;
                        break
                    }
                }
                it.isWhite=whiteFlag;
                it.isBlack=blackFlag;

            }
        }
        return newsList
    }
    public static String isNullOrNot(String str){
        String strRe="";
        strRe=(str==null?"":(str))
        return strRe
    }
    public static String strToDate(String s){
        //1496321853000
        //20170601142139
        String res;
        if (s.length() ==14){
            res = s.substring(0, 4) + "-" + s.substring(4, 6) + "-" + s.substring(6, 8) + " " + s.substring(8, 10) + ":" + s.substring(10, 12) + ":" + s.substring(12, 14);
        }else if (s.length()==13){
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            long lcc_time = Long.valueOf(s);
            res = simpleDateFormat.format(new Date(lcc_time));
        }else{
            res="";
        }
        return res;
    }
    /*竞品分析-疑似侵权/全部-excel导出*/
    Map copyrightMonitorNewsExcelOut(String list){
        String outfileName = UUID.randomUUID();
        ExportExcel ex = new ExportExcel();
        String sheetName = "";//下载文件的默认名字,sheet页名字
        String fileName = outfileName
        String headers="标题,内容,转载媒体,转载时间,";//表头
        String selname="title,contentAbstract,source,createTime,o"//标题对应key值

//        选中需要导出的数据
        list = list.replace('\\"','"')

//        JSONObject dataJson = JSONObject.parse(list)
        JSONArray lists = JSONArray.parseArray(list);
        List<Map<String, Object>> copyrightMonitorNewsList = new ArrayList<Map<String, Object>>();
        lists.each {
            Map<String, Object> news = new HashMap<String, Object>();
            news.put("title",isNullOrNot(it.title));
            news.put("contentAbstract",isNullOrNot(it.contentAbstract));
            news.put("source",isNullOrNot(it.source));
            String time=it.createTime;
            news.put("createTime",time==null?"":strToDate(time));

            sheetName = isNullOrNot(it.title);
            copyrightMonitorNewsList.add(news);
        }
        sheetName = (sheetName==null || "".equals(sheetName))?"疑似侵权信息导出":sheetName;//以标题名作为导出文件名，如果标题为空就以“疑似侵权信息导出”为文件名

        HSSFWorkbook wb = ex.exportExcel(sheetName, headers, copyrightMonitorNewsList, selname);//返回一个workbook即excel实例。datetime是sheet页的名称
        String excelFolder = "/${UPLOAD_PATH}/download/downLoadCopyMonitor/"+outfileName;
        def result= [
                status       : HttpStatus.SC_OK,
                msg          : '',
        ];
        //如果文件夹不存在则创建，如果已经存在则删除
        File outPath = new File(excelFolder)
        if(!outPath.exists()){
            FileUtils.forceMkdir(outPath)
        }else {
            FileUtils.forceDelete(outPath)
            FileUtils.forceMkdir(outPath)
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try
        {
            wb.write(os);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        byte[] content = os.toByteArray();
        OutputStream fos  = null;
        File file = new File(excelFolder+ "/"+outfileName+".xls")
        try
        {
            fos = new FileOutputStream(file);
            fos.write(content);
            os.close();
            fos.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        //zip file

        if(fileName.length() > 30){
            fileName = fileName.substring(0,30)
        }
        def zipPath = file.getParent() + "/" + fileName + ".zip"
        ZipUitl.zip(file.getParent(),zipPath )
        result.msg = zipPath
        result.outfileName = outfileName
        return result;

    }

    String getCopyrightMonitorNewsExcelByToken(String token) {
        def filePath = new File(UPLOAD_PATH, 'download').path
        def path = filePath.toString() + File.separator + "downLoadCopyMonitor" + File.separator + token
        List<String> fileList = ZipUitl.getFileNames(path)
        for (int i = 0; i < fileList.size() ; i++) {
            if(fileList.get(i).endsWith(".zip")){
                path = path + File.separator + fileList.get(i)
                return path
            }
        }
        return ""
    }

    int getReprintCount(CopyrightMonitor monitor) {
//        return captureApi.getReprintCount(seg(monitor.title))
        return captureApi.getReprintCount(monitor.title)
    }

    News getNewsByUrl(String url) {
        return captureApi.getNewsByUrl(url)
    }

    void saveSiteComparison(long userId, int siteType, String[] siteNames) {
        competitionAnalysRepo.saveSiteComparison(userId, siteType, siteNames)
    }

    Map getSiteComparison(long userId) {
        competitionAnalysRepo.getSiteComparison(userId)
    }

    void autoCopyrightMonitor(){
        //获取版权监控列表
        def pageSize = 100;
        def monitorPageNo = 1;
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyyMMddHHmmss");
        def currDate = new Date();
        String currTime = simpleDateFormat.format(currDate);
        while (true){
            List copyrightMonitors = competitionAnalysRepo.getAllCopyrightMonitors(monitorPageNo,pageSize)
            if(copyrightMonitors == null || copyrightMonitors.size() == 0){
                break
            }
            copyrightMonitors.each {  copyrightMonitor ->
                def whiteList = []
                copyrightMonitor.whiteList.each{
                    whiteList << "." + stripUrl(it)
                }
                def blackList = []
                copyrightMonitor.blackList.each{
                    blackList << "." + stripUrl(it)
                }
                //处理每一个copyright
                CopyrightMonitorLog copyrightMonitorLog  = competitionAnalysRepo.getCopyrightMonitorLogById(copyrightMonitor.monitorId)
                if(copyrightMonitorLog == null || copyrightMonitorLog.monitorId == null){
                    if(copyrightMonitorLog == null){
                        copyrightMonitorLog = new CopyrightMonitorLog()
                    }
                    copyrightMonitorLog.monitorId = copyrightMonitor.monitorId
                    copyrightMonitorLog.prevStartTime = "19000101000001"
                    copyrightMonitorLog.prevEndTime = "19000101000001"
                    copyrightMonitorLog.updateTime = currDate
                    copyrightMonitorLog.createTime = currDate
                    competitionAnalysRepo.addCopyrightMonitorLog(copyrightMonitorLog)
                }
                //处理数据
                def newsPageNo = 1
                def startTime = copyrightMonitorLog.prevEndTime
                def endTime = currTime
                def currCkTime = copyrightMonitorLog.prevEndTime
                while (true){
                    List monitorNewsList = new ArrayList<CopyrightMonitorNews>()
                    def newsList = captureApi.getPagingCopyrightMonitorNewsByTime(copyrightMonitor.title as String, newsPageNo, pageSize,startTime,endTime)

                    if(newsList.list.size() == 0){
                        break
                    }
                    newsList.list.each {
                        CopyrightMonitorNews copyrightMonitorNews = new CopyrightMonitorNews()
                        def compare = JaccardDistanceUtils.computeJaccardDistance(copyrightMonitor.title, it.title)
//                        log.debug('{}',compare)
                        if (compare > 0.8) {
                            it.isTort = true
                        } else {
                            it.isTort = false
                        }
                        copyrightMonitorNews.userId = copyrightMonitor.userId
                        copyrightMonitorNews.monitorId = copyrightMonitor.monitorId
                        copyrightMonitorNews.newsId = it.newsId
                        copyrightMonitorNews.isTort = it.isTort
                        copyrightMonitorNews.title = it.title
                        copyrightMonitorNews.source = it.source
                        copyrightMonitorNews.author = it.author
                        copyrightMonitorNews.url = it.url
                        copyrightMonitorNews.contentAbstract = it.contentAbstract
                        copyrightMonitorNews.site = it.site
                        copyrightMonitorNews.newsType = it.newsType
                        copyrightMonitorNews.Ctime = it.createTime
                        copyrightMonitorNews.DkTime = it.dkTime
                        copyrightMonitorNews.createTime = new Date()
                        copyrightMonitorNews._id = copyrightMonitorNews.createId()
                        String strUrl="."+stripUrl(it.url)
                        copyrightMonitorNews.isWhite=false;
                        copyrightMonitorNews.isBlack=false;

                        for (int i = 0; i < whiteList.size(); i++) {
                            if(strUrl.contains(whiteList.get(i))){
                                copyrightMonitorNews.isWhite=true;
                                break
                            }
                        }
                        for (int i = 0; i < blackList.size(); i++) {
                            if(strUrl.contains(blackList.get(i))){
                                copyrightMonitorNews.isBlack=true;
                                break
                            }
                        }

                        monitorNewsList.add(copyrightMonitorNews)
                        currCkTime = copyrightMonitorNews.DkTime

                    }
                    //插入copyrightMonitorNews
                    competitionAnalysRepo.addCopyrightMonitorNews(monitorNewsList)
                    // 更新copyrightMonitorLog
                    copyrightMonitorLog.prevStartTime = copyrightMonitorLog.prevEndTime
                    copyrightMonitorLog.prevEndTime = currCkTime
                    competitionAnalysRepo.modifyCopyrightMonitorLog(copyrightMonitorLog)
                    newsPageNo++
                }

            }
            monitorPageNo++
        }
    }

    Map getNewsTortCounts(Long userId, List monitorIds) {
        return competitionAnalysRepo.getNewsTortCounts(userId, monitorIds)
    }

    void removeCopyrightMonitorLogAndNews(long userId, String monitorId) {
        competitionAnalysRepo.removeCopyrightMonitorLog( monitorId)
        competitionAnalysRepo.removeCopyrightMonitorNews(userId,monitorId)
    }

    Map getCopyrightMonitorNews(String appId, String data, int orderType) {
        try {
            if(appId == null || appId.trim().equals("")){
                return apiResult([
                        status  : HttpStatus.SC_BAD_REQUEST,
                        errorId : 400001L,
                        errorMsg: "appId不能为空"
                ])
            }
            JSONObject dataJson = JSONObject.parse(data)
            String title = dataJson.get("title")?dataJson.get("title").toString() : ""
            String abstract1 = dataJson.get("abstract")?dataJson.get("abstract").toString() : ""
            if(title.equals("") && abstract1.equals("")){
                return apiResult([
                        status  : HttpStatus.SC_BAD_REQUEST,
                        errorId : 400002L,
                        errorMsg: "请填写title或摘要信息"
                ])
            }
            def pageNo = dataJson.get("pageNo")?dataJson.get("pageNo") as int: 1
            def limit = dataJson.get("limit")?dataJson.get("limit") as int: 10
            if (limit > 100){
                return apiResult([
                        status  : HttpStatus.SC_BAD_REQUEST,
                        errorId : 400003L,
                        errorMsg: "limit不能超过100"
                ])
            }
            def startTime = new Date(dataJson.get("startTime")?dataJson.get("startTime") as long : 0L)
            def endTime = new Date(dataJson.get("startTime")?dataJson.get("endTime") as long : System.currentTimeMillis())
            //appid校验
            if(!accountRepo.isValidAppId(appId)){
                return apiResult([
                        status  : HttpStatus.SC_UNAUTHORIZED,
                        errorId : 401004L,
                        errorMsg: "appId校验失败"
                ])
            }
            Paging newsList = captureApi.getCopyrightMonitorNews(title,abstract1, startTime, endTime, pageNo, limit,orderType)
            return apiResult([
                    status  : HttpStatus.SC_OK,
                    total   : newsList.total,
                    pageNo  : newsList.pageNo,
                    limit   : newsList.limit,
                    list    : newsList.list
            ])
        }catch (Exception e){
            return apiResult([
                    status  : HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    errorId : 500001L,
                    errorMsg: e.getMessage()
            ])
        }

    }

    CopyRightFilter getCopyRightFilter(long userId) {
        return competitionAnalysRepo.getCopyRightFilter(userId)
    }

    boolean isSiteMonitoring(long userId, String websiteDomain) {
        boolean isMonitoringInWhiteList =  competitionAnalysRepo.isMonitoringInWhiteList(userId,websiteDomain)
        boolean isMonitoringInBlackList =  competitionAnalysRepo.isMonitoringInBlackList(userId,websiteDomain)
        return isMonitoringInWhiteList || isMonitoringInBlackList
    }
    /*校验新增的黑白名单，是否有重复，与原有黑白名单的并集是否有重复*/
    Map chooseCopyRightFilters(Map oldCopyRightFilter,Map whiteSites,Map blackSites){
        Map result=[status: HttpStatus.SC_OK,msg:"SUCCESS"];
//
//        if (false){
//            result=[status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg:"提交黑白名单有误，请重新提交"]
//        }
        def oldWhiteSitesDomain = []//已有白名单url集合
        def oldBlackSitesDomain = []//已有黑名单url集合
        def oldDomains=[];//已有黑白名单url并集
        if (oldCopyRightFilter){
            if (oldCopyRightFilter.whiteList){
                oldCopyRightFilter.whiteList.each{
                    oldWhiteSitesDomain << it.websiteDomain
                    oldDomains<< it.websiteDomain
                }
            }
            if (oldCopyRightFilter.blackList){
                oldCopyRightFilter.blackList.each{
                    oldBlackSitesDomain << it.websiteDomain
                    oldDomains<< it.websiteDomain
                }
            }
        }
        if (null !=oldWhiteSitesDomain&&oldWhiteSitesDomain.size()>=8){
            result = [status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg:"白名单最多可设置8个"]
        }
        if (null !=oldBlackSitesDomain&&oldBlackSitesDomain.size()>=8){
            result = [status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg:"黑名单最多可设置8个"]
        }
        def newWhiteSitesDomain = []//新增白名单url集合
        def newBlackSitesDomain = []//新增黑名单url集合
        def newDomains=[];//新增黑白名单url并集
        if (whiteSites){
            whiteSites.each {
                newWhiteSitesDomain<<it.websiteDomain;
                newDomains<<it.websiteDomain;
            }
        }
        if (blackSites){
            blackSites.each {
                newBlackSitesDomain<<it.websiteDomain;
                if (!newDomains.contains(it.websiteDomain)){
                    newDomains<<it.websiteDomain;
                }
            }
        }

        if (oldDomains.contains(newDomains)){
            result= [status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg:"提交黑白名单有误，请重新提交"]
        }
        return result;
    }
    Map modifyCopyRightFilter(long userId,String whiteList, String BlackList){
        JSONObject it = new JSONObject()
        JSONArray whiteListJson = JSONArray.parse(whiteList)
        JSONArray blackListJson = JSONArray.parse(BlackList)
        def whiteSites =[]
        def whiteSitesDomain=[]
        def blackSites =[]
        def blackSitesDomain=[]
        //传入的黑白名单
        whiteListJson.each {
            whiteSites <<[
                    websiteDomain: it.get("websiteDomain") ? String.valueOf(it.get("websiteDomain")) : "",
                    websiteName  : it.get("websiteName")   ? String.valueOf(it.get("websiteName"))   : ""
            ]
            whiteSitesDomain << (it.get("websiteDomain") ? String.valueOf(it.get("websiteDomain")) : "")
        }
        blackListJson.each {
            blackSites <<[
                    websiteDomain: it.get("websiteDomain") ? String.valueOf(it.get("websiteDomain")) : "",
                    websiteName  : it.get("websiteName")   ? String.valueOf(it.get("websiteName"))  : ""
            ]
            blackSitesDomain << (it.get("websiteDomain") ? String.valueOf(it.get("websiteDomain")) : "")
        }
        if(blackSites.size() > 8){
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg:"最多设置8个黑名单站点"])
        }
        if(whiteSites.size() > 8){
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg:"最多设置8个白名单站点"])
        }
        //站点是否有重复的
        def allSites = []
        allSites = whiteSitesDomain + blackSitesDomain
        def oldSize = allSites.size()
        def newSize = (allSites.unique()).size()
        if(newSize < oldSize){
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg:"不允许输入重复的站点"])
        }
        //判断黑白名单数目
        CopyRightFilter oldCopyRightFilter = getCopyRightFilter(userId)
        if (oldCopyRightFilter){
            def oldWhiteSitesDomain = []
            def oldBlackSitesDomain = []
            if (oldCopyRightFilter){
                if (oldCopyRightFilter.whiteList){
                    oldCopyRightFilter.whiteList.each{
                        oldWhiteSitesDomain << it.websiteDomain
                    }
                }
                if (oldCopyRightFilter.blackList){
                    oldCopyRightFilter.blackList.each{
                        oldBlackSitesDomain << it.websiteDomain
                    }
                }
            }
            //校验添加的数据是否合法,黑白名单的值不能有交集，各自不能有重复值，对每个用户而言，黑白名单的配置就是一条记录
    //        Map result = chooseCopyRightFilters(oldCopyRightFilter,whiteSites,blackSites)
            //删除的数据是否在使用中

            for (int i = 0; i < oldWhiteSitesDomain.size(); i++) {
                def whiteSite = oldWhiteSitesDomain.get(i)
                if (!whiteSitesDomain.find{it.equals(whiteSite)}){
                    if(competitionAnalysRepo.isMonitoringInWhiteList(userId,whiteSite)){
                        return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg:"站点监控中不能删除或编辑"])
                    }
                }
            }
            for (int i = 0; i < oldBlackSitesDomain.size(); i++) {
                def blackSite = oldBlackSitesDomain.get(i)
                if (!blackSitesDomain.find{it.equals(blackSite)}){
                    if(competitionAnalysRepo.isMonitoringInBlackList(userId,blackSite)){
                        return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg:"站点监控中不能删除或编辑"])
                    }
                }
            }
        }

        CopyRightFilter newCopyRightFilter = new CopyRightFilter()
        if (oldCopyRightFilter){
            newCopyRightFilter.id = oldCopyRightFilter.id
        }
        newCopyRightFilter.userId = userId
        newCopyRightFilter.whiteList = whiteSites
        newCopyRightFilter.blackList = blackSites
        newCopyRightFilter.updateTime = new Date()
        newCopyRightFilter.createTime = new Date()
        boolean  flag = competitionAnalysRepo.modifyCopyRightFilter(newCopyRightFilter)
        if (flag){
            return apiResult([status: HttpStatus.SC_OK,msg:"保存成功"])
        }
    }

}
