package io.spring.api.exception;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.StringWriter;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class ErrorResourceSerializerTest {

  @Test
  void should_serialize_error_resource() throws Exception {
    FieldErrorResource fer = new FieldErrorResource("resource", "field", "code", "error message");
    ErrorResource errorResource = new ErrorResource(Arrays.asList(fer));

    ErrorResourceSerializer serializer = new ErrorResourceSerializer();
    StringWriter writer = new StringWriter();
    ObjectMapper mapper = new ObjectMapper();
    JsonGenerator gen = new JsonFactory().createGenerator(writer);
    SerializerProvider provider = mapper.getSerializerProvider();

    serializer.serialize(errorResource, gen, provider);
    gen.flush();

    String json = writer.toString();
    assertTrue(json.contains("errors"));
    assertTrue(json.contains("field"));
    assertTrue(json.contains("error message"));
  }

  @Test
  void should_group_errors_by_field() throws Exception {
    FieldErrorResource fer1 = new FieldErrorResource("res", "email", "code", "invalid");
    FieldErrorResource fer2 = new FieldErrorResource("res", "email", "code", "already exists");
    ErrorResource errorResource = new ErrorResource(Arrays.asList(fer1, fer2));

    ErrorResourceSerializer serializer = new ErrorResourceSerializer();
    StringWriter writer = new StringWriter();
    ObjectMapper mapper = new ObjectMapper();
    JsonGenerator gen = new JsonFactory().createGenerator(writer);
    SerializerProvider provider = mapper.getSerializerProvider();

    serializer.serialize(errorResource, gen, provider);
    gen.flush();

    String json = writer.toString();
    assertTrue(json.contains("email"));
    assertTrue(json.contains("invalid"));
    assertTrue(json.contains("already exists"));
  }
}
