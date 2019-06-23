package com.sputnik.ouidb;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class OUIDBNormalizerTest {

    @Test
    void normalize() {
        assertNull(OUIDBNormalizer.normalize(null));
        assertEquals("", OUIDBNormalizer.normalize(""));
        assertEquals("", OUIDBNormalizer.normalize(" "));
        assertEquals("ABC", OUIDBNormalizer.normalize("ABC"));
        assertEquals("ABC", OUIDBNormalizer.normalize(" ABC "));
        assertEquals("ABC", OUIDBNormalizer.normalize(" ABC ,"));
        assertEquals("ABC, DEF", OUIDBNormalizer.normalize("ABC,DEF"));
        assertEquals("ABC, DEF", OUIDBNormalizer.normalize("ABC, DEF"));
        assertEquals("ABC, DEF", OUIDBNormalizer.normalize("ABC , DEF"));
        assertEquals("ABC. Ltd", OUIDBNormalizer.normalize("ABC,.Ltd"));
        assertEquals("ABC. Ltd", OUIDBNormalizer.normalize("ABC,.ltd"));
        assertEquals("ABC DEF", OUIDBNormalizer.normalize("ABC DEF"));
        assertEquals("ABC  DEF", OUIDBNormalizer.normalize("ABC  DEF"));
        assertEquals("ABC  DEF", OUIDBNormalizer.normalize("ABC   DEF"));
        assertEquals("ABC  DEF", OUIDBNormalizer.normalize("ABC    DEF"));
        assertEquals("ABC  DEF", OUIDBNormalizer.normalize(" ABC    DEF "));
    }

    @Test
    void normalizePrefix() {
        assertNull(OUIDBNormalizer.normalizePrefix(null));
        assertEquals("AABBCC", OUIDBNormalizer.normalizePrefix("AA-BB-CC"));
        assertEquals("AABBCC", OUIDBNormalizer.normalizePrefix("AABBCC"));
        assertEquals("AABBCC", OUIDBNormalizer.normalizePrefix("aa-bb-cc"));
        assertEquals("AABBCC", OUIDBNormalizer.normalizePrefix("aabbcc"));
    }

    @Test
    void normalizeOrganizationName() {
        assertNull(OUIDBNormalizer.normalizeOrganizationName(null));
        assertEquals("CocaCola Company", OUIDBNormalizer.normalizeOrganizationName("CocaCola Company"));
        assertEquals("CocaCola Inc.", OUIDBNormalizer.normalizeOrganizationName("CocaCola Inc."));
        assertEquals("CocaCola Inc.", OUIDBNormalizer.normalizeOrganizationName("CocaCola Inc.orporated"));
        assertEquals("CocaCola Inc.", OUIDBNormalizer.normalizeOrganizationName("CocaCola inc.orporated"));
    }
}