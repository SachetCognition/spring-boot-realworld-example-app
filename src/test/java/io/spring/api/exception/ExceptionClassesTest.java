package io.spring.api.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ExceptionClassesTest {

  @Test
  void should_create_invalid_authentication_exception() {
    InvalidAuthenticationException ex = new InvalidAuthenticationException();
    assertNotNull(ex);
    assertEquals("invalid email or password", ex.getMessage());
    assertTrue(ex instanceof RuntimeException);
  }

  @Test
  void should_create_no_authorization_exception() {
    NoAuthorizationException ex = new NoAuthorizationException();
    assertNotNull(ex);
    assertTrue(ex instanceof RuntimeException);
  }

  @Test
  void should_create_resource_not_found_exception() {
    ResourceNotFoundException ex = new ResourceNotFoundException();
    assertNotNull(ex);
    assertTrue(ex instanceof RuntimeException);
  }

  @Test
  void should_create_field_error_resource() {
    FieldErrorResource fer = new FieldErrorResource("resource", "field", "code", "message");
    assertEquals("resource", fer.getResource());
    assertEquals("field", fer.getField());
    assertEquals("code", fer.getCode());
    assertEquals("message", fer.getMessage());
  }
}
