# fidectus-code-sample
This repository is a starting point for submitting a code sample to Fidectus. It contains a basic 
Java application to get you started, and some instructions on what we are looking for you to implement as a sample. We ask
that you fork this repository, and modify your fork as needed.

# Coding Exercise

##  Problem Statement
Implement an "event log" RESTful API. 

The API allows other services to log 
simple events such as user registration errors.
Focus is on the service itself and not non-functional 
requirements such as authentication.

## Technology Stack
This application should use the following:
* Java 11 or greater
* Spring Boot
* Maven

## Events
Events are made up of the following fields, all of which are required:
* A unique event ID
* An event type
  * Event types should be limited to
    * User registration
    * User deleted
    * User deactivated
    * User updated registration information
* A user ID
* The time the event took place
* An event hash, generated from the user id, time, and event type


## API Specification
* The API should provide a way to get events by a given ID, a way to create new events, and a way to search for events by a given user id.
* The API should not provide any means up updating or deleting events, as this log is meant to be immutable to consumers.
* The API should only support JSON
* The API should enforce required fields

## Storage Requirements
This application should be capable of storing data in memory -- save data as you see fit:

1. A simple Spring Data/In memory database solution
   - https://spring.io/guides/gs/accessing-data-jpa/ and https://spring.io/guides/gs/accessing-data-rest/ provide excellent samples of this
2. If you are not familiar with Spring Data/storage, feel free to implement your own. This can be as simple as an ArrayList or similar that
stores events, or something of your own design.

The objective here is to simply persist events by any means necessary, focus on well-written code and component architecture rather than persistence.

## Testing requirements
We are looking for you to implement well tested code for this sample so unit and integration tests are in scope.

The jacoco plugin is provided to view unit test coverage report. Note that this does not mean we expect
100% coverage, but we do ask that you implement what you feel is reasonable.
