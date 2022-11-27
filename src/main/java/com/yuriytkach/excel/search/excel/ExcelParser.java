package com.yuriytkach.excel.search.excel;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;

import org.apache.poi.UnsupportedFileFormatException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.yuriytkach.excel.search.model.Poem;

import lombok.extern.slf4j.Slf4j;
import one.util.streamex.IntStreamEx;
import one.util.streamex.StreamEx;

@Slf4j
@ApplicationScoped
public class ExcelParser {

  public List<Poem> parse(final File file) {
    try (XSSFWorkbook sheets = new XSSFWorkbook(file)) {
      final XSSFSheet sheet = sheets.getSheetAt(0);

      final Map<Columns, Integer> columns = readColumns(sheet);

      final List<Poem> items = parseRows(sheet, columns);
      log.info("Parsed items: {}", items.size());

      return items;
    } catch (final UnsupportedFileFormatException | IOException | InvalidFormatException ex) {
      log.info("Cannot parse offer file: {}", ex.getMessage(), ex);
      throw new IllegalStateException(ex);
    }
  }

  private List<Poem> parseRows(final XSSFSheet sheet, final Map<Columns, Integer> columns) {
    return StreamEx.of(sheet.iterator())
      .skip(1) // header row
      .flatMap(row -> buildItem(row, columns))
      .toList();
  }

  private Stream<Poem> buildItem(final Row row, final Map<Columns, Integer> columns) {
    try {
      final var item = Poem.builder()
        .author(row.getCell(columns.get(Columns.AUTHOR)).getStringCellValue())
        .title(row.getCell(columns.get(Columns.TITLE)).getStringCellValue())
        .text(row.getCell(columns.get(Columns.TEXT)).getStringCellValue())
        .date(row.getCell(columns.get(Columns.DATE)).getLocalDateTimeCellValue().toLocalDate())
        .count((int) row.getCell(columns.get(Columns.COUNT)).getNumericCellValue())
        .build();
      return Stream.of(item);
    } catch (final Exception ex) {
      log.error("Failed to read row {}: {}", row.getRowNum(), ex.getMessage());
      throw new IllegalStateException(ex);
      //return Stream.empty();
    }
  }

  private Map<Columns, Integer> readColumns(final XSSFSheet sheet) {
    final XSSFRow titleRow = sheet.getRow(0);
    final int colNum = titleRow.getLastCellNum();
    final var columns =  IntStreamEx.rangeClosed(0, colNum)
      .mapToObj(titleRow::getCell)
      .filter(Objects::nonNull)
      .mapToEntry(XSSFCell::getStringCellValue)
      .mapValues(Columns::fromTitle)
      .flatMapValues(Optional::stream)
      .mapKeys(XSSFCell::getColumnIndex)
      .invert()
      .peekKeyValue((col, index) -> log.info("Read column {} -> {}", col, index))
      .toImmutableMap();

    if (columns.size() < Columns.values().length) {
      throw new IllegalStateException("Not all columns were found");
    }

    return columns;
  }

}
