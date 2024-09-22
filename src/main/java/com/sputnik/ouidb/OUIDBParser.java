package com.sputnik.ouidb;

import static com.sputnik.ouidb.OUIDBNormalizer.normalize;
import static com.sputnik.ouidb.OUIDBNormalizer.normalizeAddressLine;
import static com.sputnik.ouidb.OUIDBNormalizer.normalizeOrganizationName;
import static com.sputnik.ouidb.OUIDBNormalizer.normalizePrefix;

import com.sputnik.ouidb.exception.NoRecordsFoundException;
import com.sputnik.ouidb.model.Address;
import com.sputnik.ouidb.model.Organization;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.lang3.StringUtils;

public class OUIDBParser {

  public Map<String, Organization> parseDb(Reader db) throws IOException {
    Map<String, Organization> response = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    try (BufferedReader reader = new BufferedReader(db)) {
      String line;
      int counter = 0;
      Organization organization = null;
      while ((line = reader.readLine()) != null) {
        line = normalize(line);
        if (line.contains("(hex)")) {
          String[] split = StringUtils.splitByWholeSeparator(line, "(hex)");
          String prefix = normalizePrefix(split[0]);
          String organizationName = normalizeOrganizationName(split[1]);
          counter = 0;
          organization = new Organization(organizationName);
          organization.setAddress(new Address());
          response.put(prefix, organization);
        } else if (counter < 3 && organization != null && !line.contains("(base 16)")) {
          counter = fillAddress(line, counter, organization.getAddress());
        }
      }
    }

    if (response.isEmpty()) {
      throw new NoRecordsFoundException("No records found");
    }

    return response;
  }

  private int fillAddress(String line, int counter, Address address) {
    if (counter == 0) {
      address.setLine1(normalizeAddressLine(line));
      counter++;
    } else if (counter == 1) {
      address.setLine2(normalizeAddressLine(line));
      counter++;
    } else if (counter == 2) {
      address.setCountryCode(normalizeAddressLine(line));
      counter++;
    }

    return counter;
  }
}
