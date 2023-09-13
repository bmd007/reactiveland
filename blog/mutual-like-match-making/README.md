# Events, my dear boy, events.

## How to capture two people liking each other, only with events, in order to announce them matched? (Event-Driven match making)

### Story

    Alice, Bob and John are schrolling through a match making app, looking at people living close enough to them. Liking and unliking them.
    At some point, Alice likes Bob. Later Alice dislikes John.
    A few minutes later, Bob likes Alice too. Boom, the app now tells Alice and Bob that they have mutually likes each other (matched).

### Context
 - All we have are events and state stores.
 - We are going to use KafkaStreams and stream processing notions. 
 - I assume you know enough about them, apart from general knowledge of Event-Driven systems.
 - This post is not about stream processing being a good or bad approach here. Rather, a thought experiment on Event-Driven systems.

### Event storming
 - Alice is swiping left and right through available* people.
 - Alice likes Bod
 - Alice dislikes John
 - Bob likes Alice (likes back)
 - Alice and Bob are matched

       *Available people are people who are doing the same, swiping. 
       There can be a time difference between when each person starts swiping. But as long as it's not too different, it should still be considered available.
       Other parameters, like The location from which has started swiping, can be used to limit who is available to whom. 
       In this post, we skip all of them as they can be handled without introducing a change to the overall system design.


 - Bob is liked by Alice (passive version of Alice like Bob event)

### Common language
 - People who are using the application and looking for a partner are users of the application.
        During the time that they are swiping and not concerned with their matches, we can call them `swiper`. 
        In the context of managing matches, we can come up with a different name. But that part is outside the context of this post.
 - When a `Swiper` likes someone, we call him/her `Liker` . 
 - When a `Swiper` is liked by someone, we call him/her `Likee` .


### Event definitions
 - SwiperStartedLookingForPartnerEvent(String swiper, location, time, ...)
 - SwiperLikedAnotherSwiper(String liker, String likee)
 - SwiperIsLikedByAnotherSwiper(String liker, String likee)
 - SwiperIsMatchedWithAnotherSwiper(String matchPartyA, String matchPartyB)
    
        All the mentions of `swiper`, `likee`, `liker` and `matchParty` in the events above (that have type String), are user identifier of the user playing the corresponding role.

