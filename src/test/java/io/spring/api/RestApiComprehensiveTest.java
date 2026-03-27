package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static java.util.Arrays.asList;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ArticleQueryService;
import io.spring.application.Page;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.data.ArticleDataList;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.CommentRepository;
import io.spring.core.favorite.ArticleFavoriteRepository;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({ArticlesApi.class, ArticleApi.class, ArticleFavoriteApi.class, CommentsApi.class})
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class RestApiComprehensiveTest extends TestWithCurrentUser {

  @Autowired private MockMvc mvc;

  @MockBean private ArticleQueryService articleQueryService;

  @MockBean private ArticleCommandService articleCommandService;

  @MockBean private ArticleRepository articleRepository;

  @MockBean private CommentRepository commentRepository;

  @MockBean private ArticleFavoriteRepository articleFavoriteRepository;

  @MockBean private io.spring.application.CommentQueryService commentQueryService;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_get_articles_with_tag_filter() throws Exception {
    when(articleQueryService.findRecentArticles(
            eq("java"), eq(null), eq(null), any(Page.class), eq(null)))
        .thenReturn(new ArticleDataList(new ArrayList<>(), 0));
    RestAssuredMockMvc.when()
        .get("/articles?tag=java")
        .then()
        .statusCode(200);
  }

  @Test
  public void should_get_articles_with_author_filter() throws Exception {
    when(articleQueryService.findRecentArticles(
            eq(null), eq("testauthor"), eq(null), any(Page.class), eq(null)))
        .thenReturn(new ArticleDataList(new ArrayList<>(), 0));
    RestAssuredMockMvc.when()
        .get("/articles?author=testauthor")
        .then()
        .statusCode(200);
  }

  @Test
  public void should_get_articles_with_favorited_filter() throws Exception {
    when(articleQueryService.findRecentArticles(
            eq(null), eq(null), eq("testuser"), any(Page.class), eq(null)))
        .thenReturn(new ArticleDataList(new ArrayList<>(), 0));
    RestAssuredMockMvc.when()
        .get("/articles?favorited=testuser")
        .then()
        .statusCode(200);
  }

  @Test
  public void should_get_articles_with_pagination() throws Exception {
    when(articleQueryService.findRecentArticles(
            eq(null), eq(null), eq(null), eq(new Page(0, 5)), eq(null)))
        .thenReturn(new ArticleDataList(new ArrayList<>(), 0));
    RestAssuredMockMvc.when()
        .get("/articles?offset=0&limit=5")
        .then()
        .statusCode(200);
  }

  @Test
  public void should_get_401_for_feed_without_auth() throws Exception {
    RestAssuredMockMvc.when()
        .get("/articles/feed")
        .then()
        .statusCode(401);
  }

  @Test
  public void should_get_feed_with_auth() throws Exception {
    when(articleQueryService.findUserFeed(eq(user), eq(new Page(0, 20))))
        .thenReturn(new ArticleDataList(new ArrayList<>(), 0));
    given()
        .header("Authorization", "Token " + token)
        .when()
        .get("/articles/feed")
        .then()
        .statusCode(200);
  }

  @Test
  public void should_get_401_for_create_article_without_auth() throws Exception {
    given()
        .contentType("application/json")
        .body("{\"article\":{\"title\":\"t\",\"description\":\"d\",\"body\":\"b\"}}")
        .when()
        .post("/articles")
        .then()
        .statusCode(401);
  }

  @Test
  public void should_get_404_for_update_nonexistent_article() throws Exception {
    when(articleRepository.findBySlug(eq("nonexistent"))).thenReturn(Optional.empty());
    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body("{\"article\":{\"title\":\"t\"}}")
        .when()
        .put("/articles/nonexistent")
        .then()
        .statusCode(404);
  }

  @Test
  public void should_get_error_for_delete_nonexistent_comment() throws Exception {
    Article article =
        new Article("title", "desc", "body", asList("java"), user.getId());
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    when(commentRepository.findById(eq(article.getId()), eq("nonexistent-id")))
        .thenReturn(Optional.empty());
    given()
        .header("Authorization", "Token " + token)
        .when()
        .delete("/articles/{slug}/comments/{id}", article.getSlug(), "nonexistent-id")
        .then()
        .statusCode(404);
  }

  @Test
  public void should_get_error_for_favorite_nonexistent_article() throws Exception {
    when(articleRepository.findBySlug(eq("nonexistent"))).thenReturn(Optional.empty());
    given()
        .header("Authorization", "Token " + token)
        .when()
        .post("/articles/nonexistent/favorite")
        .then()
        .statusCode(404);
  }
}
