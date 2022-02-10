package com.istar.mediabroken.utils

import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet

import javax.servlet.http.HttpServletResponse

/**
 * Author : YCSnail
 * Date   : 2017-04-21
 * Email  : liyancai1986@163.com
 */
class DownloadUtils {

    public static void download(String filePath, HttpServletResponse response, String fileName) throws IOException {
        File f = new File(filePath);
        if (!f.exists()) {
            throw new FileNotFoundException()
        }
        BufferedInputStream br = new BufferedInputStream(new FileInputStream(f));
        byte[] buf = new byte[1024];
        int len = 0;

        response.reset(); // 非常重要
        // 纯下载方式
        response.setContentType("application/octet-stream");
        if (org.apache.commons.lang3.StringUtils.isBlank(fileName)) {

            response.addHeader("Content-Disposition", "attachment; filename=${new String(f.getName().getBytes("gb2312"), "ISO8859-1")}");
        } else {
            response.addHeader("Content-Disposition", "attachment; filename=${new String(fileName.getBytes("gb2312"), "ISO8859-1")}");
        }

        OutputStream out = response.getOutputStream();
        while ((len = br.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        br.close();
        out.close();
    }
/**
 * 从网络Url中下载文件
 * @param urlStr
 * @param fileName
 * @param savePath
 * @throws IOException
 */
    public static String downLoadFromUrl(String urlStr,String fileName,String savePath) throws IOException{
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        //设置超时间为3秒
        conn.setConnectTimeout(3*1000);
        //防止屏蔽程序抓取而返回403错误
        conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");

        //得到输入流
        InputStream inputStream = conn.getInputStream();
        //获取自己数组
        byte[] getData = readInputStream(inputStream);
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
            }
        }
        //文件保存位置
        File saveDir = new File(savePath);
        if(!saveDir.exists()){
            saveDir.mkdir();
        }
        File file = new File(saveDir.toString() + File.separator + fileName);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(getData);
        if(fos!=null){
            fos.close();
        }
        if(inputStream!=null){
            inputStream.close();
        }
        return savePath + File.separator + fileName;
    }

    public static void downLoadFromUrl(String urlStr, String fileName, HttpServletResponse response){
        try{
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //设置超时间为3秒
            conn.setConnectTimeout(3 * 1000);
            //防止屏蔽程序抓取而返回403错误
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");

            //得到输入流
            InputStream inputStream = conn.getInputStream();
            BufferedInputStream br = new BufferedInputStream(inputStream);
            byte[] buf = new byte[1024];
            int len = 0;

            response.reset(); // 非常重要
            // 纯下载方式
            response.setContentType("application/octet-stream");
            response.addHeader("Content-Disposition", "attachment; filename=${new String(fileName.getBytes("gb2312"), "ISO8859-1")}");

            OutputStream out = response.getOutputStream();
            while ((len = br.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            br.close();
            inputStream.close();
            conn.disconnect();
        }catch (Exception e){
        }
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

    /**
     * 用途：下载服务器图片
     */
    public static String downLoadFromPath(String path,String urlStr,String fileName,String savePath){
        InputStream fis = null;
            // 以流的形式下载文件。
            fis = new BufferedInputStream(new FileInputStream(path+urlStr));
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            String currFileName = fileName.toLowerCase();
            if(!(currFileName.endsWith(".gif") || currFileName.endsWith(".png") || currFileName.endsWith(".jfif"))){
                byte b0 = buffer[0];
                byte b1 = buffer[1];
                byte b2 = buffer[2];
                byte b3 = buffer[3];
                byte b6 = buffer[6];
                byte b7 = buffer[7];
                byte b8 = buffer[8];
                byte b9 = buffer[9];
                if (b0 == (byte) 'G' && b1 == (byte) 'I' && b2 == (byte) 'F') {
                    fileName = fileName.concat(".gif");
                } else if (b1 == (byte) 'P' && b2 == (byte) 'N' && b3 == (byte) 'G') {
                    fileName = fileName.concat(".png");
                } else if (b6 == (byte) 'J' && b7 == (byte) 'F' && b8 == (byte) 'I'&& b9 == (byte) 'F') {
                    fileName = fileName.concat(".jpeg");
                }
            }
            //文件保存位置
            File saveDir = new File(savePath);
            if (!saveDir.exists()) {
                saveDir.mkdir();
            }
            File file = new File(saveDir.toString() + "/" + fileName);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(buffer);
            if (fos != null) {
                fos.close();
            }
            if (fis != null) {
                fis.close();
            }
        return savePath + "/" + fileName;
    }

}
