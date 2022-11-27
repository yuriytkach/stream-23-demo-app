package com.yuriytkach.excel.search;

import java.io.File;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import com.yuriytkach.excel.search.lucene.IndexService;

import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

@Slf4j
@CommandLine.Command(name = "search", mixinStandardHelpOptions = true)
public class SearchCommand implements Callable<Integer> {

  @CommandLine.Option(
    names = { "-q", "--query" },
    description = "Query for search",
    required = true
  )
  String query;

  @CommandLine.Option(
    names = { "-i", "--index-path" },
    description = "Folder with index",
    defaultValue = "./lucene/index",
    required = true
  )
  String indexPath;

  @Inject
  IndexService indexService;

  @Override
  public Integer call() {
    log.info("Searching in index: {}", new File(indexPath).getAbsolutePath());
    log.info("Query: {}", query);
    final var list = indexService.search(query, indexPath);
    list.forEach(poem -> System.out.println(poem.toShortString()));
    return 0;
  }
}
