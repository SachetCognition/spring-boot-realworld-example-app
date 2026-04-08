package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UserDataTest {

  @Test
  void should_create_with_all_args() {
    UserData data = new UserData("id", "email@test.com", "user", "bio", "img");
    assertEquals("id", data.getId());
    assertEquals("email@test.com", data.getEmail());
    assertEquals("user", data.getUsername());
    assertEquals("bio", data.getBio());
    assertEquals("img", data.getImage());
  }

  @Test
  void should_create_with_no_arg_and_setters() {
    UserData data = new UserData();
    data.setId("id");
    data.setEmail("e@t.com");
    data.setUsername("u");
    data.setBio("b");
    data.setImage("i");
    assertEquals("id", data.getId());
    assertEquals("e@t.com", data.getEmail());
    assertEquals("u", data.getUsername());
    assertEquals("b", data.getBio());
    assertEquals("i", data.getImage());
  }

  @Test
  void should_support_equals_and_hashcode() {
    UserData d1 = new UserData("id", "email", "user", "bio", "img");
    UserData d2 = new UserData("id", "email", "user", "bio", "img");
    assertEquals(d1, d2);
    assertEquals(d1.hashCode(), d2.hashCode());
    assertNotEquals(d1, new UserData("id2", "email2", "u2", "b2", "i2"));
  }

  @Test
  void should_have_toString() {
    UserData data = new UserData("id", "email", "user", "bio", "img");
    assertNotNull(data.toString());
    assertTrue(data.toString().contains("user"));
  }
}
