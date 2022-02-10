import com.mashape.unirest.http.Unirest

/**
 * Author : YCSnail
 * Date   : 2017-09-12
 * Email  : liyancai1986@163.com
 */


String url = "https://api.om.qq.com/transaction/infoauth"
String accessToken = "RYTGH9VMNWG4PHT4REPDVQ"
String openid = "645adf86b8bb52987374994a9f03cf1d"
String transactionId = "7265967816689853428"

def res = Unirest.get(url)
        .queryString('access_token', accessToken)
        .queryString('openid', openid)
        .queryString('transaction_id', transactionId)
        .asJson()

def result = res.body.object

println result


//7265967597438793753
//        {"msg":"SUCCESS","code":0,"data":{
// "transaction_id":"7265967597438793753","transaction_status":"失败","transaction_ctime":"2017-09-11 18:41:33","transaction_type":"文章","article_info":{"article_pub_flag":"","article_title":"","article_type":"","article_video_info":{"vid":"","title":"","type":"","desc":""},"article_abstract":"","article_pub_time":"","article_pid":"","article_url":"","article_imgurl":""}}}

//7265967598365117525
//{"msg":"SUCCESS","code":0,"data":{"transaction_id":"7265967598365117525","transaction_status":"失败","transaction_ctime":"2017-09-11 18:45:19","transaction_type":"文章","article_info":{"article_pub_flag":"","article_title":"","article_type":"","article_video_info":{"vid":"","title":"","type":"","desc":""},"article_abstract":"","article_pub_time":"","article_pid":"","article_url":"","article_imgurl":""}}}

//7265967816689853428
//        {"msg":"SUCCESS","code":0,"data":{"transaction_id":"7265967816689853428","transaction_status":"失败","transaction_ctime":"2017-09-12 09:33:41","transaction_type":"文章","article_info":{"article_pub_flag":"","article_title":"","article_type":"","article_video_info":{"vid":"","title":"","type":"","desc":""},"article_abstract":"","article_pub_time":"","article_pid":"","article_url":"","article_imgurl":""}}}