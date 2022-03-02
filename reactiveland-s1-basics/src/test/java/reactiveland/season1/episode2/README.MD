# GarbageDryFish!
## [Lecture](http://youtube.com/mm7amini/reactiveland/season1/episode1)

#

#### River, pipeline, tap
![bleiksa river iceland](bleiksa-river-waterfall-iceland.jpeg)

#### Declarative vs imperative
    x == null ?
    
    try(){}catch(Exception e){}
     
    x = "something long" 
    y = x.length
    z = x ˆ 10
    f = (z % 2 == 2) 
    if (f){
        print(z) 
    }

    -------------------------
    Empty (signal): DRY
    
    Error (signal): GARBAGE

    x: fish
    map(x -> )

    
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

[//]: # (Stream)
[//]: # (if eles filter empty)
[//]: # (doOnNext, Collection, Flux, filter, recursive <--> while)
[//]: # (functional programming, pure functions, only one thing)
