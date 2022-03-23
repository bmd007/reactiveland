# Fishing Net!

![bleiksa river iceland](Fishing_net.jpeg)

#### Declarative vs imperative
    Integer number = 124;
    if (number % 2 == 0){
        
    }

    if(number == null){
        number = 12;
    }
    -------------------------
    catching fishes from river using a net that allows only some of fishes to pass
    .filter(number -> ) 
  
    empty: (no fish in the river) 
        .switchIfEmpty()
    
### filter
    Mono<?> = Mono.just(54)
            .filter(number -> true/false)
            .map(number -> )//number will reach map if filter condition is true

    
### defaultIfEmpty(3)
    Mono<X> xMono = ........
        .defaultIfEmpty(new X);     
    
Whenever the mono is empty, the value inside defaultIfEmpty will be used. 
That value should have the same type as the last part of the stream.
It's similar to optional.orElse()

### switchIfEmpty(Mono.just(2))
    ...
        
[//]: # (doOnNext, Collection, Flux, filter, recursive <--> while)
[//]: # (pure functions)
