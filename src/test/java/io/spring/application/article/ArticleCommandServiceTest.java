package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ArticleCommandServiceTest {

  private ArticleRepository articleRepository;
  private ArticleCommandService articleCommandService;
  private User user;

  @BeforeEach
  void setUp() {
    articleRepository = mock(ArticleRepository.class);
    articleCommandService = new ArticleCommandService(articleRepository);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
  }

  @Test
  void should_create_article() {
    NewArticleParam param = new NewArticleParam("Title", "Desc", "Body", Arrays.asList("java"));
    Article article = articleCommandService.createArticle(param, user);
    assertNotNull(article);
    assertEquals("Title", article.getTitle());
    assertEquals("Desc", article.getDescription());
    assertEquals("Body", article.getBody());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  void should_update_article() {
    Article article = new Article("Title", "Desc", "Body", Arrays.asList("java"), user.getId());
    UpdateArticleParam param = new UpdateArticleParam("New Title", "New Body", "New Desc");
    Article updated = articleCommandService.updateArticle(article, param);
    assertNotNull(updated);
    assertEquals("New Title", updated.getTitle());
    assertEquals("New Body", updated.getBody());
    assertEquals("New Desc", updated.getDescription());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  void should_create_article_with_empty_tags() {
    NewArticleParam param = new NewArticleParam("Title", "Desc", "Body", Arrays.asList());
    Article article = articleCommandService.createArticle(param, user);
    assertNotNull(article);
    assertTrue(article.getTags().isEmpty());
  }
}
