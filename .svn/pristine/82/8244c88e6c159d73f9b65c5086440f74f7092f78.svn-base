package com.istar.mediabroken.utils;

import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;
//import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
 
/**
 * 利用开源组件POI3.0.2动态导出EXCEL文档
 * 转载时请保留以下信息，注明出处！
 * @author leno
 * @version v1.0
 * 注意这里为了简单起见，boolean型的属性xxx的get器方式为getXxx(),而不是isXxx()
 * byte[]表jpg格式的图片数据
 */
public class ExportExcel {
 
   public HSSFWorkbook exportExcel(List<Map<String,Object>> datalist, String selname, String title) {
      return exportExcel(title, null, datalist,  "yyyy-MM-dd",selname);
   }
 
   public HSSFWorkbook exportExcel(String title,String headers, List<Map<String,Object>> datalist,String selname) {
	   return exportExcel(title, headers, datalist, "yyyy-MM-dd",selname);
   }

 
   public HSSFWorkbook exportExcel(String headers, List<Map<String,Object>> datalist,String pattern,String selname,String title) {
      return exportExcel(title, headers, datalist, pattern,selname);
   }
 
   /**
    * @param title
    *            表格标题名
    * @param headers
    *            表格属性列名数组
    * @param datalist
    *            需要显示的数据集合,集合中一定要放置符合javabean风格的类的对象。此方法支持的
    *            javabean属性的数据类型有基本数据类型及String,Date,byte[](图片数据)
    * @param pattern
    *            如果有时间数据，设定输出格式。默认为"yyy-MM-dd"
    */
   @SuppressWarnings("unchecked")
   public HSSFWorkbook exportExcel(String title, String headers,List<Map<String,Object>> datalist, String pattern,String selname) {
      // 声明一个工作薄
      HSSFWorkbook workbook = new HSSFWorkbook();
      // 生成一个表格
      HSSFSheet sheet = workbook.createSheet(title);
      // 设置表格默认列宽度为15个字节
      sheet.setDefaultColumnWidth((short) 15);
      // 生成一个样式
      HSSFCellStyle style = workbook.createCellStyle();
      // 设置这些样式
      style.setFillForegroundColor(HSSFColor.SKY_BLUE.index);
      style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
      style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
      style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
      style.setBorderRight(HSSFCellStyle.BORDER_THIN);
      style.setBorderTop(HSSFCellStyle.BORDER_THIN);
      style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
      // 生成一个字体
      HSSFFont font = workbook.createFont();
      font.setColor(HSSFColor.VIOLET.index);
      font.setFontHeightInPoints((short) 12);
      font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
      // 把字体应用到当前的样式
      style.setFont(font);
      // 生成并设置另一个样式
      HSSFCellStyle style2 = workbook.createCellStyle();
      style2.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index);
      style2.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
      style2.setBorderBottom(HSSFCellStyle.BORDER_THIN);
      style2.setBorderLeft(HSSFCellStyle.BORDER_THIN);
      style2.setBorderRight(HSSFCellStyle.BORDER_THIN);
      style2.setBorderTop(HSSFCellStyle.BORDER_THIN);
      style2.setAlignment(HSSFCellStyle.ALIGN_CENTER);
      style2.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
      // 生成另一个字体
      HSSFFont font2 = workbook.createFont();
      font2.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);
      // 把字体应用到当前的样式
      style2.setFont(font2);
     
      // 声明一个画图的顶级管理器
      HSSFPatriarch patriarch = sheet.createDrawingPatriarch();
      // 定义注释的大小和位置,详见文档
      HSSFComment comment = patriarch.createComment(new HSSFClientAnchor(0, 0, 0, 0, (short) 4, 2, (short) 6, 5));
      // 设置注释内容
//      comment.setString(new HSSFRichTextString("可以在POI中添加注释！"));
      // 设置注释作者，当鼠标移动到单元格上是可以在状态栏中看到该内容.
      comment.setAuthor("leno");
 
      //产生表格标题行
      HSSFRow row = sheet.createRow(0);
      String[] a = headers.split(",");
      for (short i = 0; i < a.length; i++) {
         HSSFCell cell = row.createCell(i);
         cell.setCellStyle(style);
         HSSFRichTextString text = new HSSFRichTextString(a[i]);
         cell.setCellValue(text);
      }
//      selname
      String[] str = selname.split(",");
      
      //遍历集合数据，产生数据行
//      [{COMNAME=天津, UWYEAR=2014}, {COMNAME=广州, UWYEAR=2015}]
      for(int i = 1;i<datalist.size()+1;i++){
    	  row = sheet.createRow(i);//一行
    	  Map<String,Object> map = null;
    	  map = datalist.get(i-1);
    	  if(map != null){
    	  for(int j = 0 ;j<str.length;j++){
    		  HSSFCell cell = row.createCell((short) j);
    		  Object value = map.get(str[j]);
    		  
    		  try{
    			  String textValue = "";
              if(value !=null){
              if (value instanceof Date) {
                 Date date = (Date) value;
                 SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                  textValue = sdf.format(date);
              }  else{
                 //其它数据类型都当作字符串简单处理
                 textValue = value.toString();
              }
              cell.setCellValue(textValue);
              }
          } catch (SecurityException e) {
              e.printStackTrace();
          } catch (IllegalArgumentException e) {
              e.printStackTrace();
          } finally {
              //清理资源
          }
    	  }
    	  }
      }
      return workbook;
   }
}
