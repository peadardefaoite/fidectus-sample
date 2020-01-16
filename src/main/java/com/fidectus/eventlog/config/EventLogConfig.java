package com.fidectus.eventlog.config;

import com.fidectus.eventlog.persistence.EventRepositoryDao;
import com.fidectus.eventlog.services.EventLogService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Bean instantiation
 */
@Configuration
@ComponentScan(basePackages = "com.fidectus.eventlog")
public class EventLogConfig {
    @Bean
    public EventLogService eventLogService() {
        return new EventLogService();
    }

    @Bean
    public EventRepositoryDao eventRepositoryDao() {
        return new EventRepositoryDao();
    }
}
