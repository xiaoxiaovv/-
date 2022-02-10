package com.istar.mediabroken.utils;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class HttpUtil {
	
	
	/**
	 * get请求带参数
	 * @param url
	 * @param map
	 * @return
	 */
	public static String get(String url , Map<String,String> map ){
		CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        try {
        	// 定义请求的参数
        	URIBuilder ub = new URIBuilder(url);
        	Set<Entry<String,String>> set = map.entrySet();
        	for (Entry<String, String> e : set) {
				 ub = ub.setParameter(e.getKey(), e.getValue());
			}
        	URI uri = ub.build();
        	// 创建http GET请求
        	HttpGet httpGet = new HttpGet(uri);
            // 执行请求
            response = httpclient.execute(httpGet);
            // 判断返回状态是否为200
            if (response.getStatusLine().getStatusCode() == 200) {
                return EntityUtils.toString(response.getEntity(), "UTF-8");
            }
        }catch(Exception e){
        	e.printStackTrace();
        } finally {
            closeSource(response , httpclient);
        }
        return null;
	}
	
	
	/**
	 * get请求不带参数
	 * @param url
	 * @return
	 */
	public static String get(String url){
		// 创建Httpclient对象
        CloseableHttpClient httpclient = HttpClients.createDefault();
		  // 创建http GET请求
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = null;
        try {
            // 执行请求
            response = httpclient.execute(httpGet);
            // 判断返回状态是否为200
            if (response.getStatusLine().getStatusCode() == 200) {
                return EntityUtils.toString(response.getEntity(), "UTF-8");
            }
        }catch(Exception e){
        	//e.printStackTrace();
        } finally {
            closeSource(response , httpclient);
        }
        return null;
	}
	
	
	public static String getPost(String url , Map<String , String> map){
		 CloseableHttpResponse response = null;
		 CloseableHttpClient httpclient = HttpClients.createDefault();
		  try {
			    HttpPost httpPost = new HttpPost(url);  
			    List<NameValuePair> nvps = new ArrayList<NameValuePair>();  
			    
			   Set<Entry<String,String>> set = map.entrySet();
			   for (Entry<String, String> e : set) {
				   nvps.add(new BasicNameValuePair(e.getKey(), e.getValue()));  
			   }
			    
			    httpPost.setEntity(new UrlEncodedFormEntity(nvps,"utf-8"));
			    response = httpclient.execute(httpPost);
			    if (response.getStatusLine().getStatusCode() == 200) {
	                return EntityUtils.toString(response.getEntity(), "UTF-8");
	            }
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			 closeSource(response , httpclient);
		}
	      return null;  
	}
	
	/*public static void main(String[] args) {
		Map<String , String> map = new HashMap<String , String>();
		map.put("word","河北");
		String post = getPost("http://192.168.1.20:8090/YjSearch/region" , map);
		System.out.println(post);
	}*/
	
	private static void closeSource(CloseableHttpResponse response, CloseableHttpClient httpclient) {
		if (response != null) {
		    try {
				response.close();
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				response = null;
			}
		}
		try {
			if(httpclient != null)
			httpclient.close();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			httpclient = null;
		}
	}
	
	
}
