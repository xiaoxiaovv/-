package com.istar.mediabroken.service

import com.alibaba.fastjson.JSONArray
import com.istar.mediabroken.Const
import com.istar.mediabroken.api3rd.TopicApi3rd
import com.istar.mediabroken.entity.*
import com.istar.mediabroken.entity.capture.NewsOperation
import com.istar.mediabroken.repo.account.AccountRepo
import com.istar.mediabroken.repo.CaptureRepo
import com.istar.mediabroken.repo.ICompileRepo
import com.istar.mediabroken.utils.*
import com.istar.mediabroken.utils.WordHtml.RichHtmlHandler
import com.istar.mediabroken.utils.WordHtml.WordGeneratorWithFreemarker
import com.istar.mediabroken.utils.wordseg.WordSegUtil
import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils
import org.apache.http.HttpStatus
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import sun.misc.BASE64Decoder

import javax.imageio.ImageIO
import javax.servlet.http.HttpServletRequest
import java.awt.image.BufferedImage
import java.text.SimpleDateFormat

import static com.istar.mediabroken.api.ApiResult.apiResult
import static org.apache.http.HttpStatus.SC_BAD_REQUEST
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR

@Service
@Slf4j
class ICompileService {
    @Autowired
    AccountRepo accountRepo

    @Autowired
    TopicApi3rd topicApi

    @Autowired
    ICompileRepo iCompileRepo

    @Autowired
    CaptureRepo captureRepo

    @Autowired
    CaptureService captureSrv

    @Value('${image.upload.path}')
    String UPLOAD_PATH

    @Value('${env}')
    String env

    List<News> getRelatedNews(ICompileSummary summary, int limit) {
        return topicApi.getNewsList(summary, 1, limit)
    }

    ICompileSummary getSummary(long userId, String summaryId) {
        def summary = iCompileRepo.getSummary(userId, summaryId)
        summary.session.sid = accountRepo.getYqmsSession(summary.session.userId)
        return summary
    }

    ICompileSummary getSummary(long userId) {
        def summary = iCompileRepo.getSummary(userId)


        // 若时间,地点,人物,事件都没有值,应该是上一个版本的,用title代替
        if (!(summary.time || summary.place || summary.person || summary.event)) {
            summary.event = summary.title
            iCompileRepo.modifySummary(summary)
        }
        return summary
    }

    Map getSprendTrend(ICompileSummary summary) {
        return topicApi.getSprendTrend(summary)
    }

    List<News> getLatestWei(ICompileSummary summary, int limit) {
        def list = topicApi.getWeiboList(summary)
        def result = []
        for (int i = 0; i < list.size() && i < limit; i++) {
            result << list[list.size() - i - 1]
        }
        return result
    }

    Map getTopN(ICompileSummary summary) {
        return topicApi.getTopN(summary)
    }

    Map getEventEvolution(ICompileSummary summary) {
        return topicApi.getEventEvolution(summary)
    }

    Map getIntro(ICompileSummary summary) {
        def rep = [:]
        rep.title = summary.title

        rep.startTime = summary.startTime
        rep.endTime = summary.endTime

        def eventEvolution = topicApi.getEventEvolution(summary)
        println eventEvolution.firstPublish
        if (eventEvolution.firstPublish) {
            eventEvolution.firstPublish.sort { News a, News b ->
                return a.createTime.getTime() - b.createTime.getTime()
            }
            println eventEvolution.firstPublish
            rep.firstPublishSite = eventEvolution.firstPublish[0].site
            rep.firstPublishTime = eventEvolution.firstPublish[0].createTime
        } else {
            rep.firstPublishSite = ""
            rep.firstPublishTime = summary.endTime
        }

        def spreadTrend = topicApi.getSprendTrend(summary)
        rep.totalNews = spreadTrend.totalNews

        def maxCount = -1
        def maxDate = null
        spreadTrend.list.each {
            if (it.news > maxCount) {
                maxCount = it.news
                maxDate = it.time
            }
        }
        rep.maxCount = maxCount
        rep.maxDate = maxDate

        rep.topSites = []
        def topN = topicApi.getTopN(summary)
        for (int i = 0; i < topN.topSites.size() && i < 5; i++) {
            def it = topN.topSites[i]
            rep.topSites << it.site
        }

        return rep
    }

    def todayNewsPush(long userId, String orgId) {
        Date createTime = new DateTime().minusHours(48).toDate()
        List pushList = captureRepo.getNewsPushList(userId, orgId, createTime, 100)

        // 性能不好,可能会漏元素
//        for  ( int  i  =   0 ; i  <  pushList.size()  -   1 ; i ++ )  {
//            for  ( int  j  =  pushList.size()  -   1 ; j  >  i; j -- )  {
//                if  (pushList.get(j).title.equals(pushList.get(i).title))  {
//                    pushList.remove(j);
//                }
//            }
//        }

        // 如果要比较相似度这样写不行
        def pool = new HashSet()
        Iterator<NewsPush> it = pushList.iterator();
        while(it.hasNext()){
            def newsPush = it.next();
            if(pool.contains(newsPush.title)){
                it.remove();
            } else {
                pool.add(newsPush.title)
            }
        }

        return pushList
    }

    AbstractSetting getAbstractSetting(long userId) {
        iCompileRepo.getAbstractSetting(userId)
    }

    def setAbstractSetting(AbstractSetting setting){
        iCompileRepo.setAbstractSetting(setting)
    }
    Map uploadImgFromPc(Long userId, HttpServletRequest request, String UPLOAD_PATH, String type){
        def res = UploadUtil.uploadImg(request, UPLOAD_PATH, type)
        return apiResult(res)
    }
    def getAbstractImgsById(long userId,String abstract_id){
        //获取存储的imgs的信息
        def imgs=iCompileRepo.getAbstractImgsById(userId,abstract_id)
        return imgs;
    }

    Map createNewsAbstract(long userId, String orgId, String newsOperationIds) {
        List<String> newsOperationIdList = newsOperationIds.split(",")
        if(!newsOperationIdList)
            return apiResult(SC_INTERNAL_SERVER_ERROR, '至少选择一条新闻')

        //根据组稿设置信息，过滤新闻数据源
        AbstractSetting setting = iCompileRepo.getAbstractSetting(userId)
        if(setting){
            int newsCount = setting.newsCount
            if(newsCount <= 0)
                return apiResult(SC_INTERNAL_SERVER_ERROR, '摘要设置的要闻数量不能为零')

            if(newsOperationIdList.size() > newsCount)
                return apiResult(SC_INTERNAL_SERVER_ERROR, '新闻的数量超出了摘要设置的要闻数量')
        }
        List<News> newsList = captureSrv.getNewsListByOperationIds(newsOperationIdList)
        if(!newsList){
            return apiResult(SC_INTERNAL_SERVER_ERROR, '没有找到相关的新闻信息')
        }
        //组装新闻摘要对象
        NewsAbstract newsAbstract = new NewsAbstract()
        newsAbstract.userId = userId
        newsAbstract.orgId = orgId
        newsAbstract.author = this.dealAbstractAuthor(userId, setting)
        newsAbstract.abstractId = newsAbstract.createId()
        newsAbstract.title = this.dealAbstractTitle(newsList, setting)
        //content
        String content = ''
        String contentAbstract = ''
        def newsDetail = []
        def imgs = []
        newsList?.each {
            content += "<h2><a href='${it.url}' target='_blank'>${it.title}</a></h2>\n"
            content += "<p>${it.contentAbstract}</p>\n"
            contentAbstract += "${it.title}\n"
            newsDetail << [
                    newsId  : it.newsId,
                    title   : it.title,
                    contentAbstract : it.contentAbstract
            ]
            def currImg = StringUtils.extractImgUrl(it.content);
            if (currImg){
                imgs += currImg
            }
        }
        newsAbstract.content = content
        newsAbstract.contentAbstract = contentAbstract
        newsAbstract.newsDetail = newsDetail
        newsAbstract.imgs = imgs && imgs.size() > 1 ? imgs.unique() : imgs;
        newsAbstract.picUrl = this.dealAbstractPic(newsAbstract.imgs, setting)
        Date now = new Date()
        newsAbstract.createTime = now
        newsAbstract.updateTime = now
        newsAbstract.type = 2
        iCompileRepo.addNewsAbstract(newsAbstract)
        return apiResult([id : newsAbstract.abstractId]);
    }

    Map modifyNewsAbstract (LoginUser user,Map abstractMap){
        def newsAbstractTemp = new NewsAbstract()
        if(abstractMap.containsKey("abstractId") && (abstractMap.abstractId)){
            newsAbstractTemp = iCompileRepo.getNewsAbstract(user.userId, abstractMap.abstractId);
            if(!newsAbstractTemp){
                return apiResult(SC_INTERNAL_SERVER_ERROR, '没有找到要闻摘要信息，请重新生成')
            }
        }else {
            //新创建摘要素材
            newsAbstractTemp.userId = user.userId
            newsAbstractTemp.orgId = user.orgId
            newsAbstractTemp.abstractId = newsAbstractTemp.createId()
            newsAbstractTemp.createTime = new Date()
        }
        if(abstractMap.containsKey("title") && !(abstractMap.title.equals(""))){
            newsAbstractTemp.title = abstractMap.title
        } else {
            return apiResult(SC_INTERNAL_SERVER_ERROR, '请填写标题内容')
        }
        if(abstractMap.containsKey("author")){ newsAbstractTemp.author = abstractMap.author }
        if(abstractMap.containsKey("picUrl")){ newsAbstractTemp.picUrl = abstractMap.picUrl }
        if(abstractMap.containsKey("content")){ newsAbstractTemp.content = abstractMap.content }

        if(abstractMap.containsKey("contentAbstract")){ newsAbstractTemp.contentAbstract = abstractMap.contentAbstract }
        if(abstractMap.containsKey("keyWords") && !(abstractMap.keyWords.equals(""))){
            newsAbstractTemp.keyWords = abstractMap.keyWords
        }else {
            return apiResult(SC_INTERNAL_SERVER_ERROR, '请填写关键词')
        }
        if(abstractMap.containsKey("classification") && !(abstractMap.classification.equals(""))){
            newsAbstractTemp.classification = abstractMap.classification
        } else {
            return apiResult(SC_INTERNAL_SERVER_ERROR, '请填写新闻分类')
        }

        if(abstractMap.containsKey("source")){ newsAbstractTemp.source = abstractMap.source }
        if(abstractMap.containsKey("originalAuthor")){ newsAbstractTemp.originalAuthor = abstractMap.originalAuthor }
        if(abstractMap.containsKey("firstPublishSite")){ newsAbstractTemp.firstPublishSite = abstractMap.firstPublishSite }
        if(abstractMap.containsKey("firstPublishTime") && (abstractMap.firstPublishTime)){
            def sdf = new SimpleDateFormat('yyyy/MM/dd HH:mm')
            def publishTime = null
            try{
                publishTime = sdf.parse(abstractMap.firstPublishTime)
            }catch(Exception e){
                return apiResult(SC_INTERNAL_SERVER_ERROR, "请按'yyyy/MM/dd HH:mm'格式输入时间")
            }
            newsAbstractTemp.firstPublishTime = publishTime
        }
        if(abstractMap.containsKey("originalUrl")){ newsAbstractTemp.originalUrl = abstractMap.originalUrl }
        if(abstractMap.containsKey("type")){
            newsAbstractTemp.type = abstractMap.type
        }else {
            newsAbstractTemp.type = 3
        }
        newsAbstractTemp.updateTime = new Date()
        iCompileRepo.modifyNewsAbstract(newsAbstractTemp)
        return apiResult([abstractId: newsAbstractTemp.abstractId])

    }

    Map removeNewsAbstract (long userId,String abstractId){
        def newsAbstractTemp = iCompileRepo.getNewsAbstract(userId,abstractId);
        if(!newsAbstractTemp){
            return apiResult(SC_INTERNAL_SERVER_ERROR, '没有找到要闻摘要信息，请重新生成')
        }
        iCompileRepo.removeNewsAbstract(userId,abstractId)
        return apiResult();
    }

    private String dealAbstractAuthor(long userId, AbstractSetting setting) {
        String author = ''
        if(setting) {
            author = setting.author
        }
        if(author){
            return author
        }
        def user = accountRepo.getUserById(userId)
        if(user) {
            author  = user.nickName ?: user.userName
        }
        return author
    }

    private String dealAbstractTitle(List<News> newsList, AbstractSetting setting) {

        if(!setting) {
            setting = new AbstractSetting()
        }

        String title = ''
        int maxTitleNum = 4
        newsList.eachWithIndex { it, i ->
            if(title.concat(';').concat(it.title).length() <= setting.titleLength && i < maxTitleNum){
                title += (it.title + ';')
            } else {
                return
            }
        }
        if(!title){
            title = newsList.get(0).title.substring(0, setting.titleLength);
        }
        return title
    }

    private String dealAbstractPic(List picList, AbstractSetting setting) {

        String pic = ''
        if(setting && !setting.showThumbnail){
            return pic
        }
        //根据图片大小，取出第一张
        try {
            InputStream is = null
            BufferedImage img = null
            for (int i = 0; i < picList.size(); i++) {
                is = new URL(picList.get(i)).openStream()
                img = ImageIO.read(is)
                if(img.width > 300 && img.height > 200){
                    pic = picList.get(i)
                    break
                }
            }
        }catch (Exception e) {
            pic = picList?.get(0)
        }
        //因文章内大多没有图片，方便测试，暂时固定一张图片
//        pic = 'http://img.hb.aicdn.com/bf05e750f448d119d295b14eda770f8fc7ed41cd27067-OGSYpG_fw658'
        return pic
    }

    NewsAbstract getNewsAbstract(long userId, String abstractId) {
        return iCompileRepo.getNewsAbstract(userId, abstractId)
    }

    NewsAbstract getNewsAbstractById(String abstractId) {
        return iCompileRepo.getNewsAbstractById(abstractId)
    }

    /**
     * 获取摘要素材信息
     * @param abstractId
     * @return
     */
    Map getNewsAbstractById4Material(String abstractId) {
        NewsAbstract newsAbstract = iCompileRepo.getNewsAbstractById(abstractId)
        if(null == newsAbstract){
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR,msg: "没有找到相关的素材"])
        }
        newsAbstract.imgs = newsAbstract.imgs && newsAbstract.imgs.size() > 1? newsAbstract.imgs.unique():newsAbstract.imgs
        return apiResult([detail: newsAbstract])
    }

    NewsAbstract getTodayNewsAbstract(long userId) {
        return iCompileRepo.getTodayNewsAbstract(userId)
    }

    Paging<NewsAbstract> getPagingAbstractList(long userId, String orgId,int pageNo,int limit ) {
        long total = iCompileRepo.getAbstractTotal(userId, orgId)
        def paging = new Paging<NewsAbstract>(pageNo, limit, total)
        iCompileRepo.getPagingAbstractList(userId, orgId, paging)
        return paging
    }

    def createSummaryPush(long userId, String orgId, String summaryId) {
        ICompileSummary summary = getSummary(userId, summaryId)
        Date now = new Date()
        return captureRepo.addSummaryPush([
                summaryId   : summaryId,
                data        : summary?.data,
                title       : summary?.title,
                source      : PushTypeEnum.SUMMARY_PUSH.value,
                pushType    : PushTypeEnum.SUMMARY_PUSH.index,
                orgId       : orgId,
                userId      : userId,
                status      : Const.PUSH_STATUS_NOT_PUSH,
                createTime  : now,
                updateTime  : now
        ])
    }

    Paging<SummaryPush> getPagingSummaryPushList(long userId, String orgId, int status, int pageNo, int limit) {
        long total = captureRepo.getSummaryPushTotal(userId, orgId, status)
        def paging = new Paging<SummaryPush>(pageNo, limit, total)
        captureRepo.getSummaryPushList(userId, orgId, status, paging)
        return paging
    }

    def createAbstractPush(long userId, String orgId, String abstractId) {
        NewsAbstract newsAbstract = getNewsAbstract(userId, abstractId)
        Date now = new Date()
        return captureRepo.addAbstractPush([
                abstractId  : abstractId,
                title       : newsAbstract?.title,
                source      : PushTypeEnum.ABSTRACT_PUSH.value,
                picUrl      : newsAbstract?.picUrl,
                newsDetail  : newsAbstract?.newsDetail,
                pushType    : PushTypeEnum.ABSTRACT_PUSH.index,
                orgId       : orgId,
                userId      : userId,
                status      : Const.PUSH_STATUS_NOT_PUSH,
                content     : newsAbstract?.content,
                newsAbstract: newsAbstract.toMap(),
                createTime  : now,
                updateTime  : now
        ])
    }

    Paging<AbstractPush> getPagingAbstractPushList(long userId, String orgId, int status, int pageNo, int limit) {
        long total = captureRepo.getAbstractPushTotal(userId, orgId, status)
        def paging = new Paging<AbstractPush>(pageNo, limit, total)
        captureRepo.getAbstractPushList(userId, orgId, status, paging)
        return paging
    }

    ICompileSummary querySummary(ICompileSummary summary) {
        def oldSummary = iCompileRepo.getSummary(summary.userId)

        if (oldSummary) {
            def currentTime = new Date()

            summary.summaryId = oldSummary.summaryId
            summary.yqmsUserId = oldSummary.yqmsUserId
            summary.yqmsTopicId = oldSummary.yqmsTopicId
            summary.updateTime = currentTime
            def session = accountRepo.getYqmsSession2(summary.yqmsUserId)

            topicApi.removeTopic(session, summary.yqmsTopicId)
            def topicId = topicApi.addTopic(session,
                    summary.title,
                    summary.keywords,
                    summary.ambiguous,
                    summary.startTime,
                    summary.endTime
            )?.topicId

            summary.yqmsTopicId = topicId
            iCompileRepo.modifySummary(summary)
        } else {
            // 若没有,则创建
            def currentTime = new Date()
            summary.summaryId = UUID.randomUUID().toString()
            summary.createTime = currentTime
            summary.updateTime = currentTime

            def yqmsUserId = iCompileRepo.getLeastUsedUserIdForTopic()
            summary.yqmsUserId = yqmsUserId
            def session = accountRepo.getYqmsSession2(summary.yqmsUserId)
            def topicId = topicApi.addTopic(session,
                    summary.title,
                    summary.keywords,
                    summary.ambiguous,
                    summary.startTime,
                    summary.endTime
            )?.topicId

            summary.yqmsTopicId = topicId
            iCompileRepo.addSummary(summary)
        }

        return summary
    }

    /**
     * 获取用户摘要列表
     * @param userId
     * @return
     */
    List<ICompileSummary> getSummariesByUserId(long userId) {
        def summaries = iCompileRepo.getSummariesByUserId(userId)
        return summaries
    }

    /**
     * 获取摘要素材列表
     * @param userId
     * @return
     */
    Paging<NewsAbstract> getNewsAbstracts(long userId, int pageNo, int limit,String queryKeyWords,int orderType) {
        def newsAbstractsTotal = iCompileRepo.getNewsAbstractsTotal(userId,queryKeyWords);

        def paging = new Paging<NewsAbstract>(pageNo, limit, newsAbstractsTotal)
        iCompileRepo.getNewsAbstracts(userId, paging,queryKeyWords,orderType)
        return paging
    }
    /**
     * 获取要闻晒选列表
     * @param userId
     * @return
     */
    Paging<NewsOperation> getNewsOperations(long userId, int pageNo, int limit, String queryKeyWords, int orderType, int operationSource, int timeType, String timeStart, String timeEnd) {
        def newsOperationsTotal = iCompileRepo.getNewsOperationsTotal(userId,queryKeyWords,operationSource,timeType,timeStart,timeEnd);

        def paging = new Paging<NewsOperation>(pageNo, limit, newsOperationsTotal)
        iCompileRepo.getNewsOperations(userId, paging,queryKeyWords,orderType,operationSource,timeType,timeStart,timeEnd)

        // 排掉重复的标题
        def pool = new HashSet()
        Iterator<NewsOperation> it = paging.list.iterator()
        while(it.hasNext()){
            def newsOperation = it.next();
            if(pool.contains(newsOperation.news.title)){
                it.remove();
                log.debug('删除重复新闻{}', newsOperation.news.title)
            } else {
                pool.add(newsOperation.news.title)
            }
        }

        return paging
    }

    /**
     * 添加一条要闻摘要
     * @param summary
     * @return
     */
    Map addSummary(ICompileSummary summary) {
        if(!(summary.isEnoughInfo())){
            return apiResult(SC_INTERNAL_SERVER_ERROR, "请至少设置3个关键词")
        }
        //查询当前的总的sumnary的条数如果超过5条，则返回失败
        int userSummaryCount = iCompileRepo.getUserSummaryCount(summary.userId)
        if(userSummaryCount >= 5){
            return apiResult(SC_INTERNAL_SERVER_ERROR, "每个用户最多设置5个传播综述")
        }

        def currentTime = new Date()
        summary.summaryId = UUID.randomUUID().toString()
        summary.createTime = currentTime
        summary.updateTime = currentTime

        def yqmsUserId = iCompileRepo.getLeastUsedUserIdForTopic()
        summary.yqmsUserId = yqmsUserId
        def session = accountRepo.getYqmsSession2(summary.yqmsUserId)
        def topicId = topicApi.addTopic(session,
                summary.title,
                summary.keywords,
                summary.ambiguous,
                summary.startTime,
                summary.endTime
        )?.topicId

        summary.yqmsTopicId = topicId

        def defaultTemplate = getSummaryDefaultTemplate(summary.userId)
        summary.template = defaultTemplate
        iCompileRepo.addSummary(summary)

        return apiResult()
    }

    /**
     * 删除一条要闻摘要
     * @param summary
     * @return
     */
    Map removeSummaryById(long userId,String summaryId) {
        def summary = iCompileRepo.getSummaryById(summaryId)
        if(userId != summary.userId){
            return apiResult(SC_BAD_REQUEST, "没有找到用户的要闻综述")
        }
        if(summary){
            def session = accountRepo.getYqmsSession2(summary.yqmsUserId)
            topicApi.removeTopic(session, summary.yqmsTopicId)
            iCompileRepo.removeSummaryById(summaryId)
        }
        return apiResult()
    }

    /**
     * 获取要闻综述
     * @param id
     * @return
     */
    ICompileSummary getSummaryById(id){
        return iCompileRepo.getSummaryById(id)
    }

    /**
     * 更改要闻综述
     * @param summary
     * @return
     */
    Map modifySummary(ICompileSummary summary) {
        def oldSummary = iCompileRepo.getSummaryById(summary.summaryId)
        if(summary.userId != oldSummary.userId){
            return apiResult(SC_INTERNAL_SERVER_ERROR, "用户信息不匹配")
        }
        if(!oldSummary){
            return apiResult(SC_INTERNAL_SERVER_ERROR, "没有找到相关设置")
        }
        if(summary.equals(oldSummary)){
            return apiResult()
        }
        if(!(summary.isEnoughInfo())){
            return apiResult(SC_INTERNAL_SERVER_ERROR, "请至少设置3个关键词")
        }
        def currentTime = new Date()
        summary.summaryId = oldSummary.summaryId
        summary.yqmsUserId = oldSummary.yqmsUserId
        summary.yqmsTopicId = oldSummary.yqmsTopicId
        summary.updateTime = currentTime
        def session = accountRepo.getYqmsSession2(summary.yqmsUserId)
        topicApi.removeTopic(session, summary.yqmsTopicId)
        def topicId = topicApi.addTopic(session,
                summary.title,
                summary.keywords,
                summary.ambiguous,
                summary.startTime,
                summary.endTime
        )?.topicId
        summary.yqmsTopicId = topicId
        iCompileRepo.modifySummary(summary)
        return apiResult()
    }

    String getKeywords(String title) {
        return WordSegUtil.seg(title)
    }

    def getOpenSummaryPushList(String orgId){
        captureRepo.getOpenSummaryPushList(orgId)
    }

    def getOpenAbstractPushList(String orgId){
        captureRepo.getOpenAbstractPushList(orgId)
    }

    def updateSummaryPush2Pushed(String orgId, String summaryIds){
        captureRepo.modifySummaryPushStatus(orgId, summaryIds.tokenize(','), Const.PUSH_STATUS_PUSHED)
    }

    def updateAbstractPush2Pushed(String orgId, String abstractIds){
        captureRepo.modifyAbstractPushStatus(orgId, abstractIds.tokenize(','), Const.PUSH_STATUS_PUSHED)
    }


    List<News> getDiscussions(ICompileSummary summary, int limit) {
            return topicApi.getDiscussionList(summary, 1, limit)
    }

    List<News> getHotTopic(ICompileSummary summary) {
        return topicApi.getHotTopic(summary)
    }

    Map getWeiboAuthorRelations(ICompileSummary summary) {
        return topicApi.getWeiboAuthorRelations(summary)
    }
    Map getNewsAbstractWord(long userId,String abstractId){

        NewsAbstract newsAbstract = getNewsAbstract(userId, abstractId)
        if(!newsAbstract){
            return apiResult(SC_INTERNAL_SERVER_ERROR, '没有找到相关要闻信息')
        }
        Map<String, Object> data = new HashMap<String, Object>();
        String type = newsAbstract.type?newsAbstract.type.toString():"";
        data.put("type",type?:"");
        data.put("abstractId",newsAbstract.abstractId?:"")
        data.put("title",newsAbstract.title?:"")
        data.put("author",newsAbstract.author?:"")
        data.put("picUrl",newsAbstract.picUrl?:"")
        data.put("updateTime",newsAbstract.updateTime.toLocaleString()?:"")
        data.put("fileName",newsAbstract.abstractId?:"")
        data.fileType = 'downLoadNews'

        List<Map<String, Object>> newsList = new ArrayList<Map<String, Object>>();
        String contentStr=newsAbstract.content?:"";
        //如果文件夹不存在则创建，如果已经存在则删除
        String outfilePath = "/${UPLOAD_PATH}/download/${data.fileType as String}/${data.abstractId as String}";
        File outPath = new File(outfilePath)
        if(!outPath.exists()){
            FileUtils.forceMkdir(outPath)
        }else {
            FileUtils.forceDelete(outPath)
            FileUtils.forceMkdir(outPath)
        }
        //下载图片
        String saveFile = null;
        if(!(null == data.get("picUrl")) && !data.get("picUrl").equals("")){
            if(data.get("picUrl").startsWith("http")){
                saveFile = HttpClientUtil.downLoadFromUrl(data.get("picUrl"),"0",outfilePath)
            }else {
                saveFile = DownloadUtils.downLoadFromPath(UPLOAD_PATH,data.get("picUrl"),"0",outfilePath)
            }
        }

        def imgList = StringUtils.extractImgUrl(contentStr)
        imgList?.eachWithIndex {it, i ->
            String wechatImg = "";
            if(it.startsWith("http")){
                wechatImg = HttpClientUtil.downLoadFromUrl(it,"img"+((i+1) as String),outfilePath)
            }else {
                wechatImg = DownloadUtils.downLoadFromPath(UPLOAD_PATH,it,"img"+((i+1) as String),outfilePath)
            }
            if(wechatImg){
                contentStr = contentStr.replaceAll(it as String, wechatImg)
            }
        }

        StringBuilder sb = new StringBuilder();

        String author = newsAbstract.author?:"";
        String updateTime=newsAbstract.updateTime.toLocaleString()?:""
        String title=newsAbstract.title?:""
        sb.append("<div>");
        sb.append("<h2>"+title+"</h2>");
        sb.append("<p>"+author+" "+updateTime+"</p>");
        if(!(null == data.get("picUrl")) && !data.get("picUrl").equals("")){
            sb.append("<img style='max-width:100%;' src='"+saveFile+"'/>");
        }
        sb.append("</div>");
        sb.append(contentStr);

        RichHtmlHandler handler = new RichHtmlHandler(sb.toString());

        handler.setDocSrcLocationPrex("file:///C:/8595226D");
        handler.setDocSrcParent("file3405.files");
        handler.setNextPartId("01D214BC.6A592540");
        handler.setShapeidPrex("_x56fe__x7247__x0020");
        handler.setSpidPrex("_x0000_i");
        handler.setTypeid("#_x0000_t75");

        handler.handledHtml(false);

        String bodyBlock = handler.getHandledDocBodyBlock();
        System.out.println("bodyBlock:\n"+bodyBlock);

        String handledBase64Block = "";
        if (handler.getDocBase64BlockResults() != null
                && handler.getDocBase64BlockResults().size() > 0) {
            for (String item : handler.getDocBase64BlockResults()) {
                handledBase64Block += item + "\n";
            }
        }
        data.put("imagesBase64String", handledBase64Block);

        String xmlimaHref = "";
        if (handler.getXmlImgRefs() != null
                && handler.getXmlImgRefs().size() > 0) {
            for (String item : handler.getXmlImgRefs()) {
                xmlimaHref += item + "\n";
            }
        }
        data.put("imagesXmlHrefString", xmlimaHref);
        data.put("content", bodyBlock);
        Map result = WordGeneratorWithFreemarker.createDoc(data,UPLOAD_PATH,env);
        if(result.status != HttpStatus.SC_OK) {
            return apiResult(result)
        }

        if(result.status != HttpStatus.SC_OK) {
            return apiResult(result)
        }
        //zip file
        File file = new File(UPLOAD_PATH.concat(result.msg))
        def fileName = StringUtils.removeSpecialCode(data.get("abstractId"))
        if(fileName.length() > 30){
            fileName = fileName.substring(0,30)
        }
        def zipPath = file.getParent() + "/" + fileName + ".zip"
        ZipUitl.zip(file.getParent(),zipPath )
        result.msg = zipPath
        iCompileRepo.addAbstractDowdload(newsAbstract)
        return result
    }

    Map getSummaryWord(String keyWordCloudImg,String relatedNews){
        Map<String, Object> data = new HashMap<String, Object>();
        String outfileName = UUID.randomUUID();
        data.put("fileName",outfileName)
        data.put("fileType","summary")
        data.put("picUrl","https://")
        JSONArray jsonArray = JSONArray.parseArray(relatedNews);
        //如果文件夹不存在则创建，如果已经存在则删除
        List<Map<String, Object>> newsDetail = new ArrayList<Map<String, Object>>();
        jsonArray.each {
            Map<String, Object> news = new HashMap<String, Object>();
            news.put("title",isNullOrNot(it.title));
            news.put("contentAbstract",isNullOrNot(it.contentAbstract));
            news.put("site",isNullOrNot(it.site));
            news.put("time",it.time==null ?"":strToDate(it.time));
            newsDetail.add(news);
        }
        data.put("newsDetail",newsDetail)
        String outfilePath = "/${UPLOAD_PATH}/download/${data.fileType as String}/${data.fileName as String}";
        File outPath = new File(outfilePath)
        if(!outPath.exists()){
            FileUtils.forceMkdir(outPath)
        }else {
            FileUtils.forceDelete(outPath)
            FileUtils.forceMkdir(outPath)
        }
        String[] url = keyWordCloudImg.split(",");
        String getData = url[1];
        try {
            // Base64解码
            byte[] b = new BASE64Decoder().decodeBuffer(getData);
            File file = new File(outfilePath,"keyWordCloudImg.png");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(b);
            if (fos != null) {
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        data.put("keyWordCloudImg",getData)
        Map result = WordUtils.createWord(UPLOAD_PATH, data, env)
        if(result.status != HttpStatus.SC_OK) {
            return apiResult(result)
        }
        //zip file
        File file = new File(UPLOAD_PATH.concat(result.msg))
        def fileName = outfileName
        if(fileName.length() > 30){
            fileName = fileName.substring(0,30)
        }
        def zipPath = file.getParent() + "/" + fileName + ".zip"
        ZipUitl.zip(file.getParent(),zipPath )
        result.msg = zipPath
        return result
    }
    public static String isNullOrNot(String str){
        String strRe="";
        strRe=(str==null?"":StringUtils.html2text(str))
        return strRe
    }
    /*智能组稿-传播综述-图表下载*/
    Map getImageSummaryWord(String intro, String title,String topNews, String list, String newsRank, String psiTrend, String bsiTrend, String bloggerRank, String distribution, String trendWeek, String firstShow, String writenTop, String structure, String miiTop,String powerTop, String region, String opinion, String trendImg, String mapImg, String formImg) {
        String outfileName = UUID.randomUUID();
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("fileName",outfileName)
        data.put("fileType","downLoadImageSummary")
        data.put("picUrl","https://")

        data.put("headercontent",title==null?"图表下载":title);
        data.put("spreadscope",isNullOrNot(intro))//传播范围
        data.put("spreadtrend",isNullOrNot(trendWeek))//传播走势
        data.put("channelcontent",isNullOrNot(firstShow))//各渠道首发媒体
        data.put("articlecontent",isNullOrNot(writenTop))//稿件排名
        data.put("sherecontent",isNullOrNot(structure))//传播平台构成
        data.put("mediasharepower",isNullOrNot(powerTop))//媒体传播力排名
        data.put("mediaimpactpowercontent",isNullOrNot(miiTop))//媒体影响力排名
        data.put("suggestleadercontent",isNullOrNot(opinion))//意见领袖
        data.put("areacontent",isNullOrNot(region))//地域分布


//         各渠道首发媒体 channelList firstShow list
        JSONArray lists = JSONArray.parseArray(list);
        List<Map<String, Object>> channelList = new ArrayList<Map<String, Object>>();
        lists.each {
            Map<String, Object> news = new HashMap<String, Object>();
            news.put("mediatype",isNullOrNot(it.newsTypeName));
            String time=it.time;
            news.put("publishtime",time==null?"":strToDate(time));
            news.put("title",isNullOrNot(it.title));
            news.put("source",isNullOrNot(it.source));
            news.put("link",isNullOrNot(it.url));

            channelList.add(news);
        }

//        稿件排名 articleList writenTop newsRank
        JSONArray newsRanks = JSONArray.parseArray(newsRank);
        List<Map<String, Object>> articleList = new ArrayList<Map<String, Object>>();
        newsRanks.eachWithIndex {it, i ->
            Map<String, Object> news = new HashMap<String, Object>();
            news.put("index",(i+1));
            news.put("title",isNullOrNot(it.title));
            news.put("source",isNullOrNot(it.source));
            String time=it.time;
            news.put("publishtime",time=null?"":strToDate(time));
            news.put("siteshare",isNullOrNot(String.valueOf(it.reprintCountByPersion)));
            news.put("peopleshare",isNullOrNot(String.valueOf(it.reprintCountBySite)));
            news.put("comment",isNullOrNot(String.valueOf(it.commentCount)));
            news.put("dz",isNullOrNot(String.valueOf(it.likesCount)));

            articleList.add(news);
        }

//        媒体传播力排名 newsList powerTop psiTrend
        JSONArray psiTrends = JSONArray.parseArray(psiTrend);
        List<Map<String, Object>> newsList = new ArrayList<Map<String, Object>>();
        psiTrends.eachWithIndex {it, i ->
            Map<String, Object> news = new HashMap<String, Object>();
            news.put("index",(i+1));
            news.put("medianame",isNullOrNot(it.siteName));
            news.put("power",isNullOrNot(String.valueOf(it.psi)));

            newsList.add(news);
        }

//        媒体影响力排名 mediapowerList
        JSONArray bsiTrends = JSONArray.parseArray(bsiTrend);
        List<Map<String, Object>> mediapowerList = new ArrayList<Map<String, Object>>();
        bsiTrends.eachWithIndex {it, i ->
            Map<String, Object> news = new HashMap<String, Object>();
            news.put("index",(i+1));
            news.put("medianame",isNullOrNot(it.siteName));
            news.put("power",isNullOrNot(String.valueOf(it.psi)));

            mediapowerList.add(news);
        }

//        意见领袖 leaderList opinion
        JSONArray bloggerRanks = JSONArray.parseArray(bloggerRank);
        List<Map<String, Object>> leaderList = new ArrayList<Map<String, Object>>();
        bloggerRanks.eachWithIndex {it, i ->
            Map<String, Object> news = new HashMap<String, Object>();
            news.put("index",(i+1));
            news.put("medianame",isNullOrNot(it.name));
            news.put("power",isNullOrNot(String.valueOf(it.personsAffected)));

            leaderList.add(news);
        }
//        各渠道首发媒体 channelList
//        稿件排名 articleList
//        媒体传播力排名 newsList
//        媒体影响力排名 mediapowerList
//        意见领袖 leaderList
        data.put("channelList",channelList)
        data.put("articleList",articleList)
        data.put("newsList",newsList)
        data.put("mediapowerList",mediapowerList)
        data.put("leaderList",leaderList)


        //如果文件夹不存在则创建，如果已经存在则删除
        String outfilePath = "/${UPLOAD_PATH}/download/${data.fileType as String}/${data.fileName as String}";
        File outPath = new File(outfilePath)
        if(!outPath.exists()){
            FileUtils.forceMkdir(outPath)
        }else {
            FileUtils.forceDelete(outPath)
            FileUtils.forceMkdir(outPath)
        }

       // String trendImg, String mapImg, String formImg

        String[] url = trendImg.split(",");
        String trendImgData = url[1];
        try {
            // Base64解码
            byte[] b = new BASE64Decoder().decodeBuffer(trendImgData);
            File file = new File(outfilePath,"trendImgData.png");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(b);
            if (fos != null) {
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        data.put("trendImgData",trendImgData)

        String[] url2 = mapImg.split(",");
        String mapImgData = url2[1];
        try {
            // Base64解码
            byte[] b = new BASE64Decoder().decodeBuffer(mapImgData);
            File file = new File(outfilePath,"mapImgData.png");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(b);
            if (fos != null) {
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        data.put("mapImgData",mapImgData)

        String[] url3 = formImg.split(",");
        String formImgData = url3[1];
        try {
            // Base64解码
            byte[] b = new BASE64Decoder().decodeBuffer(formImgData);
            File file = new File(outfilePath,"formImgData.png");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(b);
            if (fos != null) {
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        data.put("formImgData",formImgData)




        Map result = WordUtils.createWord(UPLOAD_PATH, data, env)
        if(result.status != HttpStatus.SC_OK) {
            return apiResult(result)
        }
        //zip file
        File file = new File(UPLOAD_PATH.concat(result.msg))
        def fileName = outfileName
        if(fileName.length() > 30){
            fileName = fileName.substring(0,30)
        }
        def zipPath = file.getParent() + "/" + fileName + ".zip"
        ZipUitl.zip(file.getParent(),zipPath )
        result.msg = zipPath
        result.outfileName = outfileName
        return result
    }
    List<News> getFirstPublishMedias(ICompileSummary summary) {
        return topicApi.getFirstPublishMedias(summary)
    }

    Map getWeiboAnalysis(ICompileSummary summary) {
        return topicApi.getWeiboAnalysis(summary)
    }

    Paging<AbstractDownload> getPagingAbstractDownload(long userId, String orgId, int pageNo, int limit ) {
        long total = iCompileRepo.getAbstractDownloadTotal(userId as String, orgId)
        def paging = new Paging<AbstractDownload>(pageNo, limit, total)
        iCompileRepo.getPagingAbstractDownloadList(userId as String, orgId, paging)
        return paging
    }

    AbstractPush getAbstractPush(String id) {
        return captureRepo.getAbstractPush(id)
    }

    AbstractDownload getAbstractDownload(String id) {
        return iCompileRepo.getAbstractDownload(id)
    }

    void createNewsAbstractShare(long userId, String orgId, String abstractId, def shareChannelList, def shareContent) {
        NewsAbstract newsAbstract = getNewsAbstract(userId, abstractId)
        Date now = new Date()
        iCompileRepo.addAbstractShare([
                abstractId  : abstractId,
                title       : shareContent.title,
                newsAbstract: newsAbstract.toMap(),
                shareChannel: shareChannelList,//以后根据开放的渠道，记录所有的渠道
                shareContent: shareContent,
                userId      : userId,
                orgId       : orgId,
                createTime  : now,
                updateTime  : now
        ])
    }

    Paging<AbstractShare> getPagingAbstractShare(Long userId, String orgId, int pageNo, int limit) {
        long total = iCompileRepo.getAbstractShareTotal(userId, orgId)
        def paging = new Paging<AbstractShare>(pageNo, limit, total)
        iCompileRepo.getAbstractShareList(userId, orgId, paging)
        return paging
    }
/*
     * 将时间串转换为时间 2017 06 01 14 21 39
     */
    public static String strToDate(String s){
        //1496321853000
        //20170601142139
        String res;
        if (s.length() ==14){
            res = s.substring(0, 4) + "-" + s.substring(4, 6) + "-" + s.substring(6, 8) + " " + s.substring(8, 10) + ":" + s.substring(10, 12) + ":" + s.substring(12, 14);
        }else if (s.length()==13){
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            long lcc_time = Long.valueOf(s);
            res = simpleDateFormat.format(new Date(lcc_time));
        }else{
            res="";
        }
        return res;
    }
    AbstractShare getAbstractShare(String id) {
        return iCompileRepo.getAbstractShare(id)
    }

/*智能组稿-传播综述-组织文档数据*/
    Map getTextSummaryWord(String intro,String title,String keyArticle,String newsRank,String relatedNews,String hotTopic,String latestWeibo,String discussions,String relatedNewsTrack,String hotTopicIntro,String latestWeiboView,String hotDiscussions){
//        报告简介、关键文章、最新追踪、热议观点、最新观点、热门讨论

        Map<String, Object> data = new HashMap<String, Object>();
        String outfileName = UUID.randomUUID();
        data.put("fileName",outfileName)
        data.put("fileType","downLoadTextSummary")

        //关键文章 keyArticle newsRank
        JSONArray keyArticles = JSONArray.parseArray(newsRank);
        List<Map<String, Object>> keyArticleList = new ArrayList<Map<String, Object>>();
//        StringUtils.html2text(news.content),
        keyArticles.eachWithIndex {it, i ->
            Map<String, Object> news = new HashMap<String, Object>();
            news.put("title",it.title==null?"":(i+1)+"、"+ StringUtils.html2text(it.title));
            news.put("contentAbstract",it.contentAbstract==null?"":StringUtils.html2text(it.contentAbstract));
            news.put("source",it.source==null?"":StringUtils.html2text(it.source));
            String time = it.time;
            news.put("time",time==null?"":strToDate(time));

            keyArticleList.add(news)
        }


        //最新追踪   list   relatedNews
        JSONArray relatedNewss = JSONArray.parseArray(relatedNews);
        List<Map<String, Object>> relatedNewsList = new ArrayList<Map<String, Object>>();
        relatedNewss.eachWithIndex {it, i ->
            Map<String, Object> news = new HashMap<String, Object>();
            news.put("title",it.title==null?"":(i+1)+"、"+StringUtils.html2text(it.title));
            news.put("contentAbstract",it.contentAbstract==null?"":StringUtils.html2text(it.contentAbstract));
            news.put("source",it.source==null?"":StringUtils.html2text(it.source));
            String time = it.time;
            news.put("time",time==null?"":strToDate(time));

            relatedNewsList.add(news)
        }

        //热议观点 hotList hotTopic
        JSONArray hotTopics = JSONArray.parseArray(hotTopic);
        List<Map<String, Object>> hotTopicList = new ArrayList<Map<String, Object>>();
        hotTopics.eachWithIndex {it, i ->
            Map<String, Object> news = new HashMap<String, Object>();
            news.put("title",it.title==null?"":(i+1)+"、"+ StringUtils.html2text(it.title));
            news.put("contentAbstract",it.contentAbstract==null?"":StringUtils.html2text(it.contentAbstract));
            news.put("source",it.source==null?"":StringUtils.html2text(it.source));
            String time = it.time;
            news.put("time",time==null?"":strToDate(time));

            hotTopicList.add(news)
        }


        //最新观点 lists  latestWeibo
        JSONArray latestWeibos = JSONArray.parseArray(latestWeibo);
        List<Map<String, Object>> latestWeiboList = new ArrayList<Map<String, Object>>();
        latestWeibos.eachWithIndex {it, i ->
            Map<String, Object> news = new HashMap<String, Object>();
            news.put("title",it.title==null?"":(i+1)+"、"+ StringUtils.html2text(it.title));
            news.put("contentAbstract",it.contentAbstract==null?"":StringUtils.html2text(it.contentAbstract));
            news.put("source",it.source==null?"":StringUtils.html2text(it.source));
            String time = it.time;
            news.put("time",time==null?"":strToDate(time));

            latestWeiboList.add(news)
        }

        //热门讨论 talkList discussions
        JSONArray discussionss = JSONArray.parseArray(discussions);
        List<Map<String, Object>> discussionsList = new ArrayList<Map<String, Object>>();
        discussionss.eachWithIndex {it, i ->
            Map<String, Object> news = new HashMap<String, Object>();
            news.put("title",it.title==null?"":(i+1)+"、"+ StringUtils.html2text(it.title));
            news.put("contentAbstract",it.contentAbstract==null?"": StringUtils.html2text(it.contentAbstract));
            news.put("source",it.source==null?"": StringUtils.html2text(it.source));
            String time = it.time;
            news.put("time",time==null?"":strToDate(time));

            discussionsList.add(news)
        }
        data.put("keyAbstract",isNullOrNot(keyArticle))//关键文章
        data.put("relatedAbstract",isNullOrNot(relatedNewsTrack))//最近追踪
        data.put("hotAbstract",isNullOrNot(hotTopicIntro))//热议观点
        data.put("latestAbstract",isNullOrNot(latestWeiboView))//最新观点
        data.put("discussionsAbstract",isNullOrNot(hotDiscussions))//热门讨论
        data.put("intro",(intro==null||intro=="null"||"".equals(intro))?"":intro)
        data.put("title",(title==null||title=="null"||"".equals(title))?"传播综述文字报告":title)
        data.put("keyArticleList",keyArticleList)
        data.put("relatedNewsList",hotTopicList)
        data.put("hotTopicList",relatedNewsList)
        data.put("latestWeiboList",latestWeiboList)
        data.put("discussionsList",discussionsList)


        String outfilePath = "/${UPLOAD_PATH}/download/${data.fileType as String}/${data.fileName as String}";
        //如果文件夹不存在则创建，如果已经存在则删除
        File outPath = new File(outfilePath)
        if(!outPath.exists()){
            FileUtils.forceMkdir(outPath)
        }else {
            FileUtils.forceDelete(outPath)
            FileUtils.forceMkdir(outPath)
        }
        def result = WordUtils.createWord(UPLOAD_PATH, data, env)
        if(result.status != HttpStatus.SC_OK) {
            return apiResult(result)
        }
        File file = new File(UPLOAD_PATH.concat(result.msg))
        def fileName = StringUtils.removeSpecialCode(data.get("title"))
        if(fileName.length() > 30){
            fileName = fileName.substring(0,30)
        }
        def zipPath = file.getParent() + "/" + fileName + ".zip"
        ZipUitl.zip(file.getParent(),zipPath )
        result.msg = zipPath
        result.outfileName = outfileName
        return result
    }

    List getSourceTypeDistribution(ICompileSummary summary) {
        return topicApi.getSourceTypeDistribution(summary)
    }

    Map getSummaryDistribution(ICompileSummary summary) {
        return topicApi.getSummaryDistribution(summary)
    }

    Map modifySummaryTemplate(long userId,String summaryId,int template){
        iCompileRepo.modifySummaryTemplate(userId, summaryId, template)
        return apiResult()
    }
    /**
     * 获取默认的模板
     * @param userId
     * @return
     */
    int getSummaryDefaultTemplate(long userId){
        def compileSummary = accountRepo.getCompileSummaryProfile(userId)
        if(compileSummary.size() == 0){
            return 1
        }else {
            return compileSummary.defaultTemplate ? compileSummary.defaultTemplate : 1
        }
    }

    Map modifySummaryDefaultTemplate(long userId, int defaultTemplate){
        //是否存在，如果不存在则新增一个，如果存在则修改
        AccountProfile accountProfile = accountRepo.getAccountProfileByUser(userId)
        Date now = new Date();
        if(accountProfile == null){
            accountProfile = new AccountProfile()
            accountProfile.userId = userId
            accountProfile.compileSummary = [defaultTemplate : defaultTemplate]
            accountProfile.updateTime = now
            accountProfile.createTime = now
            accountRepo.addAccountProfile(accountProfile)
        }else {
            def compileSummary = accountProfile.compileSummary;
            if(compileSummary == null){
                compileSummary = [defaultTemplate : defaultTemplate]
            }else {
                compileSummary.defaultTemplate = defaultTemplate
            }
            accountProfile.compileSummary = compileSummary
            accountProfile.updateTime = now
            accountRepo.saveAccountProfile(accountProfile)
        }
        return apiResult()
    }

    String downLoadTextSummaryPathByToken(String token) {
        def filePath = new File(UPLOAD_PATH, 'download').path
        def path = filePath.toString() + File.separator + "downLoadTextSummary" + File.separator + token
        List<String> fileList = ZipUitl.getFileNames(path)
        for (int i = 0; i < fileList.size() ; i++) {
            if(fileList.get(i).endsWith(".zip")){
                path = path + File.separator + fileList.get(i)
                return path
            }
        }
        return ""
    }
    String downLoadImageSummaryPathByToken(String token) {
        def filePath = new File(UPLOAD_PATH, 'download').path
        def path = filePath.toString() + File.separator + "downLoadImageSummary" + File.separator + token
        List<String> fileList = ZipUitl.getFileNames(path)
        for (int i = 0; i < fileList.size() ; i++) {
            if(fileList.get(i).endsWith(".zip")){
                path = path + File.separator + fileList.get(i)
                return path
            }
        }
        return ""
    }
}
