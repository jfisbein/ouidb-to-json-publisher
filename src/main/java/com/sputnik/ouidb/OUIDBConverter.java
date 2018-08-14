package com.sputnik.ouidb;

import com.google.gson.GsonBuilder;
import com.sputnik.ouidb.model.OUI;
import com.sputnik.ouidb.model.Organization;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OUIDBConverter {
    public String convertToJson(Map<String, Organization> db) {
        List<OUI> ouiList = db.entrySet().stream()
                .map(entry -> new OUI(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(OUI::getPrefix))
                .collect(Collectors.toList());

        return new GsonBuilder().setPrettyPrinting().create().toJson(ouiList);
    }
}
