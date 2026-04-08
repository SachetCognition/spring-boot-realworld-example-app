package io.spring.api.exception;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ErrorResourceTest {

  @Test
  void should_create_error_resource() {
    List<FieldErrorResource> fieldErrors =
        Arrays.asList(new FieldErrorResource("resource", "field", "code", "message"));
    ErrorResource errorResource = new ErrorResource(fieldErrors);
    assertNotNull(errorResource);
    assertEquals(1, errorResource.getFieldErrors().size());
  }

  @Test
  void should_get_field_errors() {
    FieldErrorResource fer = new FieldErrorResource("res", "fld", "cd", "msg");
    ErrorResource errorResource = new ErrorResource(Arrays.asList(fer));
    assertEquals("res", errorResource.getFieldErrors().get(0).getResource());
    assertEquals("fld", errorResource.getFieldErrors().get(0).getField());
    assertEquals("cd", errorResource.getFieldErrors().get(0).getCode());
    assertEquals("msg", errorResource.getFieldErrors().get(0).getMessage());
  }
}
