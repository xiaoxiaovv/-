package com.istar.mediabroken.utils

import com.mongodb.util.JSON

/**
 * Created by steven on 17/5/31.
 */
class FetchWeiboStat {
    /*
     url每次最少给100个url

     [
        [
            url: 'http://weibo.com/1842366171/Eynyts0dy',
            commentCount: 100,  // comments
            reprintCount: 200,  // reposts
            likesCount: 200     // attiudes
        ], ...
     ]

     """
2.00sf1uTG0aC8eg7d9ba794d9SDOYBC
2.00sf1uTGMiRAMD30b0ee3e4315VcgC
2.00sf1uTG0Q2qNBb788f223fcccIISD
2.00sf1uTGtREb6Bd2088be0ebFC39OB
2.00sf1uTG09cuZn1cc0d727bfdOKD7B
2.00sf1uTGvOmtmB43188924080kQmzS
2.00sf1uTGAwGViB81a57cd4a0GZjcFB
2.00sf1uTG05Rzkt587d8faf1ca8wjSE
2.00sf1uTG0yoMxSfdf8b1ebe2Y9CMBC
2.00sf1uTGnMuuaBacf9743649z4AKgE

"""
      */


    public static List fetchWeiboStat(List url) {
        def resultList = []
        def idList = []
        def access_tokenList = ["2.00sf1uTG0aC8eg7d9ba794d9SDOYBC",
                                "2.00sf1uTGMiRAMD30b0ee3e4315VcgC"/*,
                                "2.00sf1uTG0Q2qNBb788f223fcccIISD",
                                "2.00sf1uTGtREb6Bd2088be0ebFC39OB",
                                "2.00sf1uTG09cuZn1cc0d727bfdOKD7B",
                                "2.00sf1uTGvOmtmB43188924080kQmzS",
                                "2.00sf1uTGAwGViB81a57cd4a0GZjcFB",
                                "2.00sf1uTG05Rzkt587d8faf1ca8wjSE",
                                "2.00sf1uTG0yoMxSfdf8b1ebe2Y9CMBC",
                                "2.00sf1uTGnMuuaBacf9743649z4AKgE"*/]

        //每一个URL都要转换得到一个对应的id
        url.each {
            def ids = getIdByWeiboUrl(it)
            idList << ids
        }
        //println idList


                def result = HttpHelper.doGetArray("https://api.weibo.com/2/statuses/count.json",[
                        access_token : "2.00sf1uTG0aC8eg7d9ba794d9SDOYBC",
                        ids : idList
                ])
        for(int i = 0; i < idList.size(); i++){
                resultList << [
                        url : url,
                        commentCount : result.comments,
                        reprintCount : result.reposts,
                        likesCount : result.attitudes
                ]
            }

        return resultList
    }

    static getIdByWeiboUrl(def url){
        def mids = url.split("\\/")
        def mid = mids[mids.size()-1]
       // println "mid="+mid
        def id = mid2Id(mid)
      //  println "id="+id
    }

    static String mid2Id(String mid){
        def id = ""
        for (int i = mid.length() - 4; i > -4; i = i - 4) {
            def offset1 = i < 0 ? 0 : i
            def len = i < 0 ? mid.length() % 4 : 4
            def str = mid.substring(offset1,offset1 + len)
            //println offset1+" "+len+" "+str
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

    private static String str62toInt(String str62){
        def i64 = 0
        for (int i = 0; i < str62.length(); i++) {
            def Vi = (long)Math.pow(62, (str62.length() - i - 1))
            def t = str62[i]
            i64 += Vi * getInt10(t.toString())
        }
        //println i64
        return Long.toString(i64)
    }

    private static int getInt10(String key) {
        def str62keys = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVTXYZ"
        return str62keys.indexOf(key)
    }

}
