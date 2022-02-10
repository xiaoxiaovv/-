package com.istar.mediabroken.service.alioss

import com.aliyun.oss.common.utils.BinaryUtil
import groovy.util.logging.Slf4j
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import javax.servlet.http.HttpServletRequest
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec

/**
 * @description: aliyun回调函数接口
 * @author: hexushuai
 * @date: 2019/1/22 11:43
 */
@Service
@Slf4j
class CallbackService {

    @Value('${aliyun.oss.callback}')
    String callback

    /**
     * 请求阿里云回调
     */
    public def doCheckCallback(HttpServletRequest request) {
        Map map = new HashMap()
        String ossCallbackBody = getPostBody(request.getInputStream(),
                Integer.parseInt(request.getHeader("content-length")))
        boolean ret = false
        try {
            ret = verifyOSSCallbackRequest(request, ossCallbackBody)
        } catch (Exception e) {
            log.debug("callback request error", e)
        }
        map.put("result", ret)
        map.put("fileName", ossCallbackBody.substring(10, ossCallbackBody.indexOf("&") - 1))
        map.put("orgId", ossCallbackBody.substring(ossCallbackBody.indexOf("x:var1=") + 7, ossCallbackBody.indexOf("&x:var2")))
        map.put("userId", ossCallbackBody.substring(ossCallbackBody.indexOf("x:var2=") + 7, ossCallbackBody.length()))
        return map
    }

    /**
     * 获取Post消息体
     */
    private String getPostBody(InputStream is, int contentLen) {
        if (contentLen > 0) {
            int readLen = 0
            int readLengthThisTime = 0
            byte[] message = new byte[contentLen]
            try {
                while (readLen != contentLen) {
                    readLengthThisTime = is.read(message, readLen, contentLen - readLen)
                    if (readLengthThisTime == -1) {// Should not happen.
                        break
                    }
                    readLen += readLengthThisTime
                }
                return new String(message)
            } catch (IOException e) {
                e.printStackTrace()
                log.debug("get callback post body error", e)
            }
        }
        return ""
    }

    /**
     * 验证上传回调的Request
     */
    private boolean verifyOSSCallbackRequest(HttpServletRequest request, String ossCallbackBody)
            throws NumberFormatException, IOException {
        boolean ret = true
        String autoInput = new String(request.getHeader("Authorization"))
        String pubKeyInput = request.getHeader("x-oss-pub-key-url")
        byte[] authorization = BinaryUtil.fromBase64String(autoInput)
        byte[] pubKey = BinaryUtil.fromBase64String(pubKeyInput)
        String pubKeyAddress = new String(pubKey)
        if (!pubKeyAddress.startsWith("http://gosspublic.alicdn.com/")
                && !pubKeyAddress.startsWith("https://gosspublic.alicdn.com/")) {
            return false
        }
        String retString = executeGet(pubKeyAddress)
        retString = retString.replace("-----BEGIN PUBLIC KEY-----", "")
        retString = retString.replace("-----END PUBLIC KEY-----", "")
        String queryString = request.getQueryString()
        String uri = callback.substring(callback.indexOf(".com/") + 4, callback.length())
        String decodeUri = java.net.URLDecoder.decode(uri, "UTF-8")
        String authStr = decodeUri
        if (queryString != null && !queryString.equals("")) {
            authStr += "?" + queryString
        }
        authStr += "\n" + ossCallbackBody
        ret = doCheck(authStr, authorization, retString)
        return ret
    }

    /**
     * 获取public key
     */
    private String executeGet(String url) {
        BufferedReader bufferedReader = null
        String content = null
        try {
            // 定义HttpClient
            @SuppressWarnings("resource")
            DefaultHttpClient client = new DefaultHttpClient()
            // 实例化HTTP方法
            HttpGet request = new HttpGet()
            request.setURI(new URI(url))
            HttpResponse response = client.execute(request)

            bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
            StringBuffer sb = new StringBuffer("")
            String line = ""
            String NL = System.getProperty("line.separator")
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line + NL)
            }
            bufferedReader.close()
            content = sb.toString()
        } catch (Exception e) {
            log.debug("get public key error", e)
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close()// 最后要关闭BufferedReader
                } catch (Exception e) {
                    e.printStackTrace()
                }
            }
            return content
        }
    }

    private static boolean doCheck(String content, byte[] sign, String publicKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA")
            byte[] encodedKey = BinaryUtil.fromBase64String(publicKey)
            PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey))
            java.security.Signature signature = java.security.Signature.getInstance("MD5withRSA")
            signature.initVerify(pubKey)
            signature.update(content.getBytes())
            boolean verify = signature.verify(sign)
            log.info("阿里云oss回调校验结果verify为:{}", verify)
            return verify
        } catch (Exception e) {
            e.printStackTrace()
            log.debug("oss callback doCheck error", e)
        }

        return false
    }

    public static void main(String[] args) {
        String str = "filename=\"1547779009509/douyin.mp4\"&size=2075849&mimeType=\"video/mp4\"&height=&width=&x:var1=122&x:var2=1547779009509"
        String aa = str.substring(str.indexOf("x:var1=") + 7, str.indexOf("&x:var2"))
        String bb = str.substring(str.indexOf("x:var2=") + 7, str.length())
        println aa + "<--->" + bb
        String callback = "http://test.zhihuibian.com/branch_video/api/oss/callback"
        callback = callback.substring(callback.indexOf(".com/") + 4, callback.length())
        println callback
    }
}
