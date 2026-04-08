package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.application.user.UserService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.types.CreateUserInput;
import io.spring.graphql.types.UpdateUserInput;
import io.spring.graphql.types.UserPayload;
import java.util.Optional;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserMutationTest {

  private UserRepository userRepository;
  private PasswordEncoder passwordEncoder;
  private UserService userService;
  private UserMutation userMutation;
  private User user;

  @BeforeEach
  void setUp() {
    userRepository = mock(UserRepository.class);
    passwordEncoder = mock(PasswordEncoder.class);
    userService = mock(UserService.class);
    userMutation = new UserMutation(userRepository, passwordEncoder, userService);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_create_user_successfully() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("new@test.com")
            .username("newuser")
            .password("password")
            .build();
    User newUser = new User("new@test.com", "newuser", "password", "", "");
    when(userService.createUser(any())).thenReturn(newUser);

    var result = userMutation.createUser(input);
    assertNotNull(result);
    assertNotNull(result.getData());
    assertTrue(result.getData() instanceof UserPayload);
  }

  @Test
  void should_return_errors_on_constraint_violation() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("bad")
            .username("")
            .password("")
            .build();
    when(userService.createUser(any()))
        .thenThrow(new ConstraintViolationException("validation failed", new java.util.HashSet<>()));

    var result = userMutation.createUser(input);
    assertNotNull(result);
    assertNotNull(result.getData());
  }

  @Test
  void should_login_successfully() {
    when(userRepository.findByEmail(eq("test@test.com"))).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(eq("password"), eq("password"))).thenReturn(true);

    var result = userMutation.login("password", "test@test.com");
    assertNotNull(result);
    assertNotNull(result.getData());
  }

  @Test
  void should_throw_on_invalid_login_wrong_password() {
    when(userRepository.findByEmail(eq("test@test.com"))).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(eq("wrong"), eq("password"))).thenReturn(false);

    assertThrows(
        InvalidAuthenticationException.class,
        () -> userMutation.login("wrong", "test@test.com"));
  }

  @Test
  void should_throw_on_login_user_not_found() {
    when(userRepository.findByEmail(eq("none@test.com"))).thenReturn(Optional.empty());

    assertThrows(
        InvalidAuthenticationException.class,
        () -> userMutation.login("password", "none@test.com"));
  }

  @Test
  void should_update_user_successfully() {
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));

    UpdateUserInput input =
        UpdateUserInput.newBuilder()
            .username("updated")
            .email("updated@test.com")
            .bio("new bio")
            .build();

    var result = userMutation.updateUser(input);
    assertNotNull(result);
    verify(userService).updateUser(any());
  }

  @Test
  void should_return_null_when_anonymous_on_update() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

    UpdateUserInput input =
        UpdateUserInput.newBuilder().username("updated").build();

    var result = userMutation.updateUser(input);
    assertNull(result);
  }

  @Test
  void should_return_null_when_principal_null_on_update() {
    TestingAuthenticationToken auth = new TestingAuthenticationToken(null, null);
    SecurityContextHolder.getContext().setAuthentication(auth);

    UpdateUserInput input =
        UpdateUserInput.newBuilder().username("updated").build();

    var result = userMutation.updateUser(input);
    assertNull(result);
  }
}
