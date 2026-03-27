package io.spring.graphql.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.graphql.types.Error;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ExecutionStepInfo;
import graphql.execution.ResultPath;
import graphql.schema.DataFetchingEnvironment;
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
  public void setUp() {
    handler = new GraphQLCustomizeExceptionHandler();
  }

  private DataFetchingEnvironment createMockEnv() {
    DataFetchingEnvironment mockEnv = mock(DataFetchingEnvironment.class);
    ExecutionStepInfo stepInfo = mock(ExecutionStepInfo.class);
    when(stepInfo.getPath()).thenReturn(ResultPath.rootPath());
    when(mockEnv.getExecutionStepInfo()).thenReturn(stepInfo);
    return mockEnv;
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
  public void should_handle_invalid_authentication_exception() {
    DataFetchingEnvironment mockEnv = createMockEnv();
    InvalidAuthenticationException exception = new InvalidAuthenticationException();
    DataFetcherExceptionHandlerParameters params =
        DataFetcherExceptionHandlerParameters.newExceptionParameters()
            .dataFetchingEnvironment(mockEnv)
            .exception(exception)
            .build();

    DataFetcherExceptionHandlerResult result = handler.onException(params);
    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  public void should_handle_constraint_violation_exception() {
    DataFetchingEnvironment mockEnv = createMockEnv();
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createViolation("param.field.name", "can't be empty"));

    ConstraintViolationException cve =
        new ConstraintViolationException("Validation failed", violations);
    DataFetcherExceptionHandlerParameters params =
        DataFetcherExceptionHandlerParameters.newExceptionParameters()
            .dataFetchingEnvironment(mockEnv)
            .exception(cve)
            .build();

    DataFetcherExceptionHandlerResult result = handler.onException(params);
    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  public void should_handle_generic_exception() {
    DataFetchingEnvironment mockEnv = createMockEnv();
    RuntimeException exception = new RuntimeException("some error");
    DataFetcherExceptionHandlerParameters params =
        DataFetcherExceptionHandlerParameters.newExceptionParameters()
            .dataFetchingEnvironment(mockEnv)
            .exception(exception)
            .build();

    DataFetcherExceptionHandlerResult result = handler.onException(params);
    assertNotNull(result);
  }

  @Test
  public void should_get_errors_as_data() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createViolation("param.field.email", "should be an email"));

    ConstraintViolationException cve =
        new ConstraintViolationException("Validation failed", violations);
    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);
    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertFalse(error.getErrors().isEmpty());
  }

  @Test
  public void should_handle_single_path_segment() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createViolation("fieldName", "invalid"));

    ConstraintViolationException cve =
        new ConstraintViolationException("Validation failed", violations);
    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);
    assertNotNull(error);
  }
}
