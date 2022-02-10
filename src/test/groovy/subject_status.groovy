import com.alibaba.fastjson.JSON
import com.istar.mediabroken.api.MonitorApiController
import org.springframework.context.support.ClassPathXmlApplicationContext

def ctx = new ClassPathXmlApplicationContext("classpath*:spring*.xml");

def monitorApi = ctx.getBean(MonitorApiController)
println JSON.toJSONString(monitorApi.getSubjectStatus())