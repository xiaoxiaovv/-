package com.istar.mediabroken.service.capture

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.istar.mediabroken.entity.account.Account
import com.istar.mediabroken.entity.capture.KeywordsScopeEnum
import com.istar.mediabroken.entity.capture.Site
import com.istar.mediabroken.entity.capture.Subject
import com.istar.mediabroken.repo.account.AccountRepo
import com.istar.mediabroken.repo.capture.QuerySessionRepo
import com.istar.mediabroken.repo.capture.SubjectRepo
import com.istar.mediabroken.service.rubbish.RubbishNewsService
import com.istar.mediabroken.utils.ExportExcel
import com.istar.mediabroken.utils.StringUtils
import groovy.util.logging.Slf4j
import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import org.apache.commons.io.FileUtils
import org.apache.http.HttpStatus
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

import java.text.SimpleDateFormat

import static com.istar.mediabroken.api.ApiResult.apiResult
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR

/**
 * Author: zhaochen
 * Time: 2017/8/1
 */
@Service
@Slf4j
class SubjectService {
    @Autowired
    SubjectRepo subjectRepo
    @Autowired
    QuerySessionRepo querySessionRepo
    @Autowired
    AccountRepo accountRepo;
    @Autowired
    RubbishNewsService rubbishNewsService
    @Autowired
    NewsService newsService

    @Value('${subject.orcount}')
    int orCount
    @Value('${subject.keywordscount}')
    int keywordsCount
    /**
     * 新建主题
     * @param userId
     * @param subjectName
     * @param monitorName
     * @param keywordsScope
     * @param keyWord
     * @param regionWord
     * @param excludeWord
     * @return
     */
    Map addUserSubject(Long userId, String subjectName, int keywordsScope, String titleKeywords, String keyWords, String areaWords, String excludeWords) {
        //判断主题总数不能超过限制
        int subjectCount = subjectRepo.getSubjectCount(userId);
        if (subjectCount > 19) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "最多创建20个监控主题"])
        }
        //判断主题名称是否唯一
        boolean isSubjectExist = subjectRepo.isSubjectExist(userId, subjectName);
        if (isSubjectExist) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, '主题名称已存在');
        }

        HanyuPinyinOutputFormat upperCaseFormat = new HanyuPinyinOutputFormat();
        upperCaseFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE)
        char word = subjectName.substring(0, 1) as char
        def pinYin = PinyinHelper.toHanyuPinyinStringArray(word, upperCaseFormat)
        def pinYinPrefix = (pinYin ? pinYin[0] : word) as String

        def subject = new Subject(
                userId: userId,
                subjectName: subjectName,
                keywordsScope: keywordsScope,
                areaWords: areaWords,
                excludeWords: excludeWords,
                pinYinPrefix: pinYinPrefix,
                updateTime: new Date(),
                createTime: new Date()
        )
        if (keywordsScope == KeywordsScopeEnum.contentScope.key){
            subject.keyWords = keyWords
            subject.titleKeywords = ""
        }else if (keywordsScope == KeywordsScopeEnum.titleScope.key){
            subject.titleKeywords = titleKeywords
            subject.keyWords = ""
        }
        String subjectId = subjectRepo.addSubject(subject)
        return apiResult([subjectId: subjectId, status: HttpStatus.SC_OK, msg: "添加主题成功"])
    }

    Map getUserSubjects(Long userId) {
        def subjects = subjectRepo.getUserSubjects(userId)
        if (subjects.size() == 0) {
            return apiResult([status: HttpStatus.SC_OK, list: [], msg: "请配置更多追踪主题", maxCount: Subject.SUBJECT_MAX_COUNT])
        }
        return apiResult([status: HttpStatus.SC_OK, list: subjects, msg: "", maxCount: Subject.SUBJECT_MAX_COUNT])
    }

    Subject getUserSubjectById(Long userId, String subjectId) {
        def subject = subjectRepo.getUserSubjectById(userId, subjectId)
        return subject
    }

    void removeSubject(long userId, String subjectId) {
        subjectRepo.removeSubject(userId, subjectId)
    }

    Map modifyUserSubject(Long userId, String subjectId, String subjectName, int keywordsScope, String titleKeywords, String keyWords, String areaWords, String excludeWords) {
        //判断主题名称是否已存在
        def sub = subjectRepo.getUserSubjectById(userId, subjectId)
        if (!sub) {
            return apiResult(status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: '没有找到对应的主题信息')
        }
        boolean isSubjectExist = subjectRepo.isSubjectExist(userId, subjectName);
        if ((!"".equals(subjectName)) && (!subjectName.equals(sub.subjectName)) && isSubjectExist) {
            return apiResult(SC_INTERNAL_SERVER_ERROR, '主题名称已存在');
        }
        HanyuPinyinOutputFormat upperCaseFormat = new HanyuPinyinOutputFormat();
        upperCaseFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE)
        char word = subjectName.substring(0, 1) as char
        def pinYin = PinyinHelper.toHanyuPinyinStringArray(word, upperCaseFormat)
        def pinYinPrefix = (pinYin ? pinYin[0] : word) as String

        def subject = new Subject(
                _id: subjectId,
                userId: userId,
                subjectName: subjectName,
                keywordsScope: keywordsScope,
                areaWords: areaWords,
                excludeWords: excludeWords,
                pinYinPrefix: pinYinPrefix,
                updateTime: new Date()
        )
        if (keywordsScope == KeywordsScopeEnum.contentScope.key){
            subject.keyWords = keyWords
            subject.titleKeywords = ""
        }else if (keywordsScope == KeywordsScopeEnum.titleScope.key){
            subject.titleKeywords = titleKeywords
            subject.keyWords = ""
        }
        boolean result = subjectRepo.modifySubject(subject)
        if (result) {
            return apiResult(status: HttpStatus.SC_OK, msg: '修改成功')
        } else {
            return apiResult(status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: '修改失败')
        }
    }

    Map getUserSubjectNews(Long userId, String subjectId, int siteType, int hot, Date startTime, Date endTime, String classification, int orientation, int hasPic, int order, String keyWords, int pageSize, int pageNo) {
        //拿到用户主题信息
        Subject subject = subjectRepo.getUserSubjectById(userId, subjectId)
        if (!subject) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: '请配置主题信息'])
        }

        def result = subjectRepo.getSubjectNewsFromEs(subject, siteType, hot, startTime, endTime, classification, orientation, hasPic, order, keyWords, pageSize, pageNo)
        if (result) {
            result.each {
                it.firstPublishSiteName = it.firstPublishSiteName ?: it.siteName
            }
            return apiResult([status: HttpStatus.SC_OK, newsList: result, msg: ''])
        }
        return apiResult([status: HttpStatus.SC_OK, newsList: [], msg: '查询无数据'])

    }

    Map isKeywordsVerificated(String keywords) {
        JSONArray keywordsArray = JSONArray.parse(keywords)
        if (keywordsArray.size() > (orCount * 2 - 1)) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: '最多允许配置' + orCount + '组关键词'])
        }
        boolean needKeywords = true
        boolean needExpress = false
        int keywordsSize = 0
        for (int i = 0; i < keywordsArray.size(); i++) {
            JSONObject element = keywordsArray.get(i)
            if (element.containsKey("operator")) {
                if (!needExpress) {
                    return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "表达式错误，缺少表达式"])
                }
                if (element.operator.equals("or") || element.operator.equals("and")) {
                    needExpress = false
                    needKeywords = true
                } else {
                    return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "表达式错误，关键词operator错误"])
                }
                continue
            }
            if (!needKeywords) {
                return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "表达式错误，缺少关键词"])
            }
            needExpress = true
            needKeywords = false
            def elementVerification = isElementVerificated(element)
            if (elementVerification.status == HttpStatus.SC_OK) {
                keywordsSize += elementVerification.keyList.size()
                if (keywordsSize > keywordsCount) {
                    return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "关键词不能超过" + keywordsCount + "个"])
                }
            } else {
                return elementVerification
            }
        }
        if (keywordsSize < 1) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "至少输入一个关键词"])
        }
        return apiResult([status: HttpStatus.SC_OK, msg: ''])
    }

    Map isElementVerificated(JSONObject keys) {
        if (!keys.containsKey("subOperator")) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "表达式错误，缺少关键词subOperator"])
        }
        if (!(keys.subOperator.equals("or") || keys.subOperator.equals("and"))) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "表达式错误，关键词subOperator错误"])
        }
        if (!keys.containsKey("keywords")) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "表达式错误，缺少查询关键词"])
        }
        if (!(keys.keywords instanceof JSONArray)) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "表达式错误，keywords格式错误"])
        }
        if (keys.keywords.size() <= 0) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "表达式错误，keywords数据不允许为空"])
        }
        return apiResult([status: HttpStatus.SC_OK, msg: "", keyList: keys.keywords])
    }

    Map getUserSubjectNewsByExpression(Long userId, String subjectId, int siteType, int hot, Date startTime, Date endTime, String classification, int orientation, int hasPic, int order, String keyWords, int pageSize, int pageNo) {
        //拿到用户主题信息
        Subject subject = subjectRepo.getUserSubjectById(userId, subjectId)
        if (!subject) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: '请配置主题信息'])
        }

        def result = subjectRepo.getSubjectNewsFromEsByExpression(subject, siteType, hot, startTime, endTime, classification, orientation, hasPic, order, keyWords, pageSize, pageNo)
        if (result) {
            result.each {
                it.firstPublishSiteName = it.firstPublishSiteName ?: it.siteName
            }
            return apiResult([status: HttpStatus.SC_OK, newsList: result, msg: ''])
        }
        return apiResult([status: HttpStatus.SC_OK, newsList: [], msg: '查询无数据'])

    }

    Map getUserSubjectNewsByQueryId(Long userId, String subjectId, int siteType, int hot, Date startTime, Date endTime, String classification, int orientation, int hasPic, int order, int queryScope, String queryString, int pageSize, String queryId, boolean withHighlight) {
        //拿到用户主题信息
        Subject subject = subjectRepo.getUserSubjectById(userId, subjectId)
        if (!subject) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: '请配置主题信息'])
        }
        //解析数据
        String id = ""
        Date time = new Date()
        int offset = 0
        if (queryId) {
            try {
                List queryKeys = queryId.split(",")
                id = queryKeys.get(0)
                time = new Date(queryKeys.get(1) as long)
                offset = queryKeys.get(2) as int
                querySessionRepo.removeQuerySessionByTime(id, time)
            } catch (Exception e) {
                log.error("queryId格式不合法,解析失败", e)
                return apiResult([status: HttpStatus.SC_OK, newsList: [], msg: 'queryId格式不合法', queryId: queryId])
            }
        } else {
            id = UUID.randomUUID().toString()
        }
        def newsList = []
        def newsTitleList = []
        while (true) {
            List currentNewsList = subjectRepo.getSubjectNewsFromEsByExpression(subject, siteType, hot, startTime, endTime, classification, orientation, hasPic, order, queryScope, queryString, pageSize, offset, withHighlight)
            def validResult = getValidNews(currentNewsList, pageSize - newsList.size(), id, newsTitleList, userId)
            offset += validResult.index
            newsList += validResult.newsList
            if (newsList.size() >= pageSize) {
                break;
            }
            if (currentNewsList.size() < pageSize) {
                break;
            }
        }

        queryId = id + "," + new Date().getTime().toString() + "," + offset
        if (newsList && newsList.size() > 0) {
            def newsIds = []
            newsList.each {
                it.firstPublishSiteName = it.firstPublishSiteName ?: it.siteName
                it.siteType = (it.newsType == 1 ? Site.SITE_TYPE_WEBSITE : (it.newsType == 6) ? Site.SITE_TYPE_WECHAT : (it.newsType == 401 || it.newsType == 402 || it.newsType == 8)? Site.SITE_TYPE_WEIBO : Site.SITE_TYPE_WEBSITE)
                it.content = (it.newsType == 1 || it.newsType == 6) ? "" : StringUtils.removeWeiboSuffix(it.content)
                newsIds.add(it.id)
            }
            List col = newsService.getExistNewsOperation(userId, newsIds, 3)
            List push = newsService.getExistNewsOperation(userId, newsIds, 1)
            newsList.each { it ->
                it.put("isCollection", col.contains(it.id))
                it.put("isPush", push.contains(it.id))
            }
            return apiResult([status: HttpStatus.SC_OK, newsList: newsList, msg: '', queryId: queryId])
        }
        return apiResult([status: HttpStatus.SC_OK, newsList: newsList, msg: '查询无数据', queryId: queryId])
    }

    Map getValidNews(List newsList, int maxCount, String id, List newsTitleList, long userId) {
        Map result = ["newsList": [], "index": newsList.size()]
        def resultList = []
        for (int i = 0; i < newsList.size(); i++) {
            result.index = i + 1
            def news = newsList.get(i)
            //2 查看Simhash是否存在在已经返回的list中
            def isExist = querySessionRepo.isQuerySessionRecordExist(id, news.simhash)
            if (isExist) {
                continue
            }
            if (newsTitleList.contains(news.title)) {
                continue
            }
            //查看新闻是不是被用户标记过为垃圾数据
            if (rubbishNewsService.isRubbishNews(userId, news.id as String, news.simhash as String)) {
                continue
            }
            //3 如果没有存在，则添加到返回的newslist
            resultList << news
            newsTitleList << news.title
            //4 插入到querysession
            querySessionRepo.addQuerySession(id, news.simhash)
            if (resultList.size() >= maxCount) {
                break
            }
        }
        result.newsList = resultList
        return result
    }

    Map getUserSubjectLatestNews(Long userId) {
        //拿到用户主题信息
        List subjects = subjectRepo.getUserSubjects(userId)
        if (subjects.size() == 0) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: '请配置主题信息'])
        }

        def result = subjectRepo.getSubjectsLatestNewsFromEs(subjects, 50, 1)
        def latestNewsList = []
        def simhashList = []
        def titleList = []
        def size = result.size()
        for (int i = 0; i < size; i++) {
            def news = result.get(i)
            if (simhashList.contains(news.simhash)) {
                continue
            }
            if (titleList.contains(news.title)) {
                continue
            }
            def currClassification = ""
            def keywords = []
            JSONArray keywordArray = JSONArray.parse(news.keywords)
            keywordArray.each {
                keywords << it.word
            }
            news.keywords = keywords
            if (news.siteClassification) {
                currClassification = news.siteClassification.get(0)
            } else {
                if (news.classification) {
                    currClassification = news.classification.get(0)
                }
            }
            news.classification = currClassification
            news.content = StringUtils.Text2Html(news.content)
            latestNewsList << news
            simhashList << news.simhash
            titleList << news.title
            if (latestNewsList.size() >= 5) {
                break
            }
        }
        return apiResult([status: HttpStatus.SC_OK, msg: latestNewsList])
    }

    void adjustSubjectKeywords() {
        List<Subject> subjects = subjectRepo.getSubjects()
        subjects.each { subject ->
            println("before subject" + subject)
            String keywords = subject.keyWords
            String areaWords = subject.areaWords
            List result = []
            if (keywords.indexOf("|") == -1) {
                // 没有竖线 没有地区词
                // 没有竖线 有地区词
                // keywords 没有竖线 地区词只有一个的
                // keywords 没有竖线 地区词有多个的
                List keyList = keywords.split(" ")
                keyList.removeAll([" "])
                keyList.removeAll([""])
                if (keyList.size() == 1 && keywords.indexOf("\n") != -1) {
                    keyList = keywords.split("\n")
                    keyList.removeAll([" "])
                }
                List areaList = areaWords.split(" ")
                areaList.removeAll([" "])
                areaList.removeAll([""])
                if (areaList.size() == 1 && areaWords.indexOf("\n") != -1) {
                    areaList = areaWords.split("\n")
                    areaList.removeAll([" "])
                }
                result.add(["subOperator": "and", "keywords": keyList])
                if (areaList.size() > 0) {
                    result.add(["operator": "and"])
                    result.add(["subOperator": "or", "keywords": areaList])
                }
                keywords = JSONObject.toJSON(result).toString()
            } else {
                // keywords 有竖线 没有
                // keywords 有竖线 地区词有一个的
                if (!areaWords) {
                    println(subject)
                    List<String> rootKeywords = keywords.split("\\|")
                    for (int i = 0; i < rootKeywords.size(); i++) {
                        String keys = rootKeywords.get(i)
                        List keyList = keys.split(" ")
                        keyList.removeAll([" "])
                        keyList.removeAll([""])
                        if (!keyList) {
                            continue
                        }
                        if (result.size() > 0) {
                            result.add(["operator": "or"])
                        }
                        result.add(["subOperator": "and", "keywords": keyList])
                    }
                    keywords = JSONObject.toJSON(result).toString()
                } else {
                    // keywords 有竖线 地区词有多个的
                    println("未处理的subject" + subject)
                }
            }
            subject.keyWords = keywords
            subject.areaWords = areaWords
            println("after subject " + subject)
            subjectRepo.modifySubject(subject)
        }
    }

    List queryAllSubjects() {
        List<Account> validUsers = accountRepo.getValidUsers();
        List subjects = new ArrayList<>();
        def sdf = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss')
        for (int i = 0; i < validUsers.size(); i++) {
            Account account = validUsers.get(i)
            long userId = account.getId();
            String userName = account.getUserName();
            String realName = account.getRealName();
            String company = account.getCompany();
            List<Subject> list = subjectRepo.getUserSubjects(userId);
            List<Map> mapList = new ArrayList<>()
            for (int j = 0; j < list.size(); j++) {
                Subject subject = list.get(j).toMap();
                Map map = new HashMap<>();
                map.id = subject.subjectId;
                map.userId = userId;
                map.userName = userName;
                map.realName = realName;
                map.company = company;
                map.company = company;
                map.subjectName = subject.subjectName;
                map.keyWords = subject.keyWords;
                map.excludeWords = subject.excludeWords
                map.updateTime = sdf.format(subject.updateTime)
                mapList.add(map);
            }
            subjects.addAll(mapList);
        }
        return subjects;
    }

    void excelOut(List subjectList) {
        String outfileName = UUID.randomUUID();
        ExportExcel ex = new ExportExcel();
        String sheetName = "全网监控列表";//下载文件的默认名字,sheet页名字
        String fileName = outfileName
        String headers = "id,userId,userName,realName,company,subjectName,keyWords,excludeWords,updateTime";
//表头
        String selname = "id,userId,userName,realName,company,subjectName,keyWords,excludeWords,updateTime,o"
//标题对应key值

        HSSFWorkbook wb = ex.exportExcel(sheetName, headers, subjectList, selname);
//返回一个workbook即excel实例。datetime是sheet页的名称
        String excelFolder = "D:\\" + sheetName;
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
        println("默认生成目录在 D:\\" + sheetName);
    }


    def getSubjectNewsCountBySubject(long userId, Subject subject) {

        long sys_st = System.currentTimeMillis();

        Date startDate = subject.resetTime ?: null
        long count = subjectRepo.getSubjectNewsCountFromEsByExpressionAndDate(subject, startDate, null)
        subjectRepo.modifySubjectCountInfo(userId, subject.subjectId, count)

        long sys_et = System.currentTimeMillis()
        return [
                count   : count,
                subjectName: subject.subjectName,
                time    : sys_et - sys_st
        ]
    }

    /**
     * 获取用于在某一监控主题的新闻未读数（自用户上次查看过该监控主题后，监控主题新采集的新闻数）
     * @param userId
     * @param subjectId
     * @return
     */
    def getSubjectNewsCountBySubjectId(long userId, String subjectId) {

        Subject subject = subjectRepo.getUserSubjectById(userId, subjectId)

        if(subject.countTime
                && System.currentTimeMillis() - subject.countTime.time < 10 * 60 * 1000        //如果统计间隔小于10分钟，则返回上次统计的数量
        ) {
            return [
                    count   : subject.count,
                    subjectName: subject.subjectName
            ]
        } else {
            return this.getSubjectNewsCountBySubject(userId, subject)
        }
    }

    def resetSubjectCountInfo(long userId, String subjectId) {
        subjectRepo.resetSubjectCountInfo(userId, subjectId)
    }


}

