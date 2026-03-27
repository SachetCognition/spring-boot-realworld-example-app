package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.CursorPager.Direction;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Test;

public class CursorPageParameterTest {

  @Test
  public void should_create_with_next_direction() {
    DateTime cursor = new DateTime(2023, 6, 15, 12, 0, 0, DateTimeZone.UTC);
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(cursor, 10, Direction.NEXT);
    assertTrue(param.isNext());
    assertEquals(11, param.getQueryLimit());
  }

  @Test
  public void should_create_with_prev_direction() {
    DateTime cursor = new DateTime(2023, 6, 15, 12, 0, 0, DateTimeZone.UTC);
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(cursor, 10, Direction.PREV);
    assertFalse(param.isNext());
    assertEquals(11, param.getQueryLimit());
  }

  @Test
  public void should_create_with_null_cursor() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 20, Direction.NEXT);
    assertTrue(param.isNext());
    assertEquals(21, param.getQueryLimit());
  }
}
