package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.spring.TestHelper.articleDataFixture;
import static java.util.Arrays.asList;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ArticleQueryService;
import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.Page;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleDataList;
import java.util.ArrayList;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ArticlesApi.class)
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class CursorPaginationArticlesApiTest extends TestWithCurrentUser {

  @MockBean private ArticleQueryService articleQueryService;

  @MockBean private ArticleCommandService articleCommandService;

  @Autowired private MockMvc mvc;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_get_articles_with_cursor_forward() throws Exception {
    ArticleData article1 = articleDataFixture("1", user);
    ArticleData article2 = articleDataFixture("2", user);
    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(asList(article1, article2), Direction.NEXT, true);

    when(articleQueryService.findRecentArticlesWithCursor(
            eq(null), eq(null), eq(null), any(CursorPageParameter.class), eq(null)))
        .thenReturn(cursorPager);

    given()
        .param("cursor", String.valueOf(new DateTime().getMillis()))
        .param("limit", "20")
        .param("direction", "next")
        .when()
        .get("/articles")
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("hasNext", equalTo(true))
        .body("hasPrevious", equalTo(false))
        .body("articles.size()", equalTo(2));
  }

  @Test
  public void should_get_articles_with_cursor_backward() throws Exception {
    ArticleData article1 = articleDataFixture("1", user);
    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(asList(article1), Direction.PREV, true);

    when(articleQueryService.findRecentArticlesWithCursor(
            eq(null), eq(null), eq(null), any(CursorPageParameter.class), eq(null)))
        .thenReturn(cursorPager);

    given()
        .param("cursor", String.valueOf(new DateTime().getMillis()))
        .param("limit", "20")
        .param("direction", "prev")
        .when()
        .get("/articles")
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("hasNext", equalTo(false))
        .body("hasPrevious", equalTo(true))
        .body("articles.size()", equalTo(1));
  }

  @Test
  public void should_get_articles_with_cursor_and_tag_filter() throws Exception {
    ArticleData article1 = articleDataFixture("1", user);
    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(asList(article1), Direction.NEXT, false);

    when(articleQueryService.findRecentArticlesWithCursor(
            eq("dragons"), eq(null), eq(null), any(CursorPageParameter.class), eq(null)))
        .thenReturn(cursorPager);

    given()
        .param("cursor", String.valueOf(new DateTime().getMillis()))
        .param("limit", "20")
        .param("direction", "next")
        .param("tag", "dragons")
        .when()
        .get("/articles")
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("hasNext", equalTo(false))
        .body("articles.size()", equalTo(1));
  }

  @Test
  public void should_get_articles_with_cursor_and_author_filter() throws Exception {
    ArticleData article1 = articleDataFixture("1", user);
    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(asList(article1), Direction.NEXT, false);

    when(articleQueryService.findRecentArticlesWithCursor(
            eq(null),
            eq("johnjacob"),
            eq(null),
            any(CursorPageParameter.class),
            eq(null)))
        .thenReturn(cursorPager);

    given()
        .param("cursor", String.valueOf(new DateTime().getMillis()))
        .param("limit", "20")
        .param("direction", "next")
        .param("author", "johnjacob")
        .when()
        .get("/articles")
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("hasNext", equalTo(false))
        .body("articles.size()", equalTo(1));
  }

  @Test
  public void should_get_first_page_when_cursor_is_absent() throws Exception {
    ArticleDataList articleDataList =
        new ArticleDataList(
            asList(articleDataFixture("1", user), articleDataFixture("2", user)), 2);
    when(articleQueryService.findRecentArticles(
            eq(null), eq(null), eq(null), eq(new Page(0, 20)), eq(null)))
        .thenReturn(articleDataList);

    RestAssuredMockMvc.when()
        .get("/articles")
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("articlesCount", equalTo(2))
        .body("articles.size()", equalTo(2));
  }

  @Test
  public void should_get_feed_with_cursor() throws Exception {
    ArticleData article1 = articleDataFixture("1", user);
    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(asList(article1), Direction.NEXT, false);

    when(articleQueryService.findUserFeedWithCursor(eq(user), any(CursorPageParameter.class)))
        .thenReturn(cursorPager);

    given()
        .header("Authorization", "Token " + token)
        .param("cursor", String.valueOf(new DateTime().getMillis()))
        .param("limit", "20")
        .param("direction", "next")
        .when()
        .get("/articles/feed")
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("hasNext", equalTo(false))
        .body("hasPrevious", equalTo(false))
        .body("articles.size()", equalTo(1));
  }

  @Test
  public void should_get_feed_cursor_401_without_login() throws Exception {
    given()
        .param("cursor", String.valueOf(new DateTime().getMillis()))
        .param("limit", "20")
        .param("direction", "next")
        .when()
        .get("/articles/feed")
        .prettyPeek()
        .then()
        .statusCode(401);
  }

  @Test
  public void should_still_support_offset_pagination() throws Exception {
    ArticleDataList articleDataList =
        new ArticleDataList(
            asList(articleDataFixture("1", user), articleDataFixture("2", user)), 2);
    when(articleQueryService.findRecentArticles(
            eq(null), eq(null), eq(null), eq(new Page(10, 5)), eq(null)))
        .thenReturn(articleDataList);

    given()
        .param("offset", "10")
        .param("limit", "5")
        .when()
        .get("/articles")
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("articlesCount", equalTo(2))
        .body("articles.size()", equalTo(2));
  }
}
