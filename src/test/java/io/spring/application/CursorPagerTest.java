package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.CursorPager.Direction;
import java.util.ArrayList;
import java.util.Arrays;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class CursorPagerTest {

  private io.spring.application.data.CommentData createComment(String id) {
    return new io.spring.application.data.CommentData(
        id,
        "body",
        "article-id",
        new DateTime(),
        new DateTime(),
        new io.spring.application.data.ProfileData("uid", "user", "bio", "img", false));
  }

  @Test
  void should_set_next_true_when_direction_next_and_has_extra() {
    var data = Arrays.asList(createComment("1"));
    CursorPager<io.spring.application.data.CommentData> pager =
        new CursorPager<>(data, Direction.NEXT, true);
    assertTrue(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  void should_set_previous_true_when_direction_prev_and_has_extra() {
    var data = Arrays.asList(createComment("1"));
    CursorPager<io.spring.application.data.CommentData> pager =
        new CursorPager<>(data, Direction.PREV, true);
    assertFalse(pager.hasNext());
    assertTrue(pager.hasPrevious());
  }

  @Test
  void should_have_no_next_or_previous_when_no_extra() {
    var data = Arrays.asList(createComment("1"));
    CursorPager<io.spring.application.data.CommentData> pager =
        new CursorPager<>(data, Direction.NEXT, false);
    assertFalse(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  void should_return_null_cursors_when_empty() {
    CursorPager<io.spring.application.data.CommentData> pager =
        new CursorPager<>(new ArrayList<>(), Direction.NEXT, false);
    assertNull(pager.getStartCursor());
    assertNull(pager.getEndCursor());
  }

  @Test
  void should_return_start_and_end_cursors() {
    var data = Arrays.asList(createComment("1"), createComment("2"));
    CursorPager<io.spring.application.data.CommentData> pager =
        new CursorPager<>(data, Direction.NEXT, false);
    assertNotNull(pager.getStartCursor());
    assertNotNull(pager.getEndCursor());
  }

  @Test
  void should_get_data() {
    var data = Arrays.asList(createComment("1"), createComment("2"));
    CursorPager<io.spring.application.data.CommentData> pager =
        new CursorPager<>(data, Direction.NEXT, false);
    assertEquals(2, pager.getData().size());
  }
}
