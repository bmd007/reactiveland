# Becoming someone's browser history (serving http)

![Test](history.jpeg)

## [WebTestClient](https://www.baeldung.com/spring-5-webclient)

#### servlet (webMvc) vs reactive (webflux)
    
    @PostMapping("/api/capture")
    public Integer capture(@RequestBody @Valid CaptureRequest captureRequest) {
        validationService.validate(captureRequest);
        List<CaptureItem> items = itemExtraxionService.extract(captureRequest);
        var numberOfAffectedItems = xService.y(items);
        return numberOfAffectedItems;
    }
-------------------------

    @PostMapping("/api/capture")
    public Mono<Long> capture(@RequestBody @Valid Flux<CaptureItem> items) {
        return items.filter(validateionService::validateItem)
                .map(xService::yItem)
                .switchIfEmpty(Mono.error(.....))
                .doOnError(e -> ....)
                .count()
    }

### Dependencies

    org.springframework.boot
        spring-boot-starter-webflux 

### Initialization

    1. Dependency on classpath
    2. @RestController on resource class

or you can use create a bean like (fully functional style):
   
    @Bean
    public RouterFunction<ServerResponse> routes() {
        return route(GET("/"),(ServerRequest req)-> ok()
                 .body(
                     BodyInserters.fromObject(
                            ....
                     )
                 )
            );
    }

### webMvc compatibility
Most of your knowledge about how to create servlet based HTTP endpoint using webMvc applies here as well.
The main difference is that your endpoint returns a reactive type. 

### post endpoint returning no content

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/api/answer/nothing")
    public Mono<Void> capture(@RequestBody @Valid Mono<RequestBodyDto> requestBody) {
        ....
        return Mono.empty();
    }
    
### handling errors in an adviser style
This style is not the only possible of handling exceptions.

    @GetMapping(value = "/bad-request")
    public Mono<String> badRequest() {
        return Mono.error(new IllegalArgumentException());
    }
    
    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Illegal arguments")
    @ExceptionHandler(IllegalArgumentException.class)
    public void illegalArgumentHandler() {
    //
    }
