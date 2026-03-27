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
import io.spring.application.data.ProfileData;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.CommentRepository;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.exception.GraphQLCustomizeExceptionHandler;
import java.util.Optional;
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
      ProfileDatafetcher.class,
      UserMutation.class,
      MeDatafetcher.class,
      ArticleDatafetcher.class,
      CommentDatafetcher.class,
      TagDatafetcher.class,
      RelationMutation.class,
      ArticleMutation.class,
      CommentMutation.class,
      GraphQLCustomizeExceptionHandler.class,
    })
public class ProfileDatafetcherTest {

  @Autowired private DgsQueryExecutor dgsQueryExecutor;

  @MockBean private ProfileQueryService profileQueryService;
  @MockBean private UserRepository userRepository;
  @MockBean private UserQueryService userQueryService;
  @MockBean private PasswordEncoder passwordEncoder;
  @MockBean private JwtService jwtService;
  @MockBean private ArticleQueryService articleQueryService;
  @MockBean private CommentQueryService commentQueryService;
  @MockBean private TagsQueryService tagsQueryService;
  @MockBean private ArticleRepository articleRepository;
  @MockBean private CommentRepository commentRepository;
  @MockBean private ArticleFavoriteRepository articleFavoriteRepository;
  @MockBean private ArticleCommandService articleCommandService;
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
  public void should_get_profile() {
    ProfileData profileData =
        new ProfileData(user.getId(), "testuser", "bio", "image", false);
    when(profileQueryService.findByUsername(eq("testuser"), any()))
        .thenReturn(Optional.of(profileData));

    String query = "query { profile(username: \"testuser\") { profile { username bio image following } } }";

    dgsQueryExecutor.execute(query);
  }

  @Test
  public void should_return_null_for_unknown_profile() {
    when(profileQueryService.findByUsername(eq("unknown"), any())).thenReturn(Optional.empty());

    String query = "query { profile(username: \"unknown\") { profile { username } } }";

    dgsQueryExecutor.execute(query);
  }
}
