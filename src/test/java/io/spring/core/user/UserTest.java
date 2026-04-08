package io.spring.core.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UserTest {

  @Test
  void should_create_user_with_constructor() {
    User user = new User("email@test.com", "username", "password", "bio", "image");
    assertNotNull(user.getId());
    assertEquals("email@test.com", user.getEmail());
    assertEquals("username", user.getUsername());
    assertEquals("password", user.getPassword());
    assertEquals("bio", user.getBio());
    assertEquals("image", user.getImage());
  }

  @Test
  void should_create_user_with_no_arg_constructor() {
    User user = new User();
    assertNull(user.getId());
    assertNull(user.getEmail());
  }

  @Test
  void should_update_all_fields() {
    User user = new User("old@test.com", "olduser", "oldpass", "oldbio", "oldimg");
    user.update("new@test.com", "newuser", "newpass", "newbio", "newimg");

    assertEquals("new@test.com", user.getEmail());
    assertEquals("newuser", user.getUsername());
    assertEquals("newpass", user.getPassword());
    assertEquals("newbio", user.getBio());
    assertEquals("newimg", user.getImage());
  }

  @Test
  void should_not_update_when_values_are_empty() {
    User user = new User("email@test.com", "username", "password", "bio", "image");
    user.update("", "", "", "", "");

    assertEquals("email@test.com", user.getEmail());
    assertEquals("username", user.getUsername());
    assertEquals("password", user.getPassword());
    assertEquals("bio", user.getBio());
    assertEquals("image", user.getImage());
  }

  @Test
  void should_not_update_when_values_are_null() {
    User user = new User("email@test.com", "username", "password", "bio", "image");
    user.update(null, null, null, null, null);

    assertEquals("email@test.com", user.getEmail());
    assertEquals("username", user.getUsername());
    assertEquals("password", user.getPassword());
    assertEquals("bio", user.getBio());
    assertEquals("image", user.getImage());
  }

  @Test
  void should_update_selectively() {
    User user = new User("email@test.com", "username", "password", "bio", "image");
    user.update("new@test.com", null, "", "newbio", null);

    assertEquals("new@test.com", user.getEmail());
    assertEquals("username", user.getUsername());
    assertEquals("password", user.getPassword());
    assertEquals("newbio", user.getBio());
    assertEquals("image", user.getImage());
  }

  @Test
  void should_support_equals_by_id() {
    User u1 = new User("email@test.com", "username", "password", "bio", "image");
    User u2 = new User("email@test.com", "username", "password", "bio", "image");
    // Different IDs (UUID.randomUUID), so not equal
    assertNotEquals(u1, u2);
    // Same object should be equal
    assertEquals(u1, u1);
  }

  @Test
  void should_have_consistent_hashcode() {
    User user = new User("email@test.com", "username", "password", "bio", "image");
    int hash1 = user.hashCode();
    int hash2 = user.hashCode();
    assertEquals(hash1, hash2);
  }

  @Test
  void should_not_equal_null() {
    User user = new User("email@test.com", "username", "password", "bio", "image");
    assertNotEquals(null, user);
  }

  @Test
  void should_not_equal_different_type() {
    User user = new User("email@test.com", "username", "password", "bio", "image");
    assertNotEquals("string", user);
  }
}
