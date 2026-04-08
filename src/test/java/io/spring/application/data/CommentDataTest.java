package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class CommentDataTest {

  @Test
  void should_create_with_all_args() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("pid", "user", "bio", "img", false);
    CommentData data = new CommentData("id", "body", "article-id", now, now, profile);

    assertEquals("id", data.getId());
    assertEquals("body", data.getBody());
    assertEquals("article-id", data.getArticleId());
    assertEquals(now, data.getCreatedAt());
    assertEquals(now, data.getUpdatedAt());
    assertEquals(profile, data.getProfileData());
  }

  @Test
  void should_create_with_no_arg_and_setters() {
    CommentData data = new CommentData();
    DateTime now = new DateTime();
    data.setId("id");
    data.setBody("body");
    data.setArticleId("aid");
    data.setCreatedAt(now);
    data.setUpdatedAt(now);
    data.setProfileData(null);

    assertEquals("id", data.getId());
    assertEquals("body", data.getBody());
    assertEquals("aid", data.getArticleId());
    assertNull(data.getProfileData());
  }

  @Test
  void should_get_cursor() {
    DateTime now = new DateTime();
    CommentData data = new CommentData();
    data.setCreatedAt(now);
    assertNotNull(data.getCursor());
    assertEquals(now, data.getCursor().getData());
  }

  @Test
  void should_support_equals_and_hashcode() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("pid", "user", "bio", "img", false);
    CommentData d1 = new CommentData("id", "body", "aid", now, now, profile);
    CommentData d2 = new CommentData("id", "body", "aid", now, now, profile);
    assertEquals(d1, d2);
    assertEquals(d1.hashCode(), d2.hashCode());
  }

  @Test
  void should_have_toString() {
    CommentData data = new CommentData();
    data.setId("cid");
    assertNotNull(data.toString());
    assertTrue(data.toString().contains("cid"));
  }
}
