package com.fidectus.eventlog.config;

import com.fidectus.eventlog.persistence.EventRepository;
import com.fidectus.eventlog.persistence.EventRepositoryDao;
import com.fidectus.eventlog.services.EventLogService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Profile;

@Profile("service-test")
@TestConfiguration
public class ServiceTestConfig {
    @SpyBean
    public EventLogService eventLogService;

    @SpyBean
    public EventRepositoryDao eventRepositoryDao;

    @MockBean
    public EventRepository repository;
}
