package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.spring.application.UserQueryService;
import io.spring.application.data.UserData;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.graphql.types.UserPayload;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

public class MeDatafetcherTest {

  private UserQueryService userQueryService;
  private JwtService jwtService;
  private MeDatafetcher meDatafetcher;
  private User user;

  @BeforeEach
  void setUp() {
    userQueryService = mock(UserQueryService.class);
    jwtService = mock(JwtService.class);
    meDatafetcher = new MeDatafetcher(userQueryService, jwtService);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_get_me_successfully() {
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
    UserData userData = new UserData(user.getId(), "test@test.com", "testuser", "bio", "image");
    when(userQueryService.findById(eq(user.getId()))).thenReturn(Optional.of(userData));

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    var result = meDatafetcher.getMe("Token test-token", dfe);
    assertNotNull(result);
    assertEquals("testuser", result.getData().getUsername());
    assertEquals("test@test.com", result.getData().getEmail());
    assertEquals("test-token", result.getData().getToken());
  }

  @Test
  void should_return_null_when_anonymous() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    var result = meDatafetcher.getMe("Token test-token", dfe);
    assertNull(result);
  }

  @Test
  void should_return_null_when_principal_is_null() {
    TestingAuthenticationToken auth = new TestingAuthenticationToken(null, null);
    SecurityContextHolder.getContext().setAuthentication(auth);
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    var result = meDatafetcher.getMe("Token test-token", dfe);
    assertNull(result);
  }

  @Test
  void should_get_user_payload_user() {
    when(jwtService.toToken(eq(user))).thenReturn("jwt-token");
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(user);

    var result = meDatafetcher.getUserPayloadUser(dfe);
    assertNotNull(result);
    assertEquals("testuser", result.getData().getUsername());
    assertEquals("test@test.com", result.getData().getEmail());
    assertEquals("jwt-token", result.getData().getToken());
  }
}
