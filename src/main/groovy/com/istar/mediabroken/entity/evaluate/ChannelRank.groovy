package com.istar.mediabroken.entity.evaluate


class ChannelRank {
    String id
    String channelName
    Integer siteType
    String index
    String teamName
    Integer channelStatus
    Date lastPublishTime


    ChannelRank() {
        super
    }

    Map<String, Object> toMap() {
        return [
                id             : id,
                channelName    : channelName,
                siteType       : siteType,
                index          : index,
                teamName       : teamName,
                channelStatus  : channelStatus,
                lastPublishTime: lastPublishTime
        ]
    }

    ChannelRank(Map map) {
        id = map.id
        channelName = map.channelName
        siteType = map.siteType
        index = map.index
        teamName = map.teamName
        channelStatus = map.channelStatus
        lastPublishTime = map.lastPublishTime
    }
}
