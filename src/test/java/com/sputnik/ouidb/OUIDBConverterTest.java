package com.sputnik.ouidb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.javafaker.Faker;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sputnik.ouidb.model.Address;
import com.sputnik.ouidb.model.Organization;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class OUIDBConverterTest {

  private final Faker faker = new Faker();
  private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  @Test
  void convertToJson() {
    OUIDBConverter converter = new OUIDBConverter();
    Map<String, Organization> db = generateDb(5);
    String json = converter.convertToJson(db);

    List<Map<String, Object>> parsedJson = gson.fromJson(json, new TypeToken<List<Map<String, Object>>>() {
    }.getType());

    assertEquals(db.size(), parsedJson.size());
    for (String prefix : db.keySet()) {
      Organization org = db.get(prefix);

      Optional<Map<String, Object>> jsonEntryOpt = parsedJson.stream().filter(m -> m.get("prefix").equals(prefix)).findFirst();

      assertTrue(jsonEntryOpt.isPresent());
      Map<String, Object> jsonEntry = jsonEntryOpt.get();
      Map<String, Object> jsonOrg = (Map<String, Object>) jsonEntry.get("organization");
      assertEquals(org.getName(), jsonOrg.get("name"));
      Map<String, Object> jsonAddress = (Map<String, Object>) jsonOrg.get("address");
      assertEquals(org.getAddress().getLine1(), jsonAddress.get("line1"));
      assertEquals(org.getAddress().getLine2(), jsonAddress.get("line2"));
      assertEquals(org.getAddress().getCountryCode(), jsonAddress.get("countryCode"));
    }
  }

  @Test
  void convertEmptyDb() {
    OUIDBConverter converter = new OUIDBConverter();
    Map<String, Organization> db = new HashMap<>();
    String json = converter.convertToJson(db);
    assertEquals("[]", json);
  }

  @Test
  void convertNullDb() {
    OUIDBConverter converter = new OUIDBConverter();
    String json = converter.convertToJson(null);
    assertNull(json);
  }

  private Map<String, Organization> generateDb(int size) {
    Map<String, Organization> db = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    IntStream.range(0, size).mapToObj(i -> faker.internet().macAddress().replace(":", "").substring(0, 6))
      .forEach(prefix -> db.put(prefix, generateRandomOrganization()));

    return db;
  }

  private Organization generateRandomOrganization() {
    Organization organization = new Organization(faker.company().name());
    organization.setAddress(generateRandomAddress());

    return organization;
  }

  private Address generateRandomAddress() {
    Address address = new Address();
    address.setCountryCode(faker.address().countryCode());
    address.setLine1(faker.address().streetAddress());
    address.setLine2(faker.address().secondaryAddress());

    return address;
  }
}