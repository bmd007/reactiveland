# Just a box!
## [Lecture](http://youtube.com/mm7amini/reactiveland/season1/episode1)

#

#### River, pipeline, tap
![bleiksa river iceland](bleiksa-river-waterfall-iceland.jpeg)

#### Declarative vs imperative
    Optional.ofNullable(findXById("some id"))
        .orElseGet( () -> new X())
    -------------------------
    if(findXById("some id") == null){
        return new X()
    }
    
### Blocking vs nonblocking
    public X findById(String id){
        return jdbcConnection.query(....)
    }
    
    X x = Optional.nullable(findXById("some id"))
        .orElseGet( () -> new X());
    
You have a box, you wait until you know if `findById` can put something in it or not.

#### Mono 
    public Mono<X> findById(String id){
        retunr reactiveDbConnecion.query(...)
    }
    Mono<X> XMono = findById("some id");

You have a box, `findById` might at some point put ONE thing in it. 

### just()
    Mono<T> mono = Mono.just(new T());

### subscribe()
    Mono<T> mono = Mono.just(new T());
    mono.subscribe(t -> );

![pipe tap](pipe-tap.jpeg)

### block()
    Mono<T> mono = ...;
    T t = mono.block();

### StepVerifier
    Mono<T> mono = ...;
    StepVerifier.create(mono)
        .expectNext()
        .assertNext(next -> )
        .expectComplete()
        .verify()

[//]: # ( - Chaos vs order)
[//]: # ( - River)
[//]: # ( - Stream)
[//]: # (functional programming, pure functions, only one thing)
[//]: # (Optional, Collection, Exception, null <--> Mono, Flux, Error, Empty)