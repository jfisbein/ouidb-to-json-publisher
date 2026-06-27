package com.sputnik.ouidb;

import com.sputnik.ouidb.exception.NoRecordsFoundException;
import com.sputnik.ouidb.model.Organization;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

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
  private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(30);
  private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(60);
  private static final int DEFAULT_MAX_ATTEMPTS = 5;
  private static final Duration DEFAULT_RETRY_DELAY = Duration.ofSeconds(5);

  private String ouiDbUrl = "http://standards-oui.ieee.org/oui/oui.txt";
  private final OUIDBParser ouidbParser = new OUIDBParser();
  private int maxAttempts = DEFAULT_MAX_ATTEMPTS;
  private Duration retryDelay = DEFAULT_RETRY_DELAY;

  public OUIDBDownloader() {
  }


  public OUIDBDownloader(String ouiDbUrl) {
    this.ouiDbUrl = ouiDbUrl;
  }

  /**
   * Configures the retry policy used when downloading fails (transient connection errors or non-200 responses).
   *
   * @param maxAttempts total number of attempts before giving up (must be >= 1)
   * @param retryDelay  base delay between attempts; the actual wait grows exponentially per attempt
   */
  public void setRetryPolicy(int maxAttempts, Duration retryDelay) {
    if (maxAttempts < 1) {
      throw new IllegalArgumentException("maxAttempts must be >= 1");
    }
    this.maxAttempts = maxAttempts;
    this.retryDelay = retryDelay;
  }

  public Map<String, Organization> getParsedDB() throws IOException {
    return ouidbParser.parseDb(download());
  }

  public Reader download() throws IOException {
    HttpClient httpClient = getHttpClient();
    HttpRequest httpRequest = buildRequest();

    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
      log.info("Trying to download info from {} (attempt {}/{})", ouiDbUrl, attempt, maxAttempts);
      try {
        HttpResponse<InputStream> httpResponse = httpClient.send(httpRequest, BodyHandlers.ofInputStream());

        if (httpResponse != null && httpResponse.statusCode() == 200) {
          return new InputStreamReader(httpResponse.body(), UTF_8);
        }

        int statusCode = httpResponse == null ? -1 : httpResponse.statusCode();
        String body = httpResponse == null ? "" : IOUtils.toString(httpResponse.body(), UTF_8);
        log.warn("HTTP Error downloading OUIs. Error: {} - {}", statusCode, body);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new NoRecordsFoundException("Download interrupted", e);
      } catch (IOException e) {
        log.warn("Error downloading OUIs from {} - {}", ouiDbUrl, e.getClass().getSimpleName(), e);
      }

      if (attempt < maxAttempts) {
        sleepBeforeRetry(attempt);
      }
    }

    throw new NoRecordsFoundException("No records found after " + maxAttempts + " attempts");
  }

  private void sleepBeforeRetry(int attempt) {
    // Exponential backoff: delay, delay*2, delay*4, ...
    long millis = retryDelay.toMillis() * (1L << (attempt - 1));
    if (millis <= 0) {
      return;
    }
    log.info("Waiting {} ms before retrying download", millis);
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new NoRecordsFoundException("Download interrupted while waiting to retry", e);
    }
  }

  private HttpRequest buildRequest() {
    try {
      // Browser-like headers to reduce the chance of the IEEE anti-bot WAF rejecting the request.
      // Note: we intentionally do NOT advertise Accept-Encoding (gzip/br) because the JDK HttpClient
      // does not auto-decompress responses, and a compressed body would break parsing.
      return HttpRequest.newBuilder()
        .uri(new URI(ouiDbUrl))
        .timeout(REQUEST_TIMEOUT)
        .setHeader("User-Agent", CHROME_USER_AGENT)
        .setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
        .setHeader("Accept-Language", "en-US,en;q=0.9")
        .setHeader("Upgrade-Insecure-Requests", "1")
        .setHeader("sec-ch-ua", "\"Chromium\";v=\"134\", \"Not:A-Brand\";v=\"24\", \"Google Chrome\";v=\"134\"")
        .setHeader("sec-ch-ua-mobile", "?0")
        .setHeader("sec-ch-ua-platform", "\"Windows\"")
        .setHeader("Sec-Fetch-Dest", "document")
        .setHeader("Sec-Fetch-Mode", "navigate")
        .setHeader("Sec-Fetch-Site", "none")
        .setHeader("Sec-Fetch-User", "?1")
        .GET()
        .build();
    } catch (URISyntaxException e) {
      throw new NoRecordsFoundException("Invalid URL: " + ouiDbUrl, e);
    }
  }

  private HttpClient getHttpClient() {
    return HttpClient.newBuilder()
      .followRedirects(Redirect.ALWAYS)
      .connectTimeout(CONNECT_TIMEOUT)
      .build();
  }
}
