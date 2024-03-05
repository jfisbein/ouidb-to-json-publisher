package com.sputnik.ouidb.cli;

import com.beust.jcommander.IDefaultProvider;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.lang3.StringUtils;


public class EnvironmentDefaultProvider implements IDefaultProvider {

  private final Map<String, String> env;

  public EnvironmentDefaultProvider(boolean caseSensitive) {
    if (caseSensitive) {
      env = System.getenv();
    } else {
      env = new TreeMap<>(String::compareToIgnoreCase);
      env.putAll(System.getenv());
    }
  }

  public EnvironmentDefaultProvider() {
    this(true);
  }

  @Override
  public String getDefaultValueFor(String optionName) {
    optionName = StringUtils.removeStart(optionName, "--");
    optionName = StringUtils.removeStart(optionName, "-");
    optionName = StringUtils.replace(optionName, "-", "_");

    return env.get(optionName);
  }
}
