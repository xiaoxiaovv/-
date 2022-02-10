import com.istar.mediabroken.utils.HttpHelper
import org.apache.commons.io.FileUtils

class GetWeiBo{

     private static List tokens = [
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

    static int  tokenLen = tokens.size()
    static void Main(){
        def urls = getUrl('C:\\Users\\ZHXG\\Desktop\\')
        def len = urls.size()
        def index = 0
        for (int i = 0; i <= urls.size() / 100; i++) {
            def list = []
            if (len > 100) {
                list = urls.subList(i * 100, (i + 1) * 100)
                len -= 100
            } else {
                list = urls.subList(i * 100, urls.size())
            }
            fetchWeiboStat(list,index)
        }
    }
    static  List getUrl(String basePath) {
        def readLines = FileUtils.readLines(new File(basePath + "weiboURL.txt"), 'utf-8')
        if ( new File(basePath + "weiboURLTmp.txt").exists() ){
            def weiboURLTmp = FileUtils.readLines(new File(basePath + "weiboURLTmp.txt"), 'utf-8')
            def tmpList = []
            //readLines.removeAll(weiboURLTmp)
            for (int index = weiboURLTmp.size(); index < readLines.size(); index++){
                tmpList.add(readLines.get(index))
            }
            return tmpList
        }else {
            return readLines
        }
    }

    static List fetchWeiboStat(List urlList, int index) {
        def resultList = []

        Map<String, String> mid_url = new HashMap<String, String>()
        Map<String, String> id_mid = new HashMap<String, String>()
        for (String urls : urlList) {
            def mids = urls.split("\\/")
            def mid = mids[mids.size() - 1]
            def id = mid2Id(mid)
            id_mid.put(id, mid)
            mid_url.put(mid, urls)
        }
        while (true) {
            def result
            try {
                result = HttpHelper.doGetArray("https://api.weibo.com/2/statuses/count.json",
                        [access_token: tokens[index], ids:id_mid.keySet().join(",")])

                for (int i = 0; i < result.size(); i++) {
                    def id = result[i].id
                    def getMid = id_mid.get(id as String)
                    def getUrl = mid_url.get(getMid as String)
                    resultList << [
                            url         : getUrl,
                            commentCount: result[i].comments,
                            reprintCount: result[i].reposts,
                            likesCount  : result[i].attitudes
                    ]
                }
                FileUtils.writeLines(new File('C:\\Users\\ZHXG\\Desktop\\weiboURLTmp.txt'), urlList, true)
                FileUtils.writeLines(new File('C:\\Users\\ZHXG\\Desktop\\weibo\\weibo.txt'), resultList, true)
                println resultList
                break
            } catch (Throwable e) {
                index++
                if( index == tokenLen ){
                    System.exit(-1)
                }
            }finally{
                if (index >= 2){  //数字为tokenList.size()
                    break
                }
            }

        }
    }

    private static String mid2Id(String mid) {
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
    private static String str62toInt(String str62) {
        def i64 = 0
        for (int i = 0; i < str62.length(); i++) {
            def Vi = (long) Math.pow(62, (str62.length() - i - 1))
            def t = str62[i]
            i64 += Vi * getInt10(t.toString())
        }
        return Long.toString(i64)
    }

    private static int getInt10(String key) {
        def str62keys = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVTXYZ"
        return str62keys.indexOf(key)
    }


    public static void main(String[] args) {
        Main()
    }

}


