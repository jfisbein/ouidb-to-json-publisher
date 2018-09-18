package com.sputnik.ouidb.model;

import lombok.Data;

@Data
public class OUI {
    private final String prefix;
    private final Organization organization;
}
