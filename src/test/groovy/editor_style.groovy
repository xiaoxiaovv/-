import com.istar.mediabroken.entity.compile.Style
import com.istar.mediabroken.repo.compile.StyleRepo
import org.springframework.context.support.ClassPathXmlApplicationContext

/**
 * Author : YCSnail
 * Date   : 2017-08-11
 * Email  : liyancai1986@163.com
 */


def ctx = new ClassPathXmlApplicationContext("classpath*:spring*.xml");
def repo = ctx.getBean(StyleRepo)

[
        '<img src="http://www.zhihuibian.com/theme/default/images/style/0001.png">',
        '<img src="http://www.zhihuibian.com/theme/default/images/style/0002.png">',
        '<img src="http://www.zhihuibian.com/theme/default/images/style/0003.png">',
        '<img src="http://www.zhihuibian.com/theme/default/images/style/0004.gif">'
].each {
    def style = new Style([
            content: it as String
    ])
    repo.addUserStyle(style)
}

