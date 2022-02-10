import com.alibaba.fastjson.JSON
import com.istar.mediabroken.utils.UrlUtils
import org.apache.poi.hssf.usermodel.HSSFCell
import org.apache.poi.hssf.usermodel.HSSFRow
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook

def excelFile = '/Users/steven/Desktop/siteclassly.xls'
def outFile = new File('/Users/steven/Desktop/sitesss.sql')
InputStream fis = new FileInputStream(excelFile)
HSSFWorkbook book = new HSSFWorkbook(fis);
HSSFSheet sheet = book.getSheetAt(0);
int firstRow = 0//sheet.getFirstRowNum();从第三行开始读表头
int lastRow = sheet.getLastRowNum();
//除去表头和第一行
//          ComnDao dao = SysBeans.getComnDao();
def list = []
def categoryMap = new HashSet()
def category2Map = new HashSet()
for(int i = firstRow + 1; i<lastRow; i++) {
    Map map = new HashMap();

    HSSFRow row = sheet.getRow(i);
    if (row!=null)
    {
        for(int j=0; j<3; j++) {
            HSSFCell cellKey = sheet.getRow(firstRow).getCell(j);
            String key = cellKey.getStringCellValue();

            HSSFCell cell = row.getCell(j);
            if (cell !=null){
                if(cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
                } else {
                    String val = cell.getStringCellValue();
                    if(val==null||"".equals(val)){
                        continue;
//                    if (key == 'category') {
//                        def categories = extractCategories(val)
//                        map.put(key, categories);
                    } else {
                        map.put(key, val);
                    }

                }

            }else{
                map.put(key, "");
            }
        }
    }
    if (!map.url) continue
    if (map.url.indexOf('haoyidian.com') > -1) {
        continue
    }

    if (!map.category) continue
    try {
        def categories = extractCategories(map.category)

        if (!categories) {
            continue
        }

        map.categories = categories
        map.remove('category')


        map.categories.each {
            categoryMap << it.category
            category2Map << it.category + '_' + it.second
        }

        list << map
//        println map

        if (map.size()!=0){
        }else{
            continue;
        }
    } catch (e) {
        continue
    }

}


fis.close()

//println list.size()
println categoryMap.size()
println categoryMap
println category2Map.size()
println category2Map

def categorysMap = JSON.parse("""{"财经_外汇":["财政金融"],"文学_其它":["文学艺术"],"体育_乒乓球":["体育"],"奢侈品_化妆品":["文化娱乐"],"教育_小学":["教育"],"艺术_雕塑":["文学艺术"],"体育_体操":["体育"],"汽车_新能源":["交通运输","经济"],"房地产_土地":["基建建筑","经济"],"科技_网络产品":["信息产业"],"文学_小说":["文学艺术"],"美食_西北菜":["服务旅游业"],"体育_高尔夫球":["体育"],"军事_空军":["军事"],"美食_甜点":["服务旅游业"],"美食_粤港菜":["服务旅游业"],"美食_云贵菜":["服务旅游业"],"体育_其它":["体育"],"美食_烤肉":["服务旅游业"],"珠宝_其它":["文化娱乐"],"汽车_微型":["交通运输","经济"],"游戏_页游":["信息产业"],"时尚_设计":["文化娱乐"],"奢侈品_服装":["文化娱乐"],"财经_银行":["财政金融"],"时政_其它":["时政"],"旅游_国外":["服务旅游业"],"美食_新疆菜":["服务旅游业"],"军事_军事装备":["军事"],"考试_中考":["教育"],"汽车_跑车":["交通运输","文化娱乐","经济"],"财经_原油投资":["财政金融"],"影视_电视剧":["文化娱乐"],"奢侈品_烟酒":["文化娱乐","经济"],"体育_射击射箭":["体育"],"财经_基金":["财政金融"],"家居_家具":["经济","基建建筑"],"股市_其它":["财政金融"],"时尚_服装":["文化娱乐"],"明星_歌手":["文化娱乐"],"科技_人物":["科学技术","信息产业"],"体育_篮球":["体育"],"体育_网球":["体育"],"旅游_其它":["服务旅游业"],"健康_其它":["医药卫生"],"理财_其它":["财政金融"],"爆料_其它":["文化娱乐"],"明星_影视":["文化娱乐"],"教育_高等教育":["教育"],"宗教_其它":["时政"],"影视_电影":["文化娱乐"],"汽车_其它":["交通运输","经济"],"命理_看相":["文化娱乐"],"命理_生肖":["文化娱乐"],"文学_童话":["文学艺术"],"影视_其它":["文化娱乐"],"文学_散文":["文学艺术"],"美食_客家菜":["文化娱乐","服务旅游业"],"体育_排球":["体育"],"科技_其它":["科学技术"],"旅游_休闲度假":["服务旅游业"],"体育_足球":["体育"],"美食_西餐":["服务旅游业"],"娱乐_其它":["文化娱乐"],"考试_研究生考试":["教育"],"命理_星座":["文化娱乐"],"健康_美容":["文化娱乐","医药卫生"],"明星_其它":["文化娱乐"],"财经_税务":["财政金融"],"家居_材料":["经济","基建建筑"],"美食_川湘菜":["服务旅游业"],"文学_剧本":["文学艺术"],"家居_场所":["经济","基建建筑"],"房地产_其它":["基建建筑","经济"],"命理_五行":["文化娱乐"],"健康_养生":["文化娱乐","医药卫生"],"汽车_中型":["交通运输","经济"],"体育_水上项目":["体育"],"美食_快餐":["服务旅游业"],"游戏_手游":["信息产业"],"家居_风格":["经济","基建建筑"],"考试_其它":["教育"],"体育_冰上项目":["体育"],"房地产_二手房":["基建建筑","经济"],"汽车_汽车类型":["交通运输","经济"],"汽车_豪华型":["交通运输","经济"],"文学_诗歌":["文学艺术"],"奢侈品_配饰":["文化娱乐"],"体育_拳击":["体育"],"考试_小考":["教育"],"美食_日韩料理":["服务旅游业"],"体育_赛车":["体育"],"旅游_国内":["服务旅游业"],"烟酒_其它":["经济","贸易"],"体育_举重":["体育"],"美食_京鲁菜":["服务旅游业"],"财经_股市":["财政金融"],"历史_国外史":["文化娱乐"],"教育_学前":["教育"],"命理_风水":["文化娱乐"],"科技_电脑":["信息产业"],"明星_脱口秀":["文化娱乐"],"军事_陆军":["军事"],"体育_羽毛球":["体育"],"房地产_新房":["基建建筑","经济"],"体育_自行车":["体育"],"军事_其它":["军事"],"艺术_摄影":["文学艺术","文化娱乐"],"奢侈品_其它":["文化娱乐"],"影视_综艺":["文化娱乐"],"时尚_风格":["文化娱乐","文学艺术"],"体育_柔道跆拳道":["体育"],"汽车_中大型":["交通运输","经济"],"科技_存储":["信息产业","科学技术"],"时尚_其它":["文化娱乐"],"体育_田径":["体育"],"美食_其它":["服务旅游业"],"游戏_网游":["信息产业"],"奢侈品_珠宝":["文化娱乐"],"汽车_汽车品牌":["交通运输","经济"],"命理_其它":["文化娱乐"],"美食_江浙菜":["服务旅游业"],"汽车_MPV":["交通运输","经济"],"美食_小吃":["服务旅游业"],"教育_英语教育":["教育"],"房地产_租房":["基建建筑","经济"],"科技_安全产品":["科学技术"],"考试_资格考":["教育"],"体育_台球":["体育"],"科技_手机":["信息产业"],"美食_东北菜":["服务旅游业"],"宗教_佛教":["时政"],"艺术_舞蹈":["文学艺术"],"艺术_字画":["文学艺术"],"考试_高考":["教育"],"汽车_小型":["交通运输","经济"],"汽车_微克":["交通运输","经济"],"财经_其它":["财政金融"],"历史_中国史":["文化娱乐"],"贷款_其它":["财政金融"],"家居_其它":["基建建筑","经济"],"汽车_皮卡":["交通运输","经济"],"文学_作者":["文学艺术"],"艺术_音乐":["文化娱乐","文学艺术"],"汽车_SUV":["交通运输","经济"],"财经_理财":["财政金融"],"军事_海军":["军事"],"教育_中学":["教育"],"宗教_伊斯兰教":["时政"],"教育_其它":["教育"],"宗教_道教":["时政"],"美食_火锅":["服务旅游业"],"宗教_基督教":["服务旅游业"],"汽车_紧凑型":["交通运输","经济"],"体育_棋类":["体育"],"财经_贷款":["财政金融"],"汽车_轻客":["交通运输","经济"],"奢侈品_皮具":["文化娱乐"],"美食_海鲜":["服务旅游业"],"育儿_其它":["医药卫生"],"游戏_其它":["信息产业"],"旅游_徒步":["服务旅游业"],"科技_无线网络":["信息产业"],"奢侈品_手表":["文化娱乐"]}
""")


list.each {
    it.categories.each { it2 ->
        it2.siteName = it.name
        it2.url = it.url
        it2.siteDomain = getDomain(it.url)
        if (it2.category && it2.second && it2.siteName && it2.siteDomain && it2.url) {
//        println JSON.toJSONString(it2)
        categorysMap."${(it2.category + '_' + it2.second)}".each  { it3 ->
//                println it3
            outFile.append("""insert into sitesss values ("${it3}","${it2.second.replace('"', '\\"')}",${it2.weight},"${it2.siteName.replace('"', '\\"')}","${it2.url.replace('"', '\\"')}","${it2.siteDomain.replace('"', '\\"')}");""")
        }

        outFile.append('\n')
        }
    }
}

//list.each {
//    it.categories.each { it2 ->
//        it2.siteName = it.name
//        it2.url = it.url
//        it2.siteDomain = getDomain(it.url)
////        println JSON.toJSONString(it2)
//        outFile.append(JSON.toJSONString(it2))
//        outFile.append('\n')
//    }
//}



List extractCategories(String categoryStr) {
    def categories = categoryStr.split(/\s+/)
    def list = []
    categories.each {
        def strs = it.split(/-/)
        if ((strs[strs.size() -1] as Integer) > 100) {
            if (strs.size() == 2) {
                list << [category: strs[0], second: '其它', weight: strs[1] as Integer]
            } else if (strs.size() == 3) {
                list << [category: strs[0], second: strs[1], weight: strs[2] as Integer]
            } else if (strs.size() == 4) {
                list << [category: strs[0], second: strs[1], weight: strs[3] as Integer]
            }
        }
    }
    return list
}

def getDomain(String urlStr) {
    def url = new URL(urlStr)
    return UrlUtils.stripUrl(url.host)
}