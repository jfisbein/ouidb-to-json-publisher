package com.sputnik.ouidb;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sputnik.ouidb.model.OUI;
import com.sputnik.ouidb.model.Organization;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

public class OUIDBConverter {
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public String convertToJson(Map<String, Organization> db) {
        return Optional.ofNullable(db).map(notNullDb -> notNullDb.entrySet().parallelStream()
                .map(entry -> new OUI(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(OUI::getPrefix))
                .collect(collectingAndThen(toList(), gson::toJson)))
                .orElse(null);
    }
}
