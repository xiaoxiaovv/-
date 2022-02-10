package com.jsms.api.xpt.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 * Http Demo for Java
 * 采用httpclient调用示例
 * 参考文档《短信Http接口文档.doc》
 */
public class ApiDemo4HttpClient {

	/**
	 * 短信提供商开设账号时提供一下参数:
	 *
	 * 账号、密码、通信key、IP、端口
	 */
	static String account = "JSM41634";
	static String password = "mu9u8ib8";
	static String veryCode = "t55vfi9waviz";
	static String http_url  = "http://112.74.76.186:8030";
	/**
	 * 默认字符编码集
	 */
	public static final String CHARSET_UTF8 = "UTF-8";



	/**
	 * 查询账号余额
	 * @return 账号余额，乘以10为短信条数
	 * String xml字符串，格式请参考文档说明
	 */
	public static String getBalance(){
		String balanceUrl = http_url + "/service/httpService/httpInterface.do?method=getAmount";
		Map<String,String> params = new HashMap<String,String>();
		params.put("username",account);
		params.put("password",password);
		params.put("veryCode",veryCode);
		String result = sendHttpPost(balanceUrl, params);
		return result;
	}
	/**
	 * 发送普通短信  普通短信发送需要人工审核
	 * 请求地址：
	 *   UTF-8编码：/service/httpService/httpInterface.do?method=sendUtf8Msg
	 *   GBK编码：/service/httpService/httpInterface.do?method=sendGbkMsg
	 * @param mobile 手机号码, 多个号码以英文逗号隔开,最多支持100个号码
	 * @param content 短信内容
	 * @return
	 * String xml字符串，格式请参考文档说明
	 */
	public static String sendSms(String mobile, String tempid, String content){
		Map params = new HashMap<>();
		params.put("method", "sendUtf8Msg");
		params.put("username", account);
		params.put("password", password);
		params.put("veryCode", veryCode);
		params.put("mobile", mobile);
		params.put("content", content);
		params.put("msgtype", "2");
		params.put("tempid", tempid);

		String result = sendHttpPost(http_url+"/service/httpService/httpInterface.do", params);
		return result;
	}

	/**
	 * 模版短信,无需人工审核，直接发送
	 *   (短信模版的创建参考客户端操作手册)
	 *   模版：@1@会员，动态验证码@2@(五分钟内有效)。请注意保密，勿将验证码告知他人。
	 *   参数值:@1@=某某,@2@=4293
	 *   手机接收内容：【短信签名】某某会员，动态验证码4293(五分钟内有效)。请注意保密，勿将验证码告知他人。
	 *
	 *   请求地址：
	 *   UTF-8编码：/service/httpService/httpInterface.do?method=sendUtf8Msg
	 *   GBK编码：/service/httpService/httpInterface.do?method=sendGbkMsg
	 *   注意:
	 1.发送模板短信变量值不能包含英文逗号和等号（, =）
	 2.采用此方式特殊字符需要转义
	 * 		+   %2b
	 *  	空格    %20
	 * 		&   %26
	 * 		%	%25
	 * @param mobile 手机号码
	 * @param tplId 模板编号，对应客户端模版编号
	 * @param content 模板参数值，以英文逗号隔开，如：@1@=某某,@2@=4293
	 * @return xml字符串，格式请参考文档说明
	 */
	public static String sendTplSms(String mobile,String tplId,String content){
		String sendTplSmsUrl = http_url + "/service/httpService/httpInterface.do?method=sendUtf8Msg";
		Map<String,String> params = new HashMap<String,String>();
		params.put("username", account);
		params.put("password", password);
		params.put("veryCode", veryCode);
		params.put("mobile", mobile);
		params.put("content", content);	//变量值，以英文逗号隔开
		params.put("msgtype", "2");		//2-模板短信
		params.put("tempid", tplId);	//模板编号
		params.put("code", "utf-8");
		String result = sendHttpPost(sendTplSmsUrl, params);
		return result;
	}

	/***
	 * 查询下发短信的状态报告
	 * @return
	 * String  xml字符串，格式请参考文档说明
	 */
	public static String queryReport(){
		String reportUrl = http_url + "/service/httpService/httpInterface.do?method=queryReport";
		Map<String,String> params = new HashMap<String,String>();
		params.put("username", account);
		params.put("password", password);
		params.put("veryCode", veryCode);
		String result = sendHttpPost(reportUrl, params);
		return result;
	}
	/**
	 * 查询上行回复短信
	 * @return
	 * String  xml字符串，格式请参考文档说明
	 */
	public static String queryMo(){
		String moUrl = http_url + "/service/httpService/httpInterface.do?method=queryMo";
		Map<String,String> params = new HashMap<String,String>();
		params.put("username", account);
		params.put("password", password);
		params.put("veryCode", veryCode);
		String result = sendHttpPost(moUrl, params);
		return result;
	}


	/**
	 *
	 * @param apiUrl 接口请求地址
	 * @param paramsMap 请求参数集合
	 * @return xml字符串，格式请参考文档说明
	 * String
	 */
	private static String sendHttpPost(String apiUrl, Map<String, String> paramsMap) {
		CloseableHttpClient client = HttpClients.createDefault();
		String responseText = "";
		CloseableHttpResponse response = null;
		try {
			HttpPost method = new HttpPost(apiUrl);
			if (paramsMap != null) {
				List<NameValuePair> paramList = new ArrayList<NameValuePair>();
				for (Map.Entry<String, String> param : paramsMap.entrySet()) {
					NameValuePair pair = new BasicNameValuePair(param.getKey(), param.getValue());
					paramList.add(pair);
				}
				method.setEntity(new UrlEncodedFormEntity(paramList, CHARSET_UTF8));
			}
			response = client.execute(method);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				responseText = EntityUtils.toString(entity);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				response.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return responseText;
	}

	/**
	 * @param args
	 * void
	 */
	public static void main(String[] args) {
		//查询账号余额
//		System.out.println(getBalance());
		//发送普通短信，修改接收短信的手机号码及短信内容,多个号码以英文逗号隔开，最多支持100个号码
		//System.out.println(sendSms("159*******1,159*******2", "您的验证码是8888,请注意保密，勿将验证码告知他人。"));

//		System.out.println(sendTplSms("159********","模板编号","@1@=参数值1,@2@=参数值2"));

		//查询下发短信的状态报告
		//System.out.println(queryReport());

		//查询上行回复短信
		//System.out.println(queryMo());

//		sendSms("13932628457", "@1@=23748");
	}

}
