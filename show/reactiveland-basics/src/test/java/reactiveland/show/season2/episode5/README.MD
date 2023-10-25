# [Reactive Application](https://www.infoq.com/articles/Servlet-and-Reactive-Stacks-Spring-Framework-5/#:~:text=The%20reactive%20stack%20handles%20higher,applications%20on%20the%20servlet%20stack.)

#### servlet vs reactive
    
![comparison](stack.svg)

### Dependencies

    org.springframework.boot:spring-boot-starter-webflux
    org.springframework.boot:spring-boot-starter-data-r2dbc
    org.postgresql:r2dbc-postgresql
    org.flywaydb:flyway-core
    org.postgresql:postgresql
    io.projectreactor:reactor-test        
    org.testcontainers:postgresql:1.17.2
    org.testcontainers:junit-jupiter:1.17.2

### Initialization

1. Dependencies on classpath
2. Database connection configuration
```
   r2dbc:
       url: r2dbc:postgres://username:password@host:$port/database
       username: ${postgres.username}
       password: ${postgres.password}
   ```
3. Flyway configuration
4. Exposing WebClient beans if needed
5. Creating WebFlux HTTP endpoints

### No `.block()` or blocking behaviour anywhere!
1. No blocking operation should be running in the main application thread.
2. The whole Stack should be reactive. Specifically IO related libraries (network, db, file, ...).
   1. Reactive db drivers are available.
   2. NIO for operating on files.
   3. Webflux for HTTP interfaces.
   4. RSocket for streaming.
   5. You can create reactive types from `Future` and `CompletableFuture`.
3. Initialization phase of application, can contain blocking operations, like flyway.

### Webflux stack
Webflux is non-blocking all the way down (reactive netty or NIO servlet).
![webflux-stack](reactive-stack.png)

[//]: # (Reactive application with Redis)

[//]: # (Back pressure, Reactive systems, RSocket, Kafka, Kafka Streams)

[//]: # (hot publisher vs cold publisher https://projectreactor.io/docs/core/release/reference/#reactor.hotCold)

[//]: # (defer vs just)

[//]: # (pure functions and side effects)
