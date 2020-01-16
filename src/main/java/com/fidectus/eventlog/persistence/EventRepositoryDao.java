package com.fidectus.eventlog.persistence;

import com.fidectus.eventlog.dto.Event;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * DAO layer for DB access
 *
 * Javadocs for exceptions thrown by the CrudRepository implementation are sparse.
 * DataAccessException (from StackOverflow) and IllegalArgumentExceptions are the only ones I could find.
 * Catch them if they occur and rethrow as a 500 Internal Server Error.
 */
public class EventRepositoryDao {
    private static final Log log = LogFactory.getLog(EventRepositoryDao.class);

    @Autowired
    private EventRepository repository;

    public Event addEvent(Event event) {
        Event eventRecord;
        try {
            eventRecord = repository.save(event);
        } catch (DataAccessException ex) {
            log.error("Data access exception in addEvent: " + ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (IllegalArgumentException ex) {
            log.error("Event passed to DB is null");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        log.info("Added event in DB with ID: " + event.getEventId());
        return eventRecord;
    }

    public List<Event> getEventsForUserId(UUID userId) {
        List<Event> events;
        try {
            log.info("Getting events for user id: " + userId);
            events = repository.findAllByUserId(userId);
        } catch (DataAccessException ex) {
            log.error("Data access exception in getEventsForUserId: " + ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (IllegalArgumentException ex) {
            log.error("UserID passed to DB is null");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return events;
    }

    public Event getEvent(UUID eventId) {
        Optional<Event> event;
        try {
            log.info("Searching for event with id: " + eventId);
            event = repository.findById(eventId);
        } catch (DataAccessException ex) {
            log.error("Data access exception in getEvent: " + ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (IllegalArgumentException ex) {
            log.error("EventID passed to DB is null");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return event.orElse(null);
    }
}
