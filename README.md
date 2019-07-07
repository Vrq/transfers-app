# transfers-app

How to start the transfers-app application
---

1. Run `mvn clean install` to build your application
2. Start application with `java -jar target/transfers-app-1.0-SNAPSHOT.jar server config.yml`
3. To check that your application is running enter url `http://localhost:8081`


Some caveats:
---
- /deposit endpoint should be restricted
- the TransferApplicationIntegrationTests is testing correct concurrent processing
and shares application instance and db - in the future it should be broken
down into scenarios that recreate the testing environment every time