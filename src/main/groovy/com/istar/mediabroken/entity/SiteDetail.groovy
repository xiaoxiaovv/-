package com.istar.mediabroken.entity

/**
 * Author: Luda
 * Time: 2017/6/16
 */
class SiteDetail {

    String id;
    String region;
    String type;
    long popularity;
    String second;
    long rank;
    long score;
    String siteDomain;
    String siteName;
    String baseurl;
    int isCapturing;
    String classificationId;
    String parentClassificationId;
    def parentClassificationIdGroup;
    int siteType;
    String filterClassification;
    Integer filterClassificationId;
    String filterArea;
    Integer filterAreaId;

    Map<String, Object> toMap(){
        return [
                _id                             : id == null ? UUID.randomUUID().toString() : id,
                region                          : this.region,
                type                            : this.type,
                popularity                      : this.popularity,
                second                          : this.second,
                rank                            : this.rank,
                score                           : this.score,
                siteDomain                      : this.siteDomain,
                siteName                        : this.siteName,
                baseurl                         : this.baseurl,
                isCapturing                     : this.isCapturing,
                classificationId                : this.classificationId,
                parentClassificationId          : this.parentClassificationId,
                parentClassificationIdGroup     : this.parentClassificationIdGroup,
                siteType                        : this.siteType?:1,
                filterClassification            : this.filterClassification?:null,
                filterClassificationId          : this.filterClassificationId?:null,
                filterArea                      : this.filterArea?:null,
                filterAreaId                    : this.filterAreaId?:null,
        ]
    }

    SiteDetail() {
        super
    }

    SiteDetail(Map map) {
        this.id                             = map._id
        this.region                         = map.region
        this.type                           = map.type
        this.popularity                     = map.popularity
        this.second                         = map.second
        this.rank                           = map.rank
        this.score                          = map.score
        this.siteDomain                     = map.siteDomain
        this.siteName                       = map.siteName
        this.baseurl                        = map.baseurl
        this.isCapturing                    = map.isCapturing
        this.classificationId               = map.classificationId
        this.parentClassificationId         = map.parentClassificationId
        this.parentClassificationIdGroup    = map.parentClassificationIdGroup
        this.siteType                       = map.siteType?:1
        this.filterClassification           = map.filterClassification?:null
        this.filterClassificationId         = map.filterClassificationId?:null
        this.filterArea                     = map.filterArea?:null
        this.filterAreaId                   = map.filterAreaId?:null
    }
}
