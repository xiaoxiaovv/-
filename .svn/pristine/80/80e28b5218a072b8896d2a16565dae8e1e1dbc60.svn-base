package com.istar.mediabroken.service.compile

import com.istar.mediabroken.entity.LoginUser
import com.istar.mediabroken.entity.compile.AccountTagHistory
import com.istar.mediabroken.entity.compile.OrgTag
import com.istar.mediabroken.repo.compile.TagRepo
import com.istar.mediabroken.utils.Md5Util
import com.istar.mediabroken.utils.UploadUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * @author zxj
 * @create 2019/1/17
 */
@Service
class TagService {

    @Autowired
    TagRepo tagRepo

    def saveTagHistory(long userId, List orgTagIds) {
        def history = new AccountTagHistory()
        history.setId(UUID.randomUUID().toString())
        history.setUserId(userId)
        history.setOrgTagIds(orgTagIds)
        history.setCreateTime(new Date())
        tagRepo.saveAccountTagHistory(history)
    }

    def getUerTagList(LoginUser user) {
        List<OrgTag> orgTagList = new ArrayList<>();
        //1.先去历史表查询有没有自己设置好的（如果机构管理修改orgTag 删除历史）
        def ids = tagRepo.getTagHistoryTagIds(user.userId)
        if (ids.size() > 0) {
            List get = ids.get(0)
            orgTagList = tagRepo.getOrgTagList(user.orgId)
            orgTagList.each { elem ->
                if (get.contains(elem.id)) {
                    elem.setIsHave(true)
                }
            }
        } else {
            //2.默认机构的
            orgTagList = tagRepo.getOrgTagList(user.orgId)
        }
        //3.重新组织数据结构
        if (orgTagList.size() > 0) {
            orgTagList = dealOrgTagList(orgTagList)
        }
        return orgTagList;
    }

    void orgTagImport(String orgId, String filePath) {
        File file = new File(filePath)
        InputStream input = new FileInputStream(file);
        List<Map<String, String>> data = UploadUtil.parseExcel2Tag(input);
        deleteOrgTag(orgId)
        for (int i = 0; i < data.size(); i++) {
            Map dataLine = data[i]
            List parentParmInfo = []
            Map parentTagMap = [:]
            String parentIdStr = orgId
            parentParmInfo[0] = parentTagMap
            parentParmInfo[1] = parentIdStr
            for (int j = 1; j < 6; j++) {
                processTag(orgId, dataLine, j, parentTagMap, parentIdStr, parentParmInfo)
            }
        }
    }

    void deleteOrgTag(String orgId) {
        List<OrgTag> orgTagList = tagRepo.getOrgTagList(orgId)
        tagRepo.saveOrgTagHistory(orgTagList)
        tagRepo.deleteOrgTag(orgId)
    }

    void processTag(String orgId, Map dataLine, int level, Map parentTagMap, String parentIdStr, List parentParmInfo) {
        String tagNameKey = "TagName" + level
        String tagIdKey = "TagID" + level
        String nextTagNameKey = "TagName" + (level + 1)
        String tagName = dataLine.get(tagNameKey)?.toString()?.trim()
        String tagId = dataLine.get(tagIdKey)?.toString()?.trim()
        String nextTagName = dataLine.get(nextTagNameKey)?.toString()?.trim()
        parentTagMap = parentParmInfo[0]
        if (!tagName) {
            parentTagMap == [:]
            return
        }
        if (level > 1 && parentTagMap.size() == 0) {
            return
        }
        String currIdStr = parentParmInfo[1] + tagName
        parentIdStr = currIdStr
        String orgTagId = Md5Util.md5(currIdStr)
        Map orgTagMap = tagRepo.getOrgTag(orgTagId)
        if (orgTagMap.size() > 0) {
            parentParmInfo[0] = orgTagMap
            parentParmInfo[1] = parentIdStr
            return
        }
        String parentId = level > 1 ? parentTagMap.get("_id") : ""
        boolean leaf = nextTagName ? false : true
        String parentCode = level > 1 ? parentTagMap.get("code") : ""
        String code = getNextCode(orgId, parentCode)
        orgTagMap = ["_id"     : orgTagId, "parentId": parentId, "orgId": orgId, "level": level,
                     "leaf"    : leaf, "code": code, "parentCode": parentCode ?: "", "userTagName": tagName, "userTagId": tagId ?: "",
                     updateTime: new Date(), createTime: new Date()
        ]
        parentTagMap = orgTagMap
        parentParmInfo[0] = parentTagMap
        parentParmInfo[1] = parentIdStr
        tagRepo.addOrgTag(orgTagMap)
    }

    String getNextCode(String orgId, String parentCode) {
        return parentCode + (tagRepo.getMaxCode(orgId, parentCode) + 1).toString()
    }

    private List dealOrgTagList(List<OrgTag> menuList) {

        menuList.each { it ->

            //是父级节点的
            if (!it.leaf) {
                def all = menuList.findAll { item ->
                    item.parentId == it.id
                }
                it.setSubTagList(all)
            }
        }
        def orgList = menuList.findAll {
            it.level == 1
        }

        return orgList
    }


    public static void main(String[] args) {
        String tagId = Md5Util.md5("122" + "" + "" + "" + "" + "")
    }
}
