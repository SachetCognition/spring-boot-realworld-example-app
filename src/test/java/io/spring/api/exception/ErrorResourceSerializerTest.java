package io.spring.api.exception;

import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class ErrorResourceSerializerTest {

  @Test
  public void should_serialize_error_resource() throws IOException {
    ErrorResourceSerializer serializer = new ErrorResourceSerializer();

    FieldErrorResource field1 = new FieldErrorResource("User", "email", "NotBlank", "can't be empty");
    FieldErrorResource field2 = new FieldErrorResource("User", "email", "Email", "should be an email");
    FieldErrorResource field3 = new FieldErrorResource("User", "username", "NotBlank", "can't be empty");

    ErrorResource errorResource = new ErrorResource(Arrays.asList(field1, field2, field3));

    JsonGenerator gen = mock(JsonGenerator.class);
    SerializerProvider provider = mock(SerializerProvider.class);

    serializer.serialize(errorResource, gen, provider);

    verify(gen).writeStartObject();
    verify(gen).writeObjectFieldStart("errors");
    verify(gen, atLeastOnce()).writeArrayFieldStart(anyString());
    verify(gen, atLeastOnce()).writeString(anyString());
    verify(gen, atLeastOnce()).writeEndArray();
    verify(gen, times(2)).writeEndObject();
  }

  @Test
  public void should_serialize_single_field_error() throws IOException {
    ErrorResourceSerializer serializer = new ErrorResourceSerializer();

    FieldErrorResource field = new FieldErrorResource("Article", "title", "NotBlank", "can't be empty");
    ErrorResource errorResource = new ErrorResource(Arrays.asList(field));

    JsonGenerator gen = mock(JsonGenerator.class);
    SerializerProvider provider = mock(SerializerProvider.class);

    serializer.serialize(errorResource, gen, provider);

    verify(gen).writeStartObject();
    verify(gen).writeObjectFieldStart("errors");
    verify(gen).writeArrayFieldStart("title");
    verify(gen).writeString("can't be empty");
  }
}
