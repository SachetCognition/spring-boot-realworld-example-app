package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.spring.infrastructure.mybatis.readservice.TagReadService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TagsQueryServiceTest {

  private TagReadService tagReadService;
  private TagsQueryService tagsQueryService;

  @BeforeEach
  void setUp() {
    tagReadService = mock(TagReadService.class);
    tagsQueryService = new TagsQueryService(tagReadService);
  }

  @Test
  void should_return_all_tags() {
    when(tagReadService.all()).thenReturn(Arrays.asList("java", "spring", "test"));
    List<String> result = tagsQueryService.allTags();
    assertEquals(3, result.size());
  }

  @Test
  void should_return_empty_when_no_tags() {
    when(tagReadService.all()).thenReturn(Collections.emptyList());
    List<String> result = tagsQueryService.allTags();
    assertTrue(result.isEmpty());
  }
}
