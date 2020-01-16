package com.fidectus.eventlog.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fidectus.eventlog.Application;
import com.fidectus.eventlog.dto.Event;
import com.fidectus.eventlog.persistence.EventRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
class EventLogV1IntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EventRepository repository;

    @AfterEach
    private void teardown() {
        repository.deleteAll();
    }

    @Test
    void givenEvent_postEventThenGetEvent_returnsEvent() throws Exception {
        // Arrange
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Required for Jackson to parse LocalDateTime format
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Will write as JSON array otherwise. Want 2020-01-01T23:59 format

        //Event data
        UUID eventId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        Event.EventType eventType = Event.EventType.USER_DELETED;
        UUID userId = UUID.fromString("10000000-1111-1111-1111-000000000001");
        LocalDateTime time = LocalDateTime.of(2020, 1, 1, 12, 0, 0);

        Event event = new Event(eventId, eventType, userId, time);
        List<Event> events = Collections.singletonList(event);

        String eventAsJson = objectMapper.writeValueAsString(event);
        String listEventsAsJson = objectMapper.writeValueAsString(events);

        String postJson = "{\"eventId\": \"" + eventId + "\", " +
                "\"time\": \""+ time + " \"," +
                "\"eventType\": \""+ eventType + "\"," +
                "\"userId\": \"" + userId + "\"" +
                "}";

        // Act
        // POST event to API
        ResultActions postResult = mvc.perform(post("/api/v1/event")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(postJson))
                .andDo(MockMvcResultHandlers.print());
        // GET event by its id
        ResultActions getEventResult = mvc.perform(get("/api/v1/event/" + eventId))
                .andDo(MockMvcResultHandlers.print());
        // GET all events by user
        ResultActions getEventsForUserResult = mvc.perform(get("/api/v1/event/user/" + userId))
                .andDo(MockMvcResultHandlers.print());

        // Assert
        postResult.andExpect(status().isCreated()).andExpect(content().string(eventAsJson));
        assertEquals(event, repository.findById(eventId).orElse(null)); // Event is in DB

        getEventResult.andExpect(status().isOk()).andExpect(content().string(eventAsJson)); // Event itself

        getEventsForUserResult.andExpect(status().isOk()).andExpect(content().string(listEventsAsJson)); // List of 1 Event
    }
}
