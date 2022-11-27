package com.yuriytkach.excel.search.excel;

import java.util.Optional;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Columns {
  AUTHOR("author"),
  TITLE("title"),
  TEXT("text"),
  DATE("date"),
  COUNT("count");

  private final String title;

  public static Optional<Columns> fromTitle(final String value) {
    if (value == null || value.isBlank()) {
      return Optional.empty();
    }
    final String adjustedValue = value.strip();
    return Stream.of(Columns.values())
      .filter(v -> v.title.equalsIgnoreCase(adjustedValue))
      .findFirst();
  }
}
