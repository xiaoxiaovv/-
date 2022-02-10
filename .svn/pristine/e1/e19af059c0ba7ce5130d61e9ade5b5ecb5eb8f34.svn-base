package com.istar.mediabroken.task

import com.istar.mediabroken.api3rd.SolrRepo
import com.istar.mediabroken.utils.ExportExcel
import com.istar.mediabroken.utils.Md5Util
import com.istar.mediabroken.utils.StringUtils
import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils
import org.apache.http.HttpStatus
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@Slf4j
class SolrSimilarNewsTask implements Task {
    @Autowired
    SolrRepo solrRepo

    @Override
    void execute() {
//        def news = solrRepo.getNewsByUrl("http://www.thepaper.cn/newsDetail_forward_1984790")

        String title = "冬季进补食羊肉，适合你吗"
        String content = """
吃羊肉的好处
据《本草纲目》记载，羊肉能补体虚，祛寒冷，温补气血;益肾气，补形衰，开胃健力;补益产妇，通乳治带，助元阳，益精血。主治肾虚腰疼，阳痿精衰，形瘦怕冷，病后虚寒，产妇产后大虚或腹痛，产后出血，产后无乳或带下。羊肉淳浓温厚，暖肝脾而助生长，缓迫急而止疼痛，大补温气之剂也。
羊肉历来是民间冬季进补的重要食材之一。当体内有虚有寒时，不妨来一顿羊肉汤补一下。
·吃羊肉的禁忌
羊肉并不是人人皆宜
羊肉虽是冬季良好的补益之品，但也有其适应证。患慢性病尤其是患肝病者不适合吃羊肉;口舌糜烂、眼睛红、口苦、烦躁、咽喉干痛、牙龈肿痛者表现为热证者不适合食用;腹泻者不适合食用;服中药者，若方中有半夏、石菖蒲等不适合羊肉。另外，高血压、肝阳旺盛的人也不宜多吃羊肉，否则容易引起头晕。
吃羊肉时忌喝茶
因为羊肉中含有丰富的蛋白质，而茶叶中含有较多的鞣酸，吃完羊肉后马上饮茶，容易引发便秘。
不与西瓜等寒凉食物同食
吃羊肉不仅不能喝茶，也不可以吃西瓜，在冬季或许吃西瓜的频率比较低，但是在夏季的时候一定要注意了。王清贤认为，吃羊肉后再吃西瓜，这样会伤“正气”，人体健康情况下，正气足，才能发挥着保护机体建康的作用。当人体的正气虚时，六淫邪气入侵，人就会容易生病，当一个人的正气旺盛，就会与致病邪气相抗拒，就不会生病。同时因为羊肉性属大热，而西瓜性寒，属生冷食物，冷热积聚，不宜维护“正气盛”。
忌吃南瓜等辛温燥热之品
南瓜性属温热，和羊肉同属于温热的食物，所以不可以一起吃，同样的道理，在烹调羊肉时也应少放点辣椒、胡椒、生姜、丁香、茴香、醋等辛温燥热的调味品，以防上火。▲

"""
//        println(news)
//        if (news) {
//            title = news.title
//            content = news.content
//        }
        String currentCursor = '*'
        def pageSize = 100
        def listUrl = []
        List returnList = []
        def pageNo = 1
        while (true) {
            def result1 = solrRepo.getPagingNewsByTitle(title, pageNo, 100, currentCursor)
            result1.list.each {
                if (listUrl.contains(it.url)) {
                    return
                }
                listUrl << it.url
                println(listUrl.size() + "=" + it.url + it.title)
                returnList << [id     : Md5Util.md5(it.url),
                               url    : it.url,
                               title  : it.title,
                               content: it.content]
            }
            pageNo++
            currentCursor = result1.CurrentCursor
            break
            if (result1.list.size() < 100) {
                break
            }
        }
        pageNo = 1
        println("============================-----------------------------------++++++++++++++++")

        currentCursor = '*'
        while (true) {
            def startTime = new Date().getTime()
            def result1 = solrRepo.getPagingNewsByContent(StringUtils.splitSentence(content, null).join(" "), pageNo, 100, currentCursor)
            result1.list.each {
                if (listUrl.contains(it.url)) {
                    return
                }
                listUrl << it.url
                println(listUrl.size() + "=" + it.url + it.title)
                returnList << [id     : Md5Util.md5(it.url),
                               url    : it.url,
                               title  : it.title,
                               content: it.content]
            }
            currentCursor = result1.CurrentCursor
            pageNo++
            def endTime = new Date().getTime()
            println("用时：" + (endTime - startTime) / 1000)
            break
            if (result1.list.size() < 100) {
                break
            }
        }


        ExportExcel ex = new ExportExcel();
        String sheetName = "查询站点";//下载文件的默认名字,sheet页名字
        String outfileName = "查询站点"
        String headers = "id,url,title,content";
//表头
        String selname = "id,url,title,content";
//标题对应key值

        HSSFWorkbook wb = ex.exportExcel(sheetName, headers, returnList, selname);

        String excelFolder = "D:\\" + outfileName;
        def result = [
                status: HttpStatus.SC_OK,
                msg   : '',
        ];
        //如果文件夹不存在则创建，如果已经存在则删除
        File outPath = new File(excelFolder)
        if (!outPath.exists()) {
            FileUtils.forceMkdir(outPath)
        } else {
            FileUtils.forceDelete(outPath)
            FileUtils.forceMkdir(outPath)
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            wb.write(os);
        }
        catch (IOException e) {
            e.printStackTrace();
            println("出现错误，请不要关闭此窗口，并联系开发工程师，谢谢")
        }
        byte[] exclecontent = os.toByteArray();
        OutputStream fos = null;
        File file = new File(excelFolder + "/" + "excel.xls")

        try {
            fos = new FileOutputStream(file);
            fos.write(exclecontent);
            os.close();
            fos.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            println("出现错误，请不要关闭此窗口，并联系开发工程师，谢谢")
        }
        println("生成目录在 D:\\" + outfileName)
    }
}
