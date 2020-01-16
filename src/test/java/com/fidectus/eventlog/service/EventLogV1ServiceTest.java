package com.fidectus.eventlog.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fidectus.eventlog.config.ServiceTestConfig;
import com.fidectus.eventlog.controller.EventLogV1Controller;
import com.fidectus.eventlog.dto.Event;
import com.fidectus.eventlog.persistence.EventRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EventLogV1Controller.class)
@ActiveProfiles(profiles = "service-test")
@Import(ServiceTestConfig.class)
class EventLogV1ServiceTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventRepository repository;

    @BeforeEach
    private void setup() {
        reset(repository);
    }

    @AfterEach
    private void teardown() {
        reset(repository);
    }

    @Test
    void NotExistentAPI_isNotFound() throws Exception {
        //Arrange

        //Act
        ResultActions resultsActions = mockMvc.perform(get("/api/v1/NonExistentAPI"))
                .andDo(MockMvcResultHandlers.print());

        //Assert
        resultsActions.andExpect(status().isNotFound());
    }

    @Test
    void givenEmptyJson_PostCreateEvent_returnsBadRequest() throws Exception {
        //Arrange

        //Act
        ResultActions resultsActions = mockMvc
                .perform(post("/api/v1/event").content("{}").contentType("application/json"))
                .andDo(MockMvcResultHandlers.print());

        //Assert
        MvcResult result = resultsActions.andExpect(status().isBadRequest()).andReturn();
        assertTrue(result.getResponse().getContentAsString().contains("Missing JSON field"));
    }

    @Test
    void givenBadJson_PostCreateEvent_returnsBadRequest() throws Exception {
        //Arrange
        String badEventIdJson = "{\"eventId\": \"1234\", " + // Not a UUID
                "\"time\": \"" + LocalDateTime.now().toString() +"\"," +
                "\"eventType\": \"USER_REGISTRATION\"," +
                "\"userId\": \"10000000-1111-1111-1111-000000000001\"" +
                "}";

        String badTimeJson = "{\"eventId\": \"00000000-0000-0000-0000-000000000000\", " +
                "\"time\": \"2020-102-340T123:45 \"," + //Invalid time
                "\"eventType\": \"USER_REGISTRATION\"," +
                "\"userId\": \"10000000-1111-1111-1111-000000000001\"" +
                "}";

        String badEventTypeJson = "{\"eventId\": \"00000000-0000-0000-0000-000000000000\", " +
                "\"time\": \"" + LocalDateTime.now().toString() +"\"," +
                "\"eventType\": \"SOME_OTHER_ENUM\"," + //Incorrect Enum value
                "\"userId\": \"10000000-1111-1111-1111-000000000001\"" +
                "}";

        //Act
        ResultActions badEventIdResultsActions = mockMvc
                .perform(post("/api/v1/event").contentType("application/json").content(badEventIdJson))
                .andDo(MockMvcResultHandlers.print());

        ResultActions badTimeResultsActions = mockMvc
                .perform(post("/api/v1/event").contentType("application/json").content(badTimeJson))
                .andDo(MockMvcResultHandlers.print());

        ResultActions badEventTypeResultsActions = mockMvc
                .perform(post("/api/v1/event").contentType("application/json").content(badEventTypeJson))
                .andDo(MockMvcResultHandlers.print());

        //Assert
        MvcResult badEventIdResult = badEventIdResultsActions.andExpect(status().isBadRequest()).andReturn();
        assertTrue(badEventIdResult.getResponse().getContentAsString().contains("Invalid UUID format"));

        MvcResult badEventTypeResult = badEventTypeResultsActions.andExpect(status().isBadRequest()).andReturn();
        assertTrue(badEventTypeResult.getResponse().getContentAsString().contains("Invalid eventType"));

        MvcResult badTimeResult = badTimeResultsActions.andExpect(status().isBadRequest()).andReturn();
        assertTrue(badTimeResult.getResponse().getContentAsString().contains("Invalid Date-Time format submitted"));
        // No calls to DB
        verify(repository, never()).save(any(Event.class));
    }

    @Test
    void givenEventJson_PostCreateEvent_returnsCreatedWithEvent() throws Exception {
        //Arrange
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Required for Jackson to parse LocalDateTime format

        Event event = new Event(UUID.fromString("00000000-0000-0000-0000-000000000000"),
                Event.EventType.USER_REGISTRATION,
                UUID.fromString("10000000-1111-1111-1111-000000000001"),
                LocalDateTime.of(2020, 1, 1, 12, 0, 0));

        String jsonRequest = objectMapper.writeValueAsString(event);
        when(repository.save(event)).thenReturn(event);

        //Act
        ResultActions resultsActions = mockMvc
                .perform(post("/api/v1/event").contentType("application/json").content(jsonRequest))
                .andDo(MockMvcResultHandlers.print());

        //Assert
        MvcResult result = resultsActions.andExpect(status().isCreated()).andReturn(); // 201 Response

        String jsonResponse = result.getResponse().getContentAsString();
        Event actualEvent = objectMapper.readValue(jsonResponse, Event.class);

        // JSON response matches
        assertEquals(event.getEventId(), actualEvent.getEventId());
        assertEquals(event.getEventType(), actualEvent.getEventType());
        assertEquals(event.getUserId(), actualEvent.getUserId());
        assertEquals(event.getTime(), actualEvent.getTime());

        verify(repository, times(1)).save(event);
    }

    @Test
    void givenEventId_GetEvent_returnsEvent() throws Exception {
        //Arrange
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Required for Jackson to parse LocalDateTime format

        UUID eventId = UUID.fromString("00000000-0000-0000-0000-000000000000");

        Event event = new Event(eventId,
                Event.EventType.USER_REGISTRATION,
                UUID.fromString("10000000-1111-1111-1111-000000000001"),
                LocalDateTime.of(2020, 1, 1, 12, 0, 0));

        when(repository.findById(eventId)).thenReturn(Optional.of(event));

        //Act
        ResultActions resultsActions = mockMvc
                .perform(get("/api/v1/event/" + eventId))
                .andDo(MockMvcResultHandlers.print());

        //Assert
        MvcResult result = resultsActions.andExpect(status().isOk()).andReturn(); // 200 Response

        String jsonResponse = result.getResponse().getContentAsString();
        Event actualEvent = objectMapper.readValue(jsonResponse, Event.class);

        // JSON response matches
        assertEquals(event.getEventId(), actualEvent.getEventId());
        assertEquals(event.getEventType(), actualEvent.getEventType());
        assertEquals(event.getUserId(), actualEvent.getUserId());
        assertEquals(event.getTime(), actualEvent.getTime());

        // Verify call to DB
        verify(repository, times(1)).findById(eventId);
    }

    @Test
    void givenEventId_NoSuchEvent_returns404() throws Exception {
        //Arrange
        UUID eventId = UUID.fromString("00000000-0000-0000-0000-000000000000");

        when(repository.findById(eventId)).thenReturn(Optional.empty());

        //Act
        ResultActions resultsActions = mockMvc
                .perform(get("/api/v1/event/" + eventId))
                .andDo(MockMvcResultHandlers.print());

        //Assert
        resultsActions.andExpect(status().isNotFound()); // 404 Response

        // Verify call to DB
        verify(repository, times(1)).findById(eventId);
    }

    @Test
    void givenUserId_GetEventsForUser_returnsEventList() throws Exception {
        //Arrange
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Required for Jackson to parse LocalDateTime format

        UUID userId = UUID.fromString("10000000-1111-1111-1111-000000000001");

        Event eventReg = new Event(UUID.fromString("00000000-0000-0000-aaaa-000000000000"), Event.EventType.USER_REGISTRATION, userId, LocalDateTime.of(2020, 1, 1, 12, 0, 0));
        Event eventUpdated = new Event(UUID.fromString("00000000-0000-0000-bbbb-000000000000"), Event.EventType.USER_UPDATED_REGISTRATION_INFO, userId, LocalDateTime.of(2020, 1, 2, 12, 0, 0));
        Event eventDeactivated = new Event(UUID.fromString("00000000-0000-0000-cccc-000000000000"), Event.EventType.USER_DEACTIVATED, userId, LocalDateTime.of(2020, 1, 3, 12, 0, 0));

        List<Event> events = Arrays.asList(eventReg, eventUpdated, eventDeactivated);

        when(repository.findAllByUserId(userId)).thenReturn(events);

        //Act
        ResultActions resultsActions = mockMvc
                .perform(get("/api/v1/event/user/" + userId))
                .andDo(MockMvcResultHandlers.print());

        //Assert
        MvcResult result = resultsActions.andExpect(status().isOk()).andReturn(); // 200 Response

        String jsonResponse = result.getResponse().getContentAsString();
        List<Event> actualEvents = objectMapper.readValue(jsonResponse, new TypeReference<>() {});

        // JSON response matches
        assertEquals(3, actualEvents.size());
        assertTrue(actualEvents.containsAll(events));

        // Verify call to DB
        verify(repository, times(1)).findAllByUserId(userId);
    }

    @Test
    void givenEventId_DatabaseError_returns500() throws Exception {
        //Arrange
        UUID eventId = UUID.fromString("00000000-0000-0000-0000-000000000000");

        doThrow(new IllegalArgumentException("Error in DB")).when(repository).findById(eventId);

        //Act
        ResultActions resultsActions = mockMvc
                .perform(get("/api/v1/event/" + eventId))
                .andDo(MockMvcResultHandlers.print());

        //Assert
        resultsActions.andExpect(status().isInternalServerError()); // 500 response

        // Verify call to DB
        verify(repository, times(1)).findById(eventId);
    }
}
