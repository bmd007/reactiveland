# Internet
![Internet](internet.jpeg)

## WebClient

#### Declarative vs imperative
    RestTemplate restTemplate = new RestTemplate();
    String fooResourceUrl = "http://localhost:8080/spring-rest/foos";
    ResponseEntity<String> response = restTemplate.getForEntity(fooResourceUrl + "/1", String.class);
-------------------------
    webClient.post()
        .uri("/sendMessage")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue("""
                {
                  "chat_id": %s,
                  "text": "%s"
                }
                """.formatted(666, "hello"))
        .retrieve()
        .bodyToMono(String.class)
        .doOnError(e -> LOGGER.error("error while sending message", e))
        .onErrorMap(e -> )
        .switchIfEmpty(Mono.error(new notFoundException()))
        .subscribe(responseBody -> );

### Dependencies
    org.springframework.boot
        spring-boot-starter-webflux 
            spring-boot-starter-reactor-netty
            .........
                io.projectreactor
                        reactor-core
           org.springframework
                      spring-webflux 
                              WebClient

### Initialization
    var spotifyWebClient = WebClient.builder()
            .baseUrl("https://spotify.com")
            .build();

### get one element
    Mono<X> x1 = webClient.get()
        .uri("/getX/132")
        .retrieve()
        .bodyToMono(X.class)
        .doOnError(e -> LOGGER.error("error while fetching x1", e));

### get many elements
    Flux<X> x1 = webClient.get()
        .uri("/getX/132")
        .retrieve()
        .bodyToFlux(X.class)
        .doOnError(e -> LOGGER.error("error while fetching x1", e));

### Testing and assertion
StepVerifier is still useful here.

     StepVerifier.create(webClinet......)
        .expectNext(x1)
        .expectComplete()
        .verify();

[//]: # (webflux)
[//]: # (webTestClient)
[//]: # (hot publisher vs cold publisher https://projectreactor.io/docs/core/release/reference/#reactor.hotCold)
[//]: # (defer vs just)
[//]: # (pure functions and side effects)

