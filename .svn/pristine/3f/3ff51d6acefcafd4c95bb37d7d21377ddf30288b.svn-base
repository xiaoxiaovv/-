package com.istar.mediabroken.service.account

import com.istar.mediabroken.entity.account.Menu
import com.istar.mediabroken.entity.app.Agent
import com.istar.mediabroken.repo.account.*
import com.istar.mediabroken.service.app.AgentService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import javax.servlet.http.HttpServletRequest

/**
 * Author: hh
 * Time: 2018/1/26
 */
@Service
class MenuService {

    @Autowired
    MenuVsPrivilegeRepo menuVsPrivilegeRepo
    @Autowired
    MenuRepo menuRepo
    @Autowired
    PrivilegeService privilegeService
    @Autowired
    AgentService agentService

    public static final Map<String, Map<String, String>> agentMenuMap = [
            'ppc'  : [
                    '1' : '数据采集',           //原：自动采编
                    '2' : '全网数据',           //原：热点推荐
                    '3' : '站点配置',           //原：最新资讯
                    '4' : '智能推荐',           //原：全网监控
                    '5' : '政协发布',           //原：智能组稿
                    '8' : '政协站点影响测评',    //原：传播测评
                    '9' : '政协信息传播监控',    //原：版权监控
                    '19': '专题采集',           //原：订阅管理
                    '20': '专题配置',           //原：订阅列表
                    '21': '智能排除',           //原：排除词设置

            ]
    ]

    Map getMenuByUserId(HttpServletRequest request, Long userId) {
        def privilegeIds = privilegeService.getPrivilegesByUserId(userId)
        def menuIds = menuVsPrivilegeRepo.getMenuByPrivilege(privilegeIds)
        def menuList = menuRepo.getMenuListByIds(menuIds)
        addParentMenus(menuList)

        //处理代理商下菜单的别名
        dealMenuAlias4Agent(request, menuList)

        //将菜单list处理成map
        def resultMenu = trimMenuList(menuList)
        return resultMenu
    }

    List addParentMenus(List<Menu> menuList) {
        def parentsSet = new HashSet<String>()
        for (Menu menu : menuList) {
            def parentId = menu.getParentId()
            if (parentId) {
                parentsSet.add(parentId)
            }
        }
        if (parentsSet) {
            menuRepo.addParentMenuByIds(parentsSet, menuList)
        }
        return menuList
    }

    private Map trimMenuList(List<Menu> menuList) {
        def totalGroupMap = new HashMap()
        totalGroupMap = menuList.groupBy { it.type }
        def leftSideList = totalGroupMap.get(1)

        leftSideList.collect {
            if(it.isLeaf == 0) {
                it.subList = leftSideList.findAll { item ->
                    item.parentId == it.id
                }
            }
        }
        def leftMenuList = leftSideList.findAll {
            it.level == 1
        }
        totalGroupMap.put(1, leftMenuList)

        return totalGroupMap
    }

    /**
     * 根据代理商为左侧菜单更新别名
     * @param request
     * @param menuList
     */
    private dealMenuAlias4Agent(HttpServletRequest request, List<Menu> menuList) {

        Agent agent = agentService.getAgent(request)
        def aliasMap = agentMenuMap.get(agent.agentKey)

        if(aliasMap) {
            for (Menu menu: menuList) {
                String alias = aliasMap.get(menu.id)
                menu.setAlias(alias)
                menu.setName(alias ?: menu.name)
            }
        }
    }

}
