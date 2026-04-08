package io.spring.core.article;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class TagTest {

  @Test
  void should_create_tag_with_name() {
    Tag tag = new Tag("java");
    assertNotNull(tag.getId());
    assertEquals("java", tag.getName());
  }

  @Test
  void should_create_tag_with_no_arg_constructor() {
    Tag tag = new Tag();
    assertNull(tag.getId());
    assertNull(tag.getName());
  }

  @Test
  void should_set_fields() {
    Tag tag = new Tag();
    tag.setId("id");
    tag.setName("spring");
    assertEquals("id", tag.getId());
    assertEquals("spring", tag.getName());
  }

  @Test
  void should_have_equals_based_on_name() {
    Tag t1 = new Tag("java");
    Tag t2 = new Tag("java");
    assertEquals(t1, t2);
    assertEquals(t1.hashCode(), t2.hashCode());
  }

  @Test
  void should_not_equal_different_name() {
    Tag t1 = new Tag("java");
    Tag t2 = new Tag("spring");
    assertNotEquals(t1, t2);
  }

  @Test
  void should_have_toString() {
    Tag tag = new Tag("java");
    assertNotNull(tag.toString());
    assertTrue(tag.toString().contains("java"));
  }
}
