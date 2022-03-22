# GarbageDryFish!

#### River, pipeline, tap
![bleiksa river iceland](bleiksa-river-waterfall-iceland.jpeg)

#### Declarative vs imperative
    x == null ?
    
    try(){}catch(Exception e){}
     
    x = "something long".length()
    y = x ˆ 10
    
    -------------------------
    Empty (empty signal): DRY
    
    Error (error signal): GARBAGE

    map(x -> y = x ^ 10)
    x (next signal): FISH
  
    
### Map
    Mono<?> = Mono.just("a legnthy string)
        .map(string -> string.length)
        .map(stringLength -> ?   )

mapping a string to its length and what next ? 
    
### doOnError
    Mono.just("something")
        .map(x -> x.a)
        .map(a -> errorPronefunction(a))
        .doOnError(throwable -> logger.error("something bad happened", throwable));
        ....

do something asynchronously where an error happens in the stream

### onErrorMap
    ...
        .onErrorMap(currentError -> new throwable)

map current error in the stream into another one 

### expectErrorMatches
    Mono<X> errorMono = mono containing error.
        
    StepVerifier.create(errorMono)
                .expectErrorMatches(throwable -> false/true)
                .verify();

asserting the error in the mono  

[//]: # (if eles filter empty)
[//]: # (doOnNext, Collection, Flux, filter, recursive <--> while)
[//]: # (pure functions)
