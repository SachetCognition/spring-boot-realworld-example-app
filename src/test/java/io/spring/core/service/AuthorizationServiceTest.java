package io.spring.core.service;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.core.article.Article;
import io.spring.core.comment.Comment;
import io.spring.core.user.User;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class AuthorizationServiceTest {

  @Test
  void should_allow_author_to_write_article() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");
    Article article = new Article("Title", "Desc", "Body", Arrays.asList("java"), user.getId());
    assertTrue(AuthorizationService.canWriteArticle(user, article));
  }

  @Test
  void should_deny_non_author_to_write_article() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");
    User otherUser = new User("other@test.com", "other", "password", "bio", "image");
    Article article =
        new Article("Title", "Desc", "Body", Arrays.asList("java"), otherUser.getId());
    assertFalse(AuthorizationService.canWriteArticle(user, article));
  }

  @Test
  void should_allow_article_author_to_write_comment() {
    User articleAuthor =
        new User("author@test.com", "author", "password", "bio", "image");
    User commentAuthor =
        new User("commenter@test.com", "commenter", "password", "bio", "image");
    Article article =
        new Article("Title", "Desc", "Body", Arrays.asList("java"), articleAuthor.getId());
    Comment comment = new Comment("body", commentAuthor.getId(), article.getId());
    assertTrue(AuthorizationService.canWriteComment(articleAuthor, article, comment));
  }

  @Test
  void should_allow_comment_author_to_write_comment() {
    User articleAuthor =
        new User("author@test.com", "author", "password", "bio", "image");
    User commentAuthor =
        new User("commenter@test.com", "commenter", "password", "bio", "image");
    Article article =
        new Article("Title", "Desc", "Body", Arrays.asList("java"), articleAuthor.getId());
    Comment comment = new Comment("body", commentAuthor.getId(), article.getId());
    assertTrue(AuthorizationService.canWriteComment(commentAuthor, article, comment));
  }

  @Test
  void should_deny_unrelated_user_to_write_comment() {
    User articleAuthor =
        new User("author@test.com", "author", "password", "bio", "image");
    User commentAuthor =
        new User("commenter@test.com", "commenter", "password", "bio", "image");
    User unrelatedUser =
        new User("unrelated@test.com", "unrelated", "password", "bio", "image");
    Article article =
        new Article("Title", "Desc", "Body", Arrays.asList("java"), articleAuthor.getId());
    Comment comment = new Comment("body", commentAuthor.getId(), article.getId());
    assertFalse(AuthorizationService.canWriteComment(unrelatedUser, article, comment));
  }
}
