package com.jwb.base.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
public class ThreadPoolConfig {

    // 自定义线程池参数
    private final int corePoolSize = 30;
    private final int maximumPoolSize = 40;
    private final long keepAliveTime = 60L;
    private final TimeUnit unit = TimeUnit.SECONDS;
    private final BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();

    @Bean
    public ExecutorService threadPoolExecutor() {
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                unit,
                workQueue
        );
        return threadPool;
    }
}