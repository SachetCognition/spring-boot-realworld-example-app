package io.spring.api.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;

public class InvalidRequestExceptionTest {

  @Test
  void should_create_with_errors() {
    BeanPropertyBindingResult errors = new BeanPropertyBindingResult(new Object(), "object");
    InvalidRequestException ex = new InvalidRequestException(errors);
    assertNotNull(ex);
    assertNotNull(ex.getErrors());
    assertEquals(errors, ex.getErrors());
  }

  @Test
  void should_extend_runtime_exception() {
    BeanPropertyBindingResult errors = new BeanPropertyBindingResult(new Object(), "object");
    InvalidRequestException ex = new InvalidRequestException(errors);
    assertTrue(ex instanceof RuntimeException);
  }
}
