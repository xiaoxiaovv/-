package com.istar.mediabroken.service

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.istar.mediabroken.repo.EvluationAnalysisRepo
import com.istar.mediabroken.repo.SpreadEvluationRepo
import com.istar.mediabroken.utils.DateUitl
import com.istar.mediabroken.utils.StringUtils
import com.istar.mediabroken.utils.WordUtils
import com.istar.mediabroken.utils.ZipUitl
import org.apache.commons.io.FileUtils
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import sun.misc.BASE64Decoder

import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat

import groovy.util.logging.Slf4j

import static com.istar.mediabroken.api.ApiResult.apiResult

@Slf4j
@Service
class SpreadEvluationService {

    @Autowired
    SpreadEvluationRepo spreadEvluationRepo

    @Autowired
    EvluationAnalysisRepo evluationAnalysisRepo

    @Value('${image.upload.path}')
    String UPLOAD_PATH

    @Value('${env}')
    String env

    void modifyAnalysisSites(Map analysisSites) {
        List<String> stringList = [];
        JSONArray sitesList =analysisSites.sites;//去重
        JSONArray newSitesList =[];//去重
        for (int i = 0; i <sitesList.size() ; i++) {
            JSONObject obj=sitesList.get(i)
            if (obj){
                if (stringList!=null && stringList.contains(obj.get("siteName")+","+obj.get("siteDomain")+","+obj.get("siteType")) ){
                    continue;
                }else{
                    stringList<<(obj.get("siteName")+","+obj.get("siteDomain")+","+obj.get("siteType"));
                    newSitesList<<obj;
                }
            }
        }
        analysisSites.sites=newSitesList;
        spreadEvluationRepo.modifyAnalysisSites(analysisSites)
    }

    Map getAnalysisSites(long userId) {
        return spreadEvluationRepo.getAnalysisSites(userId)
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

    List getDetailFourPower(long userId, Date startDate, Date endDate, Map analysisSites) {
        def domains = []
        def sdf = new SimpleDateFormat("yyyy-MM-dd")
        def start = sdf.format(startDate)
        def end = sdf.format(endDate)
        def sites = analysisSites.sites as Set
        sites.each { it ->
            if( it.siteType == 1 )
                domains <<[
                        domain : [it.siteDomain],
                        siteName: it.siteName,
                        siteDomain: it.siteDomain,
                        type:Integer.MIN_VALUE
                ]
            else if( it.siteType == 2){
                domains <<[
                        domain : [it.siteName],
                        siteName: it.siteName,
                        siteDomain: it.siteDomain,
                        type:4
                ]
            }else if( it.siteType == 3 ){
                domains <<[
                        domain : [it.siteName],
                        siteName: it.siteName,
                        siteDomain: it.siteDomain,
                        type:6
                ]
            }
        }
        def result = []
        def month = sdf.format( DateUitl.addDay( endDate,-29 ) )
        domains.each { it ->
            def domain = it.domain as List
            def type = new HashSet<Integer>()
            if( it.type == Integer.MIN_VALUE ){
                type.add(1)
                type.add(5)
            }else{
                type.add( it.type as Integer )
            }
            def miis= evluationAnalysisRepo.ComputeMII( domain,type, end,end )
            def mii = miis.values().sum() as double
            def psis = evluationAnalysisRepo.ComputePSI( domain,type, end,end )
            def bsi = evluationAnalysisRepo.ComputeBSI( domain,type, start,end )
            def tsi = evluationAnalysisRepo.ComputeTSI( domain,type, month,end )
            def psi = psis.values().sum() as double
            if( it.type == 6 ){
                result << [
                        siteTypeName: toSiteTypeName( 3 ),
                        siteType: 3,
                        siteName: it.siteName,
                        siteDomain: it.siteDomain,
                        psi: psi !=0 ? transferDouble(psi):'分析中',
                        mii: mii !=0 ? transferDouble(mii):'分析中',
                        bsi: bsi !=0 ? transferDouble(bsi):'分析中',
                        tsi: tsi !=0 ? transferDouble(tsi):'分析中'
                ]
            }else if( it.type == 4 ){
                result << [
                        siteTypeName: toSiteTypeName( 2 ),
                        siteType: 2,
                        siteName: it.siteName,
                        siteDomain: it.siteDomain,
                        psi: psi !=0 ? transferDouble(psi):'分析中',
                        mii: mii !=0 ? transferDouble(mii):'分析中',
                        bsi: bsi !=0 ? transferDouble(bsi):'分析中',
                        tsi: tsi !=0 ? transferDouble(tsi):'分析中'
                ]
            }else if( it.type == Integer.MIN_VALUE){
                result << [
                        siteTypeName: toSiteTypeName( 1 ),
                        siteType: 1,
                        siteName: it.siteName,
                        siteDomain: it.siteDomain,
                        psi: psi !=0 ? transferDouble(psi):'分析中',
                        mii: mii !=0 ? transferDouble(mii):'分析中',
                        bsi: bsi !=0 ? transferDouble(bsi):'分析中',
                        tsi: tsi !=0 ? transferDouble(tsi):'分析中'
                ]
            }
        }
        return result
    }

    List<String> makeState(String start, String end ){
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
            if( tmpDate==  end ){
                break
            }
            tmpDate = dfs.format(tmp)
        }
        return keys
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

    public static String isNullOrNot(String str){
        String strRe="";
        strRe=(str==null?"":StringUtils.html2text(str))
        return strRe
    }

    String downLoad4powerSummaryByToken(String token) {
        def filePath = new File(UPLOAD_PATH, 'download').path
        def path = filePath.toString() + File.separator + "4powersIndexSummary" + File.separator + token
        List<String> fileList = ZipUitl.getFileNames(path)
        for (int i = 0; i < fileList.size() ; i++) {
            if(fileList.get(i).endsWith(".zip")){
                path = path + File.separator + fileList.get(i)
                return path
            }
        }
        return ""
    }

    Map getFourPowerSummaryWord(String spreadImg, String effectImg, String leaderImg, String trustImg, String wordCldImg, String datas, String title, String sumStr) {
        String outfileName = UUID.randomUUID()
        Map<String, Object> data = new HashMap<String, Object>()
        data.put("fileName", outfileName)
        data.put("fileType", "4powersIndexSummary")
        data.put("picUrl", "https://")
        data.put("title",isNullOrNot(title))
//         详细列表 datas
        JSONArray datasList = JSONArray.parseArray(datas);
        List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
        datasList.eachWithIndex {it, i ->
            Map<String, Object> news = new HashMap<String, Object>();
            news.put("index",(i+1));
            news.put("siteTypeName",isNullOrNot(it.siteTypeName));//站点类别
            news.put("siteName",isNullOrNot(it.siteName));//站点名称
            news.put("psi",it.psi);//传播力
            news.put("mii",it.mii);//影响力
            news.put("bsi",it.bsi);//引导力
            news.put("tsi",it.tsi);//公信力
            dataList.add(news);
        }
        data.put("dataList",dataList)

        JSONObject sum = JSONObject.parse(sumStr)
        data.put("psiTot",sum.containsKey("psi")?sum.get("psi") : "0.00");
        data.put("miiTot",sum.containsKey("mii")?sum.get("mii") : "0.00");
        data.put("bsiTot",sum.containsKey("bsi")?sum.get("bsi") : "0.00");
        data.put("tsiTot",sum.containsKey("tsi")?sum.get("tsi") : "0.00");

        //如果文件夹不存在则创建，如果已经存在则删除
        String outfilePath = "/${UPLOAD_PATH}/download/${data.fileType as String}/${data.fileName as String}";
        File outPath = new File(outfilePath)
        if(!outPath.exists()){
            FileUtils.forceMkdir(outPath)
        }else {
            FileUtils.forceDelete(outPath)
            FileUtils.forceMkdir(outPath)
        }

        String[] url0 = spreadImg.split(",");
        String spreadImgData = url0[1];
        try {
            // Base64解码
            byte[] b = new BASE64Decoder().decodeBuffer(spreadImgData);
            File file = new File(outfilePath,"spreadImgData.png");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(b);
            if (fos != null) {
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        data.put("spreadImgData",spreadImgData)//一周传播力走势

        String[] url = effectImg.split(",");
        String effectImgData = url[1];
        try {
            // Base64解码
            byte[] b = new BASE64Decoder().decodeBuffer(effectImgData);
            File file = new File(outfilePath,"effectImgData.png");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(b);
            if (fos != null) {
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        data.put("effectImgData",effectImgData)//一周影响力走势

        String[] url2 = leaderImg.split(",");
        String leaderImgData = url2[1];
        try {
            // Base64解码
            byte[] b = new BASE64Decoder().decodeBuffer(leaderImgData);
            File file = new File(outfilePath,"leaderImgData.png");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(b);
            if (fos != null) {
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        data.put("leaderImgData",leaderImgData)//引导力值

        String[] url3 = trustImg.split(",");
        String trustImgData = url3[1];
        try {
            // Base64解码
            byte[] b = new BASE64Decoder().decodeBuffer(trustImgData);
            File file = new File(outfilePath,"trustImgData.png");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(b);
            if (fos != null) {
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        data.put("trustImgData",trustImgData)//公信力值

        String[] url4 = wordCldImg.split(",");
        String wordCldImgData = url4[1];
        try {
            // Base64解码
            byte[] b = new BASE64Decoder().decodeBuffer(wordCldImgData);
            File file = new File(outfilePath,"wordCldImgData.png");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(b);
            if (fos != null) {
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        data.put("wordCldImgData",wordCldImgData)//一周词云

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

    List getPsiTrend(long userid, List<String> domains,Set<Integer> types,Date startDate, Date endDate ) {
        def sdf = new SimpleDateFormat( "yyyy-MM-dd" )
        def start = sdf.format( startDate )
        def end = sdf.format( endDate )
        def list = makeState( start, end )
        def result = []
        def publishCount = evluationAnalysisRepo.getPublishCount( domains,types, start, end,0 )
        def reprintCount = evluationAnalysisRepo.getRepintCountByDay( domains,types,start,end,Integer.MIN_VALUE )
        def reprintMediaCount = evluationAnalysisRepo.getReprintMedia( domains,types,start,end )
        def propagationIndex = evluationAnalysisRepo.ComputePSI( domains,types, start, end )
        for( String s:list ){
            def timeLine = s.split("-")
            def time = timeLine[1]+"-"+timeLine[2]
            def row = [
                    // 发稿量
                    publishCount: getOrDefault(publishCount,s,0L ),
                    // 转载量
                    reprintCount: getOrDefault(reprintCount,s, 0L ),
                    // 转载媒体数
                    reprintMediaCount: getOrDefault(reprintMediaCount, s, 0L ),
                    // 传播力指数
                    psi: transferDouble(getOrDefault(propagationIndex, s, 0.0 )),
                    // 日期
                    time: time
            ]
            result << row
        }
        result
    }

    List getMII( long userid, List<String> domains, Set<Integer> types, Date startDate, Date endDate ){
        def sdf = new SimpleDateFormat("yyyy-MM-dd")
        def start = sdf.format( startDate )
        def end = sdf.format( endDate )
        def list = makeState(start, end)
        def getReadNumberByDay = evluationAnalysisRepo.getReadNumberByDay(domains,types, start, end)
        def getCommentNumberByDay = evluationAnalysisRepo.getCommentNumberByDay(domains,types, start, end)
        def getLikeNumberByDay = evluationAnalysisRepo.getLikeNumberByDay(domains, types,start, end)
        def influenceIndex = evluationAnalysisRepo.ComputeMII(domains,types, start, end)
        def weibo = evluationAnalysisRepo.getRepintCountByDay(domains,types,start,end,4)
        def result = []
        for( String s:list ){
            def timeLine = s.split("-")
            def time = timeLine[1]+"-"+timeLine[2]
            result <<  [
                    //mii
                    mii: transferDouble(getOrDefault( influenceIndex,s,0.0 )),
                    // 阅读数
                    readingCount: getOrDefault( getReadNumberByDay,s, 0L ),
                    // 评论数
                    commentsCount: getOrDefault( getCommentNumberByDay,s, 0L),
                    // 点赞数
                    likesCount: getOrDefault( getLikeNumberByDay,s, 0L),
                    reprintByWeibo:getOrDefault(weibo,s,0),
                    time:time
            ]
        }
        result
    }

    Map getBSI ( long userid, List domains, Set<Integer> type, Date startDate, Date endDate ){
        def sdf = new SimpleDateFormat("yyyy-MM-dd")
        def start = sdf.format( startDate )
        def end = sdf.format( endDate )
        def readNumberByDay = evluationAnalysisRepo.getReadNumberByDay(domains,type, start, end)
        def totalRead = 0L
        readNumberByDay.each { key,value ->
            totalRead += value
        }
        def commentNumberByDay = evluationAnalysisRepo.getCommentNumberByDay(domains,type, start, end)
        def totalComment = 0L
        commentNumberByDay.each { key,value ->
            totalComment += value
        }
        def likeNumberByDay = evluationAnalysisRepo.getLikeNumberByDay(domains,type, start, end)
        def totalLike = 0L
        likeNumberByDay.each { key,value ->
            totalLike += value
        }
        def computeGuidingForceIndex = evluationAnalysisRepo.ComputeBSI(domains,type, start, end)
        def ss =  [
                // mii
                gsi: transferDouble(computeGuidingForceIndex),
                // 阅读数
                readingCount: totalRead,
                // 评论数
                commentsCount: totalComment,
                // 点赞数
                likesCount: totalLike
        ]
        ss
    }

    List<String> getWordCloud(long userid, List<String> domain, Set<Integer> types, Date startDate, Date endDate){
        def sdf = new SimpleDateFormat("yyyy-MM-dd")
        def start = sdf.format( startDate )
        def end = sdf.format( endDate )
        def cloud = evluationAnalysisRepo.getWordCloud(domain,types, start, end)
        cloud
    }

    String getTSI(long userid, List<String> domains, Set<Integer> types, Date startDate, Date endDate){
        def sdf = new SimpleDateFormat("yyyy-MM-dd")
        def start = sdf.format( startDate )
        def end = sdf.format( endDate )
        def credibilityIndex = evluationAnalysisRepo.ComputeTSI(domains,types, start, end)
        transferDouble( credibilityIndex )
    }

    List getTrustStrength(long userid,List<String> domain, Set<Integer> types, Date startDate, Date endDate){
        def sdf = new SimpleDateFormat("yyyy-MM-dd")
        def start = sdf.format( startDate )
        def end = sdf.format( endDate )
        def publishCount = evluationAnalysisRepo.getPublishCount(domain,types, start, end,0)
        def publishSum = 0
        publishCount.each { key,value->
            publishSum+=value
        }
        def reprintCount = evluationAnalysisRepo.getRepintCountByDay(domain,types, start, end,Integer.MIN_VALUE)
        def reprintSum = 0
        reprintCount.each { key,value->
            reprintSum+=value
        }
        def importantEventCount = evluationAnalysisRepo.getImportantEvent( domain, types,start, end )
        def weibo = 0
        def commentNumber = evluationAnalysisRepo.getCommentNumberByDay( domain,types, start, end )
        def commentSum = 0
        commentNumber.each { key,value->
            commentSum+=value
        }
        def positive = 0
        def negative = 0
        def reprintMedia = evluationAnalysisRepo.getReprintMedia(domain,types, start, end)
        def reprintMediaSum = 0
        reprintMedia.each { key,value->
            reprintMediaSum +=value
        }
        def AstralMedia = evluationAnalysisRepo.getImportantMediaNumber(domain,types,start,end )
        def AstralMediaSum = AstralMedia.values().sum()
        List<Integer> result = [
                publishSum,reprintSum,importantEventCount,weibo,commentSum,positive,negative,reprintMediaSum,AstralMediaSum
        ]
        result
    }

    List getPropagationType(long userid,List<String> domains, Set<Integer> types,Date startDate, Date endDate ){
        def sdf = new SimpleDateFormat("yyyy-MM-dd")
        def start = sdf.format( startDate )
        def end = sdf.format( endDate )
        def result = []
        def app = evluationAnalysisRepo.getRepintCountByDay( domains,types,start,end,9 )
        def appSum = app!=null ? app.values().sum()  : 1
        result << [
                        type: 'APP',
                        count: appSum
        ]
        def weibo = evluationAnalysisRepo.getRepintCountByDay( domains,types,start,end,4 )
        def weiboSum = weibo!=null ? weibo.values().sum() : 1
        result << [
                type: '微博',
                count: weiboSum
        ]
        def blog = evluationAnalysisRepo.getRepintCountByDay( domains,types,start,end,2 )
        def blogSum = blog!=null ? blog.values().sum()  : 1
        result << [
                type: '论坛',
                count: blogSum
        ]
        def search = evluationAnalysisRepo.getRepintCountByDay( domains,types,start,end,99 )
        def searchSum = search!=null ? search.values().sum( ) : 1
        result << [
                type: '搜索引擎',
                count: searchSum
        ]
        def weixin = evluationAnalysisRepo.getRepintCountByDay( domains,types,start,end,6 )
        def weixinSum = weixin!=null ? weixin.values().sum( ) : 1
        result << [
                type: '微信',
                count: weixinSum
        ]

        def net = evluationAnalysisRepo.getRepintCountByDay( domains,types,start,end,1 )
        def netSum = net!=null ? net.values().sum()  : 1
        def net1 = evluationAnalysisRepo.getRepintCountByDay( domains,types,start,end,5 )
        netSum += net1!=null ? net1.values().sum()  : 1
        result << [
                type: '网站',
                count: netSum
        ]
        return result
    }

    Map getInfluenceIndex( long userid, List<String> domains, Set<Integer> types,Date startDate, Date endDate ){
        def result = []
        def upLimit = []
        def downLimit = []
        def indexes = []
        def dates = []
        def sdf = new SimpleDateFormat( "yyyy-MM-dd" )
        def start = sdf.format( startDate )
        def end = sdf.format( endDate )
        def influenceIndex = evluationAnalysisRepo.ComputeMII(domains,types, start, end)
        influenceIndex.keySet().each { date ->
            dates <<  date.substring(6,date.length())
        }
        influenceIndex.values().each { index ->
            indexes << ( index as double )
        }
        influenceIndex.keySet().each {
            upLimit << (influenceIndex.values().max() as double)
            downLimit << (influenceIndex.values().min() as double )  *0.875
        }
        return [
                upLimit: upLimit,
                index:indexes,
                downLimit: downLimit,
                date: dates
        ]
    }

    String transferDouble( double param ){
        DecimalFormat df = new DecimalFormat ( "###,##0.00")
        return df.format( param )
    }
}
