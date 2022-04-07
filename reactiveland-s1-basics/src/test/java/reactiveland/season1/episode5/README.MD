# More than one fish

![Fished](fishes.jpeg)

#### Declarative vs imperative
    var fishes = List.of(fish1, fish2, ....)
    var headLessFishes = new ArrayList(); 
    for(fish : fishes){
        headLessFishes.add(cutHead(fish));
    }
-------------------------
    Flux<Fish> headLessFishes = Flux.just(fish1, fish2, ...)
        .map(fish -> cutHead(fish))
        .filter(headLessFish -> ....)

## Flux
Flux is a reactive type which might at some point in time contain one or more elements.
It's equivalent to Collections (Set, List, ...).

### .just
    Flux<X> xs = Flux.just(x1, x2, x3, ....);
    Flux<X> xs = Flux.just(x1);
easiest way to create a Flux 

### .fromIterable();
    Flux<X> xs = Flux.fromIterable(List.of(x1));
    Flux<X> xs = Flux.fromIterable(Set.of(x1));

### .concatWith()
    Flux<X> xs1 = Flux.just(x1, x2);
    Flux<X> xs2 = Flux.just(x3, x4);
    Flux<X> xs = xs1.concatWith(xs2); ----> x1, x2, x3, x4

### .flatMapIterable 
    List<X> list = List.of(x1, x2, x3)
    var container = new Container(list, y, z)
    Mono<Container> mono = Mono.just(container)
    Flux<X> flux = mono.flatMapIterable(Container::list)
When you have an iterable inside a mono, and want a flux containing the elements of iterable.

### Publisher
Is a base reactive type.
Mono and Flux both extend publisher, so most of the apis that we played with using Mono are also available in Flux.
Filer, Map, doOnNext, SwitchIfEmpty and ... .

### Testing and assertion 
     StepVerifier.create(Flux.just(x1,x2))
        .expectNext(x1)
        .expectNext(x2)
        .expectComplete()
        .verify();
StepVerifier works for Flux similar to Mono.

## Assigment
### Learn about flatMapMany through an example
