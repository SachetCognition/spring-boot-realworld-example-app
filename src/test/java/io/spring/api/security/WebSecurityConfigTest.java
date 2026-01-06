package io.spring.api.security;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.ArticleApi;
import io.spring.api.ArticleFavoriteApi;
import io.spring.api.ArticlesApi;
import io.spring.api.CommentsApi;
import io.spring.api.CurrentUserApi;
import io.spring.api.ProfileApi;
import io.spring.api.TagsApi;
import io.spring.api.UsersApi;
import io.spring.application.ArticleQueryService;
import io.spring.application.CommentQueryService;
import io.spring.application.ProfileQueryService;
import io.spring.application.TagsQueryService;
import io.spring.application.UserQueryService;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.data.ArticleDataList;
import io.spring.application.data.ProfileData;
import io.spring.application.data.UserData;
import io.spring.application.user.UserService;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.CommentRepository;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({UsersApi.class, CurrentUserApi.class, ArticlesApi.class, ProfileApi.class, TagsApi.class, CommentsApi.class, ArticleFavoriteApi.class, ArticleApi.class})
@Import({WebSecurityConfig.class, JacksonCustomizations.class, BCryptPasswordEncoder.class})
public class WebSecurityConfigTest {

  @Autowired private MockMvc mvc;

  @MockBean private UserRepository userRepository;

  @MockBean private JwtService jwtService;

  @MockBean private UserReadService userReadService;

  @MockBean private UserService userService;

  @MockBean private UserQueryService userQueryService;

  @MockBean private ArticleQueryService articleQueryService;

  @MockBean private ArticleCommandService articleCommandService;

  @MockBean private ProfileQueryService profileQueryService;

  @MockBean private TagsQueryService tagsQueryService;

  @MockBean private ArticleRepository articleRepository;

  @MockBean private CommentRepository commentRepository;

  @MockBean private CommentQueryService commentQueryService;

  @MockBean private ArticleFavoriteRepository articleFavoriteRepository;

  private User user;
  private UserData userData;
  private String token;
  private String defaultAvatar;

  @BeforeEach
  public void setUp() {
    RestAssuredMockMvc.mockMvc(mvc);
    defaultAvatar = "https://static.productionready.io/images/smiley-cyrus.jpg";

    user = new User("test@example.com", "testuser", "password", "bio", defaultAvatar);
    userData = new UserData(user.getId(), "test@example.com", "testuser", "bio", defaultAvatar);
    token = "valid-token";

    when(jwtService.getSubFromToken(eq(token))).thenReturn(Optional.of(user.getId()));
    when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));
    when(userReadService.findById(eq(user.getId()))).thenReturn(userData);
  }

  @Test
  public void should_allow_get_articles_without_authentication() {
    when(articleQueryService.findRecentArticles(any(), any(), any(), any(), any()))
        .thenReturn(new ArticleDataList(new ArrayList<>(), 0));

    given()
        .contentType("application/json")
        .when()
        .get("/articles")
        .then()
        .statusCode(200);
  }

  @Test
  public void should_allow_get_single_article_without_authentication() {
    when(articleQueryService.findBySlug(eq("test-slug"), any())).thenReturn(Optional.empty());

    given()
        .contentType("application/json")
        .when()
        .get("/articles/test-slug")
        .then()
        .statusCode(404);
  }

  @Test
  public void should_allow_get_article_comments_without_authentication() {
    when(articleQueryService.findBySlug(eq("test-slug"), any())).thenReturn(Optional.empty());

    given()
        .contentType("application/json")
        .when()
        .get("/articles/test-slug/comments")
        .then()
        .statusCode(404);
  }

  @Test
  public void should_require_authentication_for_articles_feed() {
    given()
        .contentType("application/json")
        .when()
        .get("/articles/feed")
        .then()
        .statusCode(401);
  }

  @Test
  public void should_allow_articles_feed_with_valid_token() {
    when(articleQueryService.findUserFeed(any(), any()))
        .thenReturn(new ArticleDataList(new ArrayList<>(), 0));

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .when()
        .get("/articles/feed")
        .then()
        .statusCode(200);
  }

  @Test
  public void should_require_authentication_for_create_article() {
    given()
        .contentType("application/json")
        .body("{\"article\": {\"title\": \"test\", \"description\": \"test\", \"body\": \"test\"}}")
        .when()
        .post("/articles")
        .then()
        .statusCode(401);
  }

  @Test
  public void should_allow_get_tags_without_authentication() {
    when(tagsQueryService.allTags()).thenReturn(Arrays.asList("tag1", "tag2"));

    given()
        .contentType("application/json")
        .when()
        .get("/tags")
        .then()
        .statusCode(200);
  }

  @Test
  public void should_allow_get_profile_without_authentication() {
    ProfileData profileData = new ProfileData(user.getId(), "testuser", "bio", defaultAvatar, false);
    when(profileQueryService.findByUsername(eq("testuser"), any())).thenReturn(Optional.of(profileData));

    given()
        .contentType("application/json")
        .when()
        .get("/profiles/testuser")
        .then()
        .statusCode(200);
  }

  @Test
  public void should_require_authentication_for_follow_profile() {
    given()
        .contentType("application/json")
        .when()
        .post("/profiles/testuser/follow")
        .then()
        .statusCode(401);
  }

  @Test
  public void should_require_authentication_for_unfollow_profile() {
    given()
        .contentType("application/json")
        .when()
        .delete("/profiles/testuser/follow")
        .then()
        .statusCode(401);
  }

  @Test
  public void should_require_authentication_for_get_current_user() {
    given()
        .contentType("application/json")
        .when()
        .get("/user")
        .then()
        .statusCode(401);
  }

  @Test
  public void should_allow_get_current_user_with_valid_token() {
    when(userQueryService.findById(any())).thenReturn(Optional.of(userData));

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .when()
        .get("/user")
        .then()
        .statusCode(200);
  }

  @Test
  public void should_require_authentication_for_update_user() {
    given()
        .contentType("application/json")
        .body("{\"user\": {\"email\": \"new@example.com\"}}")
        .when()
        .put("/user")
        .then()
        .statusCode(401);
  }

  @Test
  public void should_return_401_for_invalid_token() {
    String invalidToken = "invalid-token";
    when(jwtService.getSubFromToken(eq(invalidToken))).thenReturn(Optional.empty());

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + invalidToken)
        .when()
        .get("/user")
        .then()
        .statusCode(401);
  }

  @Test
  public void should_return_401_for_token_with_nonexistent_user() {
    String tokenForNonexistentUser = "token-for-nonexistent";
    String nonexistentUserId = "nonexistent-id";
    when(jwtService.getSubFromToken(eq(tokenForNonexistentUser))).thenReturn(Optional.of(nonexistentUserId));
    when(userRepository.findById(eq(nonexistentUserId))).thenReturn(Optional.empty());

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + tokenForNonexistentUser)
        .when()
        .get("/user")
        .then()
        .statusCode(401);
  }

  @Test
  public void should_handle_options_request_for_cors_preflight() {
    given()
        .contentType("application/json")
        .when()
        .options("/articles")
        .then()
        .statusCode(200);
  }

  @Test
  public void should_require_authentication_for_favorite_article() {
    given()
        .contentType("application/json")
        .when()
        .post("/articles/test-slug/favorite")
        .then()
        .statusCode(401);
  }

  @Test
  public void should_require_authentication_for_unfavorite_article() {
    given()
        .contentType("application/json")
        .when()
        .delete("/articles/test-slug/favorite")
        .then()
        .statusCode(401);
  }

  @Test
  public void should_require_authentication_for_create_comment() {
    given()
        .contentType("application/json")
        .body("{\"comment\": {\"body\": \"test comment\"}}")
        .when()
        .post("/articles/test-slug/comments")
        .then()
        .statusCode(401);
  }

  @Test
  public void should_require_authentication_for_delete_article() {
    given()
        .contentType("application/json")
        .when()
        .delete("/articles/test-slug")
        .then()
        .statusCode(401);
  }

  @Test
  public void should_require_authentication_for_update_article() {
    given()
        .contentType("application/json")
        .body("{\"article\": {\"title\": \"updated\"}}")
        .when()
        .put("/articles/test-slug")
        .then()
        .statusCode(401);
  }
}
