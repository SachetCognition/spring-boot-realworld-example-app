# Migration Report: Spring Boot 2.6.3 to 3.4.3

## Version Comparison

| Technology | Before | After |
|---|---|---|
| Java (source/target) | 11 | 21 |
| Java Runtime | OpenJDK 11.0.29 | OpenJDK 21.0.9 (Zulu) |
| Spring Boot | 2.6.3 | 3.4.3 |
| Gradle | 7.4 | 8.12 |
| Spring Dependency Management | 1.0.11.RELEASE | 1.1.7 |
| DGS Codegen Plugin | 5.0.6 | 7.0.3 |
| Spotless Plugin | 6.2.1 | 7.0.2 |
| MyBatis Spring Boot | 2.2.2 | 3.0.4 |
| DGS Spring Boot Starter | 4.9.21 | 9.2.2 |
| jjwt | 0.11.2 | 0.12.6 |
| joda-time | 2.10.13 | 2.13.0 |
| sqlite-jdbc | 3.36.0.3 | 3.47.2.0 |
| REST Assured | 4.5.1 | 5.5.1 |
| GitHub Actions checkout | v2 | v4 |
| GitHub Actions setup-java | v2 | v4 |
| GitHub Actions cache | v2 | v4 |

## Test Result Comparison

| Metric | Pre-Migration | Post-Migration |
|---|---|---|
| Total Tests | 68 | 68 |
| Passed | 68 | 68 |
| Failed | 0 | 0 |
| Ignored | 0 | 0 |
| **Regressions** | - | **0** |

## Changes Made (by category)

### 1. Version Bumps (`build.gradle`, `gradle-wrapper.properties`, `gradle.yml`)

- Updated Spring Boot plugin: `2.6.3` -> `3.4.3`
- Updated Spring Dependency Management: `1.0.11.RELEASE` -> `1.1.7`
- Updated DGS Codegen: `5.0.6` -> `7.0.3`
- Updated Spotless: `6.2.1` -> `7.0.2`
- Updated source/target compatibility: `11` -> `21`
- Updated Gradle wrapper: `7.4` -> `8.12`
- Updated all dependency versions (see version table above)
- Updated GitHub Actions: checkout v2->v4, setup-java v2->v4, cache v2->v4
- Updated CI Java version: `11` -> `21`

### 2. Jakarta EE Namespace Migration (`javax.*` -> `jakarta.*`)

Spring Boot 3.x requires Jakarta EE 9+ which renamed `javax.*` to `jakarta.*`. Updated imports in 15+ source files:

- `javax.validation.*` -> `jakarta.validation.*` (validation constraints, validators, payloads)
- `javax.servlet.*` -> `jakarta.servlet.*` (servlet filters, request/response)
- Note: `javax.crypto.*` was NOT changed as it is part of the JDK, not Jakarta EE

**Affected files:**
- `ArticleApi.java`, `ArticlesApi.java`, `CommentsApi.java`, `CurrentUserApi.java`, `UsersApi.java`
- `JwtTokenFilter.java`
- `CustomizeExceptionHandler.java`
- `NewArticleParam.java`, `ArticleCommandService.java`, `DuplicatedArticleValidator.java`, `DuplicatedArticleConstraint.java`
- `RegisterParam.java`, `UpdateUserParam.java`, `UserService.java`
- `DuplicatedUsernameValidator.java`, `DuplicatedUsernameConstraint.java`, `DuplicatedEmailValidator.java`, `DuplicatedEmailConstraint.java`
- `UserMutation.java`
- `GraphQLCustomizeExceptionHandler.java`

### 3. Spring Security Configuration Rewrite (`WebSecurityConfig.java`)

`WebSecurityConfigurerAdapter` was removed in Spring Security 6.x (Spring Boot 3.x). Rewrote the security configuration to use the modern `SecurityFilterChain` bean approach:

- Replaced `extends WebSecurityConfigurerAdapter` with `SecurityFilterChain` `@Bean` method
- Replaced `configure(HttpSecurity)` override with `filterChain(HttpSecurity)` returning `http.build()`
- Replaced deprecated `.authorizeRequests()` / `.antMatchers()` with `.authorizeHttpRequests()` / `.requestMatchers()`
- Replaced chained `.csrf().disable().cors().and()...` with lambda DSL: `.csrf(csrf -> csrf.disable())`, `.cors(cors -> ...)`, etc.

### 4. Spring MVC Exception Handler Update (`CustomizeExceptionHandler.java`)

- Updated `handleMethodArgumentNotValid` override signature: `HttpStatus` parameter changed to `HttpStatusCode` in Spring Framework 6.x
- Added `import org.springframework.http.HttpStatusCode`

### 5. DGS GraphQL Framework Update (`GraphQLCustomizeExceptionHandler.java`)

- Updated `DataFetcherExceptionHandler.onException()` to `handleException()` (API renamed in graphql-java 21+)
- Changed return type from `DataFetcherExceptionHandlerResult` to `CompletableFuture<DataFetcherExceptionHandlerResult>`
- Wrapped return values with `CompletableFuture.completedFuture()`

### 6. DGS PageInfo Type Fix (`ArticleDatafetcher.java`, `CommentDatafetcher.java`)

- Replaced `graphql.relay.DefaultPageInfo` / `graphql.relay.DefaultConnectionCursor` with `io.spring.graphql.types.PageInfo` (DGS-generated type)
- Rewrote `buildArticlePageInfo()` and `buildCommentPageInfo()` to use `PageInfo.newBuilder()` instead of `new DefaultPageInfo()`
- This fixes type incompatibility between `graphql.relay.PageInfo` and the DGS codegen-generated `PageInfo` type

### 7. JWT (jjwt) API Migration (`DefaultJwtService.java`, `DefaultJwtServiceTest.java`)

- Removed deprecated `SignatureAlgorithm` enum usage
- Replaced `new SecretKeySpec(secret.getBytes(), "HmacSHA512")` with `Keys.hmacShaKeyFor(secret.getBytes())` (jjwt 0.12.x recommended approach)
- Replaced deprecated builder methods: `.setSubject()` -> `.subject()`, `.setExpiration()` -> `.expiration()`
- Replaced deprecated parser: `Jwts.parserBuilder().setSigningKey()` -> `Jwts.parser().verifyWith()`
- Replaced deprecated accessor: `.parseClaimsJws()` -> `.parseSignedClaims()`, `.getBody()` -> `.getPayload()`
- Updated test secret key to meet HS512 minimum key length (64 bytes) required by jjwt 0.12.x

## Regressions Found and Fixed

| Issue | Cause | Fix |
|---|---|---|
| `DefaultJwtServiceTest` failed with `UnsupportedKeyException` | jjwt 0.12.x enforces minimum HMAC key sizes; test secret was 60 bytes (HS512 requires 64) | Extended test secret to 64 bytes; used `Keys.hmacShaKeyFor()` in production code |

## Known Issues / Notes

- **`@MockBean` deprecation warnings**: 23 warnings about `@MockBean` being deprecated in Spring Boot 3.4.x. These are warnings only and do not affect functionality. The recommended replacement (`@MockitoBean`) is available but was not changed to minimize diff.
- **Gradle deprecation warnings**: Some Gradle features used are deprecated for Gradle 9.0 compatibility. These come from plugins and do not affect the build.

## Files Changed

### Source files (main):
- `build.gradle` - Plugin and dependency version updates
- `gradle/wrapper/gradle-wrapper.properties` - Gradle 8.12
- `gradle/wrapper/gradle-wrapper.jar` - Updated wrapper jar
- `gradlew` - Updated wrapper script
- `.github/workflows/gradle.yml` - Java 21, updated actions
- `src/main/java/io/spring/api/security/WebSecurityConfig.java` - SecurityFilterChain rewrite
- `src/main/java/io/spring/api/security/JwtTokenFilter.java` - jakarta.servlet imports
- `src/main/java/io/spring/api/exception/CustomizeExceptionHandler.java` - HttpStatusCode, jakarta imports
- `src/main/java/io/spring/infrastructure/service/DefaultJwtService.java` - jjwt 0.12.x API
- `src/main/java/io/spring/graphql/exception/GraphQLCustomizeExceptionHandler.java` - DGS API update
- `src/main/java/io/spring/graphql/ArticleDatafetcher.java` - PageInfo type fix
- `src/main/java/io/spring/graphql/CommentDatafetcher.java` - PageInfo type fix
- 10+ additional files with `javax.validation` -> `jakarta.validation` import changes

### Source files (test):
- `src/test/java/io/spring/infrastructure/service/DefaultJwtServiceTest.java` - Extended test secret key

### Migration artifacts:
- `migration-artifacts/pre-migration/versions.txt`
- `migration-artifacts/pre-migration/unit-test-results.txt`
- `migration-artifacts/post-migration/versions.txt`
- `migration-artifacts/post-migration/unit-test-results.txt`
- `migration-artifacts/version-pin-checklist.md`
- `migration-artifacts/MIGRATION-REPORT.md`
