package com.yuriytkach.excel.search.model;

import java.time.LocalDate;

import org.apache.commons.lang3.StringUtils;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Poem {
  private final String author;
  private final String title;
  private final String text;
  private final LocalDate date;
  private final int count;

  public String toShortString() {
    return "Poem >> %s \"%s\" %s [%d] %s%n<<".formatted(
      author,
      title,
      date,
      count,
      StringUtils.abbreviate(text, 50)
    );
  }
}
