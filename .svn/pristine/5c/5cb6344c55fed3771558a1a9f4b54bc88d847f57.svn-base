package com.istar.mediabroken.api3rd

import com.istar.mediabroken.utils.HttpHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value

abstract class BaseApi3rd {

    @Value('${yqms.api.url}')
    String domain // =  'http://m3.istarshine.com/'

    @Value('${solr.url}')
    String solrUrl // =  'http://m3.istarshine.com/'

    protected Map doGet(String url, Map params) {
        return HttpHelper.doGet(domain + url, params)
    }

    protected Map doGetSolr(Map params) {
        return HttpHelper.doGet(solrUrl, params)
    }

    protected Map doGet(String url, YqmsSession session, Map params) {
        params.session = session.sid
        params.userid = session.userId
        return HttpHelper.doGet(domain + url, params)
    }

    protected List doGetArray(String url, YqmsSession session, Map params) {
        params.session = session.sid
        params.userid = session.userId
        return HttpHelper.doGetArray(domain + url, params)
    }
}
