# Fidectus project

## Project Structure
```
|-- main    : Prodcution code
|   |-- java/com/fidectus/eventlog
|   |   |-- config      : Code for loading config and Bean init
|   |   |-- controller  : Entry point for handling/validating requests 
|   |   |-- dto         : Data Transfer Objects for marshalling to/from JSON
|   |   |-- services    : Main code logic for handling with requests and external APIs 
|   |   `-- Application  : Entry point for Spring Boot init
|   `-- resources
|       `-- application.properties : Config keys
`-- test    : Test code
    `-- java/com/fidectus/eventlog
        |-- config      : Code for mock/spy Beans init
        |-- integration : Integration tests for Application 
        `-- service     : Service tests for Application 
```

## Installation / Requirements
* Java 11  - and appropriate environment variables set such as `$JAVA_HOME`.
* Maven 3 - `mvn` binary path will need to be set on your `$PATH` environment variable.

Optionally, there is a `mvnw` in the root of the repo. This can be used in place of your own Maven installation.
Also, you can import the project into IntelliJ and compile/run tests via the IDE.

All other dependencies will be downloaded during build, such as Spring Boot, Mockito, etc.

## Running
To compile and run tests, execute this command in your shell/command prompt
 
```shell script
mvn clean verify
```

This will produce `/target/sample-event-log-app-0.1.0-SNAPSHOT.jar`. To run the Spring Boot application, execute

```shell script
java -jar target/sample-event-log-app-0.1.0-SNAPSHOT.jar
```

This will start the Spring Boot application on `http://localhost:8080` by default.
I recommend using [Postman](https://www.getpostman.com/) to send requests to the application.

## Endpoints
### POST `{host}/api/v1/event`
This endpoint creates an entry in the database for the submitted Event and returns the Event .

The request body must be of content type `application/json` and the JSON in the body must be in the following format with all fields mandatory:
```json
{
  "eventId": "<UUID>",
  "eventType": "<element on [USER_REGISTRATION, USER_DELETED, USER_DEACTIVATED, USER_UPDATED_REGISTRATION_INFO]>",
  "userId": "<UUID>",
  "time": "<Date-Time in the format of 2020-01-01T23:59 (ISO-8601)>"
}
```
Additional fields will be ignored and/or overwritten if present. 
I made the assumption that the caller to the API will generate the `eventId`, although as it is a UUID, the chance of collisions is negligible.

If successfully created in the database, the API will return a `201 CREATED` along with the Event in the DB.

If the JSON is malformed or missing fields, the API returns a `400 BAD REQUEST`.

Event JSON returned
```json
{
  "eventId": "<UUID>",
  "eventType": "<element on [USER_REGISTRATION, USER_DELETED, USER_DEACTIVATED, USER_UPDATED_REGISTRATION_INFO]>",
  "userId": "<UUID>",
  "time": "<Date-Time in the format of 2020-01-01T23:59 (ISO-8601)>",
  "eventHash": "Integer hash of the event generated from the eventType, userId, and time"
}
```

### GET `{host}/api/v1/event/{eventId}`
This endpoint queries the database and retrieves the Event with the supplied `eventId` (in valid UUID format).
If not found, the endpoint returns a `404 NOT FOUND` status. 

JSON returned is of the same format as the POST endpoint.

### GET `{host}/api/v1/event/user/{userId}`
This endpoint queries the database and retrieves all Events that belong to the supplied `userId` (in valid UUID format).
If none are found, the endpoint simply returns an empty list with `200 OK` status.

JSON returned (Events in list format)
```json
[
   {"eventId": "..."},
   {"eventId": "..."},
   {"eventId": "..."} 
]
```

## Known Bugs
SpotBugs is enabled on this project for static analysis. Execute 
```shell script
mvn clean verify
``` 
and provided all tests pass, then execute 
```shell script
mvn spotbugs:gui
``` 
to bring up a GUI for inspecting what bugs exist in the code. 

Currently SpotBugs identifies 2 bugs as `Unconfirmed cast from Throwable` where `Throwable` is cast to specific exceptions
in the `handleException()` method in `EventLogV1Controller.java`. As there are checks beforehand using `instanceof` to ensure
that a cast is suitable, this seems like a limitation of SpotBugs.

## Testing
The repo contains two test suites for the application, integration and service.

In the service tests, external calls are mocked out (such as to the database) and expected results given.

In the integration tests, an actual in-memory DB (Spring JPA) is used and the DB is checked after the POST call to ensure 
that the Event has been created successfully in there.
The two GET APIs are then called (which will make calls to the DB for Events retrieval) and their results are compared to the expected results.

#### Test Suites
My method of testing this API is to do full service tests, that is, we send a crafted request to our microservice instance, and put expectations on what the response should be.
This method can cut down on the amount of extraneous unit testing that is done at the class level, and allows us to change the internal workings of the API without having to change a
bunch of unit tests at the same time, so long as the API response remains the same expected value.
 
Various Spring Beans (from `EventLogConfig`) will be mocked/spied in different test suites, meaning we can have them return whatever we choose to.
We can also verify when these mock beans are called with specific parameters and how many times.
This is done using the Mockito framework.

## Code coverage
The test suites were run through IntelliJ with code coverage turned on. 
The main areas lacking are around exception handling in the `EventRepositoryDao`. 
As the docs around the `CrudRepository` exceptions thrown are a bit lacking, I included some extra exceptions that may not occur in production usage.

| Classes               |   Line Coverage   |
|-----------------------|:-----------------:|
| EventLogV1Controller  |        97%        |
| EventLogService       |        100%       |
| EventRepositoryDao    |        53%        |
| Overall               |        74%        |

## Potential improvements

* The `eventHash` field of `Event.java` could be more complex than a simple integer `Objects.hash()`, using something like SHA-256.
I decided to keep it simple at the risk of premature optimisation, given the unknowns regarding the likely number of Events this service will deal with.
* The `eventId` field of `Event.java` would make more logical sense to be handled by this service if its job is to log Events and then
return the generated value to the caller when it's created in the DB.
