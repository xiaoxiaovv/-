package com.istar.mediabroken.entity.capture

class SiteDetail {
    String id
    String area
    String siteName
    String classification
    String siteDomain
    String attr
    String siteType
    String url
    def tags


    Map<String, Object> toMap() {
        return [
                _id           : id ?: UUID.randomUUID().toString(),
                area          : area,
                siteName      : siteName,
                classification: classification,
                siteDomain    : siteDomain,
                attr          : attr,
                siteType      : siteType,
                url           : url,
                tags          : tags
        ]
    }

    SiteDetail() {
        super
    }

    SiteDetail(Map map) {
        this.id = map._id
        this.area = map.area
        this.siteName = map.siteName
        this.classification = map.classification
        this.siteDomain = map.siteDomain
        this.attr = map.attr
        this.siteType = map.siteType
        this.url = map.url
        this.tags = map.tags
    }

    @Override
    public String toString() {
        return "SiteDetail{" +
                "id='" + id + '\'' +
                ", area='" + area + '\'' +
                ", siteName='" + siteName + '\'' +
                ", classification='" + classification + '\'' +
                ", siteDomain='" + siteDomain + '\'' +
                ", attr='" + attr + '\'' +
                ", siteType='" + siteType + '\'' +
                ", url='" + url + '\'' +
                ", tags=" + tags +
                '}';
    }
}
