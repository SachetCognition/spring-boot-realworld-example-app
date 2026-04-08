package io.spring.graphql.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ResultPath;
import graphql.language.SourceLocation;
import io.spring.api.exception.InvalidAuthenticationException;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GraphQLCustomizeExceptionHandlerTest {

  private GraphQLCustomizeExceptionHandler handler;

  @BeforeEach
  void setUp() {
    handler = new GraphQLCustomizeExceptionHandler();
  }

  @Test
  void should_handle_invalid_authentication_exception() {
    InvalidAuthenticationException ex = new InvalidAuthenticationException();
    DataFetcherExceptionHandlerParameters params = mock(DataFetcherExceptionHandlerParameters.class);
    when(params.getException()).thenReturn(ex);
    when(params.getPath()).thenReturn(ResultPath.rootPath());

    DataFetcherExceptionHandlerResult result = handler.onException(params);
    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  void should_handle_constraint_violation_exception() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn(Object.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn("field");
    when(violation.getPropertyPath()).thenReturn(path);
    Annotation annotation1 = mock(Annotation.class);
    when(annotation1.annotationType()).thenReturn((Class) Override.class);
    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    when(descriptor.getAnnotation()).thenReturn(annotation1);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);
    when(violation.getMessage()).thenReturn("error message");
    violations.add(violation);

    ConstraintViolationException cve = new ConstraintViolationException(violations);
    DataFetcherExceptionHandlerParameters params = mock(DataFetcherExceptionHandlerParameters.class);
    when(params.getException()).thenReturn(cve);
    when(params.getPath()).thenReturn(ResultPath.rootPath());

    DataFetcherExceptionHandlerResult result = handler.onException(params);
    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  void should_handle_other_exceptions_with_default_handler() {
    RuntimeException ex = new RuntimeException("test error");
    DataFetcherExceptionHandlerParameters params = mock(DataFetcherExceptionHandlerParameters.class);
    when(params.getException()).thenReturn(ex);
    when(params.getPath()).thenReturn(ResultPath.rootPath());
    when(params.getSourceLocation()).thenReturn(new SourceLocation(0, 0));

    DataFetcherExceptionHandlerResult result = handler.onException(params);
    assertNotNull(result);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  void should_get_errors_as_data() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn(Object.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn("param.field.name");
    when(violation.getPropertyPath()).thenReturn(path);
    Annotation annotation2 = mock(Annotation.class);
    when(annotation2.annotationType()).thenReturn((Class) Override.class);
    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    when(descriptor.getAnnotation()).thenReturn(annotation2);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);
    when(violation.getMessage()).thenReturn("error");
    violations.add(violation);

    ConstraintViolationException cve = new ConstraintViolationException(violations);
    var result = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);
    assertNotNull(result);
    assertEquals("BAD_REQUEST", result.getMessage());
    assertFalse(result.getErrors().isEmpty());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  void should_handle_single_segment_path() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn(Object.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn("singleField");
    when(violation.getPropertyPath()).thenReturn(path);
    Annotation annotation3 = mock(Annotation.class);
    when(annotation3.annotationType()).thenReturn((Class) Override.class);
    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    when(descriptor.getAnnotation()).thenReturn(annotation3);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);
    when(violation.getMessage()).thenReturn("error");
    violations.add(violation);

    ConstraintViolationException cve = new ConstraintViolationException(violations);
    var result = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);
    assertNotNull(result);
  }
}
