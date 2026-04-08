package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ArticleFavoriteCountTest {

  @Test
  void should_create_and_get_fields() {
    ArticleFavoriteCount count = new ArticleFavoriteCount("id", 5);
    assertEquals("id", count.getId());
    assertEquals(5, count.getCount());
  }

  @Test
  void should_support_equals_and_hashcode() {
    ArticleFavoriteCount c1 = new ArticleFavoriteCount("id", 5);
    ArticleFavoriteCount c2 = new ArticleFavoriteCount("id", 5);
    assertEquals(c1, c2);
    assertEquals(c1.hashCode(), c2.hashCode());

    ArticleFavoriteCount c3 = new ArticleFavoriteCount("id2", 3);
    assertNotEquals(c1, c3);
  }

  @Test
  void should_have_toString() {
    ArticleFavoriteCount count = new ArticleFavoriteCount("id", 10);
    assertNotNull(count.toString());
  }
}
