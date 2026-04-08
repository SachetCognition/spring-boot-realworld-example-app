package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleQueryService;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.ArticlesConnection;
import io.spring.graphql.types.Profile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class ArticleDatafetcherTest {

  private ArticleQueryService articleQueryService;
  private UserRepository userRepository;
  private ArticleDatafetcher articleDatafetcher;
  private User user;
  private ProfileData profileData;

  @BeforeEach
  void setUp() {
    articleQueryService = mock(ArticleQueryService.class);
    userRepository = mock(UserRepository.class);
    articleDatafetcher = new ArticleDatafetcher(articleQueryService, userRepository);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    profileData = new ProfileData(user.getId(), "testuser", "bio", "image", false);
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private ArticleData createArticleData(String slug) {
    return new ArticleData(
        slug + "-id",
        slug,
        slug,
        "desc",
        "body",
        false,
        0,
        new DateTime(),
        new DateTime(),
        new ArrayList<>(),
        profileData);
  }

  @Test
  void should_get_feed_with_first() {
    ArticleData article = createArticleData("test-article");
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(article), Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(pager);

    DgsDataFetchingEnvironment dfe = new DgsDataFetchingEnvironment(mock(DataFetchingEnvironment.class));
    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, dfe);
    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_get_feed_with_last() {
    ArticleData article = createArticleData("test-article");
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(article), Direction.PREV, false);
    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(pager);

    DgsDataFetchingEnvironment dfe = new DgsDataFetchingEnvironment(mock(DataFetchingEnvironment.class));
    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(null, null, 10, null, dfe);
    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_throw_when_both_first_and_last_null_on_feed() {
    DgsDataFetchingEnvironment dfe = new DgsDataFetchingEnvironment(mock(DataFetchingEnvironment.class));
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getFeed(null, null, null, null, dfe));
  }

  @Test
  void should_get_articles_with_first() {
    ArticleData article = createArticleData("test-article");
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(article), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    DgsDataFetchingEnvironment dfe = new DgsDataFetchingEnvironment(mock(DataFetchingEnvironment.class));
    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(10, null, null, null, null, null, null, dfe);
    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_get_articles_with_last() {
    ArticleData article = createArticleData("test-article");
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(article), Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    DgsDataFetchingEnvironment dfe = new DgsDataFetchingEnvironment(mock(DataFetchingEnvironment.class));
    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(null, null, 10, null, "author", "fav", "tag", dfe);
    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_throw_when_both_first_and_last_null_on_articles() {
    DgsDataFetchingEnvironment dfe = new DgsDataFetchingEnvironment(mock(DataFetchingEnvironment.class));
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getArticles(null, null, null, null, null, null, null, dfe));
  }

  @Test
  void should_find_article_by_slug() {
    ArticleData article = createArticleData("test-slug");
    when(articleQueryService.findBySlug(eq("test-slug"), any()))
        .thenReturn(Optional.of(article));

    DataFetcherResult<Article> result = articleDatafetcher.findArticleBySlug("test-slug");
    assertNotNull(result);
    assertEquals("test-slug", result.getData().getSlug());
  }

  @Test
  void should_throw_when_article_not_found_by_slug() {
    when(articleQueryService.findBySlug(eq("not-found"), any())).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class, () -> articleDatafetcher.findArticleBySlug("not-found"));
  }

  @Test
  void should_get_article_from_payload() {
    io.spring.core.article.Article coreArticle =
        new io.spring.core.article.Article("Title", "Desc", "Body", Arrays.asList(), user.getId());
    ArticleData articleData = createArticleData("title");
    when(articleQueryService.findById(eq(coreArticle.getId()), any()))
        .thenReturn(Optional.of(articleData));

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(coreArticle);

    DataFetcherResult<Article> result = articleDatafetcher.getArticle(dfe);
    assertNotNull(result);
    assertNotNull(result.getData());
  }

  @Test
  void should_get_comment_article() {
    CommentData commentData =
        new CommentData("cid", "body", "article-id", new DateTime(), new DateTime(), profileData);

    ArticleData articleData = createArticleData("test-slug");
    when(articleQueryService.findById(eq("article-id"), any()))
        .thenReturn(Optional.of(articleData));

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(commentData);

    DataFetcherResult<Article> result = articleDatafetcher.getCommentArticle(dfe);
    assertNotNull(result);
  }

  @Test
  void should_get_user_feed_with_first() {
    ArticleData article = createArticleData("test-article");
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(article), Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(pager);
    when(userRepository.findByUsername(eq("testuser"))).thenReturn(Optional.of(user));

    Profile profile = Profile.newBuilder().username("testuser").build();
    DataFetchingEnvironment delegate = mock(DataFetchingEnvironment.class);
    when(delegate.getSource()).thenReturn(profile);
    DgsDataFetchingEnvironment dfe = new DgsDataFetchingEnvironment(delegate);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFeed(10, null, null, null, dfe);
    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_get_user_feed_with_last() {
    ArticleData article = createArticleData("test-article");
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(article), Direction.PREV, false);
    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(pager);
    when(userRepository.findByUsername(eq("testuser"))).thenReturn(Optional.of(user));

    Profile profile = Profile.newBuilder().username("testuser").build();
    DataFetchingEnvironment delegate = mock(DataFetchingEnvironment.class);
    when(delegate.getSource()).thenReturn(profile);
    DgsDataFetchingEnvironment dfe = new DgsDataFetchingEnvironment(delegate);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFeed(null, null, 10, null, dfe);
    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_throw_when_both_null_on_user_feed() {
    DgsDataFetchingEnvironment dfe = new DgsDataFetchingEnvironment(mock(DataFetchingEnvironment.class));
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userFeed(null, null, null, null, dfe));
  }

  @Test
  void should_get_user_favorites_with_first() {
    ArticleData article = createArticleData("test-article");
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(article), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    Profile profile = Profile.newBuilder().username("testuser").build();
    DataFetchingEnvironment delegate = mock(DataFetchingEnvironment.class);
    when(delegate.getSource()).thenReturn(profile);
    DgsDataFetchingEnvironment dfe = new DgsDataFetchingEnvironment(delegate);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFavorites(10, null, null, null, dfe);
    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_get_user_favorites_with_last() {
    ArticleData article = createArticleData("test-article");
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(article), Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    Profile profile = Profile.newBuilder().username("testuser").build();
    DataFetchingEnvironment delegate = mock(DataFetchingEnvironment.class);
    when(delegate.getSource()).thenReturn(profile);
    DgsDataFetchingEnvironment dfe = new DgsDataFetchingEnvironment(delegate);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFavorites(null, null, 10, null, dfe);
    assertNotNull(result);
  }

  @Test
  void should_throw_when_both_null_on_user_favorites() {
    DgsDataFetchingEnvironment dfe = new DgsDataFetchingEnvironment(mock(DataFetchingEnvironment.class));
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userFavorites(null, null, null, null, dfe));
  }

  @Test
  void should_get_user_articles_with_first() {
    ArticleData article = createArticleData("test-article");
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(article), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    Profile profile = Profile.newBuilder().username("testuser").build();
    DataFetchingEnvironment delegate = mock(DataFetchingEnvironment.class);
    when(delegate.getSource()).thenReturn(profile);
    DgsDataFetchingEnvironment dfe = new DgsDataFetchingEnvironment(delegate);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userArticles(10, null, null, null, dfe);
    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_get_user_articles_with_last() {
    ArticleData article = createArticleData("test-article");
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(article), Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    Profile profile = Profile.newBuilder().username("testuser").build();
    DataFetchingEnvironment delegate = mock(DataFetchingEnvironment.class);
    when(delegate.getSource()).thenReturn(profile);
    DgsDataFetchingEnvironment dfe = new DgsDataFetchingEnvironment(delegate);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userArticles(null, null, 10, null, dfe);
    assertNotNull(result);
  }

  @Test
  void should_throw_when_both_null_on_user_articles() {
    DgsDataFetchingEnvironment dfe = new DgsDataFetchingEnvironment(mock(DataFetchingEnvironment.class));
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userArticles(null, null, null, null, dfe));
  }

  @Test
  void should_get_empty_feed() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(new ArrayList<>(), Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(pager);

    DgsDataFetchingEnvironment dfe = new DgsDataFetchingEnvironment(mock(DataFetchingEnvironment.class));
    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, dfe);
    assertNotNull(result);
    assertTrue(result.getData().getEdges().isEmpty());
  }
}
