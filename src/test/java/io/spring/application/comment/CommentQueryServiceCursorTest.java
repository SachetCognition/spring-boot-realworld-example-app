package io.spring.application.comment;

import io.spring.application.CommentQueryService;
import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.data.CommentData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.repository.MyBatisArticleRepository;
import io.spring.infrastructure.repository.MyBatisCommentRepository;
import io.spring.infrastructure.repository.MyBatisUserRepository;
import java.util.Arrays;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({
  MyBatisCommentRepository.class,
  MyBatisUserRepository.class,
  CommentQueryService.class,
  MyBatisArticleRepository.class
})
public class CommentQueryServiceCursorTest extends DbTestBase {

  @Autowired private CommentRepository commentRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private CommentQueryService commentQueryService;
  @Autowired private ArticleRepository articleRepository;

  private User user;
  private Article article;

  @BeforeEach
  public void setUp() {
    user = new User("test@test.com", "testuser", "123", "", "");
    userRepository.save(user);
    article = new Article("title", "desc", "body", Arrays.asList("java"), user.getId());
    articleRepository.save(article);
  }

  @Test
  public void should_find_comments_with_cursor_next() {
    Comment comment1 = new Comment("content1", user.getId(), article.getId());
    commentRepository.save(comment1);
    Comment comment2 = new Comment("content2", user.getId(), article.getId());
    commentRepository.save(comment2);

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor(
            article.getId(),
            user,
            new CursorPageParameter<>(null, 20, Direction.NEXT));
    Assertions.assertEquals(2, result.getData().size());
    Assertions.assertFalse(result.hasNext());
  }

  @Test
  public void should_find_comments_with_cursor_prev() {
    Comment comment = new Comment("content", user.getId(), article.getId());
    commentRepository.save(comment);

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor(
            article.getId(),
            user,
            new CursorPageParameter<>(null, 20, Direction.PREV));
    Assertions.assertEquals(1, result.getData().size());
    Assertions.assertFalse(result.hasPrevious());
  }

  @Test
  public void should_return_empty_when_no_comments() {
    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor(
            article.getId(),
            user,
            new CursorPageParameter<>(null, 20, Direction.NEXT));
    Assertions.assertTrue(result.getData().isEmpty());
  }

  @Test
  public void should_find_comments_without_user() {
    Comment comment = new Comment("content", user.getId(), article.getId());
    commentRepository.save(comment);

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor(
            article.getId(),
            null,
            new CursorPageParameter<>(null, 20, Direction.NEXT));
    Assertions.assertEquals(1, result.getData().size());
  }
}
