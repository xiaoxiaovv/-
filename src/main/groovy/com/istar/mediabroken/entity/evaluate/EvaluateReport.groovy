package com.istar.mediabroken.entity.evaluate


class EvaluateReport {
    String id
    Long userId
    String evaluateName//评测名称
    String channelsName//总渠道名称
    boolean isAuto//true 自动生成 false 手动添加
    def channels
    int status// 1处理中 2待生成 3 已生成 4处理失败
    Date startTime
    Date endTime
    Date createTime
    Date updateTime
    boolean valid//是否有效（以删除false）

    boolean indicator//处理标识 false未处理 true处理中(报告生成后为false)

    EvaluateReport() {
    }

    EvaluateReport(Map map) {
        id = map._id
        userId = map.userId
        evaluateName = map.evaluateName
        channelsName = map.channelsName
        isAuto = map.isAuto
        channels = map.channels
        status = map.status
        startTime = map.startTime
        endTime = map.endTime
        createTime = map.createTime
        updateTime = map.updateTime
        valid = map.valid
        indicator = map.indicator
    }

    EvaluateReport(String id, Long userId, String evaluateName, String channelsName, channels, int status,
                   Date startTime, Date endTime, Date createTime, Date updateTime) {
        this.id = id
        this.userId = userId
        this.evaluateName = evaluateName
        this.channelsName = channelsName
        this.channels = channels
        this.status = status
        this.startTime = startTime
        this.endTime = endTime
        this.createTime = createTime
        this.updateTime = updateTime
    }

}
/**
 * @author zxj
 * @create 2018/7/9
 */
enum ReportStatusEnum {
    busy(1, '处理中'), //status 1
    untreated(2, '待生成'), //status 2
    completed(3, '已生成'), //status 3
    failed(4, '处理失败'), //status 4
    private Integer key
    private String desc

    ReportStatusEnum(Integer key, String desc) {
        this.key = key
        this.desc = desc
    }

    Integer getKey() {
        return key
    }

    String getDesc() {
        return desc
    }

    public Map toMap() {
        Map map = new HashMap();
        map.put("status", key);
        map.put("desc", desc);
        return map;
    }
}
