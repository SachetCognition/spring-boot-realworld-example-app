package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.CursorPager.Direction;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ProfileData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class CursorPagerTest {

  @Test
  public void should_have_next_when_direction_next_and_has_extra() {
    DateTime now = new DateTime();
    ArticleData article =
        new ArticleData("id1", "slug", "title", "desc", "body", false, 0, now, now,
            Arrays.asList("java"), new ProfileData("uid", "user", "bio", "img", false));
    List<ArticleData> data = Arrays.asList(article);
    CursorPager<ArticleData> pager = new CursorPager<>(data, Direction.NEXT, true);

    assertTrue(pager.hasNext());
    assertFalse(pager.hasPrevious());
    assertNotNull(pager.getStartCursor());
    assertNotNull(pager.getEndCursor());
  }

  @Test
  public void should_have_previous_when_direction_prev_and_has_extra() {
    DateTime now = new DateTime();
    ArticleData article =
        new ArticleData("id1", "slug", "title", "desc", "body", false, 0, now, now,
            Arrays.asList("java"), new ProfileData("uid", "user", "bio", "img", false));
    List<ArticleData> data = Arrays.asList(article);
    CursorPager<ArticleData> pager = new CursorPager<>(data, Direction.PREV, true);

    assertFalse(pager.hasNext());
    assertTrue(pager.hasPrevious());
  }

  @Test
  public void should_return_null_cursors_when_empty() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(new ArrayList<>(), Direction.NEXT, false);

    assertNull(pager.getStartCursor());
    assertNull(pager.getEndCursor());
    assertFalse(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  public void should_not_have_next_when_no_extra() {
    DateTime now = new DateTime();
    ArticleData article =
        new ArticleData("id1", "slug", "title", "desc", "body", false, 0, now, now,
            Arrays.asList("java"), new ProfileData("uid", "user", "bio", "img", false));
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(article), Direction.NEXT, false);

    assertFalse(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }
}
