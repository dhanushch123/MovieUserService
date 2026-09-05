# Configuration

All secret configuration is supplied through environment variables. `application.properties` only maps Spring property names to those variables.

1. Copy `.env.example` to `.env` and fill in the values.
2. In IntelliJ, add those key/value pairs to the application's Run Configuration under **Environment variables**. Alternatively, define them in your shell before running `./mvnw spring-boot:run`.
3. Do not commit `.env`.

Spring Boot does not load a `.env` file automatically. The file is a local template; your IDE, shell, or deployment platform must export its values.

The required variables are listed in `.env.example`. Variables without defaults must be set before the service starts.

For production, keep `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE` limited to `health,info` unless a protected operational endpoint is intentionally needed, and leave `MANAGEMENT_INFO_ENV_ENABLED=false` to avoid exposing environment-derived information.