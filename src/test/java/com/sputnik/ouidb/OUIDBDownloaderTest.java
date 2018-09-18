package com.sputnik.ouidb;

import com.sputnik.ouidb.exception.NoRecordsFoundException;
import com.sputnik.ouidb.model.Organization;
import org.junit.jupiter.api.*;

import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OUIDBDownloaderTest {

    @Test
    void parsedDB() throws IOException {
        Map<String, Organization> db = new OUIDBDownloader().parseDb(new FileReader("src/test/resources/ouidb-test.txt"));

        // All OUI parsed
        assertEquals(9, db.size(), "Missing some OUIs from parsed result");

        // Private
        testOUI(db, "E4F14C", "Private", null, null, null);

        // Normal
        testOUI(db, "00093A", "Molex CMS", "5224 Katrine Avenue", "Downers Grove  IL  60515", "US");
        testOUI(db, "18BB26", "FN-Link Technology Limited", "A Building, HuiXin industial park, No 31, YongHe road, Fuyong town, Bao'an District", "Shenzhen  Guangdong  518100", "CN");

        // Normalize
        testOUI(db, "001F18", "Hakusan.Mfg.Co. Ltd", "Tomin-Kougyou-Ikebukuro BLD.5F", "Tosima Ward  Tokyo-Met.  171-0022", "JP");
        testOUI(db, "001E61", "ITEC GmbH", "Lassnitzthal 300", "A-8200  Gleisdorf", "AT");
        testOUI(db, "000037", "Oxford Metrics Limited", "Unit 8, 7 West Way", "United  Kingdom", "GB");
        testOUI(db, "1CB044", "Askey Computer Corp", "10F, No.119, JIANKANG Rd, ZHONGHE DIST", "NEW Taipei  Taiwan  23585", "TW");
        testOUI(db, "900372", "Longnan Junya Digital Technology Co. Ltd", "Champion Asia Road, Xinzhen industrial Park, Longnan national economic and technological development zone, Ganzhou city, JiangXi Province, China", "ganzhou  jiangxi  341700", "CN");

        // No addressLine 2
        testOUI(db, "000057", "Scitex Corporation Ltd", "P.O. Box 330", null, "IL");
    }

    @Test
    void parseDBEmpty() throws IOException {
        assertThrows(NoRecordsFoundException.class, () -> new OUIDBDownloader().parseDb(new FileReader("src/test/resources/ouidb-empty.txt")));
    }

    private void testOUI(Map<String, Organization> db, String prefix, String name, String addressLine1, String addressLine2, String countryCode) {
        assertNotNull(db.get(prefix));
        assertEquals(name, db.get(prefix).getName());
        assertEquals(addressLine1, db.get(prefix).getAddress().getLine1());
        assertEquals(addressLine2, db.get(prefix).getAddress().getLine2());
        assertEquals(countryCode, db.get(prefix).getAddress().getCountryCode());
    }
}