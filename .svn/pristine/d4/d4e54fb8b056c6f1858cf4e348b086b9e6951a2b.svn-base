package com.istar.mediabroken.utils

/**
 * Author : YCSnail
 * Date   : 2017-05-19
 * Email  : liyancai1986@163.com
 */
class XmlUtil {

    static Map<String, String> toXml(String xml) {

        Map<String, String> result = new HashMap<String, String>();
        def xmlDate = new XmlParser().parseText(xml);
        xmlDate.each {
            it.name();
            it.text();
            result.put(it.name(), it.text());
        }
        return result;
    }
}
