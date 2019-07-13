# transfers-app

How to start the transfers-app application
---

1. Run `mvn clean install` to build your application
2. Start application with `java -jar target/transfers-app-1.0-SNAPSHOT.jar server config.yml`
3. To check that your application is running enter url `http://localhost:8081`


Some caveats:
---
- /deposit endpoint should be restricted
- need to add a dependency injection framework (like Guice) in the future
- more fine grained error handling within the DAOs is recommended

Technologies used:
---
- Dropwizard 1.3.12 (includes: Jetty, Jersey, Jackson)
- Hibernate 
- H2 database
- JUnit 5