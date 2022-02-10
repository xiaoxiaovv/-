package com.istar.mediabroken.entity

class LoginUser {
    long userId
    String orgId
    String teamId
    String agentId
    String userType
    def org

    @Override
    public String toString() {
        return "LoginUser{" +
                "userId=" + userId +
                ", orgId='" + orgId + '\'' +
                ", teamId='" + teamId + '\'' +
                ", agentId='" + agentId + '\'' +
                ", userType='" + userType + '\'' +
                ", org=" + org +
                '}';
    }
}
