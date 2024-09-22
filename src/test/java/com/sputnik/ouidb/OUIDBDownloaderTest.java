package com.sputnik.ouidb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sputnik.ouidb.exception.NoRecordsFoundException;
import com.sputnik.ouidb.model.Organization;
import java.io.IOException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Slf4j
class OUIDBDownloaderTest {


  @Test
  void testUnknownHostUrl() {
    OUIDBDownloader unknownHostUrlOuidbDownloader = new OUIDBDownloader("http://thisurldoes.not.exists");
    assertThatThrownBy(unknownHostUrlOuidbDownloader::getParsedDB).isInstanceOf(NoRecordsFoundException.class);
  }

  @Test
  void testWrongUrl() {
    OUIDBDownloader wrongUrlOuiDBDownloader = new OUIDBDownloader("http://www.google.com");
    assertThatThrownBy(wrongUrlOuiDBDownloader::getParsedDB).isInstanceOf(NoRecordsFoundException.class);
  }

  @Test
  @Disabled("Only for manual testing")
  void testDownloadTXT() throws IOException {
    Map<String, Organization> parsedDB = new OUIDBDownloader("http://standards-oui.ieee.org/oui/oui.txt").getParsedDB();
    log.info("ParsedDB Size: {}", parsedDB.size());
    assertThat(parsedDB).isNotEmpty();
  }
}