package com.yuriytkach.excel.search;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine;

@TopCommand
@CommandLine.Command(
  name = "search app",
  mixinStandardHelpOptions = true,
  subcommands = {
    ExcelParseCommand.class,
    SearchCommand.class
  }
)
public class AppMainCommand {

}
