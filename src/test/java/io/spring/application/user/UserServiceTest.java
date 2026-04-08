package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserServiceTest {

  private UserRepository userRepository;
  private PasswordEncoder passwordEncoder;
  private UserService userService;

  @BeforeEach
  void setUp() {
    userRepository = mock(UserRepository.class);
    passwordEncoder = mock(PasswordEncoder.class);
    userService = new UserService(userRepository, "default-image.png", passwordEncoder);
  }

  @Test
  void should_create_user() {
    when(passwordEncoder.encode("password")).thenReturn("encoded-password");
    RegisterParam param = new RegisterParam("test@test.com", "testuser", "password");

    User user = userService.createUser(param);
    assertNotNull(user);
    assertEquals("test@test.com", user.getEmail());
    assertEquals("testuser", user.getUsername());
    assertEquals("default-image.png", user.getImage());
    verify(userRepository).save(any(User.class));
  }

  @Test
  void should_update_user() {
    User user = new User("old@test.com", "olduser", "password", "old bio", "old image");
    UpdateUserParam updateParam =
        UpdateUserParam.builder()
            .email("new@test.com")
            .username("newuser")
            .password("newpass")
            .bio("new bio")
            .image("new image")
            .build();
    UpdateUserCommand command = new UpdateUserCommand(user, updateParam);

    userService.updateUser(command);
    verify(userRepository).save(any(User.class));
    assertEquals("new@test.com", user.getEmail());
    assertEquals("newuser", user.getUsername());
  }
}
