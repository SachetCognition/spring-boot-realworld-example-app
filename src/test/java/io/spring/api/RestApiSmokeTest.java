package io.spring.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
public class RestApiSmokeTest {

  @Autowired private MockMvc mvc;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void full_rest_api_smoke_test() throws Exception {
    String uid = String.valueOf(System.nanoTime());
    String username = "smoke" + uid;
    String email = "smoke" + uid + "@test.com";
    String username2 = "smoke2" + uid;
    String email2 = "smoke2" + uid + "@test.com";

    // 1. Register a user
    String registerBody =
        "{\"user\":{\"email\":\"" + email + "\",\"password\":\"password123\",\"username\":\"" + username + "\"}}";
    MvcResult registerResult =
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(registerBody))
            .andExpect(status().isCreated())
            .andReturn();

    String token = extractToken(registerResult);

    // 2. Login
    String loginBody =
        "{\"user\":{\"email\":\"" + email + "\",\"password\":\"password123\"}}";
    mvc.perform(post("/users/login").contentType(MediaType.APPLICATION_JSON).content(loginBody))
        .andExpect(status().isOk());

    // 3. Get current user
    mvc.perform(get("/user").header("Authorization", "Token " + token))
        .andExpect(status().isOk());

    // 4. Update user
    String updateBody = "{\"user\":{\"bio\":\"Updated bio\"}}";
    mvc.perform(
            put("/user")
                .header("Authorization", "Token " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateBody))
        .andExpect(status().isOk());

    // 5. Create an article
    String articleTitle = "Smoke Test Article " + uid;
    String articleBody =
        "{\"article\":{\"title\":\"" + articleTitle + "\",\"description\":\"desc\",\"body\":\"body content\",\"tagList\":[\"smoke\",\"test\"]}}";
    MvcResult articleResult =
        mvc.perform(
                post("/articles")
                    .header("Authorization", "Token " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(articleBody))
            .andExpect(status().isOk())
            .andReturn();

    String slug = extractSlug(articleResult);

    // 6. Get article by slug
    mvc.perform(get("/articles/" + slug))
        .andExpect(status().isOk());

    // 7. List articles
    mvc.perform(get("/articles")).andExpect(status().isOk());

    // 8. Update article
    String updateArticle = "{\"article\":{\"body\":\"updated body\"}}";
    mvc.perform(
            put("/articles/" + slug)
                .header("Authorization", "Token " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateArticle))
        .andExpect(status().isOk());

    // 9. Favorite article
    mvc.perform(post("/articles/" + slug + "/favorite").header("Authorization", "Token " + token))
        .andExpect(status().isOk());

    // 10. Unfavorite article
    mvc.perform(
            delete("/articles/" + slug + "/favorite").header("Authorization", "Token " + token))
        .andExpect(status().isOk());

    // 11. Add comment
    String commentBody = "{\"comment\":{\"body\":\"A smoke test comment\"}}";
    MvcResult commentResult =
        mvc.perform(
                post("/articles/" + slug + "/comments")
                    .header("Authorization", "Token " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(commentBody))
            .andExpect(status().isCreated())
            .andReturn();

    String commentId = extractCommentId(commentResult);

    // 12. List comments
    mvc.perform(get("/articles/" + slug + "/comments")).andExpect(status().isOk());

    // 13. Delete comment
    mvc.perform(
            delete("/articles/" + slug + "/comments/" + commentId)
                .header("Authorization", "Token " + token))
        .andExpect(status().isNoContent());

    // 14. Get tags
    mvc.perform(get("/tags")).andExpect(status().isOk());

    // 15. Get profile
    mvc.perform(get("/profiles/" + username)).andExpect(status().isOk());

    // 16. Create another user to follow
    String register2Body =
        "{\"user\":{\"email\":\"" + email2 + "\",\"password\":\"password123\",\"username\":\"" + username2 + "\"}}";
    mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(register2Body))
        .andExpect(status().isCreated());

    // 17. Follow user
    mvc.perform(
            post("/profiles/" + username2 + "/follow").header("Authorization", "Token " + token))
        .andExpect(status().isOk());

    // 18. Unfollow user
    mvc.perform(
            delete("/profiles/" + username2 + "/follow").header("Authorization", "Token " + token))
        .andExpect(status().isOk());

    // 19. Get feed
    mvc.perform(get("/articles/feed").header("Authorization", "Token " + token))
        .andExpect(status().isOk());

    // 20. Delete article
    mvc.perform(delete("/articles/" + slug).header("Authorization", "Token " + token))
        .andExpect(status().isNoContent());
  }

  private String extractToken(MvcResult result) throws Exception {
    String body = result.getResponse().getContentAsString();
    JsonNode node = objectMapper.readTree(body);
    return node.get("user").get("token").asText();
  }

  private String extractSlug(MvcResult result) throws Exception {
    String body = result.getResponse().getContentAsString();
    JsonNode node = objectMapper.readTree(body);
    return node.get("article").get("slug").asText();
  }

  private String extractCommentId(MvcResult result) throws Exception {
    String body = result.getResponse().getContentAsString();
    JsonNode node = objectMapper.readTree(body);
    return node.get("comment").get("id").asText();
  }
}
