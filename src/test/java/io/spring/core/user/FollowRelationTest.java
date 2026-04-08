package io.spring.core.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class FollowRelationTest {

  @Test
  void should_create_with_constructor() {
    FollowRelation rel = new FollowRelation("user-id", "target-id");
    assertEquals("user-id", rel.getUserId());
    assertEquals("target-id", rel.getTargetId());
  }

  @Test
  void should_create_with_no_arg_constructor() {
    FollowRelation rel = new FollowRelation();
    assertNull(rel.getUserId());
    assertNull(rel.getTargetId());
  }

  @Test
  void should_set_fields() {
    FollowRelation rel = new FollowRelation();
    rel.setUserId("u1");
    rel.setTargetId("t1");
    assertEquals("u1", rel.getUserId());
    assertEquals("t1", rel.getTargetId());
  }

  @Test
  void should_support_equals_and_hashcode() {
    FollowRelation r1 = new FollowRelation("u1", "t1");
    FollowRelation r2 = new FollowRelation("u1", "t1");
    assertEquals(r1, r2);
    assertEquals(r1.hashCode(), r2.hashCode());
  }

  @Test
  void should_not_equal_different() {
    FollowRelation r1 = new FollowRelation("u1", "t1");
    FollowRelation r2 = new FollowRelation("u2", "t2");
    assertNotEquals(r1, r2);
  }

  @Test
  void should_have_toString() {
    FollowRelation rel = new FollowRelation("u1", "t1");
    assertNotNull(rel.toString());
    assertTrue(rel.toString().contains("u1"));
  }
}
