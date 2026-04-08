package io.spring.core.favorite;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ArticleFavoriteTest {

  @Test
  void should_create_with_constructor() {
    ArticleFavorite fav = new ArticleFavorite("article-id", "user-id");
    assertEquals("article-id", fav.getArticleId());
    assertEquals("user-id", fav.getUserId());
  }

  @Test
  void should_create_with_no_arg_constructor() {
    ArticleFavorite fav = new ArticleFavorite();
    assertNull(fav.getArticleId());
    assertNull(fav.getUserId());
  }

  @Test
  void should_support_equals_and_hashcode() {
    ArticleFavorite f1 = new ArticleFavorite("article-id", "user-id");
    ArticleFavorite f2 = new ArticleFavorite("article-id", "user-id");
    assertEquals(f1, f2);
    assertEquals(f1.hashCode(), f2.hashCode());
  }

  @Test
  void should_not_equal_different() {
    ArticleFavorite f1 = new ArticleFavorite("a1", "u1");
    ArticleFavorite f2 = new ArticleFavorite("a2", "u2");
    assertNotEquals(f1, f2);
  }
}
