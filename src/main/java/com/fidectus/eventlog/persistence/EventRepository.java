package com.fidectus.eventlog.persistence;

import com.fidectus.eventlog.dto.Event;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Interface definition for additional operations for Repository
 * Implementation bean is instantiated by Spring at runtime.
 */
@Repository
public interface EventRepository extends CrudRepository<Event, UUID> {
    List<Event> findAllByUserId(UUID userId);
}
