# Smart Parking Management System — Web Version

Browser-based adaptation of the original Java Swing + SQLite college project.

## Features
- Dashboard showing total, available, and occupied slots
- Book a slot using a vehicle number
- Release occupied slots
- View the latest 100 booking/release events
- Responsive interface for phones and computers

## Technology
Java 17, Spring Boot, Thymeleaf, JDBC, PostgreSQL (online), H2 (local quick test).

## Run locally
1. Install JDK 17 or newer and Maven.
2. Open a terminal in this folder.
3. Run `mvn spring-boot:run`.
4. Visit `http://localhost:8080`.

Local H2 data is in memory and resets when the app stops.

## Online deployment
Deploy as a Java web service. Set these environment variables to the values supplied by your PostgreSQL provider:
- `SPRING_DATASOURCE_URL` (must start with `jdbc:postgresql://`)
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_DATASOURCE_DRIVER` = `org.postgresql.Driver`

Build command: `mvn clean package`
Start command: `java -jar target/smart-parking-web-1.0.0.jar`

Never commit database passwords to GitHub. Add them in your hosting provider's environment settings.

This prototype does not migrate existing desktop SQLite records. Add authentication/authorization before using with real customers.
