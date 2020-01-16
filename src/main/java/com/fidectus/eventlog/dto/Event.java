package com.fidectus.eventlog.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * DTO & DB object for Events
 */
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class Event {
    @Id
    private UUID eventId;
    private UUID userId;
    private EventType eventType;
    private LocalDateTime time;
    private int eventHash;

    // Default constructor for Repository usage
    protected Event() { }

    @JsonCreator
    public Event(@JsonProperty(value = "eventId", required = true) UUID eventId,
                 @JsonProperty(value = "eventType", required = true) EventType eventType,
                 @JsonProperty(value = "userId", required = true) UUID userId,
                 @JsonProperty(value = "time", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime time){
        this.eventId = eventId;
        this.eventType = eventType;
        this.userId = userId;
        this.time = time;
        updateEventHash();
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
        updateEventHash();
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
        updateEventHash();
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
        updateEventHash();
    }

    public int getEventHash() {
        return eventHash;
    }

    private void updateEventHash() {
        this.eventHash = Objects.hash(eventType, userId, time);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return eventId.equals(event.eventId) &&
                userId.equals(event.userId) &&
                eventType == event.eventType &&
                time.equals(event.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, userId, eventType, time);
    }

    @Override
    public String toString() {
        return "Event{" +
                "eventId=" + eventId +
                ", eventType=" + eventType +
                ", userId=" + userId +
                ", time=" + time +
                '}';
    }

    public enum EventType {
        USER_REGISTRATION,
        USER_DELETED,
        USER_DEACTIVATED,
        USER_UPDATED_REGISTRATION_INFO
    }
}
