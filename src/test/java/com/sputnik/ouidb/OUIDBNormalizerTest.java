package com.sputnik.ouidb;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OUIDBNormalizerTest {

  @Test
  void normalize() {
    assertThat(OUIDBNormalizer.normalize(null)).isNull();
    assertThat(OUIDBNormalizer.normalize("")).isEmpty();
    assertThat(OUIDBNormalizer.normalize(" ")).isEmpty();
    assertThat(OUIDBNormalizer.normalize("ABC")).isEqualTo("ABC");
    assertThat(OUIDBNormalizer.normalize(" ABC ")).isEqualTo("ABC");
    assertThat(OUIDBNormalizer.normalize(" ABC ,")).isEqualTo("ABC");
    assertThat(OUIDBNormalizer.normalize("ABC,DEF")).isEqualTo("ABC, DEF");
    assertThat(OUIDBNormalizer.normalize("ABC, DEF")).isEqualTo("ABC, DEF");
    assertThat(OUIDBNormalizer.normalize("ABC , DEF")).isEqualTo("ABC, DEF");
    assertThat(OUIDBNormalizer.normalize("ABC,.Ltd")).isEqualTo("ABC. Ltd");
    assertThat(OUIDBNormalizer.normalize("ABC,.ltd")).isEqualTo("ABC. Ltd");
    assertThat(OUIDBNormalizer.normalize("ABC DEF")).isEqualTo("ABC DEF");
    assertThat(OUIDBNormalizer.normalize("ABC  DEF")).isEqualTo("ABC DEF");
    assertThat(OUIDBNormalizer.normalize("ABC   DEF")).isEqualTo("ABC DEF");
    assertThat(OUIDBNormalizer.normalize("ABC    DEF")).isEqualTo("ABC DEF");
    assertThat(OUIDBNormalizer.normalize(" ABC    DEF ")).isEqualTo("ABC DEF");
  }

  @Test
  void normalizePrefix() {
    assertThat(OUIDBNormalizer.normalizePrefix(null)).isNull();
    assertThat(OUIDBNormalizer.normalizePrefix("AA-BB-CC")).isEqualTo("AABBCC");
    assertThat(OUIDBNormalizer.normalizePrefix("AABBCC")).isEqualTo("AABBCC");
    assertThat(OUIDBNormalizer.normalizePrefix("aa-bb-cc")).isEqualTo("AABBCC");
    assertThat(OUIDBNormalizer.normalizePrefix("aabbcc")).isEqualTo("AABBCC");
  }

  @Test
  void normalizeOrganizationName() {
    assertThat(OUIDBNormalizer.normalizeOrganizationName(null)).isNull();
    assertThat(OUIDBNormalizer.normalizeOrganizationName("CocaCola Company")).isEqualTo("Cocacola Company");
    assertThat(OUIDBNormalizer.normalizeOrganizationName("CocaCola Inc.")).isEqualTo("Cocacola Inc");
    assertThat(OUIDBNormalizer.normalizeOrganizationName("CocaCola Inc.orporated")).isEqualTo("Cocacola Inc");
    assertThat(OUIDBNormalizer.normalizeOrganizationName("CocaCola inc.orporated")).isEqualTo("Cocacola Inc");
  }
}