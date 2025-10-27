package com.fernandoschilder.ipaconsolebackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;


@Configuration
@EnableScheduling
public class SchedulingConfig {
    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        var t = new ThreadPoolTaskScheduler();
        t.setPoolSize(4);
        t.setThreadNamePrefix("sched-");
        t.initialize();
        return t;
    }
}