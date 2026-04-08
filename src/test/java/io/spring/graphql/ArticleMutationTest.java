package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.article.ArticleCommandService;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.user.User;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.ArticlePayload;
import io.spring.graphql.types.CreateArticleInput;
import io.spring.graphql.types.DeletionStatus;
import io.spring.graphql.types.UpdateArticleInput;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class ArticleMutationTest {

  private ArticleCommandService articleCommandService;
  private ArticleFavoriteRepository articleFavoriteRepository;
  private ArticleRepository articleRepository;
  private ArticleMutation articleMutation;
  private User user;

  @BeforeEach
  void setUp() {
    articleCommandService = mock(ArticleCommandService.class);
    articleFavoriteRepository = mock(ArticleFavoriteRepository.class);
    articleRepository = mock(ArticleRepository.class);
    articleMutation =
        new ArticleMutation(articleCommandService, articleFavoriteRepository, articleRepository);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_create_article_successfully() {
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Title")
            .description("Test Desc")
            .body("Test Body")
            .tagList(Arrays.asList("java", "spring"))
            .build();
    Article article = new Article("Test Title", "Test Desc", "Test Body", Arrays.asList("java", "spring"), user.getId());
    when(articleCommandService.createArticle(any(), eq(user))).thenReturn(article);

    var result = articleMutation.createArticle(input);
    assertNotNull(result);
    assertNotNull(result.getData());
    assertTrue(result.getData() instanceof ArticlePayload);
  }

  @Test
  void should_create_article_with_null_tags() {
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Title")
            .description("Test Desc")
            .body("Test Body")
            .build();
    Article article = new Article("Test Title", "Test Desc", "Test Body", Arrays.asList(), user.getId());
    when(articleCommandService.createArticle(any(), eq(user))).thenReturn(article);

    var result = articleMutation.createArticle(input);
    assertNotNull(result);
  }

  @Test
  void should_throw_when_not_authenticated_on_create() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Title")
            .description("Test Desc")
            .body("Test Body")
            .build();
    assertThrows(AuthenticationException.class, () -> articleMutation.createArticle(input));
  }

  @Test
  void should_update_article_successfully() {
    Article article = new Article("Old Title", "Old Desc", "Old Body", Arrays.asList(), user.getId());
    when(articleRepository.findBySlug(eq("old-title"))).thenReturn(Optional.of(article));
    when(articleCommandService.updateArticle(eq(article), any())).thenReturn(article);

    UpdateArticleInput params =
        UpdateArticleInput.newBuilder()
            .title("New Title")
            .description("New Desc")
            .body("New Body")
            .build();

    var result = articleMutation.updateArticle("old-title", params);
    assertNotNull(result);
  }

  @Test
  void should_throw_when_not_author_on_update() {
    User anotherUser = new User("other@test.com", "other", "password", "", "");
    Article article = new Article("Title", "Desc", "Body", Arrays.asList(), anotherUser.getId());
    when(articleRepository.findBySlug(eq("title"))).thenReturn(Optional.of(article));

    UpdateArticleInput params =
        UpdateArticleInput.newBuilder().title("New").build();

    assertThrows(NoAuthorizationException.class, () -> articleMutation.updateArticle("title", params));
  }

  @Test
  void should_throw_when_article_not_found_on_update() {
    when(articleRepository.findBySlug(eq("not-found"))).thenReturn(Optional.empty());
    UpdateArticleInput params = UpdateArticleInput.newBuilder().title("New").build();
    assertThrows(ResourceNotFoundException.class, () -> articleMutation.updateArticle("not-found", params));
  }

  @Test
  void should_favorite_article() {
    Article article = new Article("Title", "Desc", "Body", Arrays.asList(), user.getId());
    when(articleRepository.findBySlug(eq("title"))).thenReturn(Optional.of(article));

    var result = articleMutation.favoriteArticle("title");
    assertNotNull(result);
    verify(articleFavoriteRepository).save(any(ArticleFavorite.class));
  }

  @Test
  void should_throw_when_not_authenticated_on_favorite() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
    assertThrows(AuthenticationException.class, () -> articleMutation.favoriteArticle("slug"));
  }

  @Test
  void should_unfavorite_article() {
    Article article = new Article("Title", "Desc", "Body", Arrays.asList(), user.getId());
    when(articleRepository.findBySlug(eq("title"))).thenReturn(Optional.of(article));
    ArticleFavorite fav = new ArticleFavorite(article.getId(), user.getId());
    when(articleFavoriteRepository.find(article.getId(), user.getId())).thenReturn(Optional.of(fav));

    var result = articleMutation.unfavoriteArticle("title");
    assertNotNull(result);
    verify(articleFavoriteRepository).remove(eq(fav));
  }

  @Test
  void should_unfavorite_when_no_existing_favorite() {
    Article article = new Article("Title", "Desc", "Body", Arrays.asList(), user.getId());
    when(articleRepository.findBySlug(eq("title"))).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(article.getId(), user.getId())).thenReturn(Optional.empty());

    var result = articleMutation.unfavoriteArticle("title");
    assertNotNull(result);
    verify(articleFavoriteRepository, never()).remove(any());
  }

  @Test
  void should_delete_article() {
    Article article = new Article("Title", "Desc", "Body", Arrays.asList(), user.getId());
    when(articleRepository.findBySlug(eq("title"))).thenReturn(Optional.of(article));

    DeletionStatus result = articleMutation.deleteArticle("title");
    assertTrue(result.getSuccess());
    verify(articleRepository).remove(eq(article));
  }

  @Test
  void should_throw_when_not_author_on_delete() {
    User anotherUser = new User("other@test.com", "other", "password", "", "");
    Article article = new Article("Title", "Desc", "Body", Arrays.asList(), anotherUser.getId());
    when(articleRepository.findBySlug(eq("title"))).thenReturn(Optional.of(article));

    assertThrows(NoAuthorizationException.class, () -> articleMutation.deleteArticle("title"));
  }

  @Test
  void should_throw_when_article_not_found_on_delete() {
    when(articleRepository.findBySlug(eq("not-found"))).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> articleMutation.deleteArticle("not-found"));
  }
}
