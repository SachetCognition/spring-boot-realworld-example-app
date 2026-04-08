package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class PageTest {

  @Test
  void should_create_with_defaults() {
    Page page = new Page();
    assertEquals(0, page.getOffset());
    assertEquals(20, page.getLimit());
  }

  @Test
  void should_create_with_values() {
    Page page = new Page(5, 10);
    assertEquals(5, page.getOffset());
    assertEquals(10, page.getLimit());
  }

  @Test
  void should_cap_limit_at_100() {
    Page page = new Page(0, 200);
    assertEquals(100, page.getLimit());
  }

  @Test
  void should_keep_default_limit_when_negative() {
    Page page = new Page(0, -1);
    assertEquals(20, page.getLimit());
  }

  @Test
  void should_keep_default_limit_when_zero() {
    Page page = new Page(0, 0);
    assertEquals(20, page.getLimit());
  }

  @Test
  void should_keep_default_offset_when_negative() {
    Page page = new Page(-1, 10);
    assertEquals(0, page.getOffset());
  }

  @Test
  void should_set_positive_offset() {
    Page page = new Page(10, 10);
    assertEquals(10, page.getOffset());
  }
}
