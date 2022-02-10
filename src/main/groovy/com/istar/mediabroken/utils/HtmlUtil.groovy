package com.istar.mediabroken.utils

import groovy.util.logging.Slf4j
import org.htmlparser.Parser
import org.htmlparser.Tag
import org.htmlparser.Text
import org.htmlparser.visitors.NodeVisitor

/**
 * Author: Luda
 * Time: 2017/10/13
 */
@Slf4j
class HtmlUtil {
    static List removeDuplicateParagraph(String htmlStr) {
        if (!htmlStr) {
            return ""
        }
        htmlStr = htmlStr.replace("\r\n" as CharSequence, "<br/>" as CharSequence)
        htmlStr = htmlStr.replace("\r" as CharSequence, "<br/>" as CharSequence)
        Parser parser = Parser.createParser(htmlStr, "utf-8")

        def stringTarget = []
        def htmlList = []
        def newsLineTag = ["p", "/p", "ul", "/ul", "ol", "/ol", "li", "/li", "div", "/div"
                           , "h1", "/h1", "h2", "/h2", "h3", "/h3", "h4", "/h4", "h5", "/h5", "h6", "/h6"
                           , "br", "br/", "table", "/table", "menu", "/menu", "hr", "hr/", "form", "/form"
        ]
        NodeVisitor visitor = new NodeVisitor(true, true) {

            public void visitTag(Tag tag) {
                if (tag.getText().startsWith("img ")) {
                    htmlList.add(["type": "Tag", "ind": tag.getText().split(" ")[0], "value": "<" + tag.getText() + ">", "imgData": tag.getAttribute("src")])
                } else if (tag.getText().startsWith("video ")){
                    htmlList.add(["type": "Tag", "ind": tag.getText().split(" ")[0], "value": "<" + tag.getText() + ">", "videoData": tag.getAttribute("src")])
                } else {
                    htmlList.add(["type": "Tag", "ind": tag.getText().split(" ")[0], "value": ""])
                }

            }

            public void visitStringNode(Text string) {
                htmlList.add(["type": "Text", "ind": "Text", "value": string.text])
            }
        }

        parser.visitAllNodesWith(visitor)

        List htmlTarget = []
        for (int i = 0; i < htmlList.size(); i++) {
            def curr = htmlList.get(i)
            if ("Tag".equals(curr.type)) {
                if ("img".equals(curr.ind)) {
                    if (!stringTarget.contains(curr.imgData)) {
                        htmlTarget.add(curr.value)
                        stringTarget.add(curr.imgData)
                    }
                } else if ("video".equals(curr.ind)) {
                    if (!stringTarget.contains(curr.videoData)) {
                        htmlTarget.add(curr.value)
                        stringTarget.add(curr.videoData)
                    }
                }else {
                    if (newsLineTag.contains(curr.ind)) {
                        if (htmlTarget.size() > 0 && (!htmlTarget.get(htmlTarget.size() - 1).equals("<br/>"))) {
                            htmlTarget.add("<br/>")
                        }
                    }
                }
            }
            if ("Text".equals(curr.type)) {
                def currStr = ""
                if (htmlTarget.size() > 0 && (!htmlTarget.get(htmlTarget.size() - 1).equals("<br/>")) && (!htmlTarget.get(htmlTarget.size() - 1).startsWith("<img src="))) {
                    def prevStr = htmlTarget.get(htmlTarget.size() - 1)
                    currStr = prevStr + curr.value
                    if (!isExistInTarget(currStr, stringTarget)) {
                        htmlTarget.set(htmlTarget.size() - 1, currStr)
                        stringTarget.set(stringTarget.size() - 1, currStr)
                    }
                } else {
                    if (!isExistInTarget(curr.value, stringTarget)) {
                        htmlTarget.add(curr.value)
                        stringTarget.add(curr.value)
                    }
                }

            }
        }
        return htmlTarget
    }

    static boolean isExistInTarget(String source, List targetList) {
        if (!source) {
            return true
        }
        if (!targetList) {
            return false
        }
        for (int i = 0; i < targetList.size(); i++) {
            String currString = targetList.get(i)
            if (JaccardDistanceUtils.computeJaccardDistance(source, currString) > 0.8) {
                return true
            }
        }
        return false
    }

    static String removeWebHtmlFormat(String htmlStr) {
        if (!htmlStr) {
            return ""
        }else {
            htmlStr = htmlStr.replaceAll("\\<style[\\s\\S]*?\\</style>", "")
        }
        try {
            Parser parser = Parser.createParser(htmlStr, "utf-8");
            def newsLineTag = ["p", "/p", "ul", "/ul", "ol", "/ol", "li", "/li", "div", "/div"
                               , "h1", "/h1", "h2", "/h2", "h3", "/h3", "h4", "/h4", "h5", "/h5", "h6", "/h6"
                               , "br", "br/", "table", "/table", "menu", "/menu", "hr", "hr/", "form", "/form"
            ]
            def htmlContent = new StringBuffer()
            NodeVisitor visitor = new NodeVisitor(true, true) {

                public void visitTag(Tag tag) {
                    if (tag.getText().startsWith("img ")) {
                        htmlContent.append("<br/><" + tag.getText() + "><br/>")
                    } else {
                        if (newsLineTag.contains(tag.getText().split(" ")[0])) {
                            if (htmlContent.length() > 0 && (-1 == htmlContent.indexOf("<br/>", htmlContent.length() - 5))) {
                                htmlContent.append("<br/>")
                            }
                        }
                    }
                }

                public void visitStringNode(Text string) {
                    htmlContent.append(string.text.replace("\r", "").replace("\n", "").trim())
                }
            }
            parser.visitAllNodesWith(visitor)
            parser = null
            List htmlNodeList = htmlContent.toString().split("<br/>")
            int nodeLen = htmlNodeList.size()
            def html = new StringBuffer()
            for (int i = 0; i < nodeLen; i++) {
                String currStr = htmlNodeList.get(i)
                if (currStr) {
                    html.append("<p>")
                    html.append(currStr)
                    html.append("</p>")
                }
            }
            return html.toString()
        } catch (Exception e) {
            log.error("转换html出错" + htmlStr, e)
            return ""
        }
    }

}


