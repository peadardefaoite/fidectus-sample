package com.fidectus.eventlog.controller;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fidectus.eventlog.dto.Event;
import com.fidectus.eventlog.services.EventLogService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller class for endpoints along with some exception handling for requests
 */
@RestController
@RequestMapping("api/v1")
public class EventLogV1Controller {
    private static final Log log = LogFactory.getLog(EventLogV1Controller.class);
    private EventLogService eventLogService;

    @Autowired
    public EventLogV1Controller(EventLogService eventLogService) {
        this.eventLogService = eventLogService;
    }

    /**
     * @param userId User UUID to be queried for events
     * @return List of events that belong to userId. Can be empty if no events found
     */
    @RequestMapping(path = "/event/user/{userId}", method = RequestMethod.GET)
    public List<Event> getEventsForUser(@PathVariable(value="userId") UUID userId) {
        log.info("Received request for events with userId: " + userId);
        return eventLogService.getEventsForUser(userId);
    }

    /**
     * @param eventId Event UUID to be queried for
     * @return Event object if found in DB, or 404 if not found
     */
    @RequestMapping(path = "/event/{eventId}", method = RequestMethod.GET)
    public Event getEventById(@PathVariable(value="eventId") UUID eventId) {
        log.info("Received request for finding event with id " + eventId);
        Event event = eventLogService.getEventById(eventId);
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return event;
    }

    /**
     * @param event Event JSON with fields eventId, usedId, eventType, time
     * @return 201 Created if successful along with event
     */
    @RequestMapping(path = "/event", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createEvent(@RequestBody Event event) {
        log.info("Received request to log event: " + event);

        Event dbEvent = eventLogService.logEvent(event);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(dbEvent);
    }

    // Exception handling for malformed/missing JSON
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity handleException(HttpMessageNotReadableException ex) {
        String errorMessage = "Invalid JSON received";
        if (ex.getCause() instanceof InvalidFormatException) {
            // Fields are not in the proper format
            InvalidFormatException nestedException = (InvalidFormatException) ex.getCause();

            if (nestedException.getCause() instanceof DateTimeParseException) {
                errorMessage = "Invalid Date-Time format submitted. Please use 2000-01-01T12:00 instead.";
            } else if (nestedException.getTargetType().equals(Event.EventType.class)) {
                errorMessage = "Invalid eventType. Choose one of USER_REGISTRATION, USER_DELETED, USER_DEACTIVATED, USER_UPDATED_REGISTRATION_INFO.";
            } else if (nestedException.getTargetType().equals(UUID.class)) {
                errorMessage = "Invalid UUID format.";
            }
        } else if (ex.getCause() instanceof MismatchedInputException) {
            // Required fields are missing OR malformed JSON (missing brace for example)
            MismatchedInputException nestedException = (MismatchedInputException) ex.getCause();

            List<JsonMappingException.Reference> pathReferences = nestedException.getPath();
            // if statement for missing field. pathReferences will contain an entry for the field name.
            // Even if multiple entries are missing, only the first missing one will be in the list.
            if (pathReferences != null && !pathReferences.isEmpty()) {
                errorMessage = "Missing JSON field: " + pathReferences.get(0).getFieldName();
            }
        } else {
            // Ended up with an unexpected JSON exception, log as error to later add code for proper handling.
            log.error("Unrecognised JSON error: " + ex);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"error\": \"" + errorMessage + "\"}");
    }
}
