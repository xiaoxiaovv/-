package com.istar.mediabroken.service.tags

import com.istar.mediabroken.repo.tags.TagsManageRepo
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * @author: hexushuai
 * @date: 2019/2/28 9:33
 */
@Service
@Slf4j
class TagsManageService {

    @Autowired
    private TagsManageRepo tagsManageRepo

    List getTagsByOrgId(String orgId) {
        List list = tagsManageRepo.findTagsByOrgId(orgId)
        return list
    }

    boolean addTag(String orgId, String tagName, String tagType, List tagValue) {
        int count = tagsManageRepo.findByTagName(orgId, tagName)
        if(count > 0) {
            return false
        }
        Map map = new HashMap()
        map.put("_id", UUID.randomUUID().toString())
        map.put("tagName", tagName)
        map.put("tagType", tagType)
        map.put("tagValue", tagValue)
        map.put("orgId", orgId)
        map.put("createTime", new Date())
        map.put("updateTime", new Date())
        tagsManageRepo.insert(map)
        return true
    }

    Map getTagInfoById(String id) {
        return tagsManageRepo.findOne(id)
    }

    void updateTag(String id, String tagName, String tagType, List tagValue) {
        Map map = new HashMap()
        map.put("tagName", tagName)
        map.put("tagType", tagType)
        map.put("tagValue", tagValue)
        map.put("updateTime", new Date())
        tagsManageRepo.update(id, map)
    }

    void deleteTagById(String id) {
        tagsManageRepo.delete(id)
    }
}
