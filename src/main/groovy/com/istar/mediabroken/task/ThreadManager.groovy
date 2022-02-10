package com.istar.mediabroken.task

import groovy.util.logging.Slf4j

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Created by luda on 2017/5/11.
 */
@Slf4j
class ThreadManager {
    private static LinkedBlockingQueue<Runnable> queue = null;
    private static int limit = 10000;
    private static int threadNum = 10;
    private static ThreadManager threadManager = null;
    private ThreadPoolExecutor threadPool = null;

    private ThreadManager() {
        queue = new LinkedBlockingQueue<Runnable>(limit);
        threadPool = new ThreadPoolExecutor(threadNum, threadNum, 10L, TimeUnit.SECONDS, queue);
    }

    public static ThreadManager getInstance() {
        if (threadManager == null) {
            synchronized (ThreadManager.class) {
                if (threadManager == null) {
                    threadManager = new ThreadManager();
                }
            }
        }
        return threadManager;
    }

    public void addThread(Runnable thread) {
        if (thread == null) {
            return;
        }
        if (queue.size() > limit) {
            log.info("ignored task: " + thread.getClass().getName());
            return;
        }
        threadPool.execute(thread);
    }
}
