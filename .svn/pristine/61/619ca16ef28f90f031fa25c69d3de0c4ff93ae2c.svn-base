import com.istar.mediabroken.api3rd.CaptureApi3rd
import com.istar.mediabroken.api3rd.UserApi3rd

def userApi = new UserApi3rd(domain: 'http://m3.istarshine.com/')
def captureApi = new CaptureApi3rd(domain: 'http://m3.istarshine.com/')
//def rep = userApi.login("编加加", "123456")
def rep = userApi.login("王雪峰", "123456")
def sid = rep.session
println  rep.userId

//def sid = '71ac5472c2234309ac3bb79fcc7c1d7b'
//def rep =[userId:47227]

def subjects = captureApi.getSubjectList(sid, rep.userId)
println subjects

//KK_ISHIGH：是否是高级设置,默认0 否 1是
//KK_LABEL：高级设置标签



//subjects.list.each {
//
//    if ((it.subjectName as String).startsWith('st')) {
////        println it.subjectId
////        println it.subjectName
//        captureApi.removeSubject(sid, rep.userId, it.subjectId)
//    }
//
//}
//
//def newsList = captureApi.getNewsList2(sid, rep.userId, 'efeac4d18fa2495884070125a4c9cc65')
////efeac4d18fa2495884070125a4c9cc65&KR_INFOTYPE=01&pageSize=5&pageNo=1
//println newsList
//
//content

//println captureApi.getNews(sid, rep.userId, '172526f283704947a7b719b0cb9f4acf')
//println captureApi.getRelatedNews(sid, rep.userId, 'e91274ce0e4c461cadbc014738df6c96')
//println captureApi.getNews(sid, rep.userId, 'c8662f0349d14391a3f3fa1a36236446')


//println captureApi.getHotNewsToday(sid, rep.userId, 'e6fadea367de4aef95666764cab881ac')
//println captureApi.getNewNewsCount(sid, rep.userId, '48c0fb4e26734398afecf6b48a749da3', new Date(new Date().getTime() - 3 * 60 * 60 * 1000))

//println captureApi.addWebsiteSubject(sid, rep.userId, "www.baidu.com", "")

//def newsList = captureApi.queryNewsList(sid as String, rep.userId, '观众,尖叫', '524838d4e2fc4de489f52253eb89ee5b', 1, 10)
//efeac4d18fa2495884070125a4c9cc65&KR_INFOTYPE=01&pageSize=5&pageNo=1
//println newsList

//def list = captureApi.getSubjectRank(sid as String, rep.userId)
//println list