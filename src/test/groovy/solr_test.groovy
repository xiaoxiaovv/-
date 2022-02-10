import com.alibaba.fastjson.JSON
import com.istar.mediabroken.utils.HttpHelper

def rep =  HttpHelper.doGet('http://192.168.20.100:8082/solrMessage/solrMsgServlet', [
        q: "kvSourcetype:6 AND kvSource:\"北京日报*\" " as String,
        start: 1,
        rows: 10,
        fl: 'kvDkTime,kvTitle,kvSource,kvAuthor,kvUrl,kvUuid,kvSite,kvCtime,kvSourcetype,score,kvSourcetype,kvOrientation',
        sort: 'kvDkTime desc'
])

println JSON.toJSONString(rep)