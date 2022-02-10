package com.istar.mediabroken.entity

// 推送日志
public class PushLog {

    // 新闻内容
    String title
    String source

    // 推送信息
    String pushType             // 1 自动采编 2. 要闻摘要 3. 要闻综述
    String orgId                // 机构Id
    String userId               // 操作者Id
    int status                  // 1, 未推送  2, 已推送
    Date createTime
    Date updateTime

}

enum  PushTypeEnum {
    NEWS_PUSH(1, '自动采编'),
    ABSTRACT_PUSH(2, '要闻摘要'),
    SUMMARY_PUSH(3, '专题综述'),
    ARTICLE_PUSH(4, '推送文稿');

    private int index
    private String value

    PushTypeEnum(int index, String value) {
        this.index = index
        this.value = value
    }

    int getIndex() {
        return index
    }

    String getValue() {
        return value
    }
}