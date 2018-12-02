# Transfer REST Application

Java based REST application for creating accounts and transfers between them.

Domain model is simplified, so accounts do not have named owners and are identified by simple ids instead of full IBANs.
Requirements regarding transfers were not specific so there is support for both internal transfers between 2 accounts registered
in the system as well as external deposits and withdrawals from/to registered account. 

Regarding technical design I decided to use Jersey as JAX-RS implementation. I also use HK2 provided with Jersey for DI. Persistence layer is served by Ebean ORM.

Solution has been build and tested on OS X and Linux.

## Getting Started

### Prerequisites

* JDK8 or higher
* Maven 3.5.3 or higher (support for maven tiles used in pom.xml needed)

### Installing

It is recommended to build project from command line. Building from IDE may require installing Ebean enhancement plugin.

Go to project directory. Usual maven build command should work fine:

```
mvn clean package
```

In target directory there is transferApp-1.0.jar file. You can run it as below:

```
java -jar transferApp-1.0.jar
```

Server is started at [http://localhost:8080/rest/](http://localhost:8080/rest/)

WADL is available at [http://localhost:8080/rest/application.wadl](http://localhost:8080/rest/application.wadl)

At server initialization 2 initial accounts with ids 1 and 2 are created for tests purposes.

## Running the tests

```
mvn clean test
```

## Built With

* [Jersey](https://jersey.github.io/) - JAX-RS Reference Implementation
* [Ebean ORM](https://ebean.io/) - Ebean ORM for Java/Kotlin
* [Maven](https://maven.apache.org/) - Dependency Management

## Authors

[**Piotr Bednarczyk**](mailto:job@piotrbednarczyk.com)