package com.istar.mediabroken.service.ecloud

import com.alibaba.fastjson.JSONObject
import com.istar.mediabroken.repo.EcloudAccessTokenRepo
import com.mashape.unirest.http.Unirest
import groovy.util.logging.Slf4j
import org.apache.http.client.HttpClient
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.conn.ssl.TrustSelfSignedStrategy
import org.apache.http.entity.ContentType
import org.apache.http.impl.client.HttpClients
import org.apache.http.ssl.SSLContextBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

import javax.net.ssl.SSLContext
import java.security.KeyManagementException
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import java.security.cert.X509Certificate

/**
 * Author : YCSnail
 * Date   : 2018-04-18
 * Email  : liyancai1986@163.com
 */
@Service
@Slf4j
class EcloudOAuthService {

    @Value('${ecloud.gateway}')
    String apiHost
    @Value('${ecloud.client_id}')
    String clientId
    @Value('${ecloud.redirect_uri}')
    String redirectUri


    @Autowired
    private EcloudAccessTokenRepo accessTokenRepo


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


    /**
     * OAuth2单点鉴权接口(SaaS1001)
     * @param code
     * @return
     */
    def getOAuthToken(String code){

        //从缓存取token，如果没有则获取，如果过期则刷新

        def accessTokenObj = accessTokenRepo.get(clientId)

        if(accessTokenObj && accessTokenIsValid(accessTokenObj)) {
            log.info(['ecloud', '库中获取accessToken', accessTokenObj.access_token].join(':::') as String)
            return accessTokenObj
        } else  {
            String url = apiHost + "/services/oauth2/authorization"
            url += "?grant_type=authorization_code&client_id=${clientId}&scope=&redirect_uri=&code=${code}"

            log.info(['ecloud', '请求tokenSaaS1001-url', url].join(':::') as String)

            def res = Unirest.post(url).headers([
                    "Content-type": ContentType.APPLICATION_JSON.toString()
            ])

            log.info(['ecloud', '请求tokenSaaS1001-res', JSONObject.toJSONString(res)].join(':::') as String)

            res = res.asJson()

            log.info(['ecloud', '请求tokenSaaS1001-resJson', res].join(':::') as String)

            def result = res.body.object
            log.info(['ecloud', '请求tokenSaaS1001', result].join(':::') as String)

            //授权接口返回错误
            if(!result.isNull('code')) {
                return [
                        access_token    : null,
                        uid             : 0,
                        code            : result.getInt('code'),
                        msg             : result.getString('msg')
                ]
            }

            //更新库
            accessTokenRepo.update(
                    clientId,
                    result.access_token as String,
                    result.expires as long,
                    result.refresh_token as String,
                    result.uid as int,
                    result.username as String
            )

//            {
//                access_token    : '',
//                expires         : 111,
//                refresh_token   : '',
//                uid             : 12334,
//                username        : ''
//            }

            return result
        }

    }

    boolean accessTokenIsValid (def accessTokenObj) {
        if(!accessTokenObj){
            return false
        }
        return (System.currentTimeMillis() - accessTokenObj.expires as long) < 0
    }

}
