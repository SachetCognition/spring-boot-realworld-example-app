package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.spring.application.CursorPager.Direction;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.CommentReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CommentQueryServiceTest {

  private CommentReadService commentReadService;
  private UserRelationshipQueryService userRelationshipQueryService;
  private CommentQueryService commentQueryService;
  private User user;
  private ProfileData profileData;

  @BeforeEach
  void setUp() {
    commentReadService = mock(CommentReadService.class);
    userRelationshipQueryService = mock(UserRelationshipQueryService.class);
    commentQueryService = new CommentQueryService(commentReadService, userRelationshipQueryService);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    profileData = new ProfileData("author-id", "author", "bio", "image", false);
  }

  @Test
  void should_find_by_id() {
    CommentData commentData =
        new CommentData("cid", "body", "article-id", new DateTime(), new DateTime(), profileData);
    when(commentReadService.findById("cid")).thenReturn(commentData);
    when(userRelationshipQueryService.isUserFollowing(any(), eq("author-id"))).thenReturn(true);

    Optional<CommentData> result = commentQueryService.findById("cid", user);
    assertTrue(result.isPresent());
    assertTrue(result.get().getProfileData().isFollowing());
  }

  @Test
  void should_return_empty_when_comment_not_found() {
    when(commentReadService.findById("none")).thenReturn(null);
    Optional<CommentData> result = commentQueryService.findById("none", user);
    assertTrue(result.isEmpty());
  }

  @Test
  void should_find_by_article_id_with_user() {
    CommentData commentData =
        new CommentData("cid", "body", "article-id", new DateTime(), new DateTime(), profileData);
    when(commentReadService.findByArticleId("article-id")).thenReturn(Arrays.asList(commentData));
    when(userRelationshipQueryService.followingAuthors(any(), anyList()))
        .thenReturn(new HashSet<>(Arrays.asList("author-id")));

    List<CommentData> result = commentQueryService.findByArticleId("article-id", user);
    assertEquals(1, result.size());
    assertTrue(result.get(0).getProfileData().isFollowing());
  }

  @Test
  void should_find_by_article_id_without_user() {
    CommentData commentData =
        new CommentData("cid", "body", "article-id", new DateTime(), new DateTime(), profileData);
    when(commentReadService.findByArticleId("article-id")).thenReturn(Arrays.asList(commentData));

    List<CommentData> result = commentQueryService.findByArticleId("article-id", null);
    assertEquals(1, result.size());
  }

  @Test
  void should_find_by_article_id_empty() {
    when(commentReadService.findByArticleId("article-id")).thenReturn(new ArrayList<>());

    List<CommentData> result = commentQueryService.findByArticleId("article-id", user);
    assertTrue(result.isEmpty());
  }

  @Test
  void should_find_by_article_id_with_cursor_empty() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(new ArrayList<>());

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-id", user, page);
    assertTrue(result.getData().isEmpty());
  }

  @Test
  void should_find_by_article_id_with_cursor() {
    CommentData commentData =
        new CommentData("cid", "body", "article-id", new DateTime(), new DateTime(), profileData);
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(new ArrayList<>(Arrays.asList(commentData)));
    when(userRelationshipQueryService.followingAuthors(any(), anyList()))
        .thenReturn(new HashSet<>());

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-id", user, page);
    assertEquals(1, result.getData().size());
  }

  @Test
  void should_find_by_article_id_with_cursor_has_extra() {
    CommentData c1 =
        new CommentData("c1", "body1", "article-id", new DateTime(), new DateTime(), profileData);
    CommentData c2 =
        new CommentData("c2", "body2", "article-id", new DateTime(), new DateTime(), profileData);
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 1, Direction.NEXT);
    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(new ArrayList<>(Arrays.asList(c1, c2)));
    when(userRelationshipQueryService.followingAuthors(any(), anyList()))
        .thenReturn(new HashSet<>());

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-id", user, page);
    assertEquals(1, result.getData().size());
    assertTrue(result.hasNext());
  }

  @Test
  void should_reverse_comments_when_direction_prev() {
    CommentData c1 =
        new CommentData("c1", "body1", "article-id", new DateTime(), new DateTime(), profileData);
    CommentData c2 =
        new CommentData("c2", "body2", "article-id", new DateTime(), new DateTime(), profileData);
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.PREV);
    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(new ArrayList<>(Arrays.asList(c1, c2)));

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-id", null, page);
    assertEquals(2, result.getData().size());
  }
}
