package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class ArticleDataListTest {

  @Test
  void should_create_with_articles_and_count() {
    ArticleData article = new ArticleData();
    article.setId("id");
    ArticleDataList list = new ArticleDataList(Arrays.asList(article), 1);

    assertEquals(1, list.getArticleDatas().size());
    assertEquals(1, list.getCount());
  }

  @Test
  void should_create_with_empty_list() {
    ArticleDataList list = new ArticleDataList(Collections.emptyList(), 0);
    assertTrue(list.getArticleDatas().isEmpty());
    assertEquals(0, list.getCount());
  }
}
