package io.spring.api.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.context.request.WebRequest;

public class CustomizeExceptionHandlerTest {

  private CustomizeExceptionHandler handler;
  private WebRequest request;

  @BeforeEach
  public void setUp() {
    handler = new CustomizeExceptionHandler();
    request = mock(WebRequest.class);
  }

  private ConstraintViolation<?> createViolation(String pathStr, String message) {
    ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn(pathStr);
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getMessage()).thenReturn(message);
    when(violation.getRootBeanClass()).thenReturn((Class) String.class);

    Annotation annotation = mock(Annotation.class);
    when(annotation.annotationType()).thenReturn((Class) Override.class);
    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    when(descriptor.getAnnotation()).thenReturn(annotation);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);
    return violation;
  }

  @Test
  public void should_handle_invalid_request_exception() {
    BeanPropertyBindingResult errors =
        new BeanPropertyBindingResult(new Object(), "testObject");
    errors.addError(new FieldError("testObject", "email", "can't be empty"));

    InvalidRequestException exception = new InvalidRequestException(errors);
    ResponseEntity<Object> response = handler.handleInvalidRequest(exception, request);

    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
  }

  @Test
  public void should_handle_invalid_authentication_exception() {
    InvalidAuthenticationException exception = new InvalidAuthenticationException();
    ResponseEntity<Object> response =
        handler.handleInvalidAuthentication(exception, request);

    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
  }

  @Test
  public void should_handle_constraint_violation() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createViolation("param.field.name", "can't be empty"));

    ConstraintViolationException cve = new ConstraintViolationException(violations);
    ErrorResource result = handler.handleConstraintViolation(cve, request);
    assertNotNull(result);
    assertFalse(result.getFieldErrors().isEmpty());
  }

  @Test
  public void should_handle_constraint_violation_with_single_path() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createViolation("fieldName", "invalid"));

    ConstraintViolationException cve = new ConstraintViolationException(violations);
    ErrorResource result = handler.handleConstraintViolation(cve, request);
    assertNotNull(result);
    assertEquals("fieldName", result.getFieldErrors().get(0).getField());
  }
}
