# Testing Internet

![Test](test.jpeg)

## [WebTestClient](https://www.baeldung.com/spring-5-webclient)

#### rest assured vs webTestClient

    when()
      .get("/lotto/{id}", 5).
    then()
     .statusCode(200)
     .body("lotto.lottoId", equalTo(5), "lotto.winners.winnerId", hasItems(23, 54))

-------------------------

        //when
        webTestClient.get()
                .uri("/api/counter/message/sent/from/mahdi")
                .exchange()
                .//then
                .expectStatus()
                .isOk()
                .expectBody(MessageCounterDto.class)
                .value(counterDto -> assertEquals(1, counterDto.getNumberOfSentMessages()));

### Dependencies

    org.springframework.boot
        spring-boot-starter-test
                    org.springframework
                            spring-test
                                    WebTestClient

### Initialization

    WebTestClient testClient = WebTestClient
        .bindToServer()
        .baseUrl("http://localhost:8080")
        .build();

or you can use

    @ActiveProfiles("test")
    @ExtendWith({SpringExtension.class})
    @SpringBootTest
    @AutoConfigureWebTestClient

    @Autowired
    WebTestClient webTestClient;

or some other ways, depending on what you are going test!

### test a get endpoint

    webTestClient.get()
        .uri("/resource")
        .exchange()
        .expectStatus().isOk()
        .expectBody().isEmpty()  

### test a post endpoind

    webTestClient
        .post()
        .uri("/resource")
        .exchange()
        .expectStatus().isCreated()
        .expectHeader().valueEquals("Content-Type", "application/json")
        .expectBody().jsonPath("field").isEqualTo("value");

[//]: # (webflux)

[//]: # (hot publisher vs cold publisher https://projectreactor.io/docs/core/release/reference/#reactor.hotCold)

[//]: # (defer vs just)

[//]: # (pure functions and side effects)

