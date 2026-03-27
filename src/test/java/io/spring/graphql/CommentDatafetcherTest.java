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
      CommentDatafetcher.class,
      CommentMutation.class,
      ArticleMutation.class,
      ArticleDatafetcher.class,
      UserMutation.class,
      MeDatafetcher.class,
      ProfileDatafetcher.class,
      TagDatafetcher.class,
      RelationMutation.class,
      GraphQLCustomizeExceptionHandler.class,
    })
public class CommentDatafetcherTest {

  @Autowired private DgsQueryExecutor dgsQueryExecutor;

  @MockBean private CommentQueryService commentQueryService;
  @MockBean private ArticleQueryService articleQueryService;
  @MockBean private ArticleCommandService articleCommandService;
  @MockBean private ArticleFavoriteRepository articleFavoriteRepository;
  @MockBean private ArticleRepository articleRepository;
  @MockBean private CommentRepository commentRepository;
  @MockBean private UserRepository userRepository;
  @MockBean private UserQueryService userQueryService;
  @MockBean private PasswordEncoder passwordEncoder;
  @MockBean private JwtService jwtService;
  @MockBean private ProfileQueryService profileQueryService;
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
  public void should_get_comments_of_article() {
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

    CommentData commentData =
        new CommentData(
            "c1",
            "test comment",
            "id1",
            now,
            now,
            new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false));

    CursorPager<CommentData> cursorPager =
        new CursorPager<>(Arrays.asList(commentData), Direction.NEXT, false);
    when(commentQueryService.findByArticleIdWithCursor(any(), any(), any()))
        .thenReturn(cursorPager);

    String query =
        "query { article(slug: \"test-title\") { title comments(first: 10) { edges { cursor node {"
            + " id body createdAt updatedAt } } pageInfo { hasNextPage hasPreviousPage startCursor"
            + " endCursor } } } }";

    dgsQueryExecutor.execute(query);
  }

  @Test
  public void should_get_comments_with_last() {
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

    CursorPager<CommentData> cursorPager =
        new CursorPager<>(new ArrayList<>(), Direction.PREV, false);
    when(commentQueryService.findByArticleIdWithCursor(any(), any(), any()))
        .thenReturn(cursorPager);

    String query =
        "query { article(slug: \"test-title\") { comments(last: 10) { edges { node { id body } }"
            + " } } }";

    dgsQueryExecutor.execute(query);
  }
}
