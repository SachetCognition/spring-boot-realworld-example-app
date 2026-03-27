package io.spring.graphql;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.netflix.graphql.dgs.DgsQueryExecutor;
import com.netflix.graphql.dgs.autoconfig.DgsAutoConfiguration;
import io.spring.application.ArticleQueryService;
import io.spring.application.CommentQueryService;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.ProfileQueryService;
import io.spring.application.TagsQueryService;
import io.spring.application.UserQueryService;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.CommentRepository;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.exception.GraphQLCustomizeExceptionHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest(
    classes = {
      DgsAutoConfiguration.class,
      ArticleDatafetcher.class,
      ArticleMutation.class,
      CommentMutation.class,
      CommentDatafetcher.class,
      UserMutation.class,
      MeDatafetcher.class,
      ProfileDatafetcher.class,
      TagDatafetcher.class,
      RelationMutation.class,
      GraphQLCustomizeExceptionHandler.class,
    })
public class ArticleDatafetcherTest {

  @Autowired private DgsQueryExecutor dgsQueryExecutor;

  @MockBean private ArticleQueryService articleQueryService;
  @MockBean private ArticleCommandService articleCommandService;
  @MockBean private ArticleFavoriteRepository articleFavoriteRepository;
  @MockBean private ArticleRepository articleRepository;
  @MockBean private UserRepository userRepository;
  @MockBean private UserQueryService userQueryService;
  @MockBean private PasswordEncoder passwordEncoder;
  @MockBean private JwtService jwtService;
  @MockBean private ProfileQueryService profileQueryService;
  @MockBean private CommentQueryService commentQueryService;
  @MockBean private CommentRepository commentRepository;
  @MockBean private TagsQueryService tagsQueryService;
  @MockBean private io.spring.application.user.UserService userService;

  private User user;

  @BeforeEach
  public void setUp() {
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    TestingAuthenticationToken auth = new TestingAuthenticationToken(user, null);
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  public void should_get_feed_with_first() {
    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(new ArrayList<>(), Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(cursorPager);

    String query =
        "query { feed(first: 10) { edges { cursor node { title slug } } pageInfo { hasNextPage"
            + " hasPreviousPage } } }";

    dgsQueryExecutor.execute(query);
  }

  @Test
  public void should_get_feed_with_last() {
    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(new ArrayList<>(), Direction.PREV, false);
    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(cursorPager);

    String query =
        "query { feed(last: 10) { edges { cursor node { title slug } } pageInfo { hasNextPage"
            + " hasPreviousPage } } }";

    dgsQueryExecutor.execute(query);
  }

  @Test
  public void should_get_articles_with_first() {
    DateTime now = new DateTime();
    ArticleData articleData =
        new ArticleData(
            "id1",
            "test-title",
            "Test Title",
            "desc",
            "body",
            false,
            0,
            now,
            now,
            Arrays.asList("java"),
            new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false));

    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(cursorPager);

    String query =
        "query { articles(first: 10) { edges { cursor node { title slug body description favorited"
            + " favoritesCount tagList createdAt updatedAt } } pageInfo { hasNextPage"
            + " hasPreviousPage startCursor endCursor } } }";

    dgsQueryExecutor.execute(query);
  }

  @Test
  public void should_get_articles_with_last() {
    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(new ArrayList<>(), Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(cursorPager);

    String query =
        "query { articles(last: 10) { edges { cursor node { title } } pageInfo { hasNextPage } } }";

    dgsQueryExecutor.execute(query);
  }

  @Test
  public void should_get_articles_with_filters() {
    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(new ArrayList<>(), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(cursorPager);

    String query =
        "query { articles(first: 10, withTag: \"java\", authoredBy: \"testuser\", favoritedBy:"
            + " \"other\") { edges { node { title } } } }";

    dgsQueryExecutor.execute(query);
  }

  @Test
  public void should_get_article_by_slug() {
    DateTime now = new DateTime();
    ArticleData articleData =
        new ArticleData(
            "id1",
            "test-title",
            "Test Title",
            "desc",
            "body",
            false,
            0,
            now,
            now,
            Arrays.asList("java"),
            new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false));
    when(articleQueryService.findBySlug(eq("test-title"), any()))
        .thenReturn(Optional.of(articleData));

    // Mock profile for author sub-fetcher
    ProfileData profileData =
        new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false);
    when(profileQueryService.findByUsername(eq(user.getUsername()), any()))
        .thenReturn(Optional.of(profileData));

    String query =
        "query { article(slug: \"test-title\") { title slug body description favorited"
            + " favoritesCount tagList createdAt updatedAt author { username bio image following } } }";

    dgsQueryExecutor.execute(query);
  }

  @Test
  public void should_get_article_with_comments_and_comment_article() {
    DateTime now = new DateTime();
    ArticleData articleData =
        new ArticleData(
            "id1",
            "test-title",
            "Test Title",
            "desc",
            "body",
            false,
            0,
            now,
            now,
            Arrays.asList("java"),
            new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false));
    when(articleQueryService.findBySlug(eq("test-title"), any()))
        .thenReturn(Optional.of(articleData));

    // Mock for comment article sub-fetcher
    when(articleQueryService.findById(eq("id1"), any()))
        .thenReturn(Optional.of(articleData));

    CommentData commentData =
        new CommentData(
            "c1",
            "test comment",
            "id1",
            now,
            now,
            new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false));

    CursorPager<CommentData> commentPager =
        new CursorPager<>(Arrays.asList(commentData), Direction.NEXT, false);
    when(commentQueryService.findByArticleIdWithCursor(any(), any(), any()))
        .thenReturn(commentPager);

    ProfileData profileData =
        new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false);
    when(profileQueryService.findByUsername(eq(user.getUsername()), any()))
        .thenReturn(Optional.of(profileData));

    String query =
        "query { article(slug: \"test-title\") { title slug "
            + "comments(first: 10) { edges { cursor node { id body createdAt updatedAt "
            + "author { username bio image following } "
            + "article { title slug author { username } } } } "
            + "pageInfo { hasNextPage hasPreviousPage startCursor endCursor } } } }";

    dgsQueryExecutor.execute(query);
  }

  @Test
  public void should_get_profile_with_articles() {
    ProfileData profileData =
        new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false);
    when(profileQueryService.findByUsername(eq("testuser"), any()))
        .thenReturn(Optional.of(profileData));

    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(new ArrayList<>(), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(cursorPager);

    String query =
        "query { profile(username: \"testuser\") { profile { username bio "
            + "articles(first: 10) { edges { cursor node { title slug } } "
            + "pageInfo { hasNextPage hasPreviousPage } } } } }";

    dgsQueryExecutor.execute(query);
  }

  @Test
  public void should_get_profile_with_articles_last() {
    ProfileData profileData =
        new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false);
    when(profileQueryService.findByUsername(eq("testuser"), any()))
        .thenReturn(Optional.of(profileData));

    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(new ArrayList<>(), Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(cursorPager);

    String query =
        "query { profile(username: \"testuser\") { profile { username "
            + "articles(last: 5) { edges { cursor node { title } } "
            + "pageInfo { hasNextPage hasPreviousPage } } } } }";

    dgsQueryExecutor.execute(query);
  }

  @Test
  public void should_get_profile_with_favorites() {
    ProfileData profileData =
        new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false);
    when(profileQueryService.findByUsername(eq("testuser"), any()))
        .thenReturn(Optional.of(profileData));

    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(new ArrayList<>(), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(cursorPager);

    String query =
        "query { profile(username: \"testuser\") { profile { username "
            + "favorites(first: 10) { edges { cursor node { title } } "
            + "pageInfo { hasNextPage hasPreviousPage } } } } }";

    dgsQueryExecutor.execute(query);
  }

  @Test
  public void should_get_profile_with_favorites_last() {
    ProfileData profileData =
        new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false);
    when(profileQueryService.findByUsername(eq("testuser"), any()))
        .thenReturn(Optional.of(profileData));

    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(new ArrayList<>(), Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(cursorPager);

    String query =
        "query { profile(username: \"testuser\") { profile { username "
            + "favorites(last: 5) { edges { node { title } } } } } }";

    dgsQueryExecutor.execute(query);
  }

  @Test
  public void should_get_profile_with_feed() {
    ProfileData profileData =
        new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false);
    when(profileQueryService.findByUsername(eq("testuser"), any()))
        .thenReturn(Optional.of(profileData));
    when(userRepository.findByUsername(eq("testuser"))).thenReturn(Optional.of(user));

    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(new ArrayList<>(), Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(cursorPager);

    String query =
        "query { profile(username: \"testuser\") { profile { username "
            + "feed(first: 10) { edges { cursor node { title } } "
            + "pageInfo { hasNextPage hasPreviousPage } } } } }";

    dgsQueryExecutor.execute(query);
  }

  @Test
  public void should_get_profile_with_feed_last() {
    ProfileData profileData =
        new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false);
    when(profileQueryService.findByUsername(eq("testuser"), any()))
        .thenReturn(Optional.of(profileData));
    when(userRepository.findByUsername(eq("testuser"))).thenReturn(Optional.of(user));

    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(new ArrayList<>(), Direction.PREV, false);
    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(cursorPager);

    String query =
        "query { profile(username: \"testuser\") { profile { username "
            + "feed(last: 5) { edges { node { title } } } } } }";

    dgsQueryExecutor.execute(query);
  }

  @Test
  public void should_get_profile_with_feed_with_data() {
    DateTime now = new DateTime();
    ProfileData profileData =
        new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false);
    when(profileQueryService.findByUsername(eq("testuser"), any()))
        .thenReturn(Optional.of(profileData));
    when(userRepository.findByUsername(eq("testuser"))).thenReturn(Optional.of(user));

    ArticleData articleData =
        new ArticleData(
            "id1",
            "test-title",
            "Test Title",
            "desc",
            "body",
            false,
            0,
            now,
            now,
            Arrays.asList("java"),
            new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false));

    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(cursorPager);

    String query =
        "query { profile(username: \"testuser\") { profile { username "
            + "feed(first: 10) { edges { cursor node { title slug body description createdAt updatedAt } }"
            + " pageInfo { hasNextPage hasPreviousPage startCursor endCursor } } } } }";

    dgsQueryExecutor.execute(query);
  }
}
