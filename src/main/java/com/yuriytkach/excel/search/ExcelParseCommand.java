package com.yuriytkach.excel.search;

import java.io.File;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import com.yuriytkach.excel.search.excel.ExcelParser;
import com.yuriytkach.excel.search.lucene.IndexService;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

@Slf4j
@CommandLine.Command(name = "parse", mixinStandardHelpOptions = true)
public class ExcelParseCommand implements Callable<Integer> {

  @CommandLine.Option(
    names = { "-f", "--filename" },
    description = "Excel filename to parse",
    required = true
  )
  String filename;

  @CommandLine.Option(
    names = { "-i", "--index-path" },
    description = "Folder with index",
    defaultValue = "./lucene/index",
    required = true
  )
  String indexPath;

  @Inject
  ExcelParser excelParser;

  @Inject
  IndexService indexService;

  @Override
  @SneakyThrows
  public Integer call() {
    log.info("Reading Excel file: " + filename);
    final File file = new File(filename);
    final var items = excelParser.parse(file);

    log.info("Populating index at {}", new File(indexPath).getAbsolutePath());
    indexService.populate(items, indexPath);
    log.info("Done populating!");

    return 0;
  }
}
