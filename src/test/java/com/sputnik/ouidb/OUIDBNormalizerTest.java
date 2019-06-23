package com.sputnik.ouidb;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class OUIDBNormalizerTest {

    @Test
    void normalize() {
        OUIDBDownloader downloader = new OUIDBDownloader();
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
}