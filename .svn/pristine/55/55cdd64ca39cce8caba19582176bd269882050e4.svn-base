package com.istar.mediabroken.console

import com.istar.mediabroken.service.capture.NewsService
import com.istar.mediabroken.service.capture.SubjectService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@Slf4j
public class FavouriteExcelConsole implements Console {
    @Autowired
    NewsService newsService

    @Override
    public void execute(Map properties) {
        List favouriteList = newsService.favouriteCount()
        newsService.excelOutFavouriteCount(favouriteList)
        println "导出我的收藏数量"
    }

    @Override
    public String getPropertyFileName() {
        return "";
    }
}
