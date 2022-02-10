package com.istar.mediabroken.task

import groovy.util.logging.Slf4j
import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.springframework.stereotype.Controller

@Controller
@Slf4j
class TaskManager {
    static void main(String[] args) {
        ApplicationContext context = new
                ClassPathXmlApplicationContext("classpath*:spring*.xml");
        Task task = TaskManager.getTask(context, args[0])

        log.info("====================================")
        log.info("TaskManager 开始任务：{} 时间：{}", args[0], new Date())
        task.execute()
        log.info("TaskManager 结束任务：{} 时间：{}", args[0], new Date())
        log.info("++++++++++++++++++++++++++++++++++++")
    }

    static Task getTask(ApplicationContext context,String taskName) {
        Task task = context.getBean(taskName );
        return task
    }
}
