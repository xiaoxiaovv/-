package com.istar.mediabroken.repo

import com.istar.mediabroken.utils.BDMongoHolder
import com.mongodb.BasicDBObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import java.text.ParseException
import java.text.SimpleDateFormat
import groovy.util.logging.Slf4j

@Slf4j
@Repository
class EvluationAnalysisRepo {

    @Autowired
    BDMongoHolder bdMongoHolder

    //获取重大事件
    int getImportantEvent(List<String> domains, Set<Integer> types, String start, String end ){
        def importantEvents = 0
        if( domains == null || domains.size() == 0 )
            return importantEvents
        else{
            def collection = bdMongoHolder.getCollection("statics")
            BasicDBObject query = new BasicDBObject("domain",new BasicDBObject("\$in",domains))
                    .append("createTime",new BasicDBObject("\$gte",start).append("\$lte",end))
                    .append("newsType",new BasicDBObject('$in',types))
            BasicDBObject match = new BasicDBObject("\$match",query)
            BasicDBObject project = new BasicDBObject("\$project",new BasicDBObject( "importantEvents",1))
            BasicDBObject group = new BasicDBObject("\$group",
                    new BasicDBObject("_id",null).append("eventSum",new BasicDBObject("\$sum","\$importantEvents") ) )
            def aggregate = collection.aggregate(Arrays.asList( match,project,group ) )
            def list = aggregate.results()
            for( BasicDBObject dbObject:list ){
                importantEvents =  dbObject.get("eventSum")!=null ? dbObject.get("eventSum") : 0
            }
        }
        return importantEvents
    }
    // 每天发稿量( 原创和转载 )
    Map<String,Long> getPublishCount( List<String> domain, Set<Integer> types, String start, String end, int original ){
        def collection = bdMongoHolder.getCollection("publishCount")
        BasicDBObject query = new BasicDBObject("domain",new BasicDBObject("\$in",domain))
                .append("createTime",new BasicDBObject("\$gte",start).append("\$lte",end))
                .append("newsType",new BasicDBObject('$in',types))
        if( original != Integer.MIN_VALUE ){
            query.append("flag",original)
        }
        BasicDBObject match = new BasicDBObject("\$match",query)
        BasicDBObject projection = new BasicDBObject("\$project",new BasicDBObject("createTime",1)
                .append("publishCount",1).append("domain",1).append("_id",0))
        BasicDBObject group = new BasicDBObject("\$group",
                new BasicDBObject("_id",new BasicDBObject("createTime","\$createTime"))
                        .append("publishSum",new BasicDBObject("\$sum","\$publishCount") ) )
        def aggregate = collection.aggregate(Arrays.asList( match, projection,group ) )
        def list = aggregate.results()
        def result = new HashMap<String,Long>()
        for( BasicDBObject dbObject:list ){
            def time = ((BasicDBObject)dbObject.get("_id")).getString("createTime")
            if( time == null )
                return
            result.put( time,dbObject.get("publishSum") as Long )
        }
        return result
    }
    //每天转载量
    Map<String,Long> getRepintCountByDay(List<String> domains, Set<Integer> types, String start, String end, int reprint ){
        def collection = bdMongoHolder.getCollection("reprint")
        BasicDBObject query = new BasicDBObject("publishDomain",new BasicDBObject("\$in",domains))
                .append("publishTime",new BasicDBObject("\$gte",start).append("\$lte",end))
                .append("publishType",new BasicDBObject('$in',types))
        if( reprint != Integer.MIN_VALUE ){
            query.append("reprintType",reprint)
        }
        BasicDBObject match = new BasicDBObject("\$match",query)
        BasicDBObject projection = new BasicDBObject("\$project",new BasicDBObject("publishTime",1)
                .append("reprintCount",1).append("publishDomain",1).append("_id",0))
        BasicDBObject group = new BasicDBObject("\$group", new BasicDBObject("_id",new BasicDBObject("publishTime","\$publishTime"))
                .append("reprintSum",new BasicDBObject("\$sum","\$reprintCount") ) )
        def aggregate = collection.aggregate(Arrays.asList( match, projection,group ) )
        def list = aggregate.results()
        def result = new HashMap<String,Long>()
        for( BasicDBObject dbObject:list ){
            String time = ((BasicDBObject)dbObject.get("_id")).getString("publishTime")
            result.put( time,dbObject.get("reprintSum") as Long )
        }
        return result
    }
    //每天转载媒体数
    Map<String,Long> getReprintMedia( List<String> domains, Set<Integer> types, String start, String end ){
        return getBaseStatistics(domains,types,start,end,"reprintMediaCount","sum")
    }
    //获取指定时间段内转载媒体总数
    long getTotalReprintMediaNumber(List<String> domain,Set<Integer> types,String start,String end ){
        Map<String, Long> reprintMedia = getReprintMedia(domain, types,start, end)
        long totalReprintNumber = 0
        for( String s: reprintMedia.keySet() ){
            totalReprintNumber += reprintMedia.get(s)
        }
        return totalReprintNumber
    }
    //获取重点媒体数
    Map<String,Integer> getImportantMediaNumber( List<String> domains,Set<Integer> types,String start,String end ){
        return getBaseStatistics(domains,types,start,end,"importantMediaCount","sum")
    }
    private List<String> makeState(String start,String end ){
        List<String> keys = new ArrayList<String>()
        SimpleDateFormat dfs = new SimpleDateFormat("yyyy-MM-dd")
        GregorianCalendar cal = new GregorianCalendar()
        String tmpDate = start
        while( true ){
            keys.add( tmpDate )
            try {
                cal.setTime( dfs.parse(tmpDate))
            } catch (ParseException e) {
                e.printStackTrace()
            }
            cal.add(Calendar.DATE, 1)
            Date tmp = cal.getTime()
            if( tmpDate ==  end ){
                break
            }
            tmpDate = dfs.format(tmp)
        }
        return keys
    }
    //每天阅读数
    Map<String,Long> getReadNumberByDay(List<String> domain,Set<Integer> types,String start,String end){
        return getBaseStatistics(domain,types,start,end,"readsCount","sum")
    }
    //每天评论数
    Map<String,Long> getCommentNumberByDay(List<String> domain,Set<Integer> types,String start,String end){
        return getBaseStatistics(domain,types,start,end,"commentsCount","sum")
    }
    //每天点赞数
    Map<String,Long> getLikeNumberByDay(List<String> domain,Set<Integer> types,String start,String end){
        return getBaseStatistics(domain,types,start,end,"likesCount","sum")
    }
    //最大单篇阅读
    Map<String,Long> getOneShotMaxReadNumber(List<String> domain,Set<Integer> types ,String start,String end){
        return getBaseStatistics(domain,types,start,end,"maxRead","max")
    }
    //最大单篇评论数
    Map<String,Long> getOneShotMaxCommentNumber(List<String>domain, Set<Integer> types ,String start,String end){
        return getBaseStatistics(domain,types,start,end,"maxComment","max")
    }
    //最大单篇点赞数
    Map<String,Long> getOneShotMaxLikeNumber(List<String> domains, Set<Integer> types, String start,String end){
        return getBaseStatistics(domains,types,start,end,"maxLike","max")
    }

    Map<String,Long> getBaseStatistics(List<String> domains, Set<Integer> types,String start, String end, String field, String operator ){
        def collection = bdMongoHolder.getCollection("statics")
        Map<String,Long> result = new HashMap<String,Long>()
        BasicDBObject query = new BasicDBObject("domain",new BasicDBObject("\$in",domains))
                .append("createTime",new BasicDBObject("\$gte",start).append("\$lte",end))
                .append("newsType",new BasicDBObject('$in',types))
        BasicDBObject match = new BasicDBObject("\$match",query)
        BasicDBObject projection = new BasicDBObject("\$project",new BasicDBObject("createTime",1)
                .append(field,1).append("domain",1).append("_id",0))
        BasicDBObject group = new BasicDBObject("\$group", new BasicDBObject("_id",new BasicDBObject("createTime","\$createTime"))
                .append("sum",new BasicDBObject("\$"+operator,"\$"+field) ) )
        def aggregate = collection.aggregate(Arrays.asList( match, projection,group ) )
        def list = aggregate.results()
        for( BasicDBObject dbObject:list ){
            String time = ((BasicDBObject)dbObject.get("_id")).getString( "createTime" )
            def sum = dbObject.get("sum")
            result.put( time, sum != null && sum.toString()!="null"? ( sum as Long ) : 0L )
        }
        return result
    }
    //单篇平均点赞
    Map<String,Double>  getPerAvgLike(List<String> domain, Set<Integer> types, String start, String end ){
        return getBaseStatistics( domain, types,start, end,"avgLike","avg" )
    }
    //单篇平均阅读
    Map<String,Double> getPerAvgRead( List<String> domain, Set<Integer> types, String start, String end ){
        return getBaseStatistics( domain, types,start, end,"avgRead","avg" )
    }
    //单篇平均评论
    Map<String,Double> getPerAvgComment( List<String> domain, Set<Integer> types, String start, String end ){
        return getBaseStatistics(domain,types, start, end,"avgComment","avg")
    }
    int getOrDefault(Map<String,Integer> map,String key){
        if(map.keySet().contains( key ) )
            return map.get(key)
        else
            Integer.MIN_VALUE
    }
    //一周词云
    List<String> getWordCloud(List<String> domains, Set<Integer> types, String start, String end ){
        List<String> result = new ArrayList<String>()
        def map = new HashMap<String,Integer>()
        def collection = bdMongoHolder.getCollection('keyword')
        def totalMatch = new BasicDBObject('$match',new BasicDBObject('domain','total')
                .append("time",new BasicDBObject('$gte',start).append('$lte',end)))

        def project = new BasicDBObject("\$project",new BasicDBObject("keywords",1).append("time",1))
        def unwind = new BasicDBObject("\$unwind","\$keywords")
        def sort = new BasicDBObject("\$sort",new BasicDBObject("count",-1))
        def limit = new BasicDBObject("\$limit",20)
        def aggregate = collection.aggregate( Arrays.asList( totalMatch,project ,unwind,sort,limit))
        for( BasicDBObject dbObject : aggregate.results() ){
            def res = dbObject.get("keywords") as BasicDBObject
            map.put(res._1 as String,res._2 as int)
        }
        def query =  new BasicDBObject("domain",new BasicDBObject("\$in",domains))
                .append("time",new BasicDBObject("\$gte",start).append("\$lte",end))
                .append("newsType",new BasicDBObject('$in',types))
        BasicDBObject match = new BasicDBObject("\$match",query)
        BasicDBObject group = new BasicDBObject("\$group",new BasicDBObject("_id","\$keywords._1").
                append("count",new BasicDBObject("\$sum","\$keywords._2")))
        def siteWord = collection.aggregate( Arrays.asList( match,project ,unwind,group,sort,limit))
        for( BasicDBObject dbObject : siteWord.results() ){
            def value = getOrDefault( map,dbObject._id as String )
            if( value == Integer.MIN_VALUE ){
                map.put( dbObject._id as String,dbObject.count as int )
            }else{
                value += dbObject.count as int
                map.put( dbObject._id as String,value )
            }
        }
        map.each { key,value ->
            result <<[
                    word: key,
                    count: value
            ]
        }
        return result
    }

    //词云指数
    private int getKeyWordIndex( List<String> domains, Set<Integer> types, String start, String end, String field ){
        def collection = bdMongoHolder.getCollection("keyword")
        BasicDBObject query = new BasicDBObject("domain",new BasicDBObject("\$in",domains))
                .append("time",new BasicDBObject("\$gte",start).append("\$lte",end))
                .append("newsType",new BasicDBObject('$in',types))
        BasicDBObject match = new BasicDBObject("\$match",query)
        BasicDBObject projection = new BasicDBObject("\$project",new BasicDBObject("time",1)
                .append(field,1).append("domain",1).append("_id",0))
        BasicDBObject group = new BasicDBObject("\$group",
                new BasicDBObject("_id",new BasicDBObject("time","\$time"))
                        .append("WordMatch",new BasicDBObject("\$sum","\$"+field) ) )
        def aggregate = collection.aggregate(Arrays.asList( match , projection,group ) )
        def result = 0
        for( BasicDBObject dbObject:aggregate.results() ){
            result += dbObject.get("WordMatch") as int
        }
        return result
    }
    //关键词匹配数
    int getKeyWordMatch( List<String> domains, Set<Integer> types, String start, String end  ){
        return getKeyWordIndex(domains,types,start,end,"WordMatch")
    }
    //关键词传播数
    int getKeyWordSpread( List<String> domains, Set<Integer> types, String start, String end  ){
        return getKeyWordIndex(domains,types,start,end,"wordSpread")
    }

    long getOrDefault(Map<String,Long> map, String key, long defaultValue) {
        if (map.containsKey(key)) {
            return map.get(key)
        } else {
            return defaultValue
        }
    }
    double getOrDefault(Map<String,Double> map, String key, double defaultValue) {
        if (map.containsKey(key)) {
            return map.get(key)
        } else {
            return defaultValue
        }
    }
    //计算传播力指数
    Map<String,Double> ComputePSI( List<String> domain, Set<Integer> types, String start, String end ){
        List<String> keys = makeState(start,end)
        Map<String,Double> result = new HashMap<String, Double>( )
        Map<String,Long> PostWEB = getPublishCount(domain, types,start, end, 1)
        Map<String,Long> PostBBS = getPublishCount(domain,types, start, end, 2)
        Map<String,Long> PostWechat = getPublishCount(domain,types, start, end, 6)
        Map<String,Long> PostWeibo = getPublishCount(domain, types,start, end, 4)
        Map<String,Long> APP = getPublishCount(domain, types,start, end, 9)
        Map<String,Long> ReprintWEB = getRepintCountByDay(domain, types,start, end, 1 )
        Map<String,Long> ReprintBBS = getRepintCountByDay(domain,types, start, end, 2 )
        Map<String,Long> ReprintWeibo = getRepintCountByDay(domain, types,start, end, 6 )
        Map<String,Long> ReprintWechat = getRepintCountByDay(domain,types, start, end, 4 )
        def totalMedia = getReprintMedia(domain,types,start,end)
        def importantMediaNumber = getImportantMediaNumber(domain, types,start, end)
        for( String s:keys ){
            long postWEB = getOrDefault(PostWEB, s,0L)
            long postBBS = getOrDefault(PostBBS,s, 0L)
            long postWechat = getOrDefault(PostWechat,s, 0L)
            long postWeibo =  getOrDefault(PostWeibo,s, 0L)
            long aPP =   getOrDefault(APP,s, 0L)
            long PostMount = postWEB+postBBS+postWechat+postWeibo+aPP
            long reprintWEB =  getOrDefault(ReprintWEB,s, 0L)
            long reprintBBS =   getOrDefault(ReprintBBS,s, 0L)
            long reprintWeibo = getOrDefault(ReprintWeibo,s, 0L)
            long reprintWechat =  getOrDefault(ReprintWechat,s, 0L)
            def important = getOrDefault( importantMediaNumber,s,0 )
            long ReprintMount = reprintWEB+reprintBBS+reprintWeibo+reprintWechat
            double publish = 0.4*(0.1*Math.log(postWEB+1)+0.1*Math.log(postBBS+1)+0.1*Math.log(postWechat+1)
                    +0.1*Math.log(postWeibo+1)+0.1*Math.log(aPP+1)+0.5*Math.log(PostMount+1))
            double reprint = 0.5*(0.2*Math.log(reprintWEB+1)+0.1*Math.log(reprintBBS+1)+0.2*Math.log( reprintWeibo+1 )
                    +0.1*Math.log(reprintWechat+1) +0.4*Math.log(ReprintMount+1))
            double media =  0.1*(0.6*Math.log(getOrDefault(totalMedia,s,0L)+1)+0.4*Math.log(important+1))
            result.put(s,getZero2(publish + reprint + media,100))
        }
        return result
    }
    //计算影响力指数
    Map<String,Double> ComputeMII(List<String> domain, Set<Integer> types, String start, String end ){
        List<String> keys = makeState( start, end )
        Map<String, Double> Zav = getPerAvgLike( domain, types,start, end )
        Map<String, Double> Rav = getPerAvgRead(domain, types,start, end)
        Map<String, Double> Pav = getPerAvgComment(domain, types,start, end)
        Map<String, Long> Rmax = getOneShotMaxReadNumber(domain, types,start, end)
        Map<String, Long> Zmax = getOneShotMaxLikeNumber(domain, types,start, end)
        Map<String, Long> Pmax = getOneShotMaxCommentNumber(domain,types,start,end)
        Map<String, Long> comment = getCommentNumberByDay(domain,types,start,end)
        Map<String, Long> like    = getLikeNumberByDay(domain,types,start,end)
        Map<String, Long> read    = getReadNumberByDay (domain,types,start,end)
        Map<String,Double> result = new HashMap<>( )
        def totalMedia = getReprintMedia(domain,types,start,end)
        def important = getImportantMediaNumber(domain,types,start,end)
        for( String key:keys ){
            Double readIndex = 0.6*(0.4*Math.log( getOrDefault(read,key,0L) +1 ) + 0.4* Math.log(getOrDefault(Rav,key,0.0)+1) +
                    0.1* Math.log( getOrDefault(Rmax,key,0.0) +1)+0.1*Math.log(getOrDefault(read,key,0L)+1))
            Double commentIndex = 0.2*( 0.4*Math.log(getOrDefault(comment,key,0L) +1)+0.4*Math.log(getOrDefault(Pav,key,0.0)+1)+
                    0.1*Math.log(getOrDefault(Pmax,key,0L)+1)+ 0.1* Math.log(getOrDefault(comment,key,0L)+1) )
            Double likeIndex = 0.1*( 0.4*Math.log(getOrDefault(like,key,0L)+1) +0.4*Math.log(getOrDefault(Zav,key,0.0)+1)+
                    0.1*Math.log(getOrDefault(Zmax,key,0L)+1)+0.1*Math.log(getOrDefault(like,key,0L)+1))
            Double PathIndex = 0.1 * ( 0.6 * Math.log( getOrDefault(totalMedia,key,0)+1 ) +
                    0.4* Math.log( getOrDefault(important,key,0) +1))
            result.put( key,getZero2( readIndex+commentIndex+likeIndex+PathIndex,100 ) )
        }
        result
    }
    //计算引导力指数
    double ComputeBSI(List<String> domain, Set<Integer> types, String start, String end ){
        List<String> keys = makeState(start, end)
        int len = keys.size()
        Map<String, Double> Rav = getPerAvgRead(domain, types,start, end)
        Map<String, Double> Pav = getPerAvgComment(domain,types, start, end)
        Map<String, Long> Rmax = getOneShotMaxReadNumber(domain,types, start, end)
        Map<String, Long> Pmax = getOneShotMaxCommentNumber(domain,types,start,end)
        Map<String, Long> comment = getCommentNumberByDay(domain,types,start,end)
        Map<String, Long> read = getReadNumberByDay (domain,types,start,end)
        Map<String, Double> Zav = getPerAvgLike(domain, types,start, end)
        Map<String, Long> Zmax = getOneShotMaxLikeNumber(domain, types,start, end)
        Map<String, Long> like = getLikeNumberByDay(domain,types,start,end)
        int keyMatch  = getKeyWordMatch( domain,types,start,end )
        int wordSpread = getKeyWordSpread( domain,types,start,end )
        def totalRead = 0L
        read.each { key,value ->
            totalRead += value
        }
        def totalComment  = 0L
        comment.each { key,value ->
            totalComment += value
        }
        long totalLike = 0L
        like.each { key,value ->
            totalLike += value
        }
        double perRead = 0.0
        Rav.each { key,value ->
            perRead += value
        }
        perRead = perRead/len
        double perLike = 0.0
        Zav.each { key,value ->
            perLike += value
        }
        perLike = perLike/len
        double perCo = 0.0
        Pav.each { key,value ->
            perCo += value
        }
        perCo = perCo/len
        long ReadMax = 0L
        Rmax.each { key,value ->
            if( ReadMax <= value )
                ReadMax = value
        }
        long LikeMax = 0L
        Zmax.each { key,value ->
            if( LikeMax <= value )
                LikeMax = value
        }
        long CommentMax = 0L
        Pmax.each { key,value ->
            if( CommentMax <= value )
                CommentMax = value
        }
        def readIndex = 0.05*( 0.4*Math.log(totalRead/len+1)+0.4*Math.log(perRead+1)+0.1*Math.log(ReadMax+1) +0.1*Math.log(totalRead+1))
        def likeIndex = 0.05*( 0.4*Math.log(totalLike/len + 1)+0.4*Math.log(perLike+1)+0.1*Math.log(LikeMax+1) +0.1*Math.log(totalLike+1))
        def commentIndex = 0.1*( 0.4*Math.log(totalComment/len + 1)+0.4*Math.log(perCo+1)+0.1*Math.log(CommentMax+1) +0.1*Math.log(totalComment+1))
        def moodIndex = 0.4*(0.4*Math.log(totalComment/len+1)+0.6*Math.log( 1 )-0.4*Math.log(1))
        def wordCloud = 0.4*(0.2*Math.log(keyMatch/len+1)+0.6*Math.log( 1 )+0.2*Math.log(wordSpread/len +1 ))
        def BSI = readIndex + likeIndex+ commentIndex+moodIndex+wordCloud
        return getZero2( BSI,100 )
    }
    //计算公信力
    double ComputeTSI(List<String> domain, Set<Integer> types, String start, String end ){
        List<String> keys = makeState(start, end)
        int len = keys.size()
        Map<String, Long> publishCount = getPublishCount(domain, types,start, end,0)
        long totalPublish = 0L
        for( long value: publishCount.values( ) ){
            totalPublish+= value
        }
        //日均原稿量
        long avgPublish = totalPublish/len
        Map<String, Long> countByDay = getRepintCountByDay(domain,types, start, end,Integer.MIN_VALUE)
        long totalReprint = 0L
        for (long value : countByDay.values() ){
            totalReprint+=value
        }
        //日均转载量
        long avgReprint = totalReprint/len
        //日均被转载微博数
        long avgWeiboReprint = 0L
        //日均被转载微信数
        long avgWeixinReprint = 0L
        //日均重大事件稿量
        long avgimportantEventNum = getImportantEvent( domain,types,start,end )/len
        //转载媒体总数
        long totalMedia = getTotalReprintMediaNumber(domain,types,start,end)
        long avgMedia = totalMedia/len
        //日均重点媒体总数
        def importantMedia = getImportantMediaNumber(domain,types,start,end)
        def important = 0
        importantMedia.each {key,value->
            important += value
        }
        long avgImportant = important/len
        //日均评论总数
        Map<String, Long> commentNumberByDay = getCommentNumberByDay(domain,types, start, end)
        long avgCommentNumbers = 0L
        for( long value : commentNumberByDay.values() ){
            avgCommentNumbers+=value
        }
        avgCommentNumbers = avgCommentNumbers/len
        double eventIndexes = 0.3*(   0.1*Math.log(avgPublish+1)+0.2*Math.log(avgReprint+1)+ 0.05*Math.log(avgWeiboReprint+1)
                +0.05*Math.log(avgWeixinReprint+1)+0.6*Math.log(avgimportantEventNum+1))
        double EachOtherIndexes = 0.2*(0.4*Math.log(avgCommentNumbers+1)+0.6*Math.log(1)-0.4*Math.log(1))
        double mediaIndex = 0.5*(0.2*Math.log(avgMedia+1 )+ 0.8* Math.log(avgImportant+1))
        getZero2(eventIndexes + EachOtherIndexes + mediaIndex,100)
    }

    double getZero2(double param, int enlarge ){
        BigDecimal b = new BigDecimal(param*enlarge)
        double Zero2  = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()
        return Zero2
    }

}
