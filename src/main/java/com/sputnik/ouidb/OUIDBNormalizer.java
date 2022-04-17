package com.sputnik.ouidb;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

@UtilityClass
public class OUIDBNormalizer {

  public static String normalizeOrganizationName(String orgName) {
    if (orgName != null) {
      orgName = orgName.trim();
      orgName = StringUtils.removeEnd(orgName, ".");
      orgName = capitalizeWordsLargerThan(orgName, 3);
      orgName = StringUtils.replaceIgnoreCase(orgName, " Inc.orporated", " Inc");
      orgName = replaceEndIgnoreCase(orgName, " Ltd", " Ltd");
      orgName = replaceEndIgnoreCase(orgName, " Inc", " Inc");
      orgName = replaceEndIgnoreCase(orgName, ", Inc", " Inc");
      orgName = replaceEndIgnoreCase(orgName, ", Ltd", " Ltd");
      orgName = replaceEndIgnoreCase(orgName, " Gmbh", " GmbH");
      orgName = replaceEndIgnoreCase(orgName, " Co. Ltd", " Ltd");
      orgName = replaceEndIgnoreCase(orgName, " Co Ltd", " Ltd");
      orgName = replaceEndIgnoreCase(orgName, " CO., LT", " Ltd");

      orgName = StringUtils.replace(orgName, "Apple Inc", "Apple");
    }

    return orgName;
  }

  public static String normalizeAddressLine(String line) {
    line = StringUtils.trimToNull(line);
    if (line != null) {
      line = capitalizeWordsLargerThan(line, 2);
      line = replaceEndIgnoreCase(line, " St.", " St");
      line = replaceEndIgnoreCase(line, " Pkwy", " Parkway");
      line = StringUtils.replace(line, " AVE.", " Ave.");
      line = StringUtils.replace(line, " BLVD.", " Blvd.");
    }

    return line;
  }

  public static String normalizePrefix(String prefix) {
    if (prefix != null) {
      prefix = prefix.trim();
      prefix = prefix.replace("-", "");
      prefix = prefix.toUpperCase();
    }

    return prefix;
  }

  public static String normalize(String text) {
    String normalizedText = null;
    if (text != null) {
      normalizedText = text.trim();
      normalizedText = StringUtils.removeEnd(normalizedText, ",");
      normalizedText = StringUtils.replaceIgnoreCase(normalizedText, ",.Ltd", ". Ltd");

      // Add space after ',' if missing
      normalizedText = RegExUtils.replaceAll(normalizedText, "\\,(\\w{1})", ", $1");

      // Remove space before ',' if present
      normalizedText = RegExUtils.replaceAll(normalizedText, " \\,", ",");

      normalizedText = normalizedText.trim();
      while (normalizedText.contains("   ")) {
        normalizedText = StringUtils.replace(normalizedText, "   ", "  ");
      }

      normalizedText = normalizedText.replace("  ", " ");

      normalizedText = StringUtils.replace(normalizedText, " .", ".");
      normalizedText = normalizedText.trim();
    }

    return normalizedText;
  }

  private String replaceEndIgnoreCase(String str, String suffix, String replacement) {
    if (StringUtils.endsWithIgnoreCase(str, suffix)) {
      str = StringUtils.removeEndIgnoreCase(str, suffix);
      str = str + replacement;
    }

    return str;
  }

  private String capitalizeWordsLargerThan(String str, int length) {
    return Arrays.stream(StringUtils.split(str, " "))
      .map(word -> {
        if (word.length() > length && !word.contains("/") && !word.contains(".")) {
          word = WordUtils.capitalizeFully(word);
        }
        return word;
      })
      .collect(Collectors.joining(" "));
  }
}
