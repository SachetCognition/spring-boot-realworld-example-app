package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class ArticleDataTest {

  @Test
  void should_create_with_all_args_constructor() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("pid", "user", "bio", "img", false);
    ArticleData data =
        new ArticleData(
            "id", "slug", "title", "desc", "body", true, 5, now, now,
            Arrays.asList("java"), profile);

    assertEquals("id", data.getId());
    assertEquals("slug", data.getSlug());
    assertEquals("title", data.getTitle());
    assertEquals("desc", data.getDescription());
    assertEquals("body", data.getBody());
    assertTrue(data.isFavorited());
    assertEquals(5, data.getFavoritesCount());
    assertEquals(now, data.getCreatedAt());
    assertEquals(now, data.getUpdatedAt());
    assertEquals(1, data.getTagList().size());
    assertEquals(profile, data.getProfileData());
  }

  @Test
  void should_create_with_no_arg_constructor_and_setters() {
    ArticleData data = new ArticleData();
    DateTime now = new DateTime();
    data.setId("id");
    data.setSlug("slug");
    data.setTitle("title");
    data.setDescription("desc");
    data.setBody("body");
    data.setFavorited(true);
    data.setFavoritesCount(3);
    data.setCreatedAt(now);
    data.setUpdatedAt(now);
    data.setTagList(Arrays.asList("tag1", "tag2"));
    data.setProfileData(null);

    assertEquals("id", data.getId());
    assertEquals("slug", data.getSlug());
    assertEquals("title", data.getTitle());
    assertEquals("desc", data.getDescription());
    assertEquals("body", data.getBody());
    assertTrue(data.isFavorited());
    assertEquals(3, data.getFavoritesCount());
    assertEquals(now, data.getCreatedAt());
    assertEquals(now, data.getUpdatedAt());
    assertEquals(2, data.getTagList().size());
    assertNull(data.getProfileData());
  }

  @Test
  void should_get_cursor() {
    DateTime now = new DateTime();
    ArticleData data = new ArticleData();
    data.setUpdatedAt(now);
    assertNotNull(data.getCursor());
    assertEquals(now, data.getCursor().getData());
  }

  @Test
  void should_support_equals_and_hashcode() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("pid", "user", "bio", "img", false);
    ArticleData d1 =
        new ArticleData("id", "slug", "title", "desc", "body", false, 0, now, now, Arrays.asList(), profile);
    ArticleData d2 =
        new ArticleData("id", "slug", "title", "desc", "body", false, 0, now, now, Arrays.asList(), profile);
    assertEquals(d1, d2);
    assertEquals(d1.hashCode(), d2.hashCode());

    ArticleData d3 =
        new ArticleData("id2", "slug2", "title", "desc", "body", false, 0, now, now, Arrays.asList(), profile);
    assertNotEquals(d1, d3);
  }

  @Test
  void should_have_toString() {
    ArticleData data = new ArticleData();
    data.setId("test-id");
    assertNotNull(data.toString());
    assertTrue(data.toString().contains("test-id"));
  }
}
