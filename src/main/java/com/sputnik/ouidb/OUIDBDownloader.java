package com.sputnik.ouidb;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.sputnik.ouidb.exception.NoRecordsFoundException;
import com.sputnik.ouidb.model.Organization;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

/**
 * Simple utility class to parse the latestOUI DB and parse it as case insensitive a Map.<br> Usage:
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

  private static final String CHROME_USER_AGENT = "Mozilla/5.0 (Windows NT 11.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.6998.166 Safari/537.36";
  private String ouiDbUrl = "http://standards-oui.ieee.org/oui/oui.txt";
  private final OUIDBParser ouidbParser = new OUIDBParser();

  public OUIDBDownloader() {
  }


  public OUIDBDownloader(String ouiDbUrl) {
    this.ouiDbUrl = ouiDbUrl;
  }

  public Map<String, Organization> getParsedDB() throws IOException {
    return ouidbParser.parseDb(download());
  }

  @SneakyThrows
  public Reader download() {
    Reader result = null;
    HttpClient httpClient = getHttpClient();

    HttpRequest httpRequest = HttpRequest.newBuilder()
      .setHeader("Accept", "Accept: text/html, application/xhtml+xml, application/xml;q=0.9, */*;q=0.8")
      .setHeader("User-Agent", CHROME_USER_AGENT)
      .uri(new URI(ouiDbUrl))
      .GET()
      .build();

    log.info("Trying to download info from {}", ouiDbUrl);
    try {
      HttpResponse<InputStream> httpResponse = httpClient.send(httpRequest, BodyHandlers.ofInputStream());

      if (httpResponse != null && httpResponse.statusCode() == 200) {
        result = new InputStreamReader(httpResponse.body());
      } else {
        log.warn("Error downloading OUIs. Error: {} - {}", httpResponse.statusCode(), IOUtils.toString(httpResponse.body(), UTF_8));
      }
    } catch (IOException e) {
      log.warn("Error downloading OUIs from {} - {}: {}", ouiDbUrl, e.getClass().getSimpleName(), e.getMessage());
    }

    if (result == null) {
      throw new NoRecordsFoundException("No records found");
    }

    return result;
  }

  private HttpClient getHttpClient() {
    return HttpClient.newBuilder().followRedirects(Redirect.ALWAYS).build();
  }
}