package com.istar.mediabroken.service.copyright

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.istar.mediabroken.api3rd.CaptureApi3rd
import com.istar.mediabroken.entity.CopyrightMonitorLog
import com.istar.mediabroken.entity.CopyrightMonitorNews
import com.istar.mediabroken.entity.account.Account
import com.istar.mediabroken.entity.compile.ArticlePush
import com.istar.mediabroken.entity.compile.Material
import com.istar.mediabroken.entity.copyright.CopyRightFilter
import com.istar.mediabroken.entity.copyright.Monitor
import com.istar.mediabroken.entity.copyright.MonitorNews
import com.istar.mediabroken.repo.account.AccountRepo
import com.istar.mediabroken.repo.capture.NewsRepo
import com.istar.mediabroken.repo.compile.MaterialRepo
import com.istar.mediabroken.repo.copyright.MonitorRepo
import com.istar.mediabroken.task.RemoveCopyrightMonitorNews
import com.istar.mediabroken.task.ThreadManager
import com.istar.mediabroken.utils.*
import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils
import org.apache.http.HttpStatus
import org.apache.http.client.utils.DateUtils
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

import java.text.SimpleDateFormat

import static com.istar.mediabroken.api.ApiResult.apiResult
import static com.istar.mediabroken.utils.UrlUtils.stripUrl

@Service("MonitorService")
@Slf4j
class MonitorService {
    @Autowired
    MonitorRepo monitorRepo
    @Autowired
    CaptureApi3rd captureApi
    @Value('${image.upload.path}')
    String UPLOAD_PATH
    @Autowired
    NewsRepo newsRepo
    @Autowired
    AccountRepo accountRepo
    @Autowired
    MaterialRepo materialRepo

    public static final keyChannelMap = [
            "ahtv.cn"                               : 1,
            "mof.gov.cn"                            : 1,
            "dltv.cn"                               : 1,
            "gzstv.com"                             : 1,
            "hfbtv.com"                             : 1,
            "hbtv.com.cn"                           : 1,
            "hnradio.com"                           : 1,
            "jsbc.com"                              : 1,
            "sxrtv.com"                             : 1,
            "mofcom.gov.cn"                         : 1,
            "smg.cn"                                : 1,
            "whbc.com.cn"                           : 1,
            "sjzntv.cn"                             : 1,
            "csbtv.com"                             : 1,
            "cac.gov.cn"                            : 1,
            "chinanews.com"                         : 1,
            "ccdi.gov.cn"                           : 1,
            "szmg.com.cn"                           : 1,
            "qhbtv.com"                             : 1,
            "anhuinews.com"                         : 1,
            "banyuetan.org"                         : 1,
            "enorth.com.cn"                         : 1,
            "lnd.com.cn"                            : 1,
            "morningpost.com.cn"                    : 1,
            "brtn.cn"                               : 1,
            "ynet.com"                              : 1,
            "bmn.net.cn"                            : 1,
            "bjrb.bjd.com.cn"                       : 1,
            "bjwb.bjd.com.cn"                       : 1,
            "tjbhnews.com"                          : 1,
            "caixin.com"                            : 1,
            "e.chengdu.cn"                          : 1,
            "cdwb.com.cn"                           : 1,
            "cdbs.cditv.cn"                         : 1,
            "dahe.cn"                               : 1,
            "jxcn.cn"                               : 1,
            "dlxww.com"                             : 1,
            "dzwww.com"                             : 1,
            "dz.com"                                : 1,
            "yicai.com"                             : 1,
            "dbw.cn"                                : 1,
            "nen.com.cn"                            : 1,
            "dragontv.cn"                           : 1,
            "smgradio.cn"                           : 1,
            "eastday.com"                           : 1,
            "gog.com.cn"                            : 1,
            "epaper.legaldaily.com.cn/fzrb"         : 1,
            "legaldaily.com.cn"                     : 1,
            "fjtv.net"                              : 1,
            "fjsen.com"                             : 1,
            "zohi.tv"                               : 1,
            "fjrb.fjsen.com"                        : 1,
            "fznews.com.cn"                         : 1,
            "gsrb.gansudaily.com.cn"                : 1,
            "media.workercn.cnsites/media/grrb"     : 1,
            "epaper.gmw.cn"                         : 1,
            "gmw.cn"                                : 1,
            "radio-gd.com"                          : 1,
            "gdtv.cn"                               : 1,
            "jxgdw.com"                             : 1,
            "gztv.com"                              : 1,
            "gxtv.cn"                               : 1,
            "bbrtv.com"                             : 1,
            "gxrb.com.cn"                           : 1,
            "gxnews.com.cn"                         : 1,
            "dayoo.com"                             : 1,
            "epaper.gywb.cn/gyrb"                   : 1,
            "epaper.gywb.cn/gywb"                   : 1,
            "gzrb.gog.cn"                           : 1,
            "paper.hndnews.com"                     : 1,
            "epaper.comnews.cn"                     : 1,
            "gb.cri.cn"                             : 1,
            "sdpc.gov.cn"                           : 1,
            "hrbtv.net"                             : 1,
            "my399.com"                             : 1,
            "hkbtv.cn"                              : 1,
            "hnrb.hinews.cn"                        : 1,
            "hntqb.com"                             : 1,
            "hnntv.cn"                              : 1,
            "haiwainet.cn"                          : 1,
            "hangzhou.com.cn"                       : 1,
            "hf365.com"                             : 1,
            "hebradio.com"                          : 1,
            "hebtv.com"                             : 1,
            "hebnews.cn"                            : 1,
            "hebei.com.cn"                          : 1,
            "hnjtyx.com"                            : 1,
            "henandaily.cn"                         : 1,
            "hljnews.cn"                            : 1,
            "rednet.cn"                             : 1,
            "szb.saibeinews.com/rb"                 : 1,
            "szb.saibeinews.com/wb"                 : 1,
            "cnhubei.com"                           : 1,
            "voc.com.cn"                            : 1,
            "cqnews.net"                            : 1,
            "data.huanqiu.com"                      : 1,
            "huanqiu.com"                           : 1,
            "sxgov.cn"                              : 1,
            "jlntv.cn"                              : 1,
            "jlradio.cn"                            : 1,
            "chinajilin.com.cn"                     : 1,
            "e23.cn"                                : 1,
            "newspaper.jcrb.com"                    : 1,
            "szb.jkb.com.cn/jkbpaper"               : 1,
            "nhfpc.gov.cn"                          : 1,
            "jxnews.com.cn"                         : 1,
            "jiaodong.net"                          : 1,
            "jfdaily.com"                           : 1,
            "jrzb.zjol.com.cn"                      : 1,
            "chinatoday.dooland.com"                : 1,
            "epaper.jwb.com.cn/jwb"                 : 1,
            "jlwb.njnews.cn"                        : 1,
            "financialnews.com.cn"                  : 1,
            "jinghua.cn"                            : 1,
            "jjckb.xinhuanet.com"                   : 1,
            "81.cn/planewspaper/paperindex"         : 1,
            "wokeji.com/jbsj"                       : 1,
            "daily.clzg.cn"                         : 1,
            "lasa-eveningnews.com.cn/lsrb/pc/layout": 1,
            "lwdf.cn"                               : 1,
            "hljradio.com"                          : 1,
            "hljtv.com"                             : 1,
            "sdnews.com.cn"                         : 1,
            "hifly.tv"                              : 1,
            "ncrbw.cn"                              : 1,
            "ncwbw.cn"                              : 1,
            "southcn.com"                           : 1,
            "hinews.cn"                             : 1,
            "nbs.cn"                                : 1,
            "njdaily.cn"                            : 1,
            "nntv.cn"                               : 1,
            "nnrb.com.cn"                           : 1,
            "nnwb.com"                              : 1,
            "nmrb.com.cn"                           : 1,
            "szb.northnews.cn"                      : 1,
            "nmgnews.com.cn"                        : 1,
            "nbtv.cn"                               : 1,
            "sz.nxnews.net"                         : 1,
            "nxnews.net"                            : 1,
            "szb.farmer.com.cn/nmrb"                : 1,
            "qlwb.com.cn"                           : 1,
            "iqilu.com"                             : 1,
            "qianlong.com"                          : 1,
            "zjol.com.cn"                           : 1,
            "epaper.tibet3.com/qhrb"                : 1,
            "qtv.com.cn"                            : 1,
            "dailyqd.com"                           : 1,
            "qhtv.cn"                               : 1,
            "qhnews.com"                            : 1,
            "qstheory.cn"                           : 1,
            "rmfyb.chinacourt.org/paper"            : 1,
            "paper.people.com.cn"                   : 1,
            "paper.people.com.cn/rmrbhwb"           : 1,
            "peoplerail.com/rmtd2016"               : 1,
            "people.com.cn"                         : 1,
            "paper.cnii.com.cn/rmydb"               : 1,
            "xmtv.cn"                               : 1,
            "xmnn.cn"                               : 1,
            "60.216.0.164"                          : 1,
            "sxrb.com"                              : 1,
            "sxdaily.com.cn"                        : 1,
            "snrtv.com"                             : 1,
            "sxtvs.com"                             : 1,
            "esb.sxdaily.com.cn"                    : 1,
            "news990.cn"                            : 1,
            "sznews.com"                            : 1,
            "csytv.com"                             : 1,
            "syd.com.cn"                            : 1,
            "sjzdaily.com.cn"                       : 1,
            "cbg.cn"                                : 1,
            "sctv.com"                              : 1,
            "sichuandaily.com.cn"                   : 1,
            "newssc.org"                            : 1,
            "scol.com.cn"                           : 1,
            "tynews.com.cn"                         : 1,
            "radiotj.com"                           : 1,
            "tianjinwe.com"                         : 1,
            "ts.cn"                                 : 1,
            "fmprc.gov.cn"                          : 1,
            "k618.cn"                               : 1,
            "wenweipo.com"                          : 1,
            "xinjiangnet.com.cn/wlmqdzb/a/history"  : 1,
            "cjn.cn"                                : 1,
            "xantv.cn"                              : 1,
            "v.xiancity.cn"                         : 1,
            "cnwest.com"                            : 1,
            "epaper.tibet3.com/xhdsb"               : 1,
            "xnwbw.com"                             : 1,
            "xntv.tv"                               : 1,
            "xfrb.com.cn"                           : 1,
            "203.192.15.131"                        : 1,
            "xhby.net"                              : 1,
            "xinhuanet.com"                         : 1,
            "xinjiangyaou.com"                      : 1,
            "bjnews.com.cn"                         : 1,
            "xinmin.cn"                             : 1,
            "tv.cctv.com"                           : 1,
            "yzwb.net"                              : 1,
            "ycwb.com"                              : 1,
            "szb.ycen.com.cn"                       : 1,
            "yntv.cn"                               : 1,
            "yndaily.yunnan.cn"                     : 1,
            "yunnan.cn"                             : 1,
            "chinactv.com"                          : 1,
            "ccrb.1news.cc"                         : 1,
            "ccwb.1news.cc"                         : 1,
            "news.cjn.cn"                           : 1,
            "changsha.cn"                           : 1,
            "zjstv.com"                             : 1,
            "cztv.com"                              : 1,
            "zztv.tv"                               : 1,
            "zynews.com"                            : 1,
            "xjbs.com.cn/aod/hasakeyu"              : 1,
            "workercn.cn"                           : 1,
            "cfen.com.cn.web/cjb/bzbm"              : 1,
            "cpnn.com.cn"                           : 1,
            "paper.fnews.cc"                        : 1,
            "gscn.com.cn"                           : 1,
            "cicn.com.cn/register"                  : 1,
            "news.cenews.com.cn"                    : 1,
            "zgjx.cn"                               : 1,
            "chinajsb.cn"                           : 1,
            "jschina.com.cn"                        : 1,
            "zgjtb.com"                             : 1,
            "paper.jyb.cn/zgjyb"                    : 1,
            "ce.cn"                                 : 1,
            "ceweekly.cn/magazine/ceweekly"         : 1,
            "cpd.com.cn"                            : 1,
            "81.cn"                                 : 1,
            "szb.clssn.com"                         : 1,
            "editor.caacnews.com.cn/mhb"            : 1,
            "cnnb.com.cn"                           : 1,
            "chinaqw.com"                           : 1,
            "youth.cn"                              : 1,
            "cyol.net"                              : 1,
            "chinadaily.com.cn/cndy"                : 1,
            "cn.chinadaily.com.cn"                  : 1,
            "sdchina.com"                           : 1,
            "zgswcn.com/zgsb"                       : 1,
            "ctaxnews.net.cn"                       : 1,
            "chinaso.com"                           : 1,
            "taiwan.cn"                             : 1,
            "sportsol.com.cn"                       : 1,
            "china.com.cn"                          : 1,
            "wenming.cn"                            : 1,
            "tibet.cn"                              : 1,
            "chinatibetnews.com"                    : 1,
            "vtibet.com"                            : 1,
            "inewsweek.cn"                          : 1,
            "epaper.cs.com.cn/dnis"                 : 1,
            "gov.cn"                                : 1,
            "epaper.cqn.com.cn"                     : 1,
            "epaper.cnpharm.com/zgyyb"              : 1,
            "124.42.72.218"                         : 1,
            "tv.cntv.cn/pindao"                     : 1,
            "cnr.cn"                                : 1,
            "bfq.cnr.cn/zhibo"                      : 1,
            "cqcb.com"                              : 1,
            "cqrb.cn/html/cqrb"                     : 1,
            "epaper.cqrb.cn/html/cqrb"              : 1,
            "cqwb.com.cn"                           : 1,
            "epaper.bingtuannet.com"                : 1,
            "v.fjtv.net"                            : 1,
            "fzntv.cn"                              : 1,
            "gsmg.gstv.com.cn"                      : 1,
            "gy-bs.com"                             : 1,
            "szb.hkwb.netszb"                       : 1,
            "zixun.hunantv.com"                     : 1,
            "wccdaily.com.cn"                       : 1,
            "jntv.ijntv.cn"                         : 1,
            "jnradio.ijntv.cn"                      : 1,
            "jxtv.jxntv.cn"                         : 1,
            "radio.jxntv.cn"                        : 1,
            "paper.ce.cn"                           : 1,
            "rb.lzbs.com.cn"                        : 1,
            "wb.lzbs.com.cn"                        : 1,
            "lntv.com.cn"                           : 1,
            "nctv.net.cn"                           : 1,
            "njgb.com2013"                          : 1,
            "epaper.rmzxb.com.cn"                   : 1,
            "tv.xmtv.cn"                            : 1,
            "sdtv.cn"                               : 1,
            "tywbw.com"                             : 1,
            "whtv.com.cn"                           : 1,
            "epaper.xiancn.com"                     : 1,
            "epaper.chinatibetnews.com"             : 1,
            "zzradio.cn"                            : 1,
            "jjjcb.cn"                              : 1,
            "zqb.cyol.com"                          : 1,
            "chinawater.com.cn/slb"                 : 1,
            "epaper.ccdy.cn"                        : 1,
            "elite.youth.cn"                        : 1

    ]

    Map addCopyrightMonitor(long userId, String title, String url, String author, String startDate, String media, String whiteList, String blackList, String contentAbstract) {
        if ("".equals(title)) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "请输入作品名称"])
        }
        String title2 = StringUtils.removeSpecialCode(title)
        if (title2.length() < 8){
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "作品名称长度不能小于8个有效字符"])
        }
        def count = getCopyrightMonitorCount(userId)
        if (count > 4) {
            return apiResult([status: HttpStatus.SC_BAD_REQUEST, msg: "只能创建5个版权监控项目"])
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        if (!startDate) {
            startDate = sdf.format(new Date())
        }
        Date currDate = null;
        Date endDate = null;
        try {
            currDate = sdf.parse(startDate)
            endDate = DateUitl.addDay(currDate, 30)
        } catch (Exception e) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "时间格式不正确"])
        }
        List<String> wList = new ArrayList<String>();
        JSONArray whiteLists = JSONArray.parseArray(whiteList);
        whiteLists.each {
            wList.add(it)
        }
        List<String> bList = new ArrayList<String>();
        JSONArray blackLists = JSONArray.parseArray(blackList);
        blackLists.each {
            bList.add(it)
        }
        def monitor = new Monitor(
                monitorId: UUID.randomUUID().toString(),
                userId: userId,
                title: title,
                url: url,
                author: author,
                startDate: currDate,
                endDate: endDate,
                status: 1,
                media: media,
                contentAbstract: contentAbstract,
                whiteList: wList,
                blackList: bList,
                createTime: new Date(),
                updateTime: new Date()
        )
        String result = monitorRepo.addCopyrightMonitor(monitor)
        if (result) {
            return apiResult([monitorId: result, status: HttpStatus.SC_OK, msg: "添加监控成功"])
        } else {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "添加监控失败"])
        }
    }

    Map modifyCopyrightMonitor(long userId, String monitorId, String title, String url, String author, String startDate, String media, String whiteList, String blackList, String contentAbstract) {
        if ("".equals(title)) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "请输入作品名称"])
        }
        String title2 = StringUtils.removeSpecialCode(title)
        if (title2.length() < 8){
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "作品名称长度不能小于8个有效字符"])
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        if (!startDate) {
            startDate = sdf.format(new Date())
        }
        Date currDate = null;
        Date endDate = null;
        try {
            currDate = sdf.parse(startDate)
            endDate = DateUitl.addDay(currDate, 30)
        } catch (Exception e) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "时间格式不正确"])
        }
        List<String> wList = new ArrayList<String>();
        JSONArray whiteLists = JSONArray.parseArray(whiteList);
        whiteLists.each {
            wList.add(it)
        }
        List<String> bList = new ArrayList<String>();
        JSONArray blackLists = JSONArray.parseArray(blackList);
        blackLists.each {
            bList.add(it)
        }
        def monitor = new Monitor(
                monitorId: monitorId,
                userId: userId,
                title: title,
                url: url,
                author: author,
                startDate: currDate,
                endDate: endDate,
                media: media,
                contentAbstract: contentAbstract,
                whiteList: wList,
                blackList: bList,
                updateTime: new Date()
        )
        boolean result = monitorRepo.modifyCopyrightMonitor(monitor)
        if (result) {
            removeCopyrightMonitorLogAndNews(monitor.userId, monitor.monitorId)
            return apiResult([status: HttpStatus.SC_OK, msg: "修改成功"])
        } else {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "修改失败"])
        }
    }

    void removeCopyrightMonitorLogAndNews(long userId, String monitorId) {
        monitorRepo.removeCopyrightMonitorLog(monitorId)
        monitorRepo.removeCopyrightMonitorNews(userId, monitorId)
    }

    Map removeCopyrightMonitor(long userId, String monitorId) {
        boolean result = monitorRepo.removeCopyrightMonitor(userId, monitorId)
        if (result) {
            ThreadManager.instance.addThread(new RemoveCopyrightMonitorNews(userId, monitorId))
            return apiResult([status: HttpStatus.SC_OK, msg: "删除成功"])
        } else {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "删除失败"])
        }
    }

    Map getCopyrightMonitorNews(long userId, String monitorId, int queryType, int pageNo, int pageSize) {
        def monitor = monitorRepo.getCopyrightMonitor(userId, monitorId)
        if (!monitor) {
            return apiResult([status: HttpStatus.SC_BAD_REQUEST, msg: '监控信息错误'])
        }
        def newsList = null;
        def whiteList = []
        def blackList = [];
        monitor.whiteList.each {
            whiteList << "." + stripUrl(it)
        }
        monitor.blackList.each {
            blackList << "." + stripUrl(it)
        }
        if (monitor != null) {
            newsList = monitorRepo.getCopyrightMonitorNews(userId, monitor.monitorId, queryType, pageNo, pageSize)
        }
        return apiResult([status: HttpStatus.SC_OK, msg: newsList])
    }

    Map removeCopyrightMonitorNews(long userId, String monitorId, String newsIds) {
        def newsIdList = newsIds.split(",")
        boolean result = monitorRepo.removeCopyrightMonitorNews(userId, monitorId, newsIdList as List)
        if (result) {
            return apiResult([status: HttpStatus.SC_OK, msg: "删除成功"])
        } else {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "删除失败"])
        }
    }

    Map getAnalysisSummary(Long userId, String monitorId) {
        long reprintMediaCount = monitorRepo.getReprintMediaCount(userId, monitorId);
        long reprintCount = monitorRepo.getReprintCounts(userId, monitorId);
        long searchEngineCount = 0;
        long keyChannelCount = monitorRepo.getKeyChannelCount(userId, monitorId);
        long weiboReprintCount = monitorRepo.getWeiboReprintCount(userId, monitorId);
        long weMediaReadCount = monitorRepo.getWeMediaReadCount(userId, monitorId);
        long commentCount = monitorRepo.getCommentCount(userId, monitorId);
        long likeCount = monitorRepo.getLikeCount(userId, monitorId);
        long reprintMediaAverage = 100;
        long reprintAverage = 100;
        long searchEngineAverage = 5;
        long keyChannelAverage = 344;
        long weiboReprintAverage = 5000;
        long weMediaReadAverage = 100000;
        long commentAverage = 500;
        long likeAverage = 1000;
        def result = ["reprintMediaCount"  : reprintMediaCount,
                      "reprintCount"       : reprintCount,
                      "searchEngineCount"  : searchEngineCount,
                      "keyChannelCount"    : keyChannelCount,
                      "weiboReprintCount"  : weiboReprintCount,
                      "weMediaReadCount"   : weMediaReadCount,
                      "commentCount"       : commentCount,
                      "likeCount"          : likeCount,
                      "reprintMediaAverage": reprintMediaAverage,
                      "reprintAverage"     : reprintAverage,
                      "searchEngineAverage": searchEngineAverage,
                      "keyChannelAverage"  : keyChannelAverage,
                      "weiboReprintAverage": weiboReprintAverage,
                      "weMediaReadAverage" : weMediaReadAverage,
                      "commentAverage"     : commentAverage,
                      "likeAverage"        : likeAverage]
        return apiResult([status: HttpStatus.SC_OK, msg: result])
    }

    Map getAnalysisReprintTrend(Long userId, String monitorId) {
        Monitor monitor = monitorRepo.getCopyrightMonitorById(monitorId)
        def startTime = DateUitl.getBeginDayOfParm(monitor.startDate).getTime()
        Map resultMap = [:]
        for (int i = 0; i < 30; i++) {
            resultMap.put((startTime + (i * 24 * 60 * 60 * 1000L)) as String, 0)
        }
        Map result = monitorRepo.getAnalysisReprintTrend(userId, monitorId);
        def sdf = new SimpleDateFormat('yyyy-MM-dd')
        result.each { key, value ->
            resultMap.put(sdf.parse(key).getTime().toString(), value)
        }
        def resultList = []
        resultMap.each { key, value ->
            resultList << ["date": key as long, "count": value as long]
        }
        return apiResult([status: HttpStatus.SC_OK, msg: resultList])
    }

    Map getAnalysisChannelReprintSummary(Long userId, String monitorId) {
        List<Map> result = monitorRepo.getAnalysisChannelReprintSummary(userId, monitorId);
        long totalCount = 0;
        for (int i = 0; i < result.size(); i++) {
            Map map = result.get(i)
            totalCount += map.get("count")
        }
        List<Map> res = []
        for (int i = 0; i < result.size(); i++) {
            Map map = result.get(i)
            String channel = map.get("channelName")
            String name = "其他"
            switch (channel) {
                case "1":
                    name = "新闻";
                    break;
                case "2":
                    name = "论坛";
                    break;
                case "3":
                    name = "博客";
                    break;
                case "4":
                    name = "微博";
                    break;
                case "5":
                    name = "平媒";
                    break;
                case "6":
                    name = "微信";
                    break;
                case "7":
                    name = "视频";
                    break;
                case "8":
                    name = "长微博";
                    break;
                case "9":
                    name = "APP";
                    break;
                case "10":
                    name = "评论";
                    break;
                default:
                    name = "其他";
                    break;
            }
            map.put("channelName", name)
            map.put("percentage", (map.get("count")) * 1.0 / totalCount)
            res.add(map)
        }
        return apiResult([status: HttpStatus.SC_OK, msg: res])
    }

    int getCopyrightMonitorCount(long userId) {
        return monitorRepo.getCopyrightMonitorCount(userId)
    }

    Map getCopyrightMonitors(long userId) {
        List<Monitor> list = monitorRepo.getCopyrightMonitors(userId)
//        def monitorIds = []
//        list.each {
//            monitorIds << it.monitorId
//        }
//        def tortCountMap = monitorRepo.getNewsTortCounts(userId, monitorIds)
//        def rep = [:]
//        rep.list = []
//        list.each {
//            rep.list << [
//                    monitorId   : it.monitorId,
//                    userId      : it.userId,
//                    title       : it.title,
//                    url         : it.url,
//                    author      : it.author,
//                    startDate   : it.startDate,
//                    media       : it.media,
//                    tortCount   : tortCountMap.get(it.monitorId) ?: 0,
//                    reprintCount: getReprintCount(it),   // 转发数
//                    isTort      : false,       // 为true, 表示疑似侵权
//                    blackList   : it.blackList,
//                    whiteList   : it.whiteList,
//            ]
//        }
        def rep = [list: list]
        return apiResult([status: HttpStatus.SC_OK, msg: rep])
    }

    Map getCopyrightMonitor(long userId, String monitorId) {
        def rep = []
        def it = monitorRepo.getCopyrightMonitor(userId, monitorId)
        if (it) {
            rep = [
                    monitorId      : it.monitorId,
                    userId         : it.userId,
                    title          : it.title,
                    url            : it.url,
                    author         : it.author,
                    startDate      : it.startDate,
                    media          : it.media,
                    contentAbstract: it.contentAbstract,
                    reprintCount   : getReprintCount(it),
                    tortCount      : monitorRepo.getMonitorTortCounts(userId, monitorId) ?: 0,
                    blackList      : it.blackList,
                    whiteList      : it.whiteList,
            ]
        }
        return apiResult([status: HttpStatus.SC_OK, msg: rep]);
    }

    public static String strToDate(String s) {
        //1496321853000
        //20170601142139
        String res;
        if (s.length() == 14) {
            res = s.substring(0, 4) + "-" + s.substring(4, 6) + "-" + s.substring(6, 8) + " " + s.substring(8, 10) + ":" + s.substring(10, 12) + ":" + s.substring(12, 14);
        } else if (s.length() == 13) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            long lcc_time = Long.valueOf(s);
            res = simpleDateFormat.format(new Date(lcc_time));
        } else {
            res = "";
        }
        return res;
    }
    /*竞品分析-疑似侵权/全部-excel导出*/

    Map copyrightMonitorNewsExcelOut(List copyRightNewsList) {
        String outfileName = UUID.randomUUID();
        ExportExcel ex = new ExportExcel();
        String sheetName = "";//下载文件的默认名字,sheet页名字
        String fileName = outfileName
        String headers = "标题,内容,转载媒体,转载时间,";//表头
        String selname = "title,contentAbstract,source,createTime,o"//标题对应key值

//        查询需要导出的数据
        List<Map<String, Object>> copyrightMonitorNewsList = new ArrayList<Map<String, Object>>();
        copyRightNewsList.each {
            Map<String, Object> news = new HashMap<String, Object>();
            MonitorNews monitorNews = monitorRepo.getCopyrightMonitorNewsById(it)
            news.put("title", isNullOrNot(monitorNews.title));
            news.put("contentAbstract", isNullOrNot(monitorNews.contentAbstract));
            news.put("source", isNullOrNot(monitorNews.source));
            Date createTime = monitorNews.createTime;
            news.put("createTime", createTime == null ? "" : DateUtils.formatDate(createTime));

//            sheetName = isNullOrNot(monitorNews.title);
            copyrightMonitorNewsList.add(news);
        }
//        sheetName = (sheetName == null || "".equals(sheetName)) ? "疑似侵权信息导出" : sheetName;
        sheetName = "疑似侵权信息导出"
//以标题名作为导出文件名，如果标题为空就以“疑似侵权信息导出”为文件名

        HSSFWorkbook wb = ex.exportExcel(sheetName, headers, copyrightMonitorNewsList, selname);
//返回一个workbook即excel实例。datetime是sheet页的名称
        String excelFolder = "/${UPLOAD_PATH}/download/downLoadCopyMonitor/" + outfileName;
        def result = [
                status: HttpStatus.SC_OK,
                msg   : '',
                token : ''
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
        }
        byte[] content = os.toByteArray();
        OutputStream fos = null;
        File file = new File(excelFolder + "/" + outfileName + ".xls")
        try {
            fos = new FileOutputStream(file);
            fos.write(content);
            os.close();
            fos.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        //zip file

        if (fileName.length() > 30) {
            fileName = fileName.substring(0, 30)
        }
        def zipPath = file.getParent() + "/" + fileName + ".zip"
        ZipUitl.zip(file.getParent(), zipPath)
        result.msg = zipPath
        result.token = outfileName
        return result;

    }

    String getCopyrightMonitorNewsExcelByToken(String token) {
        def filePath = new File(UPLOAD_PATH, 'download').path
        def path = filePath.toString() + File.separator + "downLoadCopyMonitor" + File.separator + token
        List<String> fileList = ZipUitl.getFileNames(path)
        for (int i = 0; i < fileList.size(); i++) {
            if (fileList.get(i).endsWith(".zip")) {
                path = path + File.separator + fileList.get(i)
                return path
            }
        }
        return ""
    }

    CopyRightFilter getCopyRightFilter(long userId) {
        return monitorRepo.getCopyRightFilter(userId)
    }

    long getReprintCount(Monitor monitor) {
        return monitorRepo.getReprintCount(monitor.userId, monitor.monitorId)
    }

    public static String isNullOrNot(String str) {
        String strRe = "";
        strRe = (str == null ? "" : StringUtils.html2text(str))
        return strRe
    }

    Map modifyCopyRightFilter(long userId, String whiteList, String blackList) {
        JSONObject it = new JSONObject()
        JSONArray whiteListJson = JSONArray.parse(whiteList)
        JSONArray blackListJson = JSONArray.parse(blackList)
        def whiteSites = []
        def whiteSitesDomain = []
        def blackSites = []
        def blackSitesDomain = []
        //传入的黑白名单
        whiteListJson.each {
            whiteSites << [
                    websiteDomain: it.get("websiteDomain") ? String.valueOf(it.get("websiteDomain")) : "",
                    websiteName  : it.get("websiteName") ? String.valueOf(it.get("websiteName")) : ""
            ]
            whiteSitesDomain << (it.get("websiteDomain") ? String.valueOf(it.get("websiteDomain")) : "")
        }
        blackListJson.each {
            blackSites << [
                    websiteDomain: it.get("websiteDomain") ? String.valueOf(it.get("websiteDomain")) : "",
                    websiteName  : it.get("websiteName") ? String.valueOf(it.get("websiteName")) : ""
            ]
            blackSitesDomain << (it.get("websiteDomain") ? String.valueOf(it.get("websiteDomain")) : "")
        }
        if (blackSites.size() > 8) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "最多设置8个黑名单站点"])
        }
        if (whiteSites.size() > 8) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "最多设置8个白名单站点"])
        }
        //站点是否有重复的
        def allSites = []
        allSites = whiteSitesDomain + blackSitesDomain
        def oldSize = allSites.size()
        def newSize = (allSites.unique()).size()
        if (newSize < oldSize) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "不允许输入重复的站点"])
        }
        //判断黑白名单数目
        CopyRightFilter oldCopyRightFilter = getCopyRightFilter(userId)
        if (oldCopyRightFilter) {
            def oldWhiteSitesDomain = []
            def oldBlackSitesDomain = []
            if (oldCopyRightFilter) {
                if (oldCopyRightFilter.whiteList) {
                    oldCopyRightFilter.whiteList.each {
                        oldWhiteSitesDomain << it.websiteDomain
                    }
                }
                if (oldCopyRightFilter.blackList) {
                    oldCopyRightFilter.blackList.each {
                        oldBlackSitesDomain << it.websiteDomain
                    }
                }
            }
            //校验添加的数据是否合法,黑白名单的值不能有交集，各自不能有重复值，对每个用户而言，黑白名单的配置就是一条记录
            //        Map result = chooseCopyRightFilters(oldCopyRightFilter,whiteSites,blackSites)
            //删除的数据是否在使用中

            for (int i = 0; i < oldWhiteSitesDomain.size(); i++) {
                def whiteSite = oldWhiteSitesDomain.get(i)
                if (!whiteSitesDomain.find { it.equals(whiteSite) }) {
                    if (monitorRepo.isMonitoringInWhiteList(userId, whiteSite)) {
                        return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "站点监控中不能删除或编辑"])
                    }
                }
            }
            for (int i = 0; i < oldBlackSitesDomain.size(); i++) {
                def blackSite = oldBlackSitesDomain.get(i)
                if (!blackSitesDomain.find { it.equals(blackSite) }) {
                    if (monitorRepo.isMonitoringInBlackList(userId, blackSite)) {
                        return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "站点监控中不能删除或编辑"])
                    }
                }
            }
        }

        CopyRightFilter newCopyRightFilter = new CopyRightFilter()
        if (oldCopyRightFilter) {
            newCopyRightFilter.id = oldCopyRightFilter.id
        }
        newCopyRightFilter.userId = userId
        newCopyRightFilter.whiteList = whiteSites
        newCopyRightFilter.blackList = blackSites
        newCopyRightFilter.updateTime = new Date()
        newCopyRightFilter.createTime = new Date()
        boolean flag = monitorRepo.modifyCopyRightFilter(newCopyRightFilter)
        if (flag) {
            return apiResult([status: HttpStatus.SC_OK, msg: "保存成功"])
        }
    }

    void autoCopyrightMonitor() {
        //获取版权监控列表
        def pageSize = 10;
        def monitorPageNo = 1;
        while (true) {
            List copyrightMonitors = monitorRepo.getActiveCopyrightMonitors(monitorPageNo, pageSize)
            if (copyrightMonitors == null || copyrightMonitors.size() == 0) {
                break
            }
            def monitorSize = copyrightMonitors.size()
            for (int i = 0; i < monitorSize; i++) {
                try {
                    processMonitorNews(copyrightMonitors.get(i))
                } catch (Exception e) {
                    log.error("处理版权监控错误:{}：{}", copyrightMonitors.get(i), e.getMessage())
                }
            }
            monitorPageNo++
        }
    }

    private void processMonitorNews(Monitor copyrightMonitor) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        def currDate = new Date();

        def whiteList = []
        copyrightMonitor.whiteList.each {
            whiteList << "." + stripUrl(it)
        }
        def blackList = []
            copyrightMonitor.blackList.each {
            blackList << "." + stripUrl(it)
        }
        //处理每一个copyright
        CopyrightMonitorLog copyrightMonitorLog = monitorRepo.getCopyrightMonitorLogById(copyrightMonitor.monitorId)
        if (copyrightMonitorLog == null || copyrightMonitorLog.monitorId == null) {
            if (copyrightMonitorLog == null) {
                copyrightMonitorLog = new CopyrightMonitorLog()
            }
            copyrightMonitorLog.monitorId = copyrightMonitor.monitorId
            copyrightMonitorLog.prevStartTime = copyrightMonitor.startDate ? simpleDateFormat.format(copyrightMonitor.startDate) : "19700101000001"
            copyrightMonitorLog.prevEndTime = copyrightMonitor.startDate ? simpleDateFormat.format(copyrightMonitor.startDate) : "19700101000001"
            copyrightMonitorLog.updateTime = currDate
            copyrightMonitorLog.createTime = currDate
            monitorRepo.addCopyrightMonitorLog(copyrightMonitorLog)
        } else {
            if (copyrightMonitorLog.prevEndTime >= simpleDateFormat.format(copyrightMonitor.endDate ?: new Date()) || currDate > (copyrightMonitor.endDate ?: new Date())) {
                //update copyrightMonitor to complete
                monitorRepo.updateCopyrightMonitorToComplete(copyrightMonitor.monitorId)
                return
            }
        }

        def endTime = simpleDateFormat.format(copyrightMonitor.endDate ?: new Date())
        def currDkTime = copyrightMonitorLog.prevEndTime
        while (true) {
            List monitorNewsList = new ArrayList<CopyrightMonitorNews>()
            def newsList = captureApi.getPagingCopyrightMonitorNewsByTime(copyrightMonitor.title as String, 1, 100, currDkTime, endTime)
            if (newsList.list.size() == 0) {
                break
            }
            newsList.list.each {
                MonitorNews copyrightMonitorNews = new MonitorNews()
                def compare = JaccardDistanceUtils.computeJaccardDistance(copyrightMonitor.title, it.title)
                if (compare > 0.8) {
                    it.isTort = true
                } else {
                    it.isTort = false
                }
                String suffix = Md5Util.md5(it.url).substring(0, 1)
                String id = Md5Util.md5(it.url)
                long commentCount = 0
                long visitCount = 0
                long reprintCount = 0
                long likeCount = 0
                if (it.newsType == 1) {
                    commentCount = newsRepo.getNewsCommentCount(suffix, id)
                } else if (it.newsType == 4 || it.newsType == 401 || it.newsType == 402 || it.newsType == 8) {
                    Map newsMap = newsRepo.getNewsWithoutContentById(id)
                    if (newsMap) {
                        commentCount = newsMap.commentCount?:0;
                        visitCount = newsMap.visitCount?:0;
                        reprintCount = newsMap.reprintCount?:0;
                        likeCount = newsMap.likeCount?:0;
                        if (visitCount == 0) {
                            visitCount = commentCount * (new Random().nextInt(20)) + (new Random().nextInt(10) + 1)
                        }
                    }
                }

                copyrightMonitorNews.commentCount = commentCount
                copyrightMonitorNews.visitCount = visitCount
                copyrightMonitorNews.reprintCount = reprintCount
                copyrightMonitorNews.likeCount = likeCount
                copyrightMonitorNews.userId = copyrightMonitor.userId
                copyrightMonitorNews.monitorId = copyrightMonitor.monitorId
                copyrightMonitorNews.newsId = it.newsId
                copyrightMonitorNews.isTort = it.isTort
                copyrightMonitorNews.title = it.title
                copyrightMonitorNews.source = it.source
                copyrightMonitorNews.author = it.author
                copyrightMonitorNews.url = it.url
                copyrightMonitorNews.contentAbstract = it.contentAbstract
                copyrightMonitorNews.site = it.newsType.equals("6") ? it.source : it.site
                copyrightMonitorNews.newsType = it.newsType
                copyrightMonitorNews.Ctime = it.createTime
                copyrightMonitorNews.DkTime = it.dkTime
                copyrightMonitorNews.createTime = new Date()
                copyrightMonitorNews._id = copyrightMonitorNews.createId()
                String strUrl = "." + stripUrl(it.url)
                copyrightMonitorNews.isWhite = false;
                copyrightMonitorNews.isBlack = false;
                def domain = UrlUtils.getDomainFromUrl(it.url)
                if (keyChannelMap.containsKey(domain)) {
                    copyrightMonitorNews.isKeyChannel = true
                } else {
                    copyrightMonitorNews.isKeyChannel = false
                }
                for (int i = 0; i < whiteList.size(); i++) {
                    if (strUrl.contains(whiteList.get(i))) {
                        copyrightMonitorNews.isWhite = true;
                        break
                    }
                }
                for (int i = 0; i < blackList.size(); i++) {
                    if (strUrl.contains(blackList.get(i))) {
                        copyrightMonitorNews.isBlack = true;
                        break
                    }
                }
                monitorNewsList.add(copyrightMonitorNews)
                currDkTime = copyrightMonitorNews.DkTime
            }
            //插入copyrightMonitorNews
            monitorRepo.addCopyrightMonitorNews(monitorNewsList)
            // 更新copyrightMonitorLog
            copyrightMonitorLog.prevStartTime = copyrightMonitorLog.prevEndTime
            copyrightMonitorLog.prevEndTime = currDkTime
            monitorRepo.modifyCopyrightMonitorLog(copyrightMonitorLog)
            if (copyrightMonitorLog.prevEndTime >= simpleDateFormat.format(copyrightMonitor.endDate ?: new Date())) {
                monitorRepo.updateCopyrightMonitorToComplete(copyrightMonitor.monitorId)
                break
            }
            if (newsList.list.size() < 100) {
                break
            }
        }
    }
    //查询全部用户创建版权监控文章明细
    List queryAllMonitors(Date startDate, Date endDate) {
        List<Account> validUsers = accountRepo.getValidUsers();
        List subjects = new ArrayList<>();
        def sdf = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss')
        for (int i = 0; i < validUsers.size(); i++) {
            Account account = validUsers.get(i)
            long userId = account.getId();
            String userName = account.getUserName();
            String realName = account.getRealName();
            String company = account.getCompany();
            List<Monitor> monitorList = monitorRepo.getCopyrightMonitorInDate( userId ,startDate, endDate)
            if (monitorList.size()>0){
            List<Map> mapList = new ArrayList<>()
            for (int j = 0; j < monitorList.size(); j++) {
                Monitor monitor = monitorList.get(j).toMap();
                Map map = new HashMap<>();
                map.id=monitor.monitorId;
                map.userId=userId;
                map.userName=userName;
                map.realName=realName;
                map.company=company;
                map.title=monitor.getTitle()
                map.media=monitor.getMedia()
                map.updateTime=monitor.getUpdateTime()
                mapList.add(map);
            }
            subjects.addAll(mapList);
            }
        }
        return subjects;
    }

    //生成报表
    void excelOut(List subjectList){
        String outfileName = UUID.randomUUID();
        ExportExcel ex = new ExportExcel();
        String sheetName = "导出版权监控表";//下载文件的默认名字,sheet页名字
        String fileName = outfileName
        String headers = "id,userId,userName,realName,company,title,media,updateTime";
//表头
        String selname = "id,userId,userName,realName,company,title,media,updateTime,o"
//标题对应key值

        HSSFWorkbook wb = ex.exportExcel(sheetName, headers, subjectList, selname);
//返回一个workbook即excel实例。datetime是sheet页的名称
        String excelFolder = "D:\\"+sheetName;
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
        }
        byte[] content = os.toByteArray();
        OutputStream fos = null;
        File file = new File(excelFolder + "/" + "excel.xls")

        try {
            fos = new FileOutputStream(file);
            fos.write(content);
            os.close();
            fos.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        println("默认生成目录在 D:\\"+sheetName);
    }

    //查询全部用户同步稿件总量及明细
    List queryAllArticleOperation(Date startDate, Date endDate) {
        List<Account> validUsers = accountRepo.getValidUsers();
        List subjects = new ArrayList<>();
        def sdf = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss')
        for (int i = 0; i < validUsers.size(); i++) {
            Account account = validUsers.get(i)
            long userId = account.getId();
            String userName = account.getUserName();
            String realName = account.getRealName();
            String company = account.getCompany();
            int operationType=2; //同步狀態碼 2
            List<ArticlePush> articlePushList = materialRepo.getArticleOperationInDate( userId ,operationType,startDate, endDate)
            if (articlePushList.size()>0){
                List<Map> mapList = new ArrayList<>()
                for (int j = 0; j < articlePushList.size(); j++) {
                    ArticlePush articlePush = articlePushList.get(j).toMap();
                    Map map = new HashMap<>();
                    map.id=articlePush.id;
                    map.userId=userId;
                    map.userName=userName;
                    map.realName=realName;
                    map.company=company;
                    Material material=null;
                    material=articlePush.getMaterial()
                    map.title=material.getTitle()
                    map.contentAbstract=material.getContentAbstract()
                    map.keywords=material.getKeywords()
                    map.updateTime=articlePush.updateTime
                    mapList.add(map);
                }
                subjects.addAll(mapList);
            }
        }
        return subjects;
    }

    //生成报表用戶同步稿件的
    void excelOutArticleOperation(List subjectList){
        String outfileName = UUID.randomUUID();
        ExportExcel ex = new ExportExcel();
        String sheetName = "导出同步稿件表";//下载文件的默认名字,sheet页名字
        String fileName = outfileName
        String headers = "id,userId,userName,realName,company,title,contentAbstract,keywords,updateTime";
//表头
        String selname = "id,userId,userName,realName,company,title,contentAbstract,keywords,updateTime,o"
//标题对应key值

        HSSFWorkbook wb = ex.exportExcel(sheetName, headers, subjectList, selname);
//返回一个workbook即excel实例。datetime是sheet页的名称
        String excelFolder = "D:\\"+sheetName;
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
        }
        byte[] content = os.toByteArray();
        OutputStream fos = null;
        File file = new File(excelFolder + "/" + "excel.xls")

        try {
            fos = new FileOutputStream(file);
            fos.write(content);
            os.close();
            fos.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        println("默认生成目录在 D:\\"+sheetName);
    }
}