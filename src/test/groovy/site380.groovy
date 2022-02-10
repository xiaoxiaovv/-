import com.alibaba.fastjson.JSON


// 读取名称和url

/*def urlFile = new File('url.txt')
urlFile.eachLine {

}*/

//name:345  url:336
def name = "人民网,新华网,中国网,央视国际网络,国际在线,中国日报网,中国青年网,中国经济网,中国新闻网,光明网,中国广播网,中国台湾网,中国西藏网,中工网,中青在线,环球网,中国军网,法制网,海外网,中国搜索,参考消息网,中国警察网,消费日报网,中国侨网,未来网,人民日报,人民日报海外版,新华社,新华每日电讯,中国新闻社,中国新闻周刊,解放军报,求是,光明日报,经济日报,中央人民广播电台,中央电视台,中国日报,半月谈,经济参考报,中国证券报,环球时报,科技日报,工人日报,中国青年报,中国妇女报,农民日报,法制日报,人民政协报,今日中国,健康报,中国劳动保障报,中国教育报,中国工商报,中国电力报,中国民航报,人民铁道报,人民邮电报,中国交通报,中国医药报,中国质量报,中国税务报,中国体育报,中国财经报,检查日报,人民法院报,中国建设报,中国环境报,中国文化报,人民公安报,中国水利报,中华工商时报,中国纪检监察报,金融时报,国际商报,中国商报,中国经济周刊,中华儿女,中国青年,中国妇女报,瞭望东方周刊,外交部,卫生部,商务部,财政部,国家发改委,中国网信网,中国文明网,中国记协网,中国政府网,中央纪律监察部网站,千龙新闻网,财新网,北京日报,北京晚报,北京晨报,京华时报,新京报,北京青年报,北京人民广播电台,北京电视台,北方网,天津日报,今晚报,滨海时报,天津人民广播电台,长城网,河北新闻网,河北日报,石家庄日报,河北人民广播电台,河北电视台,燕赵晚报,石家庄电视台,黄河新闻网,山西日报,太原日报,山西晚报,太原晚报,山西人民广播电台,山西电视台,东北新闻网,北国网,辽宁日报,沈阳晚报,大连日报,沈阳日报,辽宁人民广播电台,辽宁电视台,大连人民广播电台,大连电视台,沈阳人民广播电台,沈阳电视台,中国吉林网,吉林日报,长春晚报,长春日报,吉林人民广播电台,吉林电视台,长春人民广播电台,长春电视台,东北网,哈尔滨新闻网,黑龙江日报,哈尔滨日报,黑龙江晨报,生活报,黑龙江人民广播电台,黑龙江电视台,哈尔滨人民广播电台,哈尔滨电视台,东方网,新民网,一财网,解放日报,文汇报,新民晚报,上海人民广播电台,上海电视台,东方电视台,东方广播电台,中国江苏网,新华报业网,新华日报,南京日报,扬子晚报,金陵晚报,江苏省广播电视总台,南京人民广播电台,南京电视台,浙江在线,中国宁波网,浙江日报,钱江晚报,杭州日报,宁波日报,今日早报,浙江人民广播电台,浙江电视台,宁波人民广播电台,宁波电视台,中国安徽在线,安徽日报,合肥日报,合肥晚报,安徽人民广播电台,安徽电视台,合肥人民广播电台,合肥电视台,东南新闻网,福建日报,福州日报,福州晚报,厦门日报,福建人民广播电台,福州人民广播电台,厦门人民广播电台,厦门电视台,福建电视台,福州电视台,中国江西网,大江网,今视网,江西日报,南昌日报,南昌晚报,江西人民广播电台,江西电视台,南昌人民广播电台,南昌电视台,大众网,齐鲁网,胶东在线,鲁网,中国山东网,大众日报,齐鲁晚报,济南日报,青岛日报,山东商报,山东人民广播电台,山东电视台,济南人民广播电台,济南电视台,青岛人民广播电台,青岛电视台,大河网,河南日报,郑州日报,郑州晚报,河南电视台,郑州人民广播电台,郑州电视台,荆楚网,长江网,湖北日报,武汉晚报,长江日报,湖北人民广播电台,湖北电视台,武汉人民广播电台,武汉电视台,红网,华声在线,金鹰网,湖南日报,长沙晚报,湖南人民广播电台,湖南电视台,长沙人民广播电台,长沙电视台,南方新闻网,大洋网,深圳新闻网,南方日报,广州日报,羊城晚报,深圳特区报,广东人民广播电台,广东电视台,广州人民广播电台,广州电视台,深圳人民广播电台,深圳电视台,广西新闻网,广西日报,南宁日报,南宁晚报,广西人民广播电台,广西电视台,南宁人民广播电台,南宁电视台,南海网,海南日报,海口晚报,海南特区报,国际旅游岛商报,海南广播电视台,海口广播电视台,华龙网,视界网,重庆日报,重庆晚报,重庆晨报,重庆人民广播电台,重庆电视台,四川新闻网,四川在线,四川日报,成都日报,成都晚报,华西都市报,成都商报,四川人民广播电台,四川电视台,金黔在线,贵州日报,贵阳日报,贵阳晚报,贵州人民广播电台,贵州电视台,云南网,云南日报,昆明日报,云南电视台,中国西藏新闻网,西藏日报,拉萨晚报,西藏人民广播电台,西藏电视台,西部网,陕西传媒网,陕西日报,西安日报,西安晚报,陕西人民广播电台,陕西电视台,西安人民广播电台,西安电视台,中国甘肃网,甘肃日报,兰州日报,兰州晚报,甘肃广播电影电视总台,青海新闻网,青海日报,西宁晚报,西海都市报,青海人民广播电台,青海电视台,西宁电视台,宁夏新闻网,宁夏日报,银川晚报,天山网,新疆日报,乌鲁木齐晚报,兵团日报,新疆人民广播电台,内蒙古新闻网,内蒙古日报,呼和浩特市日报,呼和浩特晚报,内蒙古人民广播电台,内蒙古电视台"
def url = "people.com.cn,xinhuanet.com,china.com.cn,tv.cctv.com,gb.cri.cn,cn.chinadaily.com.cn,youth.cn,ce.cn,chinanews.com,gmw.cn,cnr.cn,taiwan.cn,tibet.cn,workercn.cn,cyol.net,huanqiu.com,81.cn,legaldaily.com.cn,haiwainet.cn,chinaso.com,cankaoxiaoxi.com,cpd.com.cn,xfrb.com.cn,chinaqw.com,k618.cn,paper.people.com.cn,paper.people.com.cn,zhongguowangshi.com,cfiex.com,www.cfiex.com,news.cn,203.192.15.131,chinanews.com,inewsweek.cn,81.cn,qstheory.cn,gmw.cn,paper.ce.cn,bfq.cnr.cn,tv.cntv.cn,chinadaily.com.cn,banyuetan.org,jjckb.xinhuanet.com,cs.com.cn,data.huanqiu.com,stdaily.com,media.workercn.cn,zqb.cyol.com,cnwomen.com.cn,szb.farmer.com.cn,legaldaily.com.cn,rmzxb.com.cn,jrzgw.com.cn,szb.jkb.com.cn,clssn.com,paper.jyb.cn,cicn.com.cn,cpnn.com.cn,editor.caacnews.com.cn,peoplerail.com,paper.cnii.com.cn,zgjtb.com,cnpharm.com,cqn.com.cn,ctaxnews.net.cn,sportspress.cn,cfen.com.cn,newspaper.jcrb.com,rmfyb.chinacourt.org,chinajsb.cn,news.cenews.com.cn,ccdy.cn,cpd.com.cn,chinawater.com.cn,124.42.72.218,mos.gov.cn,financialnews.com.cn,comnews.cn,zgswcn.com,people.com.cn,youth.cn,youth.cn,paper.fnews.cc,lwdf.cn,fmprc.gov.cn,nhfpc.gov.cn,mofcom.gov.cn,mof.gov.cn,sdpc.gov.cn,cac.gov.cn,wenming.cn,xinhuanet.com,www.gov.cn,ccdi.gov.cn,qianlong.com,caixin.com,bjrb.bjd.com.cn,bjwb.bjd.com.cn,morningpost.com.cn,jinghua.cn,bjnews.com.cn,ynet.com,bmn.net.cn,brtn.cn,enorth.com.cn,tianjinwe.com,jwb.com.cn,tjbhnews.com,radiotj.com,hebei.com.cn,hebnews.cn,hebnews.cn,sjzdaily.com.cn,hebradio.com,hebtv.com,sjzdaily.com.cn,sjzntv.cn,sxgov.cn,sxrb.com,tynews.com.cn,sxrb.com,tywbw.com,sxrtv.com,nen.com.cn,lnd.com.cn,lnd.com.cn,syd.com.cn,dlxww.com,syd.com.cn,lntv.com.cn,dltv.cn,dltv.cn,csytv.com,chinajilin.com.cn,chinajilin.com.cn,1news.cc,1news.cc,jlradio.cn,jlntv.cn,chinactv.com,chinactv.com,dbw.cn,my399.com,hljnews.cn,my399.com,hljnews.cn,hljnews.cn,hljradio.com,hljtv.com,hrbtv.net,eastday.com,xinmin.cn,yicai.com,jfdaily.com,wenweipo.com,xinmin.cn,news990.cn,smg.cn,dragontv.cn,smgradio.cn,jschina.com.cn,xhby.net,xhby.net,njdaily.cn,yzwb.net,jlwb.net,jsbc.com,njgb.com,nbs.cn,zjol.com.cn,cnnb.com.cn,zjol.com.cn,zjol.com.cn,hangzhou.com.cn,cnnb.com.cn,jrzb.zjol.com.cn,cztv.com,zjstv.com,nbtv.cn,anhuinews.com,anhuinews.com,hf365.com,hf365.com,www.ahtv.cn,ahtv.cn,hfbtv.com,fjsen.com,fjrb.fjsen.com,fznews.com.cn,fznews.com.cn,xmnn.cn,fjtv.net,zohi.tv,xmtv.cn,xmtv.cn,headline.fjtv.net,jxnews.com.cn,jxcn.cn,jxgdw.com,jxnews.com.cn,ncrbw.cn,ncwbw.cn,jxntv.cn,www.jxntv.cn,nctv.net.cn,dzcom,iqilu.com,jiaodong.net,sdnews.com.cn,sdchina.com,dzcom,qlwb.com.cn,e23.cn,dailyqd.com,60.216.0.164,iqilu.com,ijntv.cn,ijntv.cn,qtv.com.cn,dahe.cn,henandaily.cn,zynews.com,zynews.com,hnjtyx.com,zzradio.cn,zztv.tv,cnhubei.com,news.cjn.cn,cnhubei.com,cjn.cn,cjn.cn,hbtv.com.cn,hbtv.com.cn,whbc.com.cn,whtv.com.cn,rednet.cn,voc.com.cn,hunantv.com,voc.com.cn,changsha.cn,hnradio.com,hunantv.com,csbtv.com,southcn.com,dayoo.com,sznews.com,southcn.com,dayoo.com,ycwb.com,sznews.com,rgd.com.cn,gdtv.cn,gztv.com,szmg.com.cn,gxnews.com.cn,gxrb.com.cn,nnrb.com.cn,nnwb.com,bbrtv.com,gxtv.cn,nntv.cn,hinews.cn,hinews.cn,hkwb.net,hntqb.com,paper.hndnews.com,hnntv.cn,cqnews.net,cbg.cn,cqnews.net,cqwb.com.cn,cqcb.com,cbg.cn,newssc.org,sicuanw.com,scol.com.cn,sichuandaily.com.cn,scdaily.cn,sichuandaily.com.cn,www.cdrb.com.cn,cdwb.com.cn,www.wccdaily.com.cn,chengdu.cn,sctv.com,gog.cn,health.gog.cn,gzsc.gog.cn,gog.cn,58.42.249.98,gzrb.gog.com.cn,gywb.cn,gywb.cn,gzstv.com,yunnan.cn,yunnan.cn,yndaily.com,clzg.cn,yntv.cn,chinatibetnews.com,chinatibetnews.com,lasa-eveningnews.com.cn,vtibet.com,cnwest.com,health.cnwest.com,sxdaily.com.cn,sxdaily.com.cn,xiancn.com,xiancn.com,sxtvs.com,snrtv.com,xiancity.cn,gscn.com.cn,gansudaily.com.cn,lzbs.com.cn,lzbs.com.cn,gstv.com.cn,qhnews.com,tibet3.com,xnwbw.com,tibet3.com,qhradio.com,qhstv.com,xntv.tv,nxnews.net,nxnews.net,ycen.com.cn,ts.cn,xjdaily.com.cn,xinjiangnet.com.cn,bingtuannet.com,xjbs.com.cn,nmgnews.com.cn,northnews.cn,saibeinews.com,saibeinews.com,nmrb.com.cn"

def urlList = url.split(",")
def nameList = name.split(",")
println urlList.size()
println nameList.size()

def map = new HashMap<String,String>()
def index = urlList.size()
for(int i = 0; i < index - 1; i++){
    String urls = urlList[i]
    String names = nameList[i]
    map.put(names,urls)
}

//def resultList = []
//for(Map.Entry<String,String> resMap : map.entrySet()){
//    //println m.getKey()+" "+m.getValue()
//    resultList << [
//            url : resMap.getValue(),
//            name : resMap.getKey()
//    ]
//}


def jsonFile = new File('C:\\Users\\ZHXG\\Desktop\\site380.json')
map.each {
    jsonFile.append(JSON.toJSONString(it))
    jsonFile.append('\n')
}

