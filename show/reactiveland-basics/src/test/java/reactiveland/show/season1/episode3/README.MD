# Fishing Net!

![Fishing Net](Fishing_net.jpeg)

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
        .switchIfEmpty() or .defaultIfEmpty

### filter

    Mono<?> = Mono.just(54)
            .filter(number -> true/false)
            .map(number -> )//number will reach map if filter condition is true

### defaultIfEmpty(3)

    Mono<X> xMono = ........
        .defaultIfEmpty(new X());     

Whenever the mono is empty, the value inside defaultIfEmpty will be used.

That value should have the same type as the last part of the stream.

    Mono<String> xMono = ........
        .map(x -> x.toString())
        .defaultIfEmpty("some string");     

### switchIfEmpty(Mono.just(2))

    Mono<X> xMono = ........
        .switchIfEmpty(Mono<X>);

Whenever the mono is empty, the main (empty) stream will be replaced by the mono inside switchIfEmpty.
It's like a mapping the main mono into another default mono when empty.
The type of default mono should be similar to the type of last part of the stream before switchIfEmpty

    Mono<String> xMono = ........
        .map(x -> x.toString())
        .switchIfEmpty(Mono<String>);
