package com.istar.mediabroken.service.evaluate

import com.istar.mediabroken.repo.evaluate.EvaluateChannelRepo
import com.istar.mediabroken.repo.evaluate.EvaluateNewsRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * @author zxj
 * @create 2018/6/25
 */
@Service
class EvaluateNewsService {

    @Autowired
    EvaluateNewsRepo evaluateNewsRepo
    @Autowired
    EvaluateChannelRepo evaluateChannelRepo
    @Autowired
    EvaluateService evaluateService

    def getEvaluateNewsList(long userId, String channelId, Date startTime, Date endTime, String key) {
        def channel = evaluateChannelRepo.findById(userId, channelId)
        def value =""
        if (key && "sumRead".equals(key)) {
            value="readCount"
        }
        if (key && "sumReprint".equals(key)) {
            value="reprintCount"
        }
        if (key && "commentCount".equals(key)){
            value="commentCount"
        }
        if (key && "sumLike".equals(key)) {
            value="likeCount"
        }
        def list = evaluateNewsRepo.getEvaluateNewsList(channel.siteName, channel.siteDomain, channel.siteType, startTime, endTime, value, 10, 1)
        return list
    }
}
