import com.alibaba.fastjson.JSON
import com.istar.mediabroken.api3rd.TopicApi3rd
import com.istar.mediabroken.api3rd.UserApi3rd
import com.istar.mediabroken.api3rd.YqmsSession
import com.istar.mediabroken.entity.ICompileSummary

import static com.istar.mediabroken.utils.DateUitl.addDay

def userApi = new UserApi3rd(domain: 'http://m3.istarshine.com/')
def topicApi = new TopicApi3rd(domain: 'http://m3.istarshine.com/')
def rep = userApi.login("编加加2", "123456")
println rep
def session = new YqmsSession(sid: rep.session, userId: rep.userId)
//def summary = new ICompileSummary(
//        yqmsTopicId: 'c9161b0f0071468e91974828bb7a0df7',
//        startTime: addDay(new Date(), -10),
//        endTime: addDay(new Date(), 10),
//        session: session
//)

//println topicApi.addTopic(session, '话题测试11111', '地铁 骂人 手机', addDay(new Date(), -7), addDay(new Date(), 7))
//dcc18270a06241338f99a62985d3cab3
//topicApi.modifyTopic(session, '35fbf4b3051f4bc08dc2133909867dc9', '话题测试1', '地铁 骂人 手机', addDay(new Date(), -7), addDay(new Date(), 7))
//println topicApi.getTopicList(session)
//topicApi.removeTopic(session, '473acf5058fd43be8b4593aaff5576c8')

//d63b998c1720414ea1803a44c1b4a189

//print topicApi.getNewsList(summary, 1, 10)
//print topicApi.getHotTopic(summary)
//print topicApi.getWeiboList(session, 'd63b998c1720414ea1803a44c1b4a189', addDay(new Date(), -10), addDay(new Date(), 10))
//print topicApi.getTopN(summary)
//print topicApi.getEventEvolution(summary)
//print topicApi.getSprendTrend(summary)
//print topicApi.getWeibo(summary)

void print (def obj) {
    println JSON.toJSONString(obj)
}