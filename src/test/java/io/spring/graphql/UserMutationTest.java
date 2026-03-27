package io.spring.graphql;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.netflix.graphql.dgs.DgsQueryExecutor;
import com.netflix.graphql.dgs.autoconfig.DgsAutoConfiguration;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.application.UserQueryService;
import io.spring.application.user.UserService;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.exception.GraphQLCustomizeExceptionHandler;
import java.util.Map;
import java.util.Optional;
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
      UserMutation.class,
      MeDatafetcher.class,
      ProfileDatafetcher.class,
      ArticleDatafetcher.class,
      CommentDatafetcher.class,
      TagDatafetcher.class,
      RelationMutation.class,
      ArticleMutation.class,
      CommentMutation.class,
      GraphQLCustomizeExceptionHandler.class,
    })
public class UserMutationTest {

  @Autowired private DgsQueryExecutor dgsQueryExecutor;

  @MockBean private UserRepository userRepository;
  @MockBean private UserService userService;
  @MockBean private UserQueryService userQueryService;
  @MockBean private PasswordEncoder passwordEncoder;
  @MockBean private JwtService jwtService;
  @MockBean private io.spring.application.ProfileQueryService profileQueryService;
  @MockBean private io.spring.application.ArticleQueryService articleQueryService;
  @MockBean private io.spring.application.CommentQueryService commentQueryService;
  @MockBean private io.spring.application.TagsQueryService tagsQueryService;
  @MockBean private io.spring.core.article.ArticleRepository articleRepository;
  @MockBean private io.spring.core.comment.CommentRepository commentRepository;
  @MockBean private io.spring.core.favorite.ArticleFavoriteRepository articleFavoriteRepository;
  @MockBean private io.spring.application.article.ArticleCommandService articleCommandService;

  @Test
  public void should_create_user_success() {
    User user = new User("test@test.com", "testuser", "password", "", "");
    when(userService.createUser(any())).thenReturn(user);
    when(jwtService.toToken(any())).thenReturn("test-token");

    String mutation =
        "mutation { createUser(input: {email: \"test@test.com\", username: \"testuser\", password:"
            + " \"password\"}) { ... on UserPayload { user { email username token } } } }";

    dgsQueryExecutor.execute(mutation);
  }

  @Test
  public void should_login_success() {
    User user = new User("test@test.com", "testuser", "encodedpw", "", "");
    when(userRepository.findByEmail(eq("test@test.com"))).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(eq("password"), eq("encodedpw"))).thenReturn(true);
    when(jwtService.toToken(any())).thenReturn("test-token");

    String mutation =
        "mutation { login(email: \"test@test.com\", password: \"password\") { user { email username"
            + " token } } }";

    dgsQueryExecutor.execute(mutation);
  }

  @Test
  public void should_fail_login_with_wrong_password() {
    when(userRepository.findByEmail(eq("test@test.com"))).thenReturn(Optional.empty());

    String mutation =
        "mutation { login(email: \"test@test.com\", password: \"wrong\") { user { email } } }";

    dgsQueryExecutor.execute(mutation);
  }

  @Test
  public void should_update_user_success() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");
    TestingAuthenticationToken auth = new TestingAuthenticationToken(user, null);
    SecurityContextHolder.getContext().setAuthentication(auth);

    String mutation =
        "mutation { updateUser(changes: {email: \"new@test.com\", username: \"newuser\"}) { user {"
            + " email username token } } }";

    try {
      dgsQueryExecutor.execute(mutation);
    } finally {
      SecurityContextHolder.clearContext();
    }
  }

  @Test
  public void should_return_null_when_anonymous_update_user() {
    SecurityContextHolder.clearContext();
    String mutation =
        "mutation { updateUser(changes: {email: \"new@test.com\"}) { user { email } } }";
    dgsQueryExecutor.execute(mutation);
  }
}
