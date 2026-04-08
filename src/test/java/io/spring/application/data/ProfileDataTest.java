package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ProfileDataTest {

  @Test
  void should_create_with_all_args() {
    ProfileData data = new ProfileData("id", "user", "bio", "img", true);
    assertEquals("id", data.getId());
    assertEquals("user", data.getUsername());
    assertEquals("bio", data.getBio());
    assertEquals("img", data.getImage());
    assertTrue(data.isFollowing());
  }

  @Test
  void should_create_with_no_arg_and_setters() {
    ProfileData data = new ProfileData();
    data.setId("id");
    data.setUsername("user");
    data.setBio("bio");
    data.setImage("img");
    data.setFollowing(false);

    assertEquals("id", data.getId());
    assertEquals("user", data.getUsername());
    assertEquals("bio", data.getBio());
    assertEquals("img", data.getImage());
    assertFalse(data.isFollowing());
  }

  @Test
  void should_support_equals_and_hashcode() {
    ProfileData d1 = new ProfileData("id", "user", "bio", "img", true);
    ProfileData d2 = new ProfileData("id", "user", "bio", "img", true);
    assertEquals(d1, d2);
    assertEquals(d1.hashCode(), d2.hashCode());

    ProfileData d3 = new ProfileData("id2", "user2", "bio", "img", false);
    assertNotEquals(d1, d3);
  }

  @Test
  void should_have_toString() {
    ProfileData data = new ProfileData("id", "user", "bio", "img", true);
    assertNotNull(data.toString());
    assertTrue(data.toString().contains("user"));
  }
}
