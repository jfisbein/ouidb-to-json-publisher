package com.sputnik.ouidb.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class Address implements Serializable {

    private String line1;

    private String line2;

    private String countryCode;
}

