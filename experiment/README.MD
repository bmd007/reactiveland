## reactive experiment

#### in context of luna

I’m conducting some experiment out of boredom comparing webflux with servlet stack in context of a RSA based authentication process (similar to LUNA).
I added concepts like DDD and technologies like graalvm to the mix.
Results are interesting.
Spring boot 3 works nicely with graalvm if you know how to tame it.

Early results shows 1.5 times more throughput when using non-blocking stack compared to a blocking stack.
The attached picture contains results from different runs, but all in all the yellow line is consistently 1.5 times higher than green line.
Which means with webflux, on my machine, I’m completing 1.5 times more login rounds.
One complete login round involves calling different APIs to get a challenge, capture it, sign it, verify it and finally authentication using it.
I used H2 for sake of simplicity. Of course, redis is the better option.

More concerete experiments will be done if close future and results will be published here.

Please have a look at implementations in this folder and send corrective feedbacks (RSocket part is not ready yet).
![img.png](img.png)

Of course, my implementations are opinionated.

I have 3 main goals with these experiments:

1. [ ]  To show that asynchronous is better than blocking when it comes to IO
2. [ ]  There is no way to write non-blocking code with exactly the same semantics of blocking code
3. [ ]  Encouraging usage of RSocket as the dominantly better option compared to servlet and webflux
