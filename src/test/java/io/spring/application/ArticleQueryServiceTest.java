package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.spring.application.CursorPager.Direction;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleDataList;
import io.spring.application.data.ArticleFavoriteCount;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.ArticleFavoritesReadService;
import io.spring.infrastructure.mybatis.readservice.ArticleReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ArticleQueryServiceTest {

  private ArticleReadService articleReadService;
  private UserRelationshipQueryService userRelationshipQueryService;
  private ArticleFavoritesReadService articleFavoritesReadService;
  private ArticleQueryService articleQueryService;
  private User user;
  private ProfileData profileData;

  @BeforeEach
  void setUp() {
    articleReadService = mock(ArticleReadService.class);
    userRelationshipQueryService = mock(UserRelationshipQueryService.class);
    articleFavoritesReadService = mock(ArticleFavoritesReadService.class);
    articleQueryService =
        new ArticleQueryService(
            articleReadService, userRelationshipQueryService, articleFavoritesReadService);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    profileData = new ProfileData(user.getId(), "testuser", "bio", "image", false);
  }

  private ArticleData createArticleData(String id) {
    return new ArticleData(
        id, "slug-" + id, "title", "desc", "body", false, 0,
        new DateTime(), new DateTime(), new ArrayList<>(), profileData);
  }

  @Test
  void should_find_by_id_with_user() {
    ArticleData articleData = createArticleData("a1");
    when(articleReadService.findById("a1")).thenReturn(articleData);
    when(articleFavoritesReadService.isUserFavorite(any(), eq("a1"))).thenReturn(true);
    when(articleFavoritesReadService.articleFavoriteCount(eq("a1"))).thenReturn(5);
    when(userRelationshipQueryService.isUserFollowing(any(), any())).thenReturn(false);

    Optional<ArticleData> result = articleQueryService.findById("a1", user);
    assertTrue(result.isPresent());
  }

  @Test
  void should_find_by_id_without_user() {
    ArticleData articleData = createArticleData("a1");
    when(articleReadService.findById("a1")).thenReturn(articleData);

    Optional<ArticleData> result = articleQueryService.findById("a1", null);
    assertTrue(result.isPresent());
  }

  @Test
  void should_return_empty_when_not_found_by_id() {
    when(articleReadService.findById("none")).thenReturn(null);
    Optional<ArticleData> result = articleQueryService.findById("none", user);
    assertTrue(result.isEmpty());
  }

  @Test
  void should_find_by_slug_with_user() {
    ArticleData articleData = createArticleData("a1");
    when(articleReadService.findBySlug("test-slug")).thenReturn(articleData);
    when(articleFavoritesReadService.isUserFavorite(any(), eq("a1"))).thenReturn(false);
    when(articleFavoritesReadService.articleFavoriteCount(eq("a1"))).thenReturn(0);
    when(userRelationshipQueryService.isUserFollowing(any(), any())).thenReturn(false);

    Optional<ArticleData> result = articleQueryService.findBySlug("test-slug", user);
    assertTrue(result.isPresent());
  }

  @Test
  void should_find_by_slug_without_user() {
    ArticleData articleData = createArticleData("a1");
    when(articleReadService.findBySlug("test-slug")).thenReturn(articleData);

    Optional<ArticleData> result = articleQueryService.findBySlug("test-slug", null);
    assertTrue(result.isPresent());
  }

  @Test
  void should_return_empty_when_not_found_by_slug() {
    when(articleReadService.findBySlug("none")).thenReturn(null);
    Optional<ArticleData> result = articleQueryService.findBySlug("none", user);
    assertTrue(result.isEmpty());
  }

  @Test
  void should_find_recent_articles_with_cursor_empty() {
    when(articleReadService.findArticlesWithCursor(any(), any(), any(), any()))
        .thenReturn(new ArrayList<>());

    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    CursorPager<ArticleData> result =
        articleQueryService.findRecentArticlesWithCursor(null, null, null, page, user);
    assertTrue(result.getData().isEmpty());
  }

  @Test
  void should_find_recent_articles_with_cursor() {
    ArticleData articleData = createArticleData("a1");
    when(articleReadService.findArticlesWithCursor(any(), any(), any(), any()))
        .thenReturn(new ArrayList<>(Arrays.asList("a1")));
    when(articleReadService.findArticles(anyList())).thenReturn(Arrays.asList(articleData));
    when(articleFavoritesReadService.articlesFavoriteCount(anyList()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("a1", 1)));
    when(articleFavoritesReadService.userFavorites(anyList(), any()))
        .thenReturn(new HashSet<>());
    when(userRelationshipQueryService.followingAuthors(any(), anyList()))
        .thenReturn(new HashSet<>());

    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    CursorPager<ArticleData> result =
        articleQueryService.findRecentArticlesWithCursor(null, null, null, page, user);
    assertEquals(1, result.getData().size());
  }

  @Test
  void should_find_recent_articles_with_cursor_has_extra() {
    List<String> ids = new ArrayList<>(Arrays.asList("a1", "a2"));
    ArticleData a1 = createArticleData("a1");
    when(articleReadService.findArticlesWithCursor(any(), any(), any(), any())).thenReturn(ids);
    when(articleReadService.findArticles(anyList())).thenReturn(Arrays.asList(a1));
    when(articleFavoritesReadService.articlesFavoriteCount(anyList()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("a1", 1)));
    when(articleFavoritesReadService.userFavorites(anyList(), any()))
        .thenReturn(new HashSet<>());
    when(userRelationshipQueryService.followingAuthors(any(), anyList()))
        .thenReturn(new HashSet<>());

    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 1, Direction.NEXT);
    CursorPager<ArticleData> result =
        articleQueryService.findRecentArticlesWithCursor(null, null, null, page, user);
    assertTrue(result.hasNext());
  }

  @Test
  void should_find_user_feed_with_cursor_empty_followed() {
    when(userRelationshipQueryService.followedUsers(any())).thenReturn(new ArrayList<>());

    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    CursorPager<ArticleData> result =
        articleQueryService.findUserFeedWithCursor(user, page);
    assertTrue(result.getData().isEmpty());
  }

  @Test
  void should_find_user_feed_with_cursor() {
    ArticleData articleData = createArticleData("a1");
    when(userRelationshipQueryService.followedUsers(any()))
        .thenReturn(Arrays.asList("followed-user"));
    when(articleReadService.findArticlesOfAuthorsWithCursor(anyList(), any()))
        .thenReturn(new ArrayList<>(Arrays.asList(articleData)));
    when(articleFavoritesReadService.articlesFavoriteCount(anyList()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("a1", 1)));
    when(articleFavoritesReadService.userFavorites(anyList(), any()))
        .thenReturn(new HashSet<>());
    when(userRelationshipQueryService.followingAuthors(any(), anyList()))
        .thenReturn(new HashSet<>());

    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    CursorPager<ArticleData> result =
        articleQueryService.findUserFeedWithCursor(user, page);
    assertEquals(1, result.getData().size());
  }

  @Test
  void should_find_recent_articles() {
    ArticleData articleData = createArticleData("a1");
    when(articleReadService.queryArticles(any(), any(), any(), any()))
        .thenReturn(Arrays.asList("a1"));
    when(articleReadService.countArticle(any(), any(), any())).thenReturn(1);
    when(articleReadService.findArticles(anyList())).thenReturn(Arrays.asList(articleData));
    when(articleFavoritesReadService.articlesFavoriteCount(anyList()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("a1", 1)));
    when(articleFavoritesReadService.userFavorites(anyList(), any()))
        .thenReturn(new HashSet<>());
    when(userRelationshipQueryService.followingAuthors(any(), anyList()))
        .thenReturn(new HashSet<>());

    Page page = new Page(0, 10);
    ArticleDataList result =
        articleQueryService.findRecentArticles(null, null, null, page, user);
    assertEquals(1, result.getArticleDatas().size());
    assertEquals(1, result.getCount());
  }

  @Test
  void should_find_recent_articles_empty() {
    when(articleReadService.queryArticles(any(), any(), any(), any()))
        .thenReturn(new ArrayList<>());
    when(articleReadService.countArticle(any(), any(), any())).thenReturn(0);

    Page page = new Page(0, 10);
    ArticleDataList result =
        articleQueryService.findRecentArticles(null, null, null, page, user);
    assertTrue(result.getArticleDatas().isEmpty());
  }

  @Test
  void should_find_user_feed() {
    ArticleData articleData = createArticleData("a1");
    when(userRelationshipQueryService.followedUsers(any()))
        .thenReturn(Arrays.asList("followed-user"));
    when(articleReadService.findArticlesOfAuthors(anyList(), any()))
        .thenReturn(Arrays.asList(articleData));
    when(articleReadService.countFeedSize(anyList())).thenReturn(1);
    when(articleFavoritesReadService.articlesFavoriteCount(anyList()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("a1", 1)));
    when(articleFavoritesReadService.userFavorites(anyList(), any()))
        .thenReturn(new HashSet<>());
    when(userRelationshipQueryService.followingAuthors(any(), anyList()))
        .thenReturn(new HashSet<>());

    Page page = new Page(0, 10);
    ArticleDataList result = articleQueryService.findUserFeed(user, page);
    assertEquals(1, result.getArticleDatas().size());
  }

  @Test
  void should_find_user_feed_empty_when_no_followed_users() {
    when(userRelationshipQueryService.followedUsers(any())).thenReturn(new ArrayList<>());

    Page page = new Page(0, 10);
    ArticleDataList result = articleQueryService.findUserFeed(user, page);
    assertEquals(0, result.getCount());
  }

  @Test
  void should_fill_extra_info_without_current_user() {
    ArticleData articleData = createArticleData("a1");
    when(articleReadService.queryArticles(any(), any(), any(), any()))
        .thenReturn(Arrays.asList("a1"));
    when(articleReadService.countArticle(any(), any(), any())).thenReturn(1);
    when(articleReadService.findArticles(anyList())).thenReturn(Arrays.asList(articleData));
    when(articleFavoritesReadService.articlesFavoriteCount(anyList()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("a1", 0)));

    Page page = new Page(0, 10);
    ArticleDataList result =
        articleQueryService.findRecentArticles(null, null, null, page, null);
    assertEquals(1, result.getArticleDatas().size());
  }
}
