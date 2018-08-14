package com.sputnik.ouidb.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OUI {
    String prefix;
    Organization organization;
}
