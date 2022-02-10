package com.istar.mediabroken.utils;

import java.util.HashMap;
import java.util.Map;

//localhost:8080/solrMessage/solrMsgServlet?kvTitle=中国,足球队&rows=10&fl=kvDkTime,kvTitle,kvUrl
public class SolrMsgUtil {

    private static final long serialVersionUID = -1885741741395344993L;
    private static final String SOLR_URL = "http://192.168.32.15:8080/solr/alldata/select";//solr-newfulltext-2.istarshine.net.cn 此处SOLR_URL使用ip地址而不用域名的原因是，域名在公司根本就不能用

    private static int count = 0;

    public String doGet(Map params){
        return this.doPost(params);
    }


    public static String doPost(Map params){
        //q=kvTitle:"中国" AND kvTitle:"足球队" &fl=kvDkTime,kvTitle,kvUrl&rows=1000&wt=json&indent=true&sort=kvDkTime desc
        String q = String.valueOf(params.get("q"));
        String start = String.valueOf(params.get("start"));
        String sort = String.valueOf(params.get("sort"));
        String rows = String.valueOf(params.get("rows"));
        String fl = String.valueOf(params.get("f1"));

        if (isNull(q)) {
            q = "*:*";
        }
        if (isNull(sort)) {
            sort = "kvDkTime desc,kvUuid asc";
        }
        String qq = q.trim().replaceAll("，", ",");

        if (isNull(rows)) {
            rows = "10";
        }
        rows = rows.trim();


        if (isNull(fl)) {
            fl = "kvReply,kvSite,kvSource,kvSourcetype,kvState,kvTitle,kvUrl,kvUserPic,kvUuid,kvAbstract,kvAuthor,"
                    + "kvCollection,kvContent,kvCtime,kvDkTime,kvISYJ,kvOrienLevel,kvOrientation,kvRerply,isread,isyj,keyword,"
                    + "krIslocal,krKeywordid,krState,krUid";
        }
        fl = fl.trim();

        Map<String, String> map = new HashMap<>();

        map.put("q", qq);
        map.put("fl", fl);
        if (!isNull(start)) {
            map.put("start", start);
        }
        map.put("rows", rows);
        map.put("sort", sort);
        map.put("wt", "json");
        String result = HttpUtil.get(SOLR_URL, map);
        return result;
    }

    private static boolean isNull(String ss) {
        return null == ss || "".equals(ss.trim()) || "null".equals(ss.trim());
    }

}
