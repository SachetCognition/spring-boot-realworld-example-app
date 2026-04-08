package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.spring.application.data.ProfileData;
import io.spring.application.data.UserData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ProfileQueryServiceTest {

  private UserReadService userReadService;
  private UserRelationshipQueryService userRelationshipQueryService;
  private ProfileQueryService profileQueryService;
  private User currentUser;

  @BeforeEach
  void setUp() {
    userReadService = mock(UserReadService.class);
    userRelationshipQueryService = mock(UserRelationshipQueryService.class);
    profileQueryService = new ProfileQueryService(userReadService, userRelationshipQueryService);
    currentUser = new User("test@test.com", "testuser", "password", "bio", "image");
  }

  @Test
  void should_return_empty_when_user_not_found() {
    when(userReadService.findByUsername("nonexistent")).thenReturn(null);
    Optional<ProfileData> result = profileQueryService.findByUsername("nonexistent", currentUser);
    assertTrue(result.isEmpty());
  }

  @Test
  void should_return_profile_when_user_found() {
    UserData userData = new UserData("uid", "email", "testuser", "bio", "image");
    when(userReadService.findByUsername("testuser")).thenReturn(userData);
    when(userRelationshipQueryService.isUserFollowing(any(), eq("uid"))).thenReturn(false);

    Optional<ProfileData> result = profileQueryService.findByUsername("testuser", currentUser);
    assertTrue(result.isPresent());
    assertEquals("testuser", result.get().getUsername());
    assertFalse(result.get().isFollowing());
  }

  @Test
  void should_return_profile_with_following_true() {
    UserData userData = new UserData("uid", "email", "testuser", "bio", "image");
    when(userReadService.findByUsername("testuser")).thenReturn(userData);
    when(userRelationshipQueryService.isUserFollowing(any(), eq("uid"))).thenReturn(true);

    Optional<ProfileData> result = profileQueryService.findByUsername("testuser", currentUser);
    assertTrue(result.isPresent());
    assertTrue(result.get().isFollowing());
  }

  @Test
  void should_return_profile_without_following_when_current_user_null() {
    UserData userData = new UserData("uid", "email", "testuser", "bio", "image");
    when(userReadService.findByUsername("testuser")).thenReturn(userData);

    Optional<ProfileData> result = profileQueryService.findByUsername("testuser", null);
    assertTrue(result.isPresent());
    assertFalse(result.get().isFollowing());
  }
}
