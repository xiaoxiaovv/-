package com.istar.mediabroken.entity.account

/**
 * Created by zxj on   2018/1/23
 */
class Team {
    public static final int TEAM_MAX_COUNT = 5
    String id
    String orgId
    String agentId
    String teamName
    Date updateTime
    Date createTime


    Team(Map map){
        id = map._id
        orgId = map.orgId
        agentId = map.agentId
        teamName = map.teamName
        updateTime = map.updateTime
        createTime = map.createTime
    }
    @Override
    public String toString() {
        return "Team{" +
                "id='" + id + '\'' +
                ", orgId='" + orgId + '\'' +
                ", agentId='" + agentId + '\'' +
                ", teamName='" + teamName + '\'' +
                ", updateTime=" + updateTime +
                ", createTime=" + createTime +
                '}';
    }
}
