package io.spring.application.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.spring.application.CursorPager;
import java.util.List;
import lombok.Getter;

@Getter
public class CursorArticleDataList {
  @JsonProperty("articles")
  private final List<ArticleData> articleDatas;

  @JsonProperty("hasNext")
  private final boolean hasNext;

  @JsonProperty("hasPrevious")
  private final boolean hasPrevious;

  @JsonProperty("startCursor")
  private final String startCursor;

  @JsonProperty("endCursor")
  private final String endCursor;

  public CursorArticleDataList(CursorPager<ArticleData> cursorPager) {
    this.articleDatas = cursorPager.getData();
    this.hasNext = cursorPager.hasNext();
    this.hasPrevious = cursorPager.hasPrevious();
    this.startCursor =
        cursorPager.getStartCursor() == null ? null : cursorPager.getStartCursor().toString();
    this.endCursor =
        cursorPager.getEndCursor() == null ? null : cursorPager.getEndCursor().toString();
  }
}
