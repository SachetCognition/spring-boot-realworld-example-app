package io.spring.application.article;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.repository.MyBatisArticleRepository;
import io.spring.infrastructure.repository.MyBatisUserRepository;
import java.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({
  ArticleCommandService.class,
  MyBatisArticleRepository.class,
  MyBatisUserRepository.class,
})
public class ArticleCommandServiceTest extends DbTestBase {

  @Autowired private ArticleCommandService articleCommandService;
  @Autowired private ArticleRepository articleRepository;
  @Autowired private UserRepository userRepository;

  private User user;

  @BeforeEach
  public void setUp() {
    user = new User("test@test.com", "testuser", "password", "", "");
    userRepository.save(user);
  }

  @Test
  public void should_create_article() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Article")
            .description("Test Description")
            .body("Test Body")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    Article article = articleCommandService.createArticle(param, user);
    Assertions.assertNotNull(article);
    Assertions.assertEquals("Test Article", article.getTitle());
    Assertions.assertEquals("Test Description", article.getDescription());
    Assertions.assertEquals("Test Body", article.getBody());
    Assertions.assertNotNull(article.getSlug());
  }

  @Test
  public void should_update_article() {
    NewArticleParam createParam =
        NewArticleParam.builder()
            .title("Original Title")
            .description("Original Description")
            .body("Original Body")
            .tagList(Arrays.asList("java"))
            .build();

    Article article = articleCommandService.createArticle(createParam, user);

    UpdateArticleParam updateParam = new UpdateArticleParam("New Title", "New Body", "New Description");
    Article updated = articleCommandService.updateArticle(article, updateParam);

    Assertions.assertEquals("New Title", updated.getTitle());
    Assertions.assertEquals("New Body", updated.getBody());
    Assertions.assertEquals("New Description", updated.getDescription());
  }
}
