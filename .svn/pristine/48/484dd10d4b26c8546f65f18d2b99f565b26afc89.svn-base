import com.alibaba.fastjson.JSONObject
import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.Unirest
import com.mashape.unirest.http.async.Callback
import com.mashape.unirest.http.exceptions.UnirestException
import org.apache.commons.io.FileUtils
import org.apache.http.entity.ContentType
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

/**
 * @author YCSnail
 * @date 2018-08-21
 * @email liyancai1986@163.com
 * @company SnailTech
 */


public Map getminiqrQr(String sceneStr, String url, File file) {
    RestTemplate rest = new RestTemplate();
    InputStream inputStream = null;


        Map<String,Object> param = new HashMap<>();
        param.put("scene", sceneStr);
        param.put("page", "pages/issue/detail");
        param.put("width", 430);
        param.put("auto_color", false);
        param.put("is_hyaline", false);
        Map<String,Object> line_color = new HashMap<>();
        line_color.put("r", 0);
        line_color.put("g", 0);
        line_color.put("b", 0);
        param.put("line_color", line_color);

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        HttpEntity requestEntity = new HttpEntity(param, headers);
        ResponseEntity<byte[]> entity = rest.exchange(url, HttpMethod.POST, requestEntity, byte[].class, new Object[0]);

        byte[] result = entity.getBody();

        inputStream = new ByteArrayInputStream(result);

        def x = FileUtils.copyInputStreamToFile(inputStream, file)


}


String url = 'https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=12_YoyJezydCp23bBiK1h_V66Ob_0zykx3hwK0r_p9V8b_55tQglQi9qXDS1L925fVUmuLJgGFOWV-Hl9ewSt8bupwqP13QzMKQ71mrYkxpKvyISTfgX35napBdoVbH8d0cjBEKra9DauPZHfxkLXEbAGABNU'



File f = new File('/Users/liyc/Desktop/abcd.png')

this.getminiqrQr('abcccccc12345', url, f)



//String fileName = Md5Util.md5(issueId.replaceAll("-", "")) + ".png";
//String filePath = uploadPath + "/upload/journal/qrcode/" + fileName;
//File file = new File(filePath);
//result.put("qrcode", domainUrl + "/upload/journal/qrcode/" + fileName);
