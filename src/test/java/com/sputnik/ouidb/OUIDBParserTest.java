package com.sputnik.ouidb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sputnik.ouidb.exception.NoRecordsFoundException;
import com.sputnik.ouidb.model.Organization;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class OUIDBParserTest {

  @Test
  void parseDBEmpty() {
    try (FileReader emptyDBFileReader = new FileReader("src/test/resources/ouidb-empty.txt")) {
      OUIDBParser ouidbParser = new OUIDBParser();
      assertThatThrownBy(() -> ouidbParser.parseDb(emptyDBFileReader)).isInstanceOf(NoRecordsFoundException.class);
    } catch (IOException e) {
      log.warn(e.getMessage());
    }
  }

  @Test
  void parsedDB() throws IOException {
    Map<String, Organization> db = new OUIDBParser().parseDb(new FileReader("src/test/resources/ouidb-test.txt"));

    // All OUI parsed
    assertThat(db).as("Missing some OUIs from parsed result").hasSize(9);

    // Private
    testOUI(db, "E4F14C", "Private", null, null, null);

    // Normal
    testOUI(db, "00093A", "Molex CMS", "5224 Katrine Avenue", "Downers Grove IL 60515", "US");
    testOUI(db, "18BB26", "Fn-link Technology Limited",
      "A Building, Huixin Industial Park, No 31, Yonghe Road, Fuyong Town, Bao'an District", "Shenzhen Guangdong 518100", "CN");

    // Normalize
    testOUI(db, "001F18", "Hakusan.Mfg.Co. Ltd", "Tomin-kougyou-ikebukuro BLD.5F", "Tosima Ward Tokyo-Met. 171-0022", "JP");
    testOUI(db, "001E61", "Itec GmbH", "Lassnitzthal 300", "A-8200 Gleisdorf", "AT");
    testOUI(db, "000037", "Oxford Metrics Limited", "Unit 8, 7 West Way", "United Kingdom", "GB");
    testOUI(db, "1CB044", "Askey Computer Corp", "10f, No.119, Jiankang Rd, Zhonghe Dist", "New Taipei Taiwan 23585", "TW");
    testOUI(db, "900372", "Longnan Junya Digital Technology Ltd",
      "Champion Asia Road, Xinzhen Industrial Park, Longnan National Economic And Technological Development Zone, Ganzhou City, Jiangxi Province, China",
      "Ganzhou Jiangxi 341700", "CN");

    // No addressLine 2
    testOUI(db, "000057", "Scitex Corporation Ltd", "P.O. Box 330", null, "IL");
  }


  private void testOUI(Map<String, Organization> db, String prefix, String name, String addressLine1, String addressLine2,
    String countryCode) {
    assertThat(db).containsKey(prefix);

    assertThat(db.get(prefix)).satisfies(org -> {
        assertThat(org.getName()).isEqualTo(name);
        assertThat(org.getAddress().getLine1()).isEqualTo(addressLine1);
        assertThat(org.getAddress().getLine2()).isEqualTo(addressLine2);
        assertThat(org.getAddress().getCountryCode()).isEqualTo(countryCode);
      }
    );
  }
}