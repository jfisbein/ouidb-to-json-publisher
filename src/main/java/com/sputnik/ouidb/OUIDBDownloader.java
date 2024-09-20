package com.sputnik.ouidb;

import com.sputnik.ouidb.exception.NoRecordsFoundException;
import com.sputnik.ouidb.model.Address;
import com.sputnik.ouidb.model.Organization;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

import static com.sputnik.ouidb.OUIDBNormalizer.*;

/**
 * Simple utility class to parse the latestOUI DB and parse it as case insensitive a Map.<br>
 * Usage:
 * <pre>
 * {@code
 * Map<String,String> ouidb = new OUIDBDownloader().getParsedDB();
 * String vendor = ouidb.get("C47130");
 * System.out.println(vendor); // Fon Technology S.L.
 * }
 * </pre>
 */
@Slf4j
public class OUIDBDownloader {

  public static final int TEN_MB = 10 * 1024 * 1024;
  //private String[] ouiDbUrls = new String[]{"https://linuxnet.ca/ieee/oui.txt.bz2", "https://linuxnet.ca/ieee/oui.txt.gz", "https://linuxnet.ca/ieee/oui.txt"};
  private String[] ouiDbUrls = new String[]{"http://standards-oui.ieee.org/oui/oui.txt"};

  public OUIDBDownloader() {
  }

  public OUIDBDownloader(String[] ouiDbUrls) {
    this.ouiDbUrls = ouiDbUrls;
  }

  public OUIDBDownloader(String ouiDbUrl) {
    ouiDbUrls = new String[]{ouiDbUrl};
  }

  public Map<String, Organization> getParsedDB() throws IOException {
    return parseDb(download());
  }

  public Reader download() {
    Cache cache = new Cache(new File(System.getProperty("java.io.tmpdir")), TEN_MB);
    OkHttpClient client = new OkHttpClient.Builder().cache(cache).build();
    Reader result = null;
    Iterator<String> urlIterator = Arrays.asList(ouiDbUrls).iterator();
    while (result == null && urlIterator.hasNext()) {
      String ouiDbUrl = urlIterator.next();
      Request request = new Request.Builder()
        .url(ouiDbUrl)
        .build();

      log.info("Trying to download info from {}", ouiDbUrl);
      try {
        Response response = client.newCall(request).execute();
        if (response.body() != null) {
          String contentTypeHeader = getContentTypeHeader(response).orElse("text/plain; charset=utf-8");
          if (contentTypeHeader.equalsIgnoreCase("application/x-gzip")) {
            result = new InputStreamReader(new GZIPInputStream(response.body().byteStream()));
          } else if (contentTypeHeader.equalsIgnoreCase("application/x-bzip2")) {
            result = new InputStreamReader(new BZip2CompressorInputStream(response.body().byteStream()));
          } else {
            result = response.body().charStream();
          }
        } else {
          log.warn("Error downloading OUIs. Error: {} - {}", response.code(), response.message());
        }
      } catch (IOException e) {
        log.warn("Error downloading OUIs from {} - {}: {}", ouiDbUrl, e.getClass().getSimpleName(), e.getMessage());
      }
    }
    if (result == null) {
      throw new NoRecordsFoundException("No records found");
    }

    return result;
  }

  private Optional<String> getContentTypeHeader(Response response) {
    String headerValue = null;
    List<String> contentTypeHeaders = response.headers("Content-Type");
    if (!contentTypeHeaders.isEmpty()) {
      headerValue = contentTypeHeaders.get(0);
    }

    return Optional.ofNullable(headerValue);
  }

  protected Map<String, Organization> parseDb(Reader db) throws IOException {
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