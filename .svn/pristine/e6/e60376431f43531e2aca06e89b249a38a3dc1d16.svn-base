package com.istar.mediabroken.entity

/**
 * Author: Luda
 * Time: 2017/6/15
 */
class SiteClassification {
    String id;
    String classification;
    String parentId;
    def subSiteClassification;

    Map<String, Object> toMap(){
        return [
                _id                : id == null?UUID.randomUUID().toString() : id,
                classification     : this.classification,
                parentId           : this.parentId,
        ]
    }

    SiteClassification() {
        super
    }

    SiteClassification(Map map) {
        this.id             = map._id
        this.classification = map.classification
        this.parentId       = map.parentId
    }
}
