package com.istar.mediabroken.utils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

/**
 * Created by ZHXG on 2017/5/3.
 */
public class HttpClientUtil {
    @SuppressWarnings("resource")
    public static String doPost(String url, String jsonstr, String charset){
        HttpClient httpClient = null;
        HttpPost httpPost = null;
        String result = null;
        try{
            httpClient = new SSLClient();
            httpPost = new HttpPost(url);
            httpPost.addHeader("Content-Type", "application/json");
            StringEntity se = new StringEntity(jsonstr);
            se.setContentType("text/json");
            se.setContentEncoding(new BasicHeader("Content-Type", "application/json"));
            httpPost.setEntity(se);
            HttpResponse response = httpClient.execute(httpPost);
            if(response != null){
                HttpEntity resEntity = response.getEntity();
                if(resEntity != null){
                    result = EntityUtils.toString(resEntity,charset);
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return result;
    }

    /**
     * 从网络Url中下载文件
     * @param urlStr
     * @param fileName
     * @param savePath
     * @throws IOException
     */
    public static String downLoadFromUrl(String urlStr, String fileName, String savePath) throws Exception {

        HttpClient httpClient = new SSLClient();
        HttpGet httpGet = new HttpGet(urlStr);
        String result = null;
        HttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            //getResponse
            InputStream in = entity.getContent();
            byte[] getData = readInputStream(in);
            String currFileName = fileName.toLowerCase();
            if(!(currFileName.endsWith(".gif") || currFileName.endsWith(".png") || currFileName.endsWith(".jfif"))){
                byte b0 = getData[0];
                byte b1 = getData[1];
                byte b2 = getData[2];
                byte b3 = getData[3];
                byte b6 = getData[6];
                byte b7 = getData[7];
                byte b8 = getData[8];
                byte b9 = getData[9];
                if (b0 == (byte) 'G' && b1 == (byte) 'I' && b2 == (byte) 'F') {
                    fileName = fileName.concat(".gif");
                } else if (b1 == (byte) 'P' && b2 == (byte) 'N' && b3 == (byte) 'G') {
                    fileName = fileName.concat(".png");
                } else if (b6 == (byte) 'J' && b7 == (byte) 'F' && b8 == (byte) 'I'&& b9 == (byte) 'F') {
                    fileName = fileName.concat(".jpeg");
                }else {
                    fileName = fileName.concat(".jpg");
                }
            }
            //文件保存位置
            File saveDir = new File(savePath);
            if (!saveDir.exists()) {
                saveDir.mkdir();
            }

            File file = new File(saveDir, fileName);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(getData);
            if (fos != null) {
                fos.close();
            }
            if (in != null) {
                in.close();
            }
        }
        return savePath + "/" + fileName;
    }
    /**
     * 从输入流中获取字节数组
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static  byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }

    public static String doPost(String url, File file) throws IOException, NoSuchAlgorithmException, NoSuchProviderException, KeyManagementException {
        URL urlObj = new URL(url);
        //连接
        HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
        String result = null;
        con.setDoInput(true);

        con.setDoOutput(true);

        con.setUseCaches(false); // post方式不能使用缓存

        // 设置请求头信息
        con.setRequestProperty("Connection", "Keep-Alive");
        con.setRequestProperty("Charset", "UTF-8");
        // 设置边界
        String BOUNDARY = "----------" + System.currentTimeMillis();
        con.setRequestProperty("Content-Type",
                "multipart/form-data; boundary="
                        + BOUNDARY);

        // 请求正文信息
        // 第一部分：
        StringBuilder sb = new StringBuilder();
        sb.append("--"); // 必须多两道线
        sb.append(BOUNDARY);
        sb.append("\r\n");
        sb.append("Content-Disposition: form-data;name=\"media\";filelength=\"" + file.length() + "\";filename=\""

                + file.getName() + "\"\r\n");
        sb.append("Content-Type:application/octet-stream\r\n\r\n");
        byte[] head = sb.toString().getBytes("utf-8");
        // 获得输出流
        OutputStream out = new DataOutputStream(con.getOutputStream());
        // 输出表头
        out.write(head);

        // 文件正文部分
        // 把文件已流文件的方式 推入到url中
        DataInputStream in = new DataInputStream(new FileInputStream(file));
        int bytes = 0;
        byte[] bufferOut = new byte[1024];
        while ((bytes = in.read(bufferOut)) != -1) {
            out.write(bufferOut, 0, bytes);
        }
        in.close();
        // 结尾部分
        byte[] foot = ("\r\n--" + BOUNDARY + "--\r\n").getBytes("utf-8");// 定义最后数据分隔线
        out.write(foot);
        out.flush();
        out.close();
        StringBuffer buffer = new StringBuffer();
        BufferedReader reader = null;
        try {
            // 定义BufferedReader输入流来读取URL的响应
            reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            if (result == null) {
                result = buffer.toString();
            }
        } catch (IOException e) {
            System.out.println("发送POST请求出现异常！" + e);
            e.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return result;
    }

}
