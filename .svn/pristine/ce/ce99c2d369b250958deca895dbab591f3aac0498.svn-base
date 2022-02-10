package com.istar.mediabroken.utils;

import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;

import org.apache.poi.POIXMLDocument;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.hwpf.model.PicturesTable;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Picture;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Table;
import org.apache.poi.hwpf.usermodel.TableCell;
import org.apache.poi.hwpf.usermodel.TableIterator;
import org.apache.poi.hwpf.usermodel.TableRow;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.xmlbeans.impl.piccolo.io.FileFormatException;


/**
 * @Description: 利用poi将word简单的转换成html文件
 */
public class Word2Html {
    /**
     * 回车符ASCII码
     */
    private static final short ENTER_ASCII = 13;

    /**
     * 空格符ASCII码
     */
    private static final short SPACE_ASCII = 32;

    /**
     * 水平制表符ASCII码
     */
    private static final short TABULATION_ASCII = 9;

    private static String htmlText = "";
    private static String htmlTextTbl = "";
    private static int counter = 0;
    private static int beginPosi = 0;
    private static int endPosi = 0;
    private static int beginArray[];
    private static int endArray[];
    private static String htmlTextArray[];
    private static boolean tblExist = false;

    /**
     * 项目路径
     */
    private static String projectRealPath = "";
    /**
     * 临时文件路径
     */
    private static String tempPath = "/upfile/" + File.separator + "transferFile" + File.separator;
    /**
     * word文档名称
     */
    private static String wordName = "";

    public static void main(String argv[]) {
        try {
            String result = wordToHtml("D:\\", "test1.doc");
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取每个文字样式
     *
     * @param fileName
     * @throws Exception
     */

    private static String getWordAndStyle(String fileName) throws Exception {
        String returnStr = "";
        FileInputStream in = new FileInputStream(new File(fileName));
        int index = fileName.lastIndexOf(".");
        String fileType = fileName.substring(index);
        POIFSFileSystem pfs = null;
        XWPFWordExtractor docx = null;
        if (fileType.equals(".docx")) {
            docx = new XWPFWordExtractor(POIXMLDocument.openPackage(fileName));//对docx文档的操作
            returnStr = docx.getText();
        } else if (fileType.equals(".doc")) {
            pfs = new POIFSFileSystem(in); // 对doc文档的操作
            HWPFDocument doc = new HWPFDocument(pfs);
            returnStr = doc.getText().toString();

        }
        returnStr=returnStr.replaceAll("\\r\\u0003<br/>\\r<br/>", "");
        returnStr=returnStr.replaceAll("\\r\\u0004<br/>\\r<br/>", "");
        returnStr=returnStr.replaceAll("\\u000b", "<br/>");
        return returnStr;
    }

    /**
     * 读写文档中的表格
     *
     * @throws Exception
     */
    private static void readTable(TableIterator it, Range rangetbl) throws Exception {

        htmlTextTbl = "";
        // 迭代文档中的表格

        counter = -1;
        while (it.hasNext()) {
            tblExist = true;
            htmlTextTbl = "";
            Table tb = (Table) it.next();
            beginPosi = tb.getStartOffset();
            endPosi = tb.getEndOffset();

            // System.out.println("............"+beginPosi+"...."+endPosi);
            counter = counter + 1;
            // 迭代行，默认从0开始
            beginArray[counter] = beginPosi;
            endArray[counter] = endPosi;

            htmlTextTbl += "<table border='1' cellpadding='0' cellspacing='0' >";
            for (int i = 0; i < tb.numRows(); i++) {
                TableRow tr = tb.getRow(i);

                htmlTextTbl += "<tr align='center'>";
                // 迭代列，默认从0开始
                for (int j = 0; j < tr.numCells(); j++) {
                    TableCell td = tr.getCell(j);// 取得单元格
                    int cellWidth = td.getWidth();

                    // 取得单元格的内容
                    for (int k = 0; k < td.numParagraphs(); k++) {
                        Paragraph para = td.getParagraph(k);
                        CharacterRun crTemp = para.getCharacterRun(0);
                        String fontStyle = "<span style=\"font-family:" + crTemp.getFontName() + ";font-size:"
                                + crTemp.getFontSize() / 2 + "pt;color:" + crTemp.getColor() + ";";

                        if (crTemp.isBold())
                            fontStyle += "font-weight:bold;";
                        if (crTemp.isItalic())
                            fontStyle += "font-style:italic;";

                        String s = fontStyle + "\">" + para.text().toString().trim() + "</span>";
                        if (s == "") {
                            s = " ";
                        }
                        // System.out.println(s);
                        htmlTextTbl += "<td width=" + cellWidth + ">" + s + "</td>";
                        // System.out.println(i + ":" + j + ":" + cellWidth + ":" + s);
                    } // end for
                } // end for
            } // end for
            htmlTextTbl += "</table>";
            htmlTextArray[counter] = htmlTextTbl;

        } // end while
    }

    /**
     * 读写文档中的图片
     *
     * @throws Exception
     */
    /*private static String readPicture(PicturesTable pTable, CharacterRun cr) throws Exception {
        String imgText="";
        // 提取图片
        Picture pic = pTable.extractPicture(cr, false);
        BufferedImage image = null;// 图片对象
        // 获取图片样式
        int picHeight = pic.getHeight() * pic. getVerticalScalingFactor() / 100;
        int picWidth = pic. getHorizontalScalingFactor() * pic.getWidth() / 100;
        if (picWidth > 500) {
            picHeight = 500 * picHeight / picWidth;
            picWidth = 500;
        }
        String style = " style='height:" + picHeight + "px;width:" + picWidth + "px'";

        // 返回POI建议的图片文件名
        String afileName = pic.suggestFullFileName();
        //单元测试路径
        String directory = "images/" + wordName + "/";
        //项目路径
        //String directory = tempPath + "images/" + wordName + "/";
        makeDir(projectRealPath, directory);// 创建文件夹

        int picSize = cr.getFontSize();
        int myHeight = 0;

        if (afileName.indexOf(".wmf") > 0) {
            OutputStream out = new FileOutputStream(new File(projectRealPath + directory + afileName));
            out.write(pic.getContent());
            out.close();
            afileName = Wmf2Png.convert(projectRealPath + directory + afileName);

            File file = new File(projectRealPath + directory + afileName);

            try {
                image = ImageIO.read(file);
            } catch (Exception e) {
                e.printStackTrace();
            }

            int pheight = image.getHeight();
            int pwidth = image.getWidth();
            if (pwidth > 500) {
                htmlText += "<img style='width:" + pwidth + "px;height:" + myHeight + "px'" + " src=\"" + directory
                        + afileName + "\"/>";
                imgText += "<img style='width:" + pwidth + "px;height:" + myHeight + "px'" + " src=\"" + directory
                        + afileName + "\"/>";
            } else {
                myHeight = (int) (pheight / (pwidth / (picSize * 1.0)) * 1.5);
                htmlText += "<img style='vertical-align:middle;width:" + picSize * 1.5 + "px;height:" + myHeight
                        + "px'" + " src=\"" + directory + afileName + "\"/>";
                imgText += "<img style='vertical-align:middle;width:" + picSize * 1.5 + "px;height:" + myHeight
                        + "px'" + " src=\"" + directory + afileName + "\"/>";
            }

        } else {
            OutputStream out = new FileOutputStream(new File(projectRealPath + directory + afileName));
            // pic.writeImageContent(out);
            out.write(pic.getContent());
            out.close();
            // 处理jpg或其他（即除png外）
            if (afileName.indexOf(".png") == -1) {
                try {
                    File file = new File(projectRealPath + directory + afileName);
                    image = ImageIO.read(file);
                    picHeight = image.getHeight();
                    picWidth = image.getWidth();
                    if (picWidth > 500) {
                        picHeight = 500 * picHeight / picWidth;
                        picWidth = 500;
                    }
                    style = " style='height:" + picHeight + "px;width:" + picWidth + "px'";
                } catch (Exception e) {
                    // e.printStackTrace();
                }
            }
            htmlText += "<img " + style + " src=\"" + directory + afileName + "\"/>";
            imgText += "<img " + style + " src=\"" + directory + afileName + "\"/>";
        }
        if (pic.getWidth() > 450) {
            htmlText += "<br/>";
            imgText += "<br/>";
        }
        return imgText;
    }*/
    private static boolean compareCharStyle(CharacterRun cr1, CharacterRun cr2) {
        boolean flag = false;
        if (cr1.isBold() == cr2.isBold() && cr1.isItalic() == cr2.isItalic()
                && cr1.getFontName().equals(cr2.getFontName()) && cr1.getFontSize() == cr2.getFontSize()) {
            flag = true;
        }
        return flag;
    }

    /**
     * 写文件（成功返回true，失败则返回false）
     *
     * @param s        要写入的内容
     * @param filePath 文件
     */
    private static boolean writeFile(String s, String filePath) {
        FileOutputStream fos = null;
        BufferedWriter bw = null;
        s = s.replaceAll("EMBED", "").replaceAll("Equation.DSMT4", "");
        try {
            makeDir(projectRealPath, tempPath);// 创建文件夹
            File file = new File(filePath);
            if (file.exists()) {
                return false;
            }
            fos = new FileOutputStream(file);
            bw = new BufferedWriter(new OutputStreamWriter(fos, "utf-8"));
            bw.write(s);
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (bw != null)
                    bw.close();
                if (fos != null)
                    fos.close();
            } catch (IOException ie) {
                ie.printStackTrace();
            }
        }
        return true;
    }

    /**
     * 根据路径名生成多级路径
     *
     * @param url 参数要以"\classes\cn\qtone\"或者"/classes/cn/qtone/"
     */
    private static String makeDir(String root, String url) {
        String[] sub;
        url = url.replaceAll("\\/", "\\\\");
        if (url.indexOf("\\") > -1) {
            sub = url.split("\\\\");
        } else {
            return "-1";
        }

        File dir = null;
        try {
            dir = new File(root);
            for (int i = 0; i < sub.length; i++) {
                if (!dir.exists() && !sub[i].equals("")) {
                    dir.mkdir();
                }
                File dir2 = new File(dir + File.separator + sub[i]);
                if (!dir2.exists()) {
                    dir2.mkdir();
                }
                dir = dir2;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "-1";
        }
        return dir.toString();
    }

    /**
     * 将word文档转化,返回转化后的文件路径
     *
     * @param projectPath      项目路径
     * @param relativeFilePath 文件相对路径
     * @return 返回生成的htm路径（如果出错，则返回null）
     */
    public static String wordToHtml(String projectPath, String relativeFilePath) {
        String resultPath = null;
        String result = "";
        projectRealPath = projectPath;// 项目路径
        String filePath = "";
        try {
            File file = new File(projectPath + relativeFilePath);
            if (file.exists()) {
                if (file.getName().indexOf(".doc") == -1 || file.getName().indexOf(".docx") > 0) {
                    throw new FileFormatException("请确认文件格式为doc!");
                } else {
                    wordName = file.getName();
                    wordName = wordName.substring(0, wordName.indexOf("."));

                    filePath = projectRealPath + tempPath + wordName + ".html";
                    synchronized (relativeFilePath) {// 处理线程同步问题
                        File ff = new File(filePath);
                        if (!ff.exists()) {// 如果不存在则进行转换
                            result = getWordAndStyle(projectPath + relativeFilePath);
                            writeFile(htmlText, filePath);
                        }
                    }
                    resultPath = tempPath + wordName + ".html";
                }
            } else {
                throw new FileNotFoundException("没找到相关文件！");
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
