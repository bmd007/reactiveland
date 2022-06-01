# [R2DBC](https://r2dbc.io)

![Test](https://r2dbc.io/images/PVLG-R2DBC-Logo-RGB.png)

#### jdbc vs r2dbc

    X savedX = (CrudRepository<X, ID>) repo.save(x)

-----------------------------------------------

    Mono<X> insertedXMono = (R2dbcEntityOperations) dbOps.insert(x)
    Mono<X> foundXMono = (ReactiveCrudRepository<X, ID>) reactiveRepo.findById(id)
    ... R2dbcEntityTemplate ...

### Dependencies

    org.springframework.boot
        spring-boot-starter-data-r2dbc

plus a db driver like `org.postgresql:r2dbc-postgresql`. Please note that reactive DB drivers are different from
blocking ones.

### Initialization

1. Dependencies on classpath
2. Configuration
```
   r2dbc:
       url: r2dbc:postgres://username:password@host:$port/database
       username: ${postgres.username}
       password: ${postgres.password}
   ```
**Note that url structure is different from jdbc**

3. @Repository or inject R2dbcEntityOperations or inject R2dbcEntityTemplate

### JPA repositories compatibility

Most of your knowledge on how to use JDBC based JPA repositories are applicable for ReactiveCrudRepository as well.
They follow the same contact basically.
The
notable [difference](https://docs.spring.io/spring-data/r2dbc/docs/current/reference/html/#r2dbc.entity-persistence.id-generation)
is that to insert a new record either **implement Persistable by your entity, or use something other than
ReactiveCrudRepository for insert.**

Also, annotations like `@Table("xxxx")` and `@Id` work the same way here.

And ofcourse, methods return reactive type with R2DBC. So test your code using StepVerifier