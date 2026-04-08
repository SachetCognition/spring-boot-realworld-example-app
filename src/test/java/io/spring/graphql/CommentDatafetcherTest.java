package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.spring.application.CommentQueryService;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.Comment;
import io.spring.graphql.types.CommentsConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class CommentDatafetcherTest {

  private CommentQueryService commentQueryService;
  private CommentDatafetcher commentDatafetcher;
  private User user;
  private ProfileData profileData;

  @BeforeEach
  void setUp() {
    commentQueryService = mock(CommentQueryService.class);
    commentDatafetcher = new CommentDatafetcher(commentQueryService);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    profileData = new ProfileData(user.getId(), "testuser", "bio", "image", false);
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_get_comment_from_payload() {
    CommentData commentData =
        new CommentData("cid", "body", "article-id", new DateTime(), new DateTime(), profileData);

    DataFetchingEnvironment delegate = mock(DataFetchingEnvironment.class);
    when(delegate.getLocalContext()).thenReturn(commentData);
    DgsDataFetchingEnvironment dfe = new DgsDataFetchingEnvironment(delegate);

    DataFetcherResult<Comment> result = commentDatafetcher.getComment(dfe);
    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals("cid", result.getData().getId());
    assertEquals("body", result.getData().getBody());
  }

  @Test
  void should_get_article_comments_with_first() {
    CommentData commentData =
        new CommentData("cid", "body", "article-id", new DateTime(), new DateTime(), profileData);
    CursorPager<CommentData> pager =
        new CursorPager<>(Arrays.asList(commentData), Direction.NEXT, false);
    when(commentQueryService.findByArticleIdWithCursor(any(), any(), any())).thenReturn(pager);

    ArticleData articleData =
        new ArticleData(
            "article-id", "test-slug", "title", "desc", "body", false, 0,
            new DateTime(), new DateTime(), new ArrayList<>(), profileData);

    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);

    Article article = Article.newBuilder().slug("test-slug").build();

    DataFetchingEnvironment delegate1 = mock(DataFetchingEnvironment.class);
    when(delegate1.getSource()).thenReturn(article);
    when(delegate1.getLocalContext()).thenReturn(map);
    DgsDataFetchingEnvironment dfe = new DgsDataFetchingEnvironment(delegate1);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(10, null, null, null, dfe);
    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_get_article_comments_with_last() {
    CommentData commentData =
        new CommentData("cid", "body", "article-id", new DateTime(), new DateTime(), profileData);
    CursorPager<CommentData> pager =
        new CursorPager<>(Arrays.asList(commentData), Direction.PREV, false);
    when(commentQueryService.findByArticleIdWithCursor(any(), any(), any())).thenReturn(pager);

    ArticleData articleData =
        new ArticleData(
            "article-id", "test-slug", "title", "desc", "body", false, 0,
            new DateTime(), new DateTime(), new ArrayList<>(), profileData);

    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);

    Article article = Article.newBuilder().slug("test-slug").build();

    DataFetchingEnvironment delegate2 = mock(DataFetchingEnvironment.class);
    when(delegate2.getSource()).thenReturn(article);
    when(delegate2.getLocalContext()).thenReturn(map);
    DgsDataFetchingEnvironment dfe = new DgsDataFetchingEnvironment(delegate2);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(null, null, 10, null, dfe);
    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_throw_when_both_first_and_last_null() {
    DgsDataFetchingEnvironment dfe = new DgsDataFetchingEnvironment(mock(DataFetchingEnvironment.class));
    assertThrows(
        IllegalArgumentException.class,
        () -> commentDatafetcher.articleComments(null, null, null, null, dfe));
  }

  @Test
  void should_get_empty_comments() {
    CursorPager<CommentData> pager =
        new CursorPager<>(new ArrayList<>(), Direction.NEXT, false);
    when(commentQueryService.findByArticleIdWithCursor(any(), any(), any())).thenReturn(pager);

    ArticleData articleData =
        new ArticleData(
            "article-id", "test-slug", "title", "desc", "body", false, 0,
            new DateTime(), new DateTime(), new ArrayList<>(), profileData);

    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);

    Article article = Article.newBuilder().slug("test-slug").build();

    DataFetchingEnvironment delegate3 = mock(DataFetchingEnvironment.class);
    when(delegate3.getSource()).thenReturn(article);
    when(delegate3.getLocalContext()).thenReturn(map);
    DgsDataFetchingEnvironment dfe = new DgsDataFetchingEnvironment(delegate3);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(10, null, null, null, dfe);
    assertNotNull(result);
    assertTrue(result.getData().getEdges().isEmpty());
  }
}
