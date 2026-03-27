package io.spring.api;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class GraphQLRemovedTest {

  @Autowired private MockMvc mvc;

  @Test
  public void graphql_endpoint_should_not_exist() throws Exception {
    mvc.perform(
            post("/graphql").contentType("application/json").content("{\"query\": \"{ tags }\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  public void graphiql_endpoint_should_not_exist() throws Exception {
    mvc.perform(get("/graphiql")).andExpect(status().isUnauthorized());
  }

  @Test
  public void no_graphql_classes_should_exist() {
    assertThrows(
        ClassNotFoundException.class,
        () -> {
          Class.forName("io.spring.graphql.ArticleDatafetcher");
        });
    assertThrows(
        ClassNotFoundException.class,
        () -> {
          Class.forName("io.spring.graphql.SecurityUtil");
        });
  }
}
