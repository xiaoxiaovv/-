import com.alibaba.fastjson.JSONObject
import com.mashape.unirest.http.Unirest
import com.mashape.unirest.http.exceptions.UnirestException
import org.apache.http.client.HttpClient
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.conn.ssl.TrustSelfSignedStrategy
import org.apache.http.entity.ContentType
import org.apache.http.impl.client.HttpClients
import org.apache.http.ssl.SSLContextBuilder
import org.junit.Test

import javax.net.ssl.SSLContext
import java.security.KeyManagementException
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import java.security.cert.X509Certificate

/**
 * Author : YCSnail
 * Date   : 2018-04-16
 * Email  : liyancai1986@163.com
 */

class NewtranxDemo {

    public static final String api_key = "abb17ab7335bd41313f878e3946d2a18"


    private static HttpClient unsafeHttpClient;

    static {
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustSelfSignedStrategy() {
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }).build();

            unsafeHttpClient = HttpClients.custom()
                    .setSSLContext(sslContext)
                    .setSSLHostnameVerifier(new NoopHostnameVerifier())
                    .build();

            Unirest.setHttpClient(unsafeHttpClient);
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
            e.printStackTrace();
        }
    }

//    public static HttpClient getClient() {
//        return unsafeHttpClient;
//    }


    //智能翻译接口MT调用
    public static def newTranxTranslate(String cont, String sourceLang, String targetLang) {
        String url = "https://api.open.newtranx.com/mt-api/v1/common/${sourceLang + '-' + targetLang}/translate"

        url = "https://open.newtranx.com/mt-demo/common/${sourceLang + '-' + targetLang}/translate"
        def param = [
                "srcl"      : sourceLang,
                "tgtl"      : targetLang,
                "text"      : cont,
                "detoken"   : true,
                "align"     : true,
                "nbest"     : 1
        ]
        try {
            def res = Unirest.post(url)
                    .headers([
                        "apikey"      : api_key,
                        "content-type": ContentType.APPLICATION_JSON.toString()
                    ])
                    .body(JSONObject.toJSONString(param))
                    .asJson()
            def result = res.body.object

//            System.out.println(result)

            return result
        } catch (UnirestException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static def dealResult(def res) {

        def translatedList = res.translation[0].translated
        String res_text = ""
        translatedList.each {
            res_text += it.text
        }
        return res_text
    }

    //语言检测接口调用
    @Test
    public void test1(){

        String url = "https://api.open.newtranx.com/lang-api/v1/lang"
        def param = ["text": "你好 世界"]

        try {
//            HttpClient creepyClient = NewtranxDemo.getClient();
//            Unirest.setHttpClient(creepyClient);

            def res = Unirest.post(url)
                    .headers([
                        "apikey"      : api_key,
                        "content-type": "application/json"
                    ])
                    .body(JSONObject.toJSONString(param))
                    .asJson()
            def result = res.body.object

            System.out.println(result)

        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void test2(){

        String lang = "zh-en"

        String url = "https://api.open.newtranx.com/mt-api/v1/common/zh-en/translate"
        def param = [
                "srcl"      : "nzh",
                "tgtl"      : "nen",
                "text"      : "你好 世界",
                "detoken"   : true,
                "align"     : true,
                "nbest"     : 1
        ]

        try {
//            HttpClient creepyClient = NewtranxDemo.getClient();
//            Unirest.setHttpClient(creepyClient);

            def res = Unirest.post(url)
                    .headers([
                        "apikey"      : api_key,
                        "content-type": ContentType.APPLICATION_JSON
                    ])
                    .body(JSONObject.toJSONString(param))
                    .asJson()
            def result = res.body.object

            System.out.println(result)

        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test3() {

        def list = [
//                "你好世界",
//                "北京智慧星光传媒事业部",
//                "青青子衿悠悠我心",
//                "前往世界的尽头",
//                "瓜帅：如果要踢到最后一场球才能决定联赛冠军，那压力就很大",
//                "不管进攻还是防守，申花队都是一个整体，都要在战术体系中发挥作用。",
//                "“除去最初的岁月，我和国际米兰已经分开的太久了。世事变迁，沧海桑田，但是我对球队的热爱从未改变。”",
        ]

        list.each {
            def startTime = System.currentTimeMillis()
            def res = NewtranxDemo.newTranxTranslate(it, 'zh', 'en')
            def endTime = System.currentTimeMillis()

            println it
            println dealResult(res)
            println "-" * 20 + (endTime - startTime) + 'ms'
        }

        def list2 = [
                """The millions of Muslims who are minorities in the countries where they live must do more to integrate amid fears they could fall prey to extremist recruiters.

Muslim leaders and government officials will meet in Abu Dhabi next month to discuss ways to prevent radicalisation among an estimated 500 million people.

The conference, The International Muslim Minorities Congress, is being convened by the Muslim Council of Elders and will include representatives from 140 countries.

Dr Ali Al Nuaimi, chairman of the conference said the conference would be a platform to tackle issues of marginalisation and disenfranchisement in non-Muslim countries.

“Muslims in non-Muslim countries are facing many challenges, whether in terms of services or education … but the biggest challenge is for them to fit in with their societies."

He said not integrating into society was no longer an option, but a necessity.

“If a person wants to live in Germany, they have to live by German laws and as a German citizen given their rights and fulfilling their obligations.

“In the UAE we feel a responsibility to help Muslims blend in with their societies; Islam was hijacked from and presented to the world in a distorted image, and some countries have abused that to serve their political agendas. And there has been many victims as a result.

“So we feel a responsibility to provide them will full citizenship opportunities,” he said.
"""
,
                """Models of future projects are shown to investors and visitors at Cityscape on Tuesday. Victor Besa / The National
At Cityscape Abu Dhabi 2018, everyone is your friend. The men in too tight suits and hair styled as slickly as their sales patter. The young women with painful shoes and an overabundance of foundation. There are a lot of them and they all want to catch your eye.

“Hi there sir. Can I interest you in tax-free land in the United Kingdom? And might I say you are looking particularly dapper today?”

There is something for everyone at Cityscape, especially if you are looking for a floating Venice-themed hotel with underwater rooms.

Kleindienst is promising this latest wonder of Dubai by 2022, along with another development whose design is based on upside down Viking longships, and a Swiss-themed resort that will boast real snow even in the heat of a Gulf summer.

“German technology” explains one of the sales team.

_______________

Read more:

Cityscape Abu Dhabi to showcase projects including affordable housing

Emaar and Nakheel to skip Cityscape 2009

_______________

The Klendienst projects are all based on The World Islands, the famous, some might say infamous, off-shore development that was one of the casualities of the property crash of late 2008, part of the global economic turndown.

That was the era of the rotating skyscraper and the residential tower designed like an iPod. Ten years later, the idea of snow that doesn’t melt at 48C might provoke flashbacks in more nervous investors.

But no, says the salesman: “These are crazy ideas. But they are going to happen.”

The first underwater villa is already ready to show to prospective buyers. Venice will float in 2022.

“Your trusted realtor” is the tag line of one company, as if there could be any other. Cityscape 2018 is quieter, more measured event than the craziness of 2008, when investors queued for hours to slap down their hard earned savings for a deposit on an off plan property with an ambiguous completion date but a promise of huge returns.

Back then, stories were told of those at the front of the queue heading straight to those at the back, selling on, with a solid mark-up, an investment they had owned for barely a few minutes. For many, it did not end well.

Reforms in the property market have made the UAE as safer place for investors and Cityscape 2018 is a calmer event that fills only part of the Abu Dhabi National Exhibition Centre. Or "bijou” as the estate agents like to call it. The longest queue is for Costa Coffee.

Still, the big players are all here. Aldar is launching the next phase of Alghadeer, on the border of Abu Dhabi and Dubai. Over the next 15 years it will add over 14,000 homes to the 2,000 already built, with an emphasis of affordability.

The model of the project is set under dozens of suspended inverted glass eggs that rise and fall to music, like a benevolent Alien space invasion.

Models, of course, are one of the things that make any Cityscape, sparkling with tiny lights that reflect off the plastic lakes and lagoons, a perfect vision of the future.

They see potential in unexpected places, say a rainwater collection lake in Bani Yas with its own beach.

The grandest model of them all hides behind a black curtain, admission only with a VIP ticket that is given to anyone who asks. This is starchitect Thomas Hetherwick’s vision of a boutique hotel for downtown Abu Dhabi.

Even scaled down, it is a vast structure composed of blocks on stills, the putative guests rendered in plastic as tiny black silhouettes. Ras Al Akhdar is the vision of Abu Dhabi based international developers IMKAN, whose philosophy is: “We’re here to create soulful places that enrich people’s lives.”

The young man designated to show guests the project seems less certain. Location? Opening date? Number of rooms. “I can’t really tell you anything about it.”

The IMKAN stand is also showcasing a design for revolutionary mosque whose prayer hall is made entirely of glass and sees: “the prayer’s row as the generative unit of the mosque” according to the promotional video.

“It’s the design of an Egyptian architect in his 90s for Cairo”, offers a saleswoman. “I think.”

Cityscape Abu Dhabi 2018 ranges from high end villas to modest studio apartments in the UAE and beyond. A greenfield site on the outskirts of Liverpool not to your taste? How about a villa on Spain’s Costa Del Sol?

“Hi there, sir. Might I say you are looking particularly dapper today?\"
"""



        ]

        list2.each {
            def startTime = System.currentTimeMillis()
            def res = NewtranxDemo.newTranxTranslate(it, 'en', 'zh')
            def endTime = System.currentTimeMillis()

            println it
            println dealResult(res)
            println "-" * 20 + (endTime - startTime) + 'ms'
        }



    }



}
