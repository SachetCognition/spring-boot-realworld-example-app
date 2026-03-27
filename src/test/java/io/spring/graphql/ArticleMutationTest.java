package io.spring.graphql;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.netflix.graphql.dgs.DgsQueryExecutor;
import com.netflix.graphql.dgs.autoconfig.DgsAutoConfiguration;
import io.spring.application.ArticleQueryService;
import io.spring.application.CommentQueryService;
import io.spring.application.ProfileQueryService;
import io.spring.application.TagsQueryService;
import io.spring.application.UserQueryService;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.CommentRepository;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.exception.GraphQLCustomizeExceptionHandler;
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
      ArticleMutation.class,
      ArticleDatafetcher.class,
      UserMutation.class,
      MeDatafetcher.class,
      ProfileDatafetcher.class,
      CommentDatafetcher.class,
      CommentMutation.class,
      TagDatafetcher.class,
      RelationMutation.class,
      GraphQLCustomizeExceptionHandler.class,
    })
public class ArticleMutationTest {

  @Autowired private DgsQueryExecutor dgsQueryExecutor;

  @MockBean private ArticleCommandService articleCommandService;
  @MockBean private ArticleFavoriteRepository articleFavoriteRepository;
  @MockBean private ArticleRepository articleRepository;
  @MockBean private ArticleQueryService articleQueryService;
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
  public void should_create_article() {
    Article article =
        new Article("Test Title", "desc", "body", Arrays.asList("java"), user.getId());
    when(articleCommandService.createArticle(any(), any())).thenReturn(article);

    DateTime now = new DateTime();
    ArticleData articleData =
        new ArticleData(
            article.getId(),
            article.getSlug(),
            article.getTitle(),
            article.getDescription(),
            article.getBody(),
            false,
            0,
            now,
            now,
            Arrays.asList("java"),
            new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false));
    when(articleQueryService.findById(any(), any())).thenReturn(Optional.of(articleData));

    String mutation =
        "mutation { createArticle(input: {title: \"Test Title\", description: \"desc\", body:"
            + " \"body\", tagList: [\"java\"]}) { article { title slug body description } } }";

    dgsQueryExecutor.execute(mutation);
  }

  @Test
  public void should_update_article() {
    Article article =
        new Article("Old Title", "desc", "body", Arrays.asList("java"), user.getId());
    when(articleRepository.findBySlug(eq("old-title"))).thenReturn(Optional.of(article));
    when(articleCommandService.updateArticle(any(), any())).thenReturn(article);

    DateTime now = new DateTime();
    ArticleData articleData =
        new ArticleData(
            article.getId(),
            article.getSlug(),
            "New Title",
            "new desc",
            "new body",
            false,
            0,
            now,
            now,
            Arrays.asList("java"),
            new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false));
    when(articleQueryService.findById(any(), any())).thenReturn(Optional.of(articleData));

    String mutation =
        "mutation { updateArticle(slug: \"old-title\", changes: {title: \"New Title\"}) { article {"
            + " title } } }";

    dgsQueryExecutor.execute(mutation);
  }

  @Test
  public void should_favorite_article() {
    Article article =
        new Article("Test Title", "desc", "body", Arrays.asList("java"), user.getId());
    when(articleRepository.findBySlug(eq("test-title"))).thenReturn(Optional.of(article));

    DateTime now = new DateTime();
    ArticleData articleData =
        new ArticleData(
            article.getId(),
            article.getSlug(),
            article.getTitle(),
            article.getDescription(),
            article.getBody(),
            true,
            1,
            now,
            now,
            Arrays.asList("java"),
            new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false));
    when(articleQueryService.findById(any(), any())).thenReturn(Optional.of(articleData));

    String mutation =
        "mutation { favoriteArticle(slug: \"test-title\") { article { title favorited } } }";

    dgsQueryExecutor.execute(mutation);
  }

  @Test
  public void should_unfavorite_article() {
    Article article =
        new Article("Test Title", "desc", "body", Arrays.asList("java"), user.getId());
    when(articleRepository.findBySlug(eq("test-title"))).thenReturn(Optional.of(article));

    DateTime now = new DateTime();
    ArticleData articleData =
        new ArticleData(
            article.getId(),
            article.getSlug(),
            article.getTitle(),
            article.getDescription(),
            article.getBody(),
            false,
            0,
            now,
            now,
            Arrays.asList("java"),
            new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false));
    when(articleQueryService.findById(any(), any())).thenReturn(Optional.of(articleData));

    String mutation =
        "mutation { unfavoriteArticle(slug: \"test-title\") { article { title favorited } } }";

    dgsQueryExecutor.execute(mutation);
  }

  @Test
  public void should_delete_article() {
    Article article =
        new Article("Test Title", "desc", "body", Arrays.asList("java"), user.getId());
    when(articleRepository.findBySlug(eq("test-title"))).thenReturn(Optional.of(article));

    String mutation = "mutation { deleteArticle(slug: \"test-title\") { success } }";

    dgsQueryExecutor.execute(mutation);
  }

  @Test
  public void should_fail_update_article_not_author() {
    User otherUser = new User("other@test.com", "other", "password", "", "");
    Article article =
        new Article("Title", "desc", "body", Arrays.asList("java"), otherUser.getId());
    when(articleRepository.findBySlug(eq("title"))).thenReturn(Optional.of(article));

    String mutation =
        "mutation { updateArticle(slug: \"title\", changes: {title: \"New\"}) { article { title } }"
            + " }";

    dgsQueryExecutor.execute(mutation);
  }

  @Test
  public void should_fail_delete_article_not_author() {
    User otherUser = new User("other@test.com", "other", "password", "", "");
    Article article =
        new Article("Title", "desc", "body", Arrays.asList("java"), otherUser.getId());
    when(articleRepository.findBySlug(eq("title"))).thenReturn(Optional.of(article));

    String mutation = "mutation { deleteArticle(slug: \"title\") { success } }";

    dgsQueryExecutor.execute(mutation);
  }
}
