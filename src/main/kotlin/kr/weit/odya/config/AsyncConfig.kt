package kr.weit.odya.config

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@Configuration
@EnableAsync
class AsyncConfig : AsyncConfigurer {

    @Override
    override fun getAsyncExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        // 일단 수치는 아래와 같이 잡았는데 나중에 수치 조절 필요
        executor.corePoolSize = 2
        executor.maxPoolSize = 5
        executor.queueCapacity = 50
        executor.setThreadNamePrefix("async-")
        executor.initialize()
        return executor
    }
}
