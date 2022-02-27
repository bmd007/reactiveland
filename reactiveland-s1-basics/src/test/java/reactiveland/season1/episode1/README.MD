# Just a box!
## [Lecture](http://youtube.com/mm7amini/reactiveland/season1/episode1)

### New paradigms:

#### River, pipeline, tap
   - ![bleiksa river iceland](bleiksa-river-waterfall-iceland.jpeg)

#### Declarative vs imperative
    Declerative: 
      Mono.just("number: 12")
        .map(text -> text.split(":")[1])
        .map(String::trim)
        .map(Interger::valueOf)
        .filter(number -> number % 2 == 0)
        .subscribe(evenNumber -> print(number + " is even");

    -------------------------
    Imperative:
    String text = "number 12";
    String numberWithoutSpace = text.split(":").trim();
    Integer number = Integer.valueOf(numberWithoutSpace);
    If(number % 2 == 0){
        print(number + " is even")
    }
    
### Blocking vs nonblocking (and optional)
    public X findById(String id){
        return dbConnection.query(....)
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

### subscribe()
    XMono.subscribe(x -> );

![pipe tap](pipe-tap.jpeg)

### block()

### StepVerifier


[//]: # ( - Chaos vs order)
[//]: # ( - River)
[//]: # ( - Stream)
[//]: # (functional programming, pure functions, only one thing)
[//]: # (Optional, Collection, Exception, null <--> Mono, Flux, Error, Empty)