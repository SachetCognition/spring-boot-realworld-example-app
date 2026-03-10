# Version Pin Checklist

## Files requiring version updates for Spring Boot 2.6.3 → 3.4.x migration

### build.gradle
- [x] `org.springframework.boot` plugin: `2.6.3` → `3.4.3`
- [x] `io.spring.dependency-management` plugin: `1.0.11.RELEASE` → `1.1.7`
- [x] `com.netflix.dgs.codegen` plugin: `5.0.6` → `7.0.3`
- [x] `com.diffplug.spotless` plugin: `6.2.1` → `7.0.2`
- [x] `sourceCompatibility`: `11` → `21`
- [x] `targetCompatibility`: `11` → `21`
- [x] `mybatis-spring-boot-starter`: `2.2.2` → `3.0.4`
- [x] `graphql-dgs-spring-boot-starter`: `4.9.21` → `9.2.2`
- [x] `jjwt-api/impl/jackson`: `0.11.2` → `0.12.6`
- [x] `joda-time`: `2.10.13` → `2.13.0`
- [x] `sqlite-jdbc`: `3.36.0.3` → `3.47.2.0`
- [x] `rest-assured`: `4.5.1` → `5.5.1`
- [x] `mybatis-spring-boot-starter-test`: `2.2.2` → `3.0.4`
- [x] ~~Add `flyway-database-sqlite` dependency~~ — NOT NEEDED: artifact does not exist. Flyway 10.20.1 handles SQLite via `flyway-core` without a separate module. Verified working.

### gradle/wrapper/gradle-wrapper.properties
- [x] `distributionUrl`: `gradle-7.4-bin.zip` → `gradle-8.12-bin.zip`

### .github/workflows/gradle.yml
- [x] `actions/setup-java` version: `v2` → `v4`
- [x] `java-version`: `11` → `21`
- [x] `actions/checkout`: `v2` → `v4`
- [x] `actions/cache`: `v2` → `v4`
- [x] Step name: "Set up JDK 11" → "Set up JDK 21"

### Source files (javax → jakarta migration)
- [x] All `javax.validation.*` imports → `jakarta.validation.*` (15+ files)
- [x] All `javax.servlet.*` imports → `jakarta.servlet.*` (JwtTokenFilter.java)
- [x] Note: `javax.crypto.*` stays as-is (part of JDK, not Jakarta EE)

### Spring Security changes (WebSecurityConfigurerAdapter removed)
- [x] `WebSecurityConfig.java`: Rewrite to use `SecurityFilterChain` bean pattern

### Spring MVC changes
- [x] `CustomizeExceptionHandler.java`: Update `handleMethodArgumentNotValid` signature (Spring 6 changed parameter types)

### DGS GraphQL changes
- [x] `GraphQLCustomizeExceptionHandler.java`: Update `DataFetcherExceptionHandler` interface (return type changed to `CompletableFuture`)
- [x] `ArticleDatafetcher.java` / `CommentDatafetcher.java`: Update PageInfo type to DGS-generated type

### JWT (jjwt) changes
- [x] `DefaultJwtService.java`: Migrate to jjwt 0.12.x API
- [x] `DefaultJwtServiceTest.java`: Extend test secret key to meet HS512 minimum length (64 bytes)
