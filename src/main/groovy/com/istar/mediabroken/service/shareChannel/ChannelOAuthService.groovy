package com.istar.mediabroken.service.shareChannel

import com.istar.mediabroken.entity.SystemSetting
import com.istar.mediabroken.entity.app.Agent
import com.istar.mediabroken.repo.admin.SettingRepo
import com.istar.mediabroken.service.app.AgentService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
 * @author YCSnail
 * @date 2018-05-23
 * @email liyancai1986@163.com
 */
@Service
@Slf4j
class ChannelOAuthService<T> {

    public static final String SHARE_CHANNEL_OAUTH_TYPE = 'shareChannelOAuth'

    public Map<String, T> configMap = new HashMap<>()
    public T config = null

    @Autowired
    public SettingRepo settingRepo
    @Autowired
    public AgentService agentService


    def getShareChannelOAuthConfig(Agent agent) {

        SystemSetting setting = settingRepo.getSystemSetting(SHARE_CHANNEL_OAUTH_TYPE, agent.agentKey)

        return setting.content;
    }

    T getOAuthConfig(HttpServletRequest request) {
        return T
    }

}
