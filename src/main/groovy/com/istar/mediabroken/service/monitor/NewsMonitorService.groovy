package com.istar.mediabroken.service.monitor

import com.istar.mediabroken.entity.capture.Site
import com.istar.mediabroken.entity.capture.SiteDetail
import com.istar.mediabroken.service.capture.SiteService
import com.istar.mediabroken.utils.DateUitl
import com.istar.mediabroken.utils.MailUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

import javax.mail.MessagingException
/**
 * @author hanhui
 * @date 2018/4/23 15:43
 * @desc 新闻数据监控服务
 * */
@Service
public class NewsMonitorService {
    @Autowired
    SiteService siteService;
    @Value('${base.sites.monitoring.mail}')
    String[] mailTo

    public void newsMonitor() {
        StringBuffer info = new StringBuffer("");
        getNoNewsSite(info)
        if (info) {
            try {
                String msg = "数据监控服务发现以下站点48小时内无数据：\n"
                MailUtil.sendMail(mailTo, "基础站点数据监控服务", msg + info.toString() + "请检查！");
            } catch (MessagingException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    private void getNoNewsSite(StringBuffer info) {
        List<SiteDetail> siteDetailList = siteService.getAllSiteDetail()
        String newLine = System.getProperty("line.separator");
        Site site = null
        String siteType = null
        String siteName = null
        String url = null
        String siteDomain = null
        for (SiteDetail detail : siteDetailList) {
            site = new Site()
            siteType = detail.getSiteType()
            siteName = detail.getSiteName().replace("\u00A0", "")
            url = detail.getUrl()
            siteDomain = detail.getSiteDomain()
            switch (siteType) {
                case "网站":
                    site.setSiteType(Site.SITE_TYPE_WEBSITE)
                    site.setWebsiteDomain(url)
                    break;
                case "微信公众号":
                    site.setSiteType(Site.SITE_TYPE_WECHAT)
                    site.setSiteName(siteName)
                    break;
                case "微博":
                    site.setSiteType(Site.SITE_TYPE_WEIBO)
                    site.setSiteName(siteName)
                    break;
            }
            Map data = siteService.getRecentNewsBySite(site)
            if (data) {
                Date captureTime = DateUitl.convertEsDate(data.get("captureTime"))
                int difference = DateUitl.getHourDistance(captureTime, new Date())
                if (difference > 48) {
                    appendInfo(siteType, info, siteName, url, siteDomain, newLine)
                }
            } else {
                appendInfo(siteType, info, siteName, url, siteDomain, newLine)
            }
        }
    }

    private void appendInfo(String siteType, StringBuffer info, String siteName, String url, String siteDomain, String newLine) {
        if ("网站".equals(siteType)) {
            info.append(siteType + "  " + siteName + "  " + url + "  " + siteDomain + newLine)
        } else {
            info.append(siteType + "  " + siteName + newLine)
        }
    }
}
