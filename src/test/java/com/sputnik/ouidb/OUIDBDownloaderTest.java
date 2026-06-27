package com.sputnik.ouidb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sputnik.ouidb.exception.NoRecordsFoundException;
import com.sputnik.ouidb.model.Organization;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Slf4j
class OUIDBDownloaderTest {

  private static final String SAMPLE_OUI = """
    00-00-0C   (hex)\t\tCisco Systems, Inc
    00000C     (base 16)\t\tCisco Systems, Inc
    \t\t\t\t170 West Tasman Drive
    \t\t\t\tSan Jose CA 95134
    \t\t\t\tUS
    """;

  @Test
  void testUnknownHostUrl() {
    OUIDBDownloader unknownHostUrlOuidbDownloader = new OUIDBDownloader("http://thisurldoes.not.exists");
    unknownHostUrlOuidbDownloader.setRetryPolicy(2, Duration.ZERO);
    assertThatThrownBy(unknownHostUrlOuidbDownloader::getParsedDB).isInstanceOf(NoRecordsFoundException.class);
  }

  @Test
  void testWrongUrl() {
    OUIDBDownloader wrongUrlOuiDBDownloader = new OUIDBDownloader("http://www.google.com");
    wrongUrlOuiDBDownloader.setRetryPolicy(2, Duration.ZERO);
    assertThatThrownBy(wrongUrlOuiDBDownloader::getParsedDB).isInstanceOf(NoRecordsFoundException.class);
  }

  @Test
  void testRetriesUntilSuccessAndSendsBrowserHeaders() throws IOException {
    AtomicInteger attempts = new AtomicInteger();
    String[] capturedUserAgent = new String[1];
    String[] capturedAccept = new String[1];

    HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/oui.txt", exchange -> {
      int attempt = attempts.incrementAndGet();
      // Fail the first two attempts with a server error, succeed on the third.
      if (attempt < 3) {
        exchange.sendResponseHeaders(503, -1);
        exchange.close();
        return;
      }
      capturedUserAgent[0] = exchange.getRequestHeaders().getFirst("User-Agent");
      capturedAccept[0] = exchange.getRequestHeaders().getFirst("Accept");
      respond(exchange, SAMPLE_OUI);
    });
    server.start();

    try {
      String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/oui.txt";
      OUIDBDownloader downloader = new OUIDBDownloader(url);
      downloader.setRetryPolicy(5, Duration.ZERO);

      Map<String, Organization> parsedDB = downloader.getParsedDB();

      assertThat(attempts.get()).isEqualTo(3);
      assertThat(parsedDB).containsKey("00000C");
      assertThat(capturedUserAgent[0]).contains("Chrome");
      // The Accept header value must not be malformed (it used to start with "Accept: ").
      assertThat(capturedAccept[0]).startsWith("text/html").doesNotContain("Accept:");
    } finally {
      server.stop(0);
    }
  }

  @Test
  void testDownloadAsStringReturnsRawTextForMirroring() throws IOException {
    HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/oui.txt", exchange -> respond(exchange, SAMPLE_OUI));
    server.start();

    try {
      String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/oui.txt";
      OUIDBDownloader downloader = new OUIDBDownloader(url);

      String raw = downloader.downloadAsString();

      // The raw text must be returned verbatim so it can be published as a mirror.
      assertThat(raw).isEqualTo(SAMPLE_OUI);
      // ...and still parse correctly from that same raw text.
      assertThat(downloader.parse(raw)).containsKey("00000C");
    } finally {
      server.stop(0);
    }
  }

  @Test
  void testGivesUpAfterMaxAttempts() {
    AtomicInteger attempts = new AtomicInteger();

    HttpServer server;
    try {
      server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    server.createContext("/oui.txt", exchange -> {
      attempts.incrementAndGet();
      exchange.sendResponseHeaders(503, -1);
      exchange.close();
    });
    server.start();

    try {
      String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/oui.txt";
      OUIDBDownloader downloader = new OUIDBDownloader(url);
      downloader.setRetryPolicy(3, Duration.ZERO);

      assertThatThrownBy(downloader::getParsedDB).isInstanceOf(NoRecordsFoundException.class);
      assertThat(attempts.get()).isEqualTo(3);
    } finally {
      server.stop(0);
    }
  }

  @Test
  @Disabled("Only for manual testing")
  void testDownloadTXT() throws IOException {
    Map<String, Organization> parsedDB = new OUIDBDownloader("http://standards-oui.ieee.org/oui/oui.txt").getParsedDB();
    log.info("ParsedDB Size: {}", parsedDB.size());
    assertThat(parsedDB).isNotEmpty();
  }

  private static void respond(HttpExchange exchange, String body) throws IOException {
    byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
    exchange.sendResponseHeaders(200, bytes.length);
    exchange.getResponseBody().write(bytes);
    exchange.close();
  }
}
