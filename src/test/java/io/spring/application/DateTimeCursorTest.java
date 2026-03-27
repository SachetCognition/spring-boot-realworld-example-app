package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Test;

public class DateTimeCursorTest {

  @Test
  public void should_create_cursor_from_datetime() {
    DateTime dateTime = new DateTime(2023, 6, 15, 12, 0, 0, DateTimeZone.UTC);
    DateTimeCursor cursor = new DateTimeCursor(dateTime);
    assertNotNull(cursor);
    assertEquals(dateTime, cursor.getData());
  }

  @Test
  public void should_convert_to_string() {
    DateTime dateTime = new DateTime(2023, 6, 15, 12, 0, 0, DateTimeZone.UTC);
    DateTimeCursor cursor = new DateTimeCursor(dateTime);
    String str = cursor.toString();
    assertEquals(String.valueOf(dateTime.getMillis()), str);
  }

  @Test
  public void should_parse_from_string() {
    DateTime original = new DateTime(2023, 6, 15, 12, 0, 0, DateTimeZone.UTC);
    String millis = String.valueOf(original.getMillis());
    DateTime parsed = DateTimeCursor.parse(millis);
    assertEquals(original.getMillis(), parsed.getMillis());
  }
}
