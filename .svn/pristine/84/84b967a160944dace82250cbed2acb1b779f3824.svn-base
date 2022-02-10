package com.istar.mediabroken.service.ecloud

import com.mashape.unirest.http.Unirest
import groovy.util.logging.Slf4j
import org.apache.http.entity.ContentType
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * Author : YCSnail
 * Date   : 2018-04-19
 * Email  : liyancai1986@163.com
 */
@Service
@Slf4j
class EcloudOrgService {

    @Value('${ecloud.gateway}')
    String apiHost

    /**
     * 获取企业组织结构列表接口(SaaS2002)
     * @param custId    企业id
     * @return
     */
    def getEcloudOrgList(int custId){

        String accessToken = "xxxxxxxx"

        String url = apiHost + "/services/department/list?access_token=${accessToken}"
        def params = [
                custid          : custId
        ]

        def res = Unirest.get(url).headers([
                "Content-type": ContentType.APPLICATION_JSON.toString()
        ]).queryString(params as Map<String, Object>).asJson()

        def result = res.body.object

        println result

        return result.response
    }

    /**
     * 获取企业员工列表接口(SaaS2003)
     * @param custId    企业id
     * @return
     */
    def getEcloudOrgUserList(int custId){

        String accessToken = "xxxxxxxx"

        String url = apiHost + "/services/user/list?access_token=${accessToken}"
        def params = [
                custid          : custId,
                expandkeys      : 'headphoto'
        ]

        def res = Unirest.get(url).queryString(params as Map<String, Object>).asJson()

        def result = res.body.object

        println result

        return result.response
    }

}
