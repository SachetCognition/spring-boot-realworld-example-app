package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Test;

public class DateTimeCursorTest {

  @Test
  void should_create_cursor_with_datetime() {
    DateTime dt = new DateTime(2023, 1, 1, 0, 0, DateTimeZone.UTC);
    DateTimeCursor cursor = new DateTimeCursor(dt);
    assertEquals(dt, cursor.getData());
  }

  @Test
  void should_convert_to_string_as_millis() {
    DateTime dt = new DateTime(2023, 1, 1, 0, 0, DateTimeZone.UTC);
    DateTimeCursor cursor = new DateTimeCursor(dt);
    assertEquals(String.valueOf(dt.getMillis()), cursor.toString());
  }

  @Test
  void should_parse_null_to_null() {
    assertNull(DateTimeCursor.parse(null));
  }

  @Test
  void should_parse_millis_string_to_datetime() {
    DateTime dt = new DateTime(2023, 1, 1, 0, 0, DateTimeZone.UTC);
    DateTime parsed = DateTimeCursor.parse(String.valueOf(dt.getMillis()));
    assertNotNull(parsed);
    assertEquals(dt.getMillis(), parsed.getMillis());
  }
}
