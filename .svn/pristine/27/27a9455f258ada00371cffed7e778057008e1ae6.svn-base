package com.istar.mediabroken.api.compile

import com.istar.mediabroken.api.CurrentUserId
import com.istar.mediabroken.service.compile.ArticleTemplateService
import groovy.util.logging.Slf4j
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import static com.istar.mediabroken.api.ApiResult.apiResult
import static org.springframework.web.bind.annotation.RequestMethod.*

/**
 * 一键排版相关接口
 * Author: zc
 * Time: 20180419
 */
@RestController
@Slf4j
@RequestMapping(value = "/api/compile")
class ArticleTemplateApiController {
    @Autowired
    ArticleTemplateService articleTemplateService

    /**
     * 获取模板名称及模板的配置
     * @param userId
     * @return
     */
    @RequestMapping(value = "/templateNames", method = GET)
    public Map getTemplateNames(
            @CurrentUserId userId
    ) {
        def result = articleTemplateService.getTemplateNames(userId)
        return result
    }

    /**
     * 保存（添加、编辑）
     * @param userId
     * @param index
     * @param templateName
     * @param bodyFontSize 字号
     * @param bodyFontFamily 字体
     * @param bodyAlignment
     * @param bodyImgAlignment
     * @param wordSpace 字间距
     * @param firstLineIndent
     * @param lineSpace 行间距
     * @param textIndent 两端缩进
     * @param beforeParagraphSpace 段前距
     * @param afterParagraphSpace 段后距
     * @return
     */
    @RequestMapping(value = "/template/{index}", method = PUT)
    public Map saveTemplate(
            @CurrentUserId userId,
            @PathVariable(value = "index") int index,
            @RequestParam(value = "templateName", required = false, defaultValue = "推荐模板") String templateName,
            @RequestParam(value = "bodyFontSize", required = true) String bodyFontSize,
            @RequestParam(value = "bodyFontFamily", required = false, defaultValue = "微软雅黑,Microsoft YaHei") String bodyFontFamily,
            @RequestParam(value = "bodyAlignment", required = true) String bodyAlignment,
            @RequestParam(value = "bodyImgAlignment", required = true) String bodyImgAlignment,
            @RequestParam(value = "wordSpace", required = true) String wordSpace,
            @RequestParam(value = "firstLineIndent", required = true) String firstLineIndent,
            @RequestParam(value = "lineSpace", required = true) String lineSpace,
            @RequestParam(value = "textIndent", required = true) String textIndent,
            @RequestParam(value = "beforeParagraphSpace", required = true) String beforeParagraphSpace,
            @RequestParam(value = "afterParagraphSpace", required = true) String afterParagraphSpace
    ) {
        //校验
        /**
         推荐模板中在输入框中显示默认填写，剩下两个自定义框，输入框中无内容，
         但用灰字提示可输入的内容限制范围：只能填写数字，其中字间距和行间距支持两位小数输入，
         其他需输入整数，只输入数字即可，单位在输入框外。
         字号：6~48px
         字间距：0~5px
         行间距：0~5倍
         两端缩进：0~48px
         段前距：0~25px
         段后距：0~25px
         如果用户输入内容不符合规定，错误的内容编辑框标红，在保存内容时提示：输入的部分内容不符合标准，请重新输入。
         */
        try {
            Integer bodyFontSizeInt = Integer.valueOf(bodyFontSize)
            Double wordSpaceDouble = Double.valueOf(wordSpace)
            Double lineSpaceDouble = Double.valueOf(lineSpace)
            Integer textIndentInt = Integer.valueOf(textIndent)
            Integer beforeParagraphSpaceInt = Integer.valueOf(beforeParagraphSpace)
            Integer afterParagraphSpaceInt = Integer.valueOf(afterParagraphSpace)
            if(bodyFontSizeInt < 6 || bodyFontSizeInt >48 || wordSpaceDouble < 0.0 || wordSpaceDouble > 5.0 || wordSpaceDouble.toString().split("\\.")[1].length()>2
                    || lineSpaceDouble < 0.0 || lineSpaceDouble > 5.0 || lineSpaceDouble.toString().split("\\.")[1].length()>2 || textIndentInt < 0 || textIndentInt > 48
                    || beforeParagraphSpaceInt < 0 || beforeParagraphSpaceInt >25 || afterParagraphSpaceInt < 0 || afterParagraphSpaceInt >25 ){
                return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "输入的部分内容不符合标准，请重新输入。"])
            }
        }catch (Exception e){
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "输入的部分内容不符合标准，请重新输入。"])
        }
        def result = articleTemplateService.saveTemplate(userId, index, templateName, bodyFontSize, bodyFontFamily, bodyAlignment, bodyImgAlignment, wordSpace, firstLineIndent, lineSpace, textIndent, beforeParagraphSpace, afterParagraphSpace)
        return result
    }

    /**
     * 获取第index个模板的配置
     * @param userId
     * @param index
     * @return
     */
    @RequestMapping(value = "/template/{index}", method = GET)
    public Map getTemplateByIndex(
            @CurrentUserId userId,
            @PathVariable(value = "index") int index
    ) {
        def result = articleTemplateService.getTemplateByIndex(userId, index)
        return result
    }
}
