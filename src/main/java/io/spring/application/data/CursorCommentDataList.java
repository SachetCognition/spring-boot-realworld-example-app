package io.spring.application.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.spring.application.CursorPager;
import java.util.List;
import lombok.Getter;

@Getter
public class CursorCommentDataList {
  @JsonProperty("comments")
  private final List<CommentData> commentDatas;

  @JsonProperty("hasNext")
  private final boolean hasNext;

  @JsonProperty("hasPrevious")
  private final boolean hasPrevious;

  @JsonProperty("startCursor")
  private final String startCursor;

  @JsonProperty("endCursor")
  private final String endCursor;

  public CursorCommentDataList(CursorPager<CommentData> cursorPager) {
    this.commentDatas = cursorPager.getData();
    this.hasNext = cursorPager.hasNext();
    this.hasPrevious = cursorPager.hasPrevious();
    this.startCursor =
        cursorPager.getStartCursor() == null ? null : cursorPager.getStartCursor().toString();
    this.endCursor =
        cursorPager.getEndCursor() == null ? null : cursorPager.getEndCursor().toString();
  }
}
