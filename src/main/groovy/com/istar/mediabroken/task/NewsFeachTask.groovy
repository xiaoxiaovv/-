package com.istar.mediabroken.task

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.istar.mediabroken.entity.News
import com.istar.mediabroken.service.CaptureService
import com.istar.mediabroken.utils.DateUitl
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.text.SimpleDateFormat

@Component
@Slf4j
public class NewsFeachTask implements Task {
    @Autowired
    CaptureService captureSrv

    String newsPath = '/Users/steven/Desktop/news'

    @Override
    void execute() {
        def start = DateUitl.getBeginDayOfYesterday()
        def end = DateUitl.getDayBegin()
        def sdf = new SimpleDateFormat("yyyyMMdd")

        def file = new File(newsPath, sdf.format(start))
        file.mkdirs()
        def publishFile = new File(file, 'publish.json')
//        if (file.exists())
//            file.delete()


        for (int pageNo = 1; true ; pageNo++) {
            def paging = captureSrv.getNewsListBySite('劳动午报', start, end, pageNo, 10)

            paging.list.each {
                def news = getNews(it)
                publishFile.append(JSON.toJSONString(news))
                publishFile.append("\n")

                feachReprintNews(it, start, end, file)

            }



            if (!paging.list || paging.list.size() < 10) break;
        }

    }

    private void feachReprintNews(News publishNews, Date start, Date end, File file) {
        def sdf = new SimpleDateFormat("yyyyMMdd")
        def reprintFile = new File(file, 'reprint_' + sdf.format(start) + ".json")
        for (int pageNo = 1; true ; pageNo++) {
            def paging = captureSrv.getNewsListByTitle(publishNews.title, start, end, pageNo, 10)
            paging.list.each {
                def news = getNews(it)
                news.publishNewsId = publishNews.newsId
                reprintFile.append(JSON.toJSONString(news))
                reprintFile.append("\n")
            }

            if (!paging.list || paging.list.size() < 10) break;
        }
    }

    JSONObject getNews(News news) {
        return JSONObject.toJSON(news)
    }
}
