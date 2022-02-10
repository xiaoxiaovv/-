package com.istar.mediabroken.entity.app

/**
 * Author : YCSnail
 * Date   : 2018-01-19
 * Email  : liyancai1986@163.com
 */
class Agent {

    public static final String AGENT_BJJ_ID = '1'

    String id
    String agentKey
    String siteName
    String agentName
    def agentDomains
    String telephone
    String slogen
    String desc
    String footer
    String qrcode;
    String logo;
    String pureLogo;
    String bgImage;
    String icon;


    Agent() {
        super
    }

    static Agent toObject (Map map) {
        return new Agent(
                id          : map._id ? map._id : map.id,
                agentKey    : map.agentKey,
                siteName    : map.siteName,
                agentName   : map.agentName,
                agentDomains: map.agentDomains,
                telephone   : map.telephone,
                slogen      : map.slogen,
                desc        : map.desc,
                footer      : map.footer,
                qrcode      : map.qrcode,
                logo        : map.logo,
                pureLogo    : map.pureLogo,
                bgImage     : map.bgImage,
                icon        : map.icon
        )
    }
}
