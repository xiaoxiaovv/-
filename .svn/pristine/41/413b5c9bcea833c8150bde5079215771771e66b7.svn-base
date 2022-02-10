package com.istar.mediabroken.task

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.istar.mediabroken.entity.News
import com.istar.mediabroken.service.CaptureService
import com.istar.mediabroken.utils.BDMongoHolder
import com.istar.mediabroken.utils.DateUitl
import com.istar.mediabroken.utils.MongoHolder
import com.mongodb.BasicDBObject
import com.mongodb.DBCollection
import com.mongodb.DBCursor
import com.mongodb.QueryBuilder
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.text.SimpleDateFormat

@Component
@Slf4j
class NewsFeach2Task implements Task {
    @Autowired
    CaptureService captureSrv
    @Autowired
    BDMongoHolder mongo
    SimpleDateFormat dateSdf = new SimpleDateFormat('yyyy-MM-dd')
    String newsPath = '/var/mediabroken/datapower/news/'

    def weiboFile = new File(newsPath,'weiboUrl.txt')
    @Override
    void execute() {
        if(!weiboFile.exists())
            weiboFile.createNewFile()
        def sdf = new SimpleDateFormat('yyyyMMdd')
        def collection = mongo.getCollection("Configuration")
        def sites = []
        def find = collection.find(  )
        while( find.hasNext( ) ){
            def next = find.next()
            sites << next
        }
//        def sites=
//                [
//                       [ dir:"劳动午报",
//                         value: [
//                                        [
//                                            value:"劳动午报",
//                                            field:"kvSite",
//                                            url:'workerbj.cn'
//                                        ],
//                                        [
//                                            value:"中工网",
//                                            field:"kvSite",
//                                            url:'workercn.cn'
//                                        ],
//                                        [
//                                            value:"京工网",
//                                            field:"kvSite",
//                                            url:'ldwb.com.cn'
//                                        ],
//                                        [
//                                            field:"kvSource",
//                                            value:"劳动午报",
//                                            url:''
//                                        ],
//                                        [
//                                            field:"kvSource",
//                                            value:"中工网",
//                                            url:''
//
//                                        ],
//                                        [
//                                            field:"kvSource",
//                                            value:"京工网",
//                                            url:''
//                                        ]
//                                ]
//                        ]
//                       ,
//                    [
//                        dir:"人民网",
//                        value: [
//                                [
//                                        value:"人民网",
//                                        field:"kvSite",
//                                        filter:{
//                                            News it ->
//                                                if( it.url.indexOf('people.com.cn')&&
//                                                        ( it.newsType == 1 || it.newsType == 9 || it.newsType == 5 ))
//                                                    return false
//                                                else
//                                                    return true
//                                        }
//                                ],
//                                [
//                                        field:"kvSource",
//                                        value:"人民网",
//                                        filter:{
//                                            News it ->
//                                                if( it.newsType == 4 || it.newsType == 6 )
//                                                    return false
//                                                else
//                                                    return true
//                                        }
//
//                                ]
//                            ]
//                        ]
//                ]
        def sTime = System.currentTimeMillis( )
        sites.each { base ->
            def now = DateUitl.getDayBegin( )
            def startTime = DateUitl.getBeginDayOfYesterday()
            def baseFile = new File( newsPath + ( base.organization as String ) )
            log.debug("getdata execute:{}",sdf.format( now ) +":"+ sdf.format(startTime)+":"+baseFile.path)
            if( !baseFile.exists( ) )
                baseFile.mkdirs( )
            def display = startTime
            def disss  = DateUitl.getDistance( now,display )
            for( int i =0 ;i < disss;i++){
                def f = new File( baseFile,sdf.format( display ) )
                display = DateUitl.addDay( display,i)
                if ( f.exists( ) )
                    continue
                else
                    f.mkdirs( )
            }
            while ( true ){
                    for( File dateFile: baseFile.listFiles() ){
                        if( dateFile.isFile() )
                            continue
                        def subFiles = dateFile.listFiles()
                        log.debug("dateFile:{}",dateFile.path )
                        if( subFiles.size() > 0 ){
                            if( subFiles.size() > 15 ){
                                continue
                            }
                            def last = 0L
                            for( File file : subFiles ){
                                if( file.getName().contains("reprint")){
                                    def ss = file.getName().split("_")[1]
                                    def split = sdf.parse(  ss ).getTime()
                                    if( last <= split )
                                        last = split
                                }
                            }
                            def existTime = new Date( last )
                            def startTimeForPublish = sdf.parse( dateFile.getName() )
                            def startTimeForReprint = DateUitl.addDay( existTime,1 )
                            if( startTimeForReprint >= now )
                                continue
                            def distance = DateUitl.getDistance( startTimeForReprint,startTime )
                            def lines = new File( dateFile,"publish.json").readLines()
                            def  titles = []
                            lines.each {
                                def title = null
                                def newsId = null
                                try {
                                    title = JSON.parse(it).title
                                    newsId = JSON.parse(it).newsId
                                }catch (Exception e ){
                                    e.printStackTrace()
                                }
                                titles << [
                                        'title':title,
                                        'newsId':newsId
                                ]
                            }
                            for( int i = 0; i<= distance;i++ ){
                                base.value.each { it ->
                                    saveReprintNewsForSomeDay( startTimeForPublish, startTimeForReprint, titles,it as Map,base.organization as String )
                                }
                                startTimeForReprint = DateUitl.addDay(startTimeForReprint, 1)
                            }
                            lines.clear()
                            titles.clear()
                        }else{
                            def start = sdf.parse( dateFile.getName() )
                            def startReprintTime = start
                            base.value.each { it ->
                                def file = savePublishDate( start, it as Map,base.organization as String)
                                saveReprintNewsForSomeDay( start, startReprintTime, file,it as Map,base.organization  as String )
                            }
                        }
                    }
                    break
                }
            }
        log.debug("spend time:"+ (System.currentTimeMillis() - sTime ) )
    }


    private  boolean filter(String url,List types,News it){
        if( it.url.indexOf( url )&&types.contains(it.newsType))
            return false
        else
            return true
    }
    private  boolean filter(List types,News it){
        if ( types.contains( it.newsType ) )
            return false
        else
            return true
    }

    private List<News> savePublishDate(Date startTimeForPublish, Map newsSite,String base) {
        def endTimeForPublish = getEndOfDay( startTimeForPublish )
        log.debug("getDataOfOneDay:{}, {}", base+":"+dateSdf.format(startTimeForPublish), dateSdf.format(startTimeForPublish))
        def sdf = new SimpleDateFormat("yyyyMMdd")
        def file = new File(newsPath + base +"/", sdf.format(startTimeForPublish))
        if(!file.exists())
            file.mkdirs()
        def publishFile = new File(file, 'publish.json')
        if( !publishFile.exists( ) )
            publishFile.createNewFile( )
        def result = []
        String currentCursor = '*'
        while( true ){
            def paging = captureSrv.getNewsListBySite(newsSite.field as String ,newsSite.value as String, startTimeForPublish,
                    endTimeForPublish, currentCursor, 200)
            paging.list.each {
                def isFilter = false
                if( newsSite.field == "kvSite"){
                    isFilter = filter( newsSite.url as String,newsSite.newsType as List,it )
                }else if( newsSite.field == "kvSource" ){
                    isFilter = filter( newsSite.newsType as List, it )
                }
                if (!isFilter) {
                    result << [
                            'title':it.title,
                            'newsId':it.newsId
                    ]
                    if( it.source!=null && it.source.contains(newsSite.value as String) && it.newsType == 4 ){
                        weiboFile.append( sdf.format(it.createTime) + "\t" + it.url )
                        weiboFile.append("\n")
                    }
                    def news = getNews(it)
                    publishFile.append(JSON.toJSONString(news))
                    publishFile.append("\n")
                }
            }
            if (paging.list.size() <= 0) break
            currentCursor = paging.CurrentCursor
        }
        return result
    }

    private void saveReprintNewsForSomeDay(Date startTimeForPublish, Date startTimeForReprint, List news,Map newsSite,String base){
        def sdf = new SimpleDateFormat("yyyyMMdd")
        def file = new File(newsPath + base +"/", sdf.format(startTimeForPublish))
        def newsIds = new HashSet()
        news.each {
            newsIds.add( it.newsId )
        }
        news.each {
            log.debug ("get media:"+ newsSite.get("field")+":"+newsSite.get("value")+";"+ file.getPath() + "\t" +"publish time:"+startTimeForPublish+"\treprint time:"+startTimeForReprint)
            feachReprintNews( it, startTimeForReprint, file,newsIds,newsSite )
        }
        news.clear()
    }

    private void feachReprintNews( Object publishNews, Date startTimeForReprint, File file,Set newsIds,Map newsSite) {
        def sdf = new SimpleDateFormat("yyyyMMdd")
        def endTimeForReprint = getEndOfDay( startTimeForReprint )
        def reprintFile = new File(file, 'reprint_' + sdf.format(startTimeForReprint) + ".json")
        if(!reprintFile.exists())
            reprintFile.createNewFile()
        def currentCursor = '*'
        while( true ){
            def paging = captureSrv.getNewsListByTitle( publishNews.title as String, startTimeForReprint,
                    endTimeForReprint, currentCursor, 200 )
            paging.list.each {
                if( it.newsType == 1 || it.newsType == 4 || it.newsType == 5 || it.newsType == 6 || it.newsType == 9 ){
                    if( it.newsId != publishNews.newsId && !newsIds.contains(it.newsId) ){
                        def news = getNews(it)
                        newsIds << news.newsId
                        news.publishNewsId = publishNews.newsId
                        reprintFile.append(JSON.toJSONString(news))
                        reprintFile.append("\n")
                        if( it.source!=null && it.source.contains(newsSite.value as String) && it.newsType == 4 ){
                            weiboFile.append( sdf.format(it.createTime) + "\t" + it.url )
                            weiboFile.append("\n")
                        }
                    }
                }
            }
            if ( paging.list.size() <= 0 ) break
            currentCursor = paging.currentCursor
        }
    }

    JSONObject getNews(News news) {
        return JSONObject.toJSON(news)
    }

    Date getBeginDayOf7days() {
        Calendar cal = new GregorianCalendar()
        cal.setTime(DateUitl.getDayBegin())
        cal.add(Calendar.DAY_OF_MONTH, -9)
        cal.add(Calendar.DAY_OF_MONTH, -7)
        cal.add(Calendar.DAY_OF_MONTH, -30)
        return cal.getTime()
    }

    //获取某一指定日期的末点
    Date getEndOfDay(Date date){
        Calendar cal = Calendar.getInstance()
        cal.setTime(date)
        cal.add(Calendar.HOUR_OF_DAY,23)
        cal.add(Calendar.MINUTE,59)
        cal.add(Calendar.SECOND,59)
        cal.add(Calendar.MILLISECOND,999)
        return cal.getTime()
    }

}
