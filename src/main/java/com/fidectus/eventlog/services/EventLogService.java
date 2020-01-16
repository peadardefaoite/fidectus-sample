package com.fidectus.eventlog.services;

import com.fidectus.eventlog.dto.Event;
import com.fidectus.eventlog.persistence.EventRepositoryDao;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

/**
 * Middle-layer class between Controller and Persistence layers.
 * Additional logic can be added here if the Events from the DB/Controller need something done with them.
 */
public class EventLogService {
    private static final Log log = LogFactory.getLog(EventLogService.class);

    @Autowired
    private EventRepositoryDao eventRepositoryDao;

    public List<Event> getEventsForUser(UUID userId) {
        return eventRepositoryDao.getEventsForUserId(userId);
    }

    public Event getEventById(UUID eventId) {
        return eventRepositoryDao.getEvent(eventId);
    }

    public Event logEvent(Event event) {
        return eventRepositoryDao.addEvent(event);
    }
}
