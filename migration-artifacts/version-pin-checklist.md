# Version Pin Checklist

## Files requiring version updates for Spring Boot 2.6.3 → 3.4.x migration

### build.gradle
- [ ] `org.springframework.boot` plugin: `2.6.3` → `3.4.x`
- [ ] `io.spring.dependency-management` plugin: `1.0.11.RELEASE` → latest
- [ ] `com.netflix.dgs.codegen` plugin: `5.0.6` → latest compatible with Spring Boot 3.x
- [ ] `com.diffplug.spotless` plugin: `6.2.1` → latest
- [ ] `sourceCompatibility`: `11` → `21`
- [ ] `targetCompatibility`: `11` → `21`
- [ ] `mybatis-spring-boot-starter`: `2.2.2` → `3.x` (Jakarta EE compatible)
- [ ] `graphql-dgs-spring-boot-starter`: `4.9.21` → latest compatible with Spring Boot 3.x
- [ ] `jjwt-api/impl/jackson`: `0.11.2` → latest
- [ ] `joda-time`: `2.10.13` → latest (or evaluate removal)
- [ ] `sqlite-jdbc`: `3.36.0.3` → latest
- [ ] `rest-assured`: `4.5.1` → `5.x` (Jakarta EE compatible)
- [ ] `mybatis-spring-boot-starter-test`: `2.2.2` → `3.x`
- [ ] Add `flyway-database-sqlite` dependency (required for Flyway 10+ with SQLite)

### gradle/wrapper/gradle-wrapper.properties
- [ ] `distributionUrl`: `gradle-7.4-bin.zip` → `gradle-8.x-bin.zip`

### .github/workflows/gradle.yml
- [ ] `actions/setup-java` version: `v2` → `v4`
- [ ] `java-version`: `11` → `21`
- [ ] `actions/checkout`: `v2` → `v4`
- [ ] `actions/cache`: `v2` → `v4`
- [ ] Step name: "Set up JDK 11" → "Set up JDK 21"

### Source files (javax → jakarta migration)
- [ ] All `javax.validation.*` imports → `jakarta.validation.*` (20+ files)
- [ ] All `javax.servlet.*` imports → `jakarta.servlet.*` (JwtTokenFilter.java)
- [ ] Note: `javax.crypto.*` stays as-is (part of JDK, not Jakarta EE)

### Spring Security changes (WebSecurityConfigurerAdapter removed)
- [ ] `WebSecurityConfig.java`: Rewrite to use `SecurityFilterChain` bean pattern

### Spring MVC changes
- [ ] `CustomizeExceptionHandler.java`: Update `handleMethodArgumentNotValid` signature (Spring 6 changed parameter types)

### DGS GraphQL changes
- [ ] `GraphQLCustomizeExceptionHandler.java`: Update `DataFetcherExceptionHandler` interface (return type changed to `CompletableFuture`)
