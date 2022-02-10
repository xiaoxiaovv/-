package com.istar.mediabroken.service.ecloud

import com.istar.mediabroken.entity.account.Account
import com.istar.mediabroken.utils.Md5Util
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * Author : YCSnail
 * Date   : 2018-04-19
 * Email  : liyancai1986@163.com
 */
@Service
@Slf4j
class EcloudService {

    @Value('${ecloud.client_id}')
    String appId
    @Value('${ecloud.client_key}')
    String appSecret
    @Value('${elcoud.product.type.code}')
    String productTypeCodes

    private static final String client_key = "72ff7f4e-9b2e-4b33-8232-07cb0d08bbf3"     //消费方私钥


    String getSign(long timestamp) {
        String str = "client_id=${appId}client_key=${appSecret}timestamp=${timestamp}"
        String sign = Md5Util.md5(str).toUpperCase()
        return sign;
    }

    /**
     * 验证sign值是否正确
     * @param timestamp
     * @param sign
     * @return
     */
    boolean verifySign(long timestamp, String sign) {

        log.info(['ecloud', 'verifySign', timestamp, sign].join(':::') as String)

        if(!sign) return false

        String signStr = this.getSign(timestamp)

        log.info(['ecloud', 'verifySign', signStr, sign.equals(signStr)].join(':::') as String)

        return sign.equals(signStr)
    }

    String convertProductType4EcloudOrg(def ecloudOrgInfo) {

        String[] productTypeArray = ['Std.Version', 'Sen.Version', 'Pro.Version']

        String productTypeCode = ecloudOrgInfo.services[0].code

        ecloudOrgInfo.services?.each {
            if(it.opttype as int == 0) {    //取新增业务的套餐类型
                productTypeCode = it.code
                return
            }
        }

        def codeList = Arrays.asList(productTypeCodes.split(','))

        return productTypeArray[codeList.indexOf(productTypeCode)]
    }

    String convertUserType4EcloudOrg(def ecloudOrgInfo) {
        return ecloudOrgInfo.trial ? Account.USERTYPE_TRIAL : Account.USERTYPE_OFFICIAL
    }

    boolean isEcloudOrgManager(def ecloudOrgInfo, String username) {

        String manager = Account.getEcloudUsername(ecloudOrgInfo.userid as int)

        return username.equals(manager)
    }

    public static void main(String[] args) {

        //        client_id=124124client_key=72ff7f4e-9b2e-4b33-8232-07cb0d08bbf3timestamp=1411718510123
//        通过MD5加密后为70D86BAF02B64003B7383785DB93D143

        String s = "70D86BAF02B64003B7383785DB93D143"

        println s
//        println getSign(1411718510123)
//
//        assert s == getSign(1411718510123)


    }


}
