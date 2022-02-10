import com.istar.mediabroken.task.Task
import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext

def context = new ClassPathXmlApplicationContext("classpath*:spring*.xml");
def task = getTask(context,'newsFeachTask');
task.execute()

 Task getTask(ApplicationContext context,String taskName) {
    Task task = context.getBean(taskName );
    return task
}
