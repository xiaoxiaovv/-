package com.istar.mediabroken.utils.WordHtml;

import com.istar.mediabroken.Const;
import com.istar.mediabroken.utils.WordUtils;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.http.HttpStatus;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description:word导出帮助类 通过freemarker模板引擎来实现
 * @author:LiaoFei
 * @date :2016-3-24 下午3:49:25
 * @version V1.0
 * 
 */
public class WordGeneratorWithFreemarker {

	private WordGeneratorWithFreemarker() {
	}

	public static Map createDoc(Map<String, Object> dataMap,String targetPath, String env){
		Map resultMap = new HashMap();
		Configuration configuration = new Configuration(Configuration.VERSION_2_3_23);
		configuration.setDefaultEncoding("utf-8");
		configuration.setClassicCompatible(true);
		WordHtmlGeneratorHelper.handleAllObject(dataMap);
		String filePath;
		if (Const.ENV_ONLINE.equals(env) || Const.ENV_TEST.equals(env)) {
			filePath = new File(WordUtils.class.getResource("/").getPath(), "template").getPath();
		} else {
			filePath = new File(WordUtils.class.getResource("/").getPath().replace("/classes/", "/resources/"), "template").getPath();
		}
		Template t = null;
		try {
			configuration.setDirectoryForTemplateLoading(new File(filePath));
			t = configuration.getTemplate(dataMap.get("fileType").toString()+".ftl");
		} catch (IOException e) {
			e.printStackTrace();
			resultMap.put("status",HttpStatus.SC_INTERNAL_SERVER_ERROR);
			resultMap.put("msg",e.getMessage());
			return resultMap;
		}

		String completeFilePath = "/download/" + dataMap.get("fileType").toString() + "/" + dataMap.get("fileName").toString()  +
				"/" + dataMap.get("fileType").toString() + ".doc";
		File outFile = new File(targetPath, completeFilePath);

		try {
			Writer w = new OutputStreamWriter(new FileOutputStream(outFile),Charset.forName("utf-8"));
			t.process(dataMap, w);
			w.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			resultMap.put("status",HttpStatus.SC_INTERNAL_SERVER_ERROR);
			resultMap.put("msg",ex.getMessage());
			return resultMap;
		}
		resultMap.put("status",HttpStatus.SC_OK);
		resultMap.put("msg",completeFilePath);
		return resultMap;
	}

}