package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.spring.application.TagsQueryService;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class TagDatafetcherTest {

  @Test
  void should_return_all_tags() {
    TagsQueryService tagsQueryService = mock(TagsQueryService.class);
    TagDatafetcher tagDatafetcher = new TagDatafetcher(tagsQueryService);

    when(tagsQueryService.allTags()).thenReturn(Arrays.asList("java", "spring", "graphql"));

    List<String> result = tagDatafetcher.getTags();
    assertEquals(3, result.size());
    assertTrue(result.contains("java"));
    assertTrue(result.contains("spring"));
    assertTrue(result.contains("graphql"));
  }

  @Test
  void should_return_empty_list_when_no_tags() {
    TagsQueryService tagsQueryService = mock(TagsQueryService.class);
    TagDatafetcher tagDatafetcher = new TagDatafetcher(tagsQueryService);

    when(tagsQueryService.allTags()).thenReturn(Arrays.asList());

    List<String> result = tagDatafetcher.getTags();
    assertTrue(result.isEmpty());
  }
}
