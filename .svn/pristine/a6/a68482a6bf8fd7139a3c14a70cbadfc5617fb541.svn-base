package com.istar.mediabroken.service.rubbish

import com.istar.mediabroken.entity.rubbish.RubbishNews
import com.istar.mediabroken.repo.capture.NewsRepo
import com.istar.mediabroken.repo.rubbish.RubbishNewsRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Author : YCSnail
 * Date   : 2017-11-10
 * Email  : liyancai1986@163.com
 */
@Service
class RubbishNewsService {

    @Autowired
    private RubbishNewsRepo rubbishNewsRepo
    @Autowired
    private NewsRepo newsRepo

    void addNews2Rubbish(long userId, String newsId) {

        def rubbishNews = rubbishNewsRepo.get(newsId)
        if(rubbishNews){
            def userIds = rubbishNews.submitter as List<Long>
            if(!userIds.contains(userId)){
                userIds.add(userId)
                rubbishNewsRepo.update(rubbishNews.id, newsId, userIds)
            }
        } else {
            def news = newsRepo.getNewsById(newsId)
            if(!news) return
            rubbishNewsRepo.add(newsId, [userId], news.simhash as String, news)
        }
    }

    boolean isRubbishNews(long userId, String newsId, String simhash){

        //首先单独判断了该用户是否把该条新闻标为了垃圾
        //如果，查不到该条新闻的记录，还需要把相同simhash的新闻也同时过滤掉
        RubbishNews rubbishNews = rubbishNewsRepo.get(newsId)
        if(rubbishNews){
            if(RubbishNews.RUBBISH_NEWS_APPROVED_YES.equals(rubbishNews.approved as String)){
                return true
            } else {
                return rubbishNews.submitter.contains(userId)
            }
        } else {
            // 处理相同simhash的新闻（此处从前端逻辑上并不好核对数据，但却实实在在的过滤了垃圾数据）

            boolean isRubbish = false

            List<RubbishNews> rubbishNewsList = rubbishNewsRepo.getListBySimhash(simhash)
            for(RubbishNews item: rubbishNewsList){
                if(RubbishNews.RUBBISH_NEWS_APPROVED_YES.equals(item.approved as String)){
                    isRubbish = true
                    break
                } else {
                    if(item.submitter.contains(userId)){
                        isRubbish = true
                        break
                    }
                }
            }
            return isRubbish
        }
    }

}
