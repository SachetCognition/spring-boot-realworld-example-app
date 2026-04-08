package io.spring.core.comment;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CommentTest {

  @Test
  void should_create_comment_with_constructor() {
    Comment comment = new Comment("body text", "user-id", "article-id");
    assertNotNull(comment.getId());
    assertEquals("body text", comment.getBody());
    assertEquals("user-id", comment.getUserId());
    assertEquals("article-id", comment.getArticleId());
    assertNotNull(comment.getCreatedAt());
  }

  @Test
  void should_create_comment_with_no_arg_constructor() {
    Comment comment = new Comment();
    assertNull(comment.getId());
    assertNull(comment.getBody());
  }

  @Test
  void should_support_equals_by_id() {
    Comment c1 = new Comment("body", "user", "article");
    Comment c2 = new Comment("body", "user", "article");
    assertNotEquals(c1, c2); // Different UUIDs
    assertEquals(c1, c1);
  }

  @Test
  void should_have_consistent_hashcode() {
    Comment comment = new Comment("body", "user", "article");
    assertEquals(comment.hashCode(), comment.hashCode());
  }

  @Test
  void should_not_equal_null() {
    Comment comment = new Comment("body", "user", "article");
    assertNotEquals(null, comment);
  }
}
