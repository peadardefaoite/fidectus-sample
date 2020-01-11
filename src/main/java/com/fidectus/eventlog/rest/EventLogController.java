package com.fidectus.eventlog.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/event")
public class EventLogController {

    @GetMapping
    public ResponseEntity getTransactionById(){
        //TODO: Implement GET by id functionality...
        return ResponseEntity.ok().build();
    }

}
