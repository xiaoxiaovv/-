package com.istar.mediabroken.task

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.istar.mediabroken.entity.News
import com.istar.mediabroken.service.CaptureService
import com.istar.mediabroken.utils.DateUitl
import com.istar.mediabroken.utils.HttpHelper
import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.text.SimpleDateFormat

/**
 * Created by MCYarn on 2017/6/1.
 */

@Component
@Slf4j
public class NewsFeach3Task implements Task {
    @Autowired
    CaptureService captureSrv
    String basePath = '/var/mediabroken/datapower/news/'
//    String basePath = 'F:\\Users\\steven\\'
    def need = new File(basePath ,'weiboUrl.txt')
    def done = new File(basePath ,'weibodone.txt')
    def clr = new File(basePath,'clr.json')
    def sdf = new SimpleDateFormat("yyyyMMdd")
    def today = new Date()
    List tokens = [
            "2.00sf1uTGvOmtmB43188924080kQmzS",
            "2.00sf1uTGAwGViB81a57cd4a0GZjcFB",
            "2.00sf1uTG05Rzkt587d8faf1ca8wjSE",
            "2.00sf1uTG0yoMxSfdf8b1ebe2Y9CMBC",
            "2.00sf1uTGnMuuaBacf9743649z4AKgE",
            "2.00sf1uTG0aC8eg7d9ba794d9SDOYBC",
            "2.00sf1uTGMiRAMD30b0ee3e4315VcgC",
            "2.00sf1uTG0Q2qNBb788f223fcccIISD",
            "2.00sf1uTGtREb6Bd2088be0ebFC39OB",
            "2.00sf1uTG09cuZn1cc0d727bfdOKD7B"]
    @Override
    void execute() {
        def index = 0
        def urls = getUrl()
        boolean flag = true
        while(  urls.size() > 0 && flag ){
            def len = urls.size()
            for (int i = 0; i <= urls.size() / 100; i++) {
                def list = []
                if (len > 100) {
                    list = urls.subList(i * 100, (i + 1) * 100)
                    len -= 100
                }else{
                    list = urls.subList(i * 100, urls.size())
                }
                flag = fetchWeiboStat( list,index )
            }
        }
    }

    List getUrl() {
        def result = []
        def begin = sdf.format(DateUitl.addDay(new Date(),-15))
        def end = sdf.format(new Date())
        try{
            if(!need.exists())
                System.exit(-1)
            def lines = FileUtils.readLines(need, 'utf-8')
            lines.each { line ->
                def str = line.split("\t")
                if( str[0] >= begin && str[0] < end )
                    result << line
            }
            return result
        }catch ( Exception e ){
            log.debug( e.getMessage() )
        }
        return result
    }

    boolean fetchWeiboStat( List urlList, int index ) {
        def resultList = []
        boolean flag = true
        Map<String, String> mid_url = new HashMap<String, String>()
        Map<String, String> id_mid = new HashMap<String, String>()
        for (String line : urlList) {
            def urls = line.split("\t")[1]
            def mids = urls.split("\\/")
            def mid = mids[mids.size() - 1]
            def id = mid2Id(mid)
            id_mid.put(id, mid)
            mid_url.put(mid, urls)
        }
        while (flag) {
            def result
            try {
                result = HttpHelper.doGetArray("https://api.weibo.com/2/statuses/count.json", [
                        access_token: tokens[index],
                        ids         : id_mid.keySet().join(",")
                ])
                for (int i = 0; i < result.size(); i++) {
                    def id = result[i].id
                    def getMid = id_mid.get(id as String)
                    def getUrl = mid_url.get(getMid as String)
                    resultList << JSONObject.toJSON([
                            url         : getUrl,
                            commentCount: result[i].comments,
                            reprintCount: result[i].reposts,
                            likesCount  : result[i].attitudes,
                            time:sdf.format(today)
                    ])
                }
                FileUtils.writeLines(clr, resultList, true)
                index++
                flag = false
                break
            } catch (Throwable e) {
                index++
                if( index >= 10 ){
                    break
                }
            }
        }
        return flag
    }

    String mid2Id(String mid) {
        def id = ""
        for (int i = mid.length() - 4; i > -4; i = i - 4) {
            def offset1 = i < 0 ? 0 : i
            def len = i < 0 ? mid.length() % 4 : 4
            def str = mid.substring(offset1, offset1 + len)
            str = str62toInt(str)
            if (offset1 > 0) {
                while (str.length() < 7) {
                    str = "0" + str
                }
            }
            id = str + id
        }
        return id
    }

    String str62toInt(String str62) {
        def i64 = 0
        for (int i = 0; i < str62.length(); i++) {
            def Vi = (long) Math.pow(62, (str62.length() - i - 1))
            def t = str62[i]
            i64 += Vi * getInt10(t.toString())
        }
        return Long.toString(i64)
    }

    int getInt10(String key) {
        def str62keys = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVTXYZ"
        return str62keys.indexOf(key)
    }


    String writeWeiboUrl(String siteName){
        def filterTime = DateUitl.getBeginDayOfYesterday()
        def sdf = new SimpleDateFormat("yyyyMMdd")
        def baseFile = new File( basePath,siteName )
        def list = []
        for( File file: baseFile.listFiles()){
            if( file.isDirectory() ){
                for( File sub : file.listFiles( )){
                    if( sub.getName().contains( sdf.format( filterTime ))){
                        def lines = FileUtils.readLines(sub)
                        lines.each { line ->
                            def jSON = JSONObject.parse(line)
                            def source = jSON.source
                            if( source != null ){
                                if( jSON.newsType == 4 && (source.contains("劳动午报") || source.contains("京工网") ||
                                        source.contains("中工网"))){
                                    list.add(jSON.url)
                                }
                            }
                        }
                    }
                }
            }
        }
        def save = new File(baseFile,'urls.txt')
        FileUtils.writeLines( save,list,true)
        return save.getParent()
    }

}
