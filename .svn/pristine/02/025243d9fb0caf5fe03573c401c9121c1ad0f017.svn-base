package com.istar.mediabroken.task

import com.istar.mediabroken.service.CompetitionAnalysService
import com.istar.mediabroken.utils.SpringContextHolder

/**
 * Created by luda on 2017/5/11.
 */
class RemoveCopyrightMonitorNews implements Runnable {
    private Long userId;
    private String monitorId;

    public RemoveCopyrightMonitorNews(Long userId,String monitorId) {
        this.userId = userId
        this.monitorId = monitorId
    }

    @Override
    void run() {
        CompetitionAnalysService competitionAnalysService = SpringContextHolder.getBean('competitionAnalysService');
        competitionAnalysService.removeCopyrightMonitorLogAndNews(this.userId,this.monitorId);
    }
}
