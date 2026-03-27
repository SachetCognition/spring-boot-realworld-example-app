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
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.Comment;
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
      CommentMutation.class,
      CommentDatafetcher.class,
      ArticleMutation.class,
      ArticleDatafetcher.class,
      UserMutation.class,
      MeDatafetcher.class,
      ProfileDatafetcher.class,
      TagDatafetcher.class,
      RelationMutation.class,
      GraphQLCustomizeExceptionHandler.class,
    })
public class CommentMutationTest {

  @Autowired private DgsQueryExecutor dgsQueryExecutor;

  @MockBean private ArticleRepository articleRepository;
  @MockBean private CommentRepository commentRepository;
  @MockBean private CommentQueryService commentQueryService;
  @MockBean private ArticleCommandService articleCommandService;
  @MockBean private ArticleFavoriteRepository articleFavoriteRepository;
  @MockBean private ArticleQueryService articleQueryService;
  @MockBean private UserRepository userRepository;
  @MockBean private UserQueryService userQueryService;
  @MockBean private PasswordEncoder passwordEncoder;
  @MockBean private JwtService jwtService;
  @MockBean private ProfileQueryService profileQueryService;
  @MockBean private TagsQueryService tagsQueryService;
  @MockBean private io.spring.application.user.UserService userService;

  private User user;
  private Article article;

  @BeforeEach
  public void setUp() {
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    TestingAuthenticationToken auth = new TestingAuthenticationToken(user, null);
    SecurityContextHolder.getContext().setAuthentication(auth);

    article = new Article("Test Title", "desc", "body", Arrays.asList("java"), user.getId());
    when(articleRepository.findBySlug(eq("test-title"))).thenReturn(Optional.of(article));
  }

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  public void should_add_comment() {
    Comment comment = new Comment("test comment", user.getId(), article.getId());
    DateTime now = new DateTime();
    CommentData commentData =
        new CommentData(
            comment.getId(),
            comment.getBody(),
            article.getId(),
            now,
            now,
            new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false));
    when(commentQueryService.findById(any(), any())).thenReturn(Optional.of(commentData));

    String mutation =
        "mutation { addComment(slug: \"test-title\", body: \"test comment\") { comment { id body } }"
            + " }";

    dgsQueryExecutor.execute(mutation);
  }

  @Test
  public void should_delete_comment() {
    Comment comment = new Comment("test comment", user.getId(), article.getId());
    when(commentRepository.findById(eq(article.getId()), eq(comment.getId())))
        .thenReturn(Optional.of(comment));

    String mutation =
        "mutation { deleteComment(slug: \"test-title\", id: \""
            + comment.getId()
            + "\") { success } }";

    dgsQueryExecutor.execute(mutation);
  }

  @Test
  public void should_fail_delete_comment_not_author() {
    User otherUser = new User("other@test.com", "other", "password", "", "");
    Article otherArticle =
        new Article("Other Title", "desc", "body", Arrays.asList("java"), otherUser.getId());
    when(articleRepository.findBySlug(eq("other-title"))).thenReturn(Optional.of(otherArticle));

    Comment comment = new Comment("test comment", otherUser.getId(), otherArticle.getId());
    when(commentRepository.findById(eq(otherArticle.getId()), eq(comment.getId())))
        .thenReturn(Optional.of(comment));

    String mutation =
        "mutation { deleteComment(slug: \"other-title\", id: \""
            + comment.getId()
            + "\") { success } }";

    dgsQueryExecutor.execute(mutation);
  }
}
