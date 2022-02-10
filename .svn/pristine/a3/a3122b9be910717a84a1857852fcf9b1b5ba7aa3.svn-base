package com.istar.mediabroken.service.compile

import com.istar.mediabroken.entity.AccountProfile
import com.istar.mediabroken.entity.SystemSetting
import com.istar.mediabroken.repo.account.AccountRepo
import com.istar.mediabroken.repo.admin.SettingRepo
import groovy.util.logging.Slf4j
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import static com.istar.mediabroken.api.ApiResult.apiResult

/**
 * 一键排版相关
 * Author: zc
 * Time: 20180419
 */
@Service
@Slf4j
class ArticleTemplateService {
    @Autowired
    AccountRepo accountRepo
    @Autowired
    SettingRepo settingRepo

    Map getTemplateNames(long userId) {
        AccountProfile accountProfile = accountRepo.getAccountProfileByUser(userId)
        def articleTemplates = accountProfile.articleTemplate;
        def templateNames = []
        def settingList = []
        if (!articleTemplates) {
            SystemSetting systemSetting = settingRepo.getSystemSetting("compile", "articleTemplate")
            if (systemSetting) {
                def standardTemplate = systemSetting.content
                articleTemplates << standardTemplate
                accountProfile.articleTemplate = articleTemplates
                accountProfile.updateTime = new Date()
                accountRepo.saveAccountProfile(accountProfile)
            }
        }
        articleTemplates.each {
            if (it.templateName) {
                templateNames << it.templateName
                settingList << it
            }
        }
        return apiResult([status: HttpStatus.SC_OK, templateNameList: templateNames, settingList: settingList])
    }

    Map saveTemplate(long userId, int index, String templateName, String bodyFontSize, String bodyFontFamily, String bodyAlignment, String bodyImgAlignment, String wordSpace, String firstLineIndent, String lineSpace, String textIndent, String beforeParagraphSpace, String afterParagraphSpace) {
        AccountProfile accountProfile = accountRepo.getAccountProfileByUser(userId)
        List articleTemplates = accountProfile.articleTemplate;
        List updateTemplates = []
        def updateTemplate = [
                templateName        : templateName,
                bodyFontSize        : bodyFontSize,
                bodyFontFamily      : bodyFontFamily,
                bodyAlignment       : bodyAlignment,
                bodyImgAlignment    : bodyImgAlignment,
                wordSpace           : wordSpace,
                firstLineIndent     : firstLineIndent,
                lineSpace           : lineSpace,
                textIndent          : textIndent,
                beforeParagraphSpace: beforeParagraphSpace,
                afterParagraphSpace : afterParagraphSpace
        ]
        if (index < 3) {
            if (index + 1 <= articleTemplates.size()) {
                for (int i = 0; i < articleTemplates.size(); i++) {
                    if (i != index) {
                        updateTemplates << articleTemplates.get(i)
                    } else {
                        updateTemplates << updateTemplate
                    }
                }
            } else {
                updateTemplates = articleTemplates
                for (int i = articleTemplates.size(); i < index + 1; i++) {
                    if (i == index) {
                        updateTemplates << updateTemplate
                    } else {
                        updateTemplates << [:]
                    }
                }
            }
            accountProfile.articleTemplate = updateTemplates
            accountProfile.updateTime = new Date()
            accountRepo.saveAccountProfile(accountProfile)
        } else {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "最多保存3个模板"])
        }
        return apiResult([status: HttpStatus.SC_OK, msg: "保存成功"])
    }

    Map getTemplateByIndex(long userId, int index) {
        AccountProfile accountProfile = accountRepo.getAccountProfileByUser(userId)
        List articleTemplates = accountProfile.articleTemplate;
        def result = []
        if (index + 1 <= articleTemplates.size()) {
            result = articleTemplates.get(index)
        }
        return apiResult([status: HttpStatus.SC_OK, template: result])
    }
}
