package account.configurations;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@EnableAsync
@Configuration
public class AsyncListenerConfig implements AsyncConfigurer {


    @Override
    @Bean("listenerExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        /* Core Pool Size represents the minimum number of active threads in the pool (whether idle or not)
          This configuration helps handle immediate task submissions without thread creation overhead.
        */
        executor.setCorePoolSize(5);
        /* Max Pool Size defines the maximum number of threads in the pool.
          Threads beyond this limit are created only when needed (up to the max).
         */
        executor.setMaxPoolSize(100);
        /* Queue Capacity determines the maximum number of tasks that can wait in the queue when all threads are busy.
          Balances task submission rate with thread availability and prevents excessive memory usage due to unbounded queues.
        */
        executor.setQueueCapacity(100);
        // Configures whether the thread pool should wait for currently executing tasks to complete before shutting down
        executor.setWaitForTasksToCompleteOnShutdown(true);

        return executor;
    }

    @Bean
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }

}
