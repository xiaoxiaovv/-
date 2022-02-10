package com.istar.mediabroken.api

import com.alibaba.fastjson.JSON
import com.istar.mediabroken.entity.LoginUser
import com.istar.mediabroken.service.SpreadEvluationService
import com.istar.mediabroken.utils.DateUitl
import com.istar.mediabroken.utils.DownloadUtils
import com.istar.mediabroken.utils.Paging
import groovy.util.logging.Slf4j
import org.apache.commons.httpclient.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletResponse
import java.text.SimpleDateFormat

import static com.istar.mediabroken.api.ApiResult.apiResult
import static org.springframework.web.bind.annotation.RequestMethod.GET
import static org.springframework.web.bind.annotation.RequestMethod.POST

@RequestMapping(value = "/api/4power")
@RestController
@Slf4j
class FourPowerApiController {
    final int ONE_DAY = 24 * 60 * 60 * 1000

    @Autowired
    SpreadEvluationService spreadEvluationSrv


    @Value('${4power.demoUserId}')
    long demoUserId

    /*传播测评-综合分析-报告下载*/
    @RequestMapping(value = "/downLoad4powerSummary", method = POST)
    public Object downLoad4powerSummary(
            HttpServletResponse response,
            @CurrentUser LoginUser user,
            @RequestParam(value = "spreadImg" ,required = false) String spreadImg,
            @RequestParam(value = "effectImg" ,required = false) String effectImg,
            @RequestParam(value = "leaderImg" ,required = false) String leaderImg,
            @RequestParam(value = "trustImg" ,required = false) String trustImg,
            @RequestParam(value = "wordCldImg" ,required = false) String wordCldImg,
            @RequestParam(value = "list" ,required = false) String list,
            @RequestParam(value = "title" ,required = false) String title,
            @RequestParam(value = "sum" ,required = false) String sum
            ) {
        def wordRes = spreadEvluationSrv.getFourPowerSummaryWord(spreadImg,effectImg,leaderImg,trustImg,wordCldImg,list,title,sum)
        if(wordRes.status != HttpStatus.SC_OK) {
            return apiResult(wordRes)
        }
        return apiResult([token:wordRes.outfileName])
    }
    @Deprecated
    @RequestMapping(value = "/downLoad4powerSummaryByToken/{token}", method = GET)
    @ResponseBody
    public Object downLoad4powerSummaryByToken(
            HttpServletResponse response,
            @PathVariable String token
    ){
        def path = spreadEvluationSrv.downLoad4powerSummaryByToken(token);
        if (path.equals("")){
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '没有找到相关下载文件！')
        }
        try {
            DownloadUtils.download(path, response,null)
        } catch (IOException e) {
            return apiResult(HttpStatus.SC_INTERNAL_SERVER_ERROR, '新闻下载发生错误！')
        }
        return apiResult()
    }


    // 得到一周汇总数据
    @RequestMapping(value = "/summaryWeekly", method = GET)
    public Map getSummaryWeekly(
            @CurrentUserId Long userId) {
        def yesterday = DateUitl.getBeginDayOfYesterday()
        def week = DateUitl.addDay(yesterday, -6)
        def rep = [:]
        def analysisSites = spreadEvluationSrv.getAnalysisSites(userId)
        def useDemoData = false
        log.debug('analysisSites: {}', analysisSites)
        if (!analysisSites) {
            userId = demoUserId
            useDemoData = true
            analysisSites = spreadEvluationSrv.getAnalysisSites(userId)
            log.debug('analysisSites2: {}', analysisSites)
        }
        def domains = new HashSet( )
        def types = new HashSet<Integer>()
        analysisSites.sites.each{ site ->
            if( site.siteType == 1 ){
                domains << site.siteDomain
            }
            else{
                domains << site.siteName
            }
            types.addAll( swapType( site.siteType as Integer ))
        }
        log.debug("analysisSites.sites'type:{}",types)
        def distinctDomain = domains as List
        rep.psiTrend = spreadEvluationSrv.getPsiTrend( userId, distinctDomain,types,week, yesterday )
        // 传播力趋势
//        rep.psiTrend = createList(7, { int i ->
//            println i
//            return [
//                    // 发稿量
//                    publishCount: random(100, 800),
//                    // 转载量
//                    reprintCount: random(100, 800),
//                    // 转载媒体数
//                    reprintMediaCount: random(10, 100),
//                    // 转载媒体数
//                    psi: random(300, 800),
//                    // 日期
//                    time: date(tomorrow, i, 7)
//        ]})

        // 影响力
        rep.mii = spreadEvluationSrv.getMII( userId, distinctDomain,types,week, yesterday )
//        rep.mii = createList(7, { int i ->
//            return [
//                    // mii
//                    mii: random(200, 800),
//                    // 阅读数
//                    readingCount: random(100, 800),
//                    // 评论数
//                    commentsCount: random(100, 800),
//                    // 点赞数
//                    likesCount: random(300, 800),
//
//            ]})
        //引导力走势
        rep.gsi = spreadEvluationSrv.getBSI( userId,distinctDomain,types,week, yesterday )
//        rep.bsi =  [
//                // mii
//                bsi: random(10, 100),
//                // 阅读数
//                readingCount: random(1, 10),
//                // 评论数
//                commentsCount: random(1, 10),
//                // 点赞数
//                likesCount: random(1, 10),
//        ]
        // 公信力趋势
//        rep.tsiTrend = createList(7, { int i ->
//            return [
//                    // 阅读数
//                    readingCount: random(5000, 10000),
//                    // 评论数
//                    commentsCount: random(10, 100),
//                    // 点赞数
//                    likesCount: random(300, 800),
//                    // 转载媒体数
//                    reprintMediaCount: random(10, 100),
//                    // 转载媒体数
//                    tsi: random(300, 800),
//                    // 日期
//                    time: date(yesterday, i, 7)
//            ]})
        // 公信力指数
        def month = DateUitl.addDay( yesterday,-29 )

        rep.trustStrengthIndex = spreadEvluationSrv.getTSI( userId,distinctDomain,types,month, yesterday )
//        rep.trustStrengthIndex = random(1,100)
        // 公信力指数
        rep.trustStrength = [
                // 上限
                name: ['稿量', '转量', '大事', '微博', '评论', '正面', '负面', '转媒', '星媒'],
                // 平均
                // [60, 20, 20, 80, 100, 60, 40, 20, 80, 100]
                average: randomList(9, 100, 50)/*[9000,27000,150,1000,2000,1600,200,500,420]*/,
                // 实际
                // [30, 10, 5, 50, 80, 30, 10, 5, 50, 80]
                actual: /*spreadEvluationSrv.getTrustStrength( userId,distinctDomain,type, month, yesterday )*/randomList(9, 80, 5)
        ]
        // 公信力趋势
//        rep.tsiTrend = createList(7, { int i ->
//            return [
//                    // 阅读数
//                    readingCount: random(5000, 10000),
//                    // 评论数
//                    commentsCount: random(10, 100),
//                    // 点赞数
//                    likesCount: random(300, 800),
//                    // 转载媒体数
//                    reprintMediaCount: random(10, 100),
//                    // 转载媒体数
//                    tsi: random(300, 800),
//                    // 日期
//                    time: date(yesterday, i, 7)
//            ]})
//
//        def words = ["北京房价","人民的名义","认购","两会","医改","雾霾","天气","养老金","Express","物联网","牛奶","科罗多",
//                     "Lewis","二孩","进口食品","letv","国际电影节","银行","共享单车","膜拜"]

        // 词云
        rep.tsiTrend = spreadEvluationSrv.getWordCloud( userId, distinctDomain,types,week, yesterday )
//        rep.tsiTrend = createList(words.size(), { int i ->
//            return [
//                    word: words[i],
//                    count: random(200, 10000)
//            ]})
        def details = spreadEvluationSrv.getDetailFourPower( userId, week,yesterday, analysisSites )
        rep.sum = [
                psi: rep.psiTrend.get(6).psi,
                mii: rep.mii.get(6).mii,
                bsi: rep.gsi.gsi,
                tsi: rep.trustStrengthIndex
        ]
        rep.details = details
        rep.details.each {
            if (useDemoData) {
                it.siteName = hideSiteName( it.siteName as String )
            }
        }
//        details.each {
//            rep.sum.psi += it.psi
//            rep.sum.mii += it.mii
//            rep.sum.bsi += it.bsi
//            rep.sum.tsi += it.tsi
//        }
//        analysisSites.sites.each {
//            def result = [
//                    siteTypeName: toSiteTypeName(it.siteType),
//                    siteType: it.siteType,
//                    siteName: it.siteName,
//                    siteDomain: it.get("siteDomain")?it.siteDomain:"",
//                    psi: '分析中',
//                    mii: '分析中',
//                    bsi: '分析中',
//                    tsi: '分析中'
//            ]
//            rep.details << result
//            rep.sum.psi += result.psi
//            rep.sum.mii += result.mii
//            rep.sum.bsi += result.bsi
//            rep.sum.tsi += result.tsi
//        }
        rep.orgName = analysisSites ? analysisSites.orgName : ""
        apiResult(rep)
    }

    List swapType( int siteType ){
        switch (siteType) {
            case 1: return [1,5]
            case 2: return [4]
            case 3: return [6]
            default: [Integer.MIN_VALUE]
        }
    }

    String hideSiteName(String name){
        if (!name) return ''
        def newName = name.substring(0, 1)
        newName += name.substring(1).replaceAll(/./, '*')
        return newName
    }

    String toSiteTypeName(int siteType ){
        switch (siteType) {
            case 1: return '网站'
            case 2: return '微博'
            case 3: return '微信'
            case 4: return '专业网站'
            default: '其它'
        }
    }

    // 得到公信力的发布详情
    @RequestMapping(value = "/tsi/publishDetails", method = GET)
    public Map getPublishDetails (
//            @CurrentUserId Long userId,
//            @PathVariable("days") int days,
            @RequestParam("pageNo") int pageNo,
            @RequestParam("limit") int limit) {

        def rep = [:]
        def medias = [
            ['人民网', '网站'],
              ['微博搞笑排行榜', '公众号'],
            ['澎湃新闻', '网站'],
            ['凤凰网', '网站'],
             ['央视新闻', '公众号'],
            ['新浪体育', '网站'],
            ['回忆专用小马甲', '公众号'],
            ['体育频道', '网站'],
             ['新华网', '网站'],
             ['思想聚焦', '公众号'],
            ['财富网', '网站'],
            ['新浪女性', '网站'],
            ['百度百科', '网站'],
            ['人民网公众号', '公众号'],
            ['新闻哥', '公众号'],
            ['51cto', '网站'],
            ['头条新聞', '网站'],
            ['36氪', '公众号'],
            ['新浪军事', '网站'],
            ['新浪娱乐', '公众号']]

        def paging = new Paging(pageNo, limit, medias.size())

        rep.total = paging.total
        rep.totalPage = paging.totalPage
        rep.list = []
        for (int i = 0; i < limit && (i + paging.offset) < medias.size(); i++) {
            rep.list << [
                    channel: medias[i + paging.offset][1],
                    media: medias[i + paging.offset][0],
                    reprintCount: random(500000),    // 總转载量
                    maxReprintCountOneDay: random(5000),    // 最大日转载量
                    commentCount: random(1500000),  // 評論總量
                    maxCommentCountOneDay: random(15000),  // 最大日論總量
                    badCommentPercentOneDay: 0.18,    // 单日负面评论占比
                    badCommentPercentOneMonth: 0.18,   // 月负面评论占比
            ]
        }

        apiResult(rep)
    }
    // 得到传播力一周汇总数据
    @RequestMapping(value = "/psi/summary/{days:.*}", method = GET)
    public Map getPsiSummary(
            @PathVariable("days") int days,
            @CurrentUserId Long userId) {
        def yesterday = DateUitl.getBeginDayOfYesterday()
        def before = DateUitl.addDay( yesterday,-(days-1))
        def rep = [:]

        // 传播力指数分析
        rep.psi = createList(days, { int i ->
            return [
                    // 发稿量
                    publishCount: random(100000, 150000),
                    // 转载量
                    reprintCount: random(60000, 80000),
                    // 转载媒体数
                    reprintMediaCount: random(5000, 6000),
                    // 转载媒体数
                    psi: floatRandom(8.0, 9.0, 1),
                    // 日期
                    time: date(before, i, days)
            ]})

        // 传播力因素分析
        rep.psiFactor = createList(days, { int i ->
            return [
                    // 发稿量
                    publishCount: random(100000, 150000),
                    // 转载量
                    reprintCount: random(60000, 80000),
                    // 转载媒体数
                    reprintMediaCount: random(4000, 6000),
                   // 日期
                   time: date(before, i, days),
        ]})

        // 传播渠道
//        def types = ["网站","论坛","微信","微博","APP","搜索引擎"]
//        rep.propagationType =  createList(types.size(), { int i ->
//            return [
//                    type: types[i],
//                    count: random(100, 50000)
//            ]})
        rep.propagationType = [
                [
                        type: '网站',
                        count: random(1000, 5000)
                ],
                [
                        type: '论坛',
                        count: random(80, 150)
                ],
                [
                        type: '微信',
                        count: random(80, 260)
                ],
                [
                        type: '微博',
                        count: random(1000, 4000)
                ],
                [
                        type: 'APP',
                        count: random(80, 120)
                ],
                [
                        type: '搜索引擎',
                        count: random(200, 600)
                ],
        ]

        apiResult(rep)
    }

    List createList(int n , Closure closure){
        def list = []
        for (int i = 0; i < n; i++) {
            list << closure(i)
        }
        return list
    }

// 得到媒体影响力汇总数据
    @RequestMapping(value = "/mii/summary/{days:.*}", method = GET)
    public Map getMiiSummary(
//            @CurrentUserId Long userId,
            @PathVariable("days")  int days) {

        def rep = [:]

        // 影响力指数
        rep.influenceIndex = [
            // 上限
            upLimit: numList(days, 47),
            // 指数
            index: randomList(days, 47, 40),
            // 下限
            downLimit: numList(days, 35),
            // 日期
            date: dateList(days)
        ]

        // todo 修改改造
        // 影响力分析
        rep.influence = [
            // 转载媒体
            reprientMedia: randomList(days, 6000, 5000),
            // 评论
            comment: randomList(days, 5000, 2000),
            // 点赞
            like: randomList(days, 10000, 8000),
            // 阅读
            reading: randomList(days, 100000, 80000),
            // 日期
            date: dateList(days)
        ]


        // 传播关键元素,合计
        rep.keyFactorSum = [
            name: ['转载媒体总数', '评论总数', '点赞总数', '阅读总数'],
            value: [random(90000), random(5000), random(30000), random(8000)],
//            // 转载媒体
//            reprientMedia: random(2000),
//            // 评论
//            comment: random(2000),
//            // 点赞
//            like: random(2000),
//            // 阅读
//            reading: random(2000),
        ]

        // 传播关键元素,最大
        rep.keyFactorMax = [
            name: ['单篇最大阅读', '单篇最大媒体转载数', '单篇最大点赞数', '单篇最大评论'],
            value: [random(90000), random(5000), random(30000), random(8000)],
//            // 转载媒体
//            reprientMedia: random(2000),
//            // 评论
//            comment: random(2000),
//            // 点赞
//            like: random(2000),
//            // 阅读
//            reading: random(2000),
        ]


        // todo 修改改造
        // 最佳单篇
        rep.bestNews = [
//            name: ['评论量', '点赞量', '阅读量'],
//            value: [random(2000), random(2000), random(2000)],
            // 标题
            title: '萨德真的威协到我们了吗?!',
            // 转载媒体
            reprientMedia: random(1,1000),
            // 评论
            comment: random(2000,5000),
            // 点赞
            like: random(10000,20000),
            // 阅读
            reading: random(1000,100000),
        ]


        apiResult(rep)
    }

    // 得到公信力(Trust strength index)汇总信息
    @RequestMapping(value = "/tsi/summary/{days:.*}", method = GET)
    public Map getTsiSummary(
//            @CurrentUserId Long userId,
            @PathVariable("days")  int days) {

        def rep = [:]

        // 公信力指数
        rep.trustStrengthIndex = random(100)

        // 公信力指数
        rep.trustStrength = [
            // 上限
            name: ['稿量', '转量', '大事', '微博', '评论', '正面', '负面', '转媒', '星媒'],
            // 平均
            // [60, 20, 20, 80, 100, 60, 40, 20, 80, 100]
            average: randomList(9, 100, 50) ,
            // 实际
            // [30, 10, 5, 50, 80, 30, 10, 5, 50, 80]
            actual: randomList(9, 80, 5),
        ]

        // 公信力变化趋势
        // 30天该如何处理?  不显示
        def now = new Date()
        if (days == 90) {
            rep.tsiTrend = [
                // 公信力指数
                tsi: randomList(3, 45, 35),
                // 时间
                time: [month(now, -3), month(now, -2), month(now, -1)]
            ]
        }

        // 公信力构成
        rep.constructure = [
            // 发布
            publish : [
                name: ['非重大新闻稿量', '重大新闻稿量'],
//                value: [810, 1335],
                value: randomList(2, 2000, 500),
            ],
            comment : [
                name: ['非负面评论量', '负面评论量'],
//                value: [69765, 5908783],
                value: randomList(2, 5908783, 69765),
            ],
            reprient : [
                name: ['非重点媒体量', '重点媒体量'],
//                value: [789, 688],
                value: randomList(2, 800, 600),
            ],

        ]

        apiResult(rep)
    }

    // 得到引导力(Bootstrap Strength Index)汇总信息
    @RequestMapping(value = "/bsi/summary/{days:.*}", method = GET)
    public Map getBsiSummary(
//            @CurrentUserId Long userId,
            @PathVariable("days")  int days) {

        def rep = [:]

        // 引导力变化趋势
        def weeks =  days / 7 as int
        rep.bsiTrend = [
            // 引导力指数
            bsi: randomList(weeks, 90, 85),
            // 时间
            time: weekList(weeks)
        ]

        // 媒体词云
        rep.wordCloud = [
            // 关键词
            word: ["北京房价","人民的名义","认购","两会","医改","雾霾","天气","养老金","Express","物联网","牛奶","科罗多",
                     "Lewis","二孩","进口食品","letv","国际电影节","银行","共享单车","膜拜"],
            // 词频
//            count: [10000,6181,4386,4055,2467,2244,1898,1484,1112,965,847, 582,555, 550,462,366, 360,282,273,265]
        ]

        rep.wordCloud.count = randomList(rep.wordCloud.word.size(), 10000, 200)

        // 全网词云
        rep.theWholeWordCloud = [
            // 关键词
            word: ["北京房价","人民的名义","认购","两会","医改","雾霾","天气","养老金","Express","物联网","牛奶","科罗多",
                     "Lewis","二孩","进口食品","letv","国际电影节","银行","共享单车","膜拜"],
            // 词频
//            count: [100000,26181,14386,14055,12467,2244,1898,1484,1112,965,847,582,555,550,462,366,360,282,273,265]
        ]
        rep.theWholeWordCloud.count = randomList(rep.theWholeWordCloud.word.size(), 10000, 200)

        apiResult(rep)
    }

    // 得到引导力(Bootstrap Strength Index)下,词云相关的关键词
    @RequestMapping(value = "/bsi/topKeywords", method = GET)
    public Map getTopKeywords (
//            @CurrentUserId Long userId,
            @RequestParam("pageNo") int pageNo,
            @RequestParam("limit") int limit) {

        def rep = [:]

        def titles = [
            ['父母被撞，儿子脚踹司机未站稳又摔断腿', '车祸'],
            ['赌博挥霍2400多万公款，贵州黄平县财政局一会计被判无期', '贪污'],
            ['强烈要求政府按教师法解决国企教师待遇问题！！！', '教师待遇'],
            ['为什么举报人心惊胆战，违法者还能趾高气扬呢？', '举报'],
            ['女司机倒车紧张过度，漂移“完美入库”', '女司机'],
            ['佟丽娅半夜晒自拍 陈思诚无视两人无互动', '佟丽娅'],
            ['票房重要吗？冯小刚：不会再被票房绑架', '冯小刚'],
            ['海淀建材城西路一工地发生火灾 无人员伤亡', '火灾'],
            ['西城首设共享单车1.5米“文明停车线', '共享单车'],
            ['俄罗斯说要优先为叙利亚提供防空系统', '叙利亚'],
            ['鹿晗片酬过亿？卓伟:8千万他拿钱后买了别墅', '鹿晗'],
            ['电脑手机一起用，小心记忆被偷走', '记忆'],
            ['妹子追求真爱，却被网友讽刺又肥又丑！男友却说，嫁给我吧！', '真爱'],
            ['偷车贼被抓伺机逃 高校学生:咱体院的!doge', '偷车'],
        ]
        def paging = new Paging(pageNo, limit, titles.size())

        rep.total = paging.total
        rep.totalPage = paging.totalPage
        rep.list = []

        for (int i = 0; i < limit && (paging.offset + i) < titles.size(); i++) {
            rep.list << [
                    keyword: titles[paging.offset + i][1],
                    title: titles[paging.offset + i][0],
                    reprintCount: random(500000),    // 總转载量
                    commentCount: random(1500000),  // 評論總量
                    reprintMediaCount: random(5000),    // 轉載媒體數
                    reprintKeyMediaCount: random(300),   // 重點媒體數
            ]
        }

        apiResult(rep)
    }

    List weekList(int weeks){
        def tomorrow = DateUitl.getBeginDayOfYesterday()
        def sdf = new SimpleDateFormat("MM-dd")
        def list = []
        for (int i = 0; i < weeks; i++) {
            def date = sdf.format(addDate(tomorrow, -((weeks - i - 1) * 7)))
            list << date
        }
        return list
    }

    int random(int num) {
        def random = new Random()
        return random.nextInt(num)
    }

    int random(int low, int high) {
        def interval = high - low
        def random = new Random()
        return random.nextInt(interval) + low
    }

    List numList(int days, int num) {
        def list = []
        for (int i = 0; i < days; i++) {
            list << num
        }
        return list
    }

    List dateList(int days) {
        def tomorrow = DateUitl.getBeginDayOfYesterday()
        def sdf = new SimpleDateFormat("MM-dd")
        def list = []
        for (int i = 0; i < days; i++) {
            def date = sdf.format(addDate(tomorrow, -(days-i) + 1))
            list << date
        }
        return list
    }

    public static Date addDate(Date date, int n) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, n);
        return cal.getTime();
    }

    public static Date addMonth(Date date, int n) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, n);
        return cal.getTime();
    }


    String month(Date now, int interval) {
        def sdf = new SimpleDateFormat("M")
        def d = sdf.format(addMonth(now, interval))
        return d + '月'
    }

    String date(Date tomorrow, int interval, int days) {
        def sdf = new SimpleDateFormat("MM-dd")
        def d = sdf.format(addDate(tomorrow, - (days -interval) + 1))
        return d
    }

    List randomList(int days, int high, int low) {
        def interval = high - low
        def list = []
        def random = new Random()
        for (int i = 0; i < days; i++) {
            list << random.nextInt(interval) + low
        }
        return list
    }

    public static double getRandomNum2(int min,int max){
        return Math.round(Math.random()*(max-min))+min
    }

    static String floatRandom(float min, float max, int digit) {
        def num = Math.random() * (max - min) + min
        return String.format('%.' + digit + 'f', num)
    }


    /**
     * 设置要分析的站点数据
     * sites是一个json结构列表,类似下面的结构
     * [
     *  { siteType: 1, siteName: '网站名', siteDomain: 'domain.com'},
     *  { siteType: 2, siteName: '微信公众号名称'},
     *  { siteType: 4, siteName: '微博名称'},
     * ]
     *
     */
    @RequestMapping(value = "/analysisSites", method = RequestMethod.PUT)
    public Map modifySites(
            @CurrentUserId Long userId,
            @RequestParam(value = "orgName") String orgName,
            @RequestParam(value = "sites") String sites
    ) {
        def sitesArray = JSON.parseArray(sites)
        spreadEvluationSrv.modifyAnalysisSites([
                userId: userId,
                orgName: orgName,
                sites: sitesArray
        ])
        apiResult()
    }

}
