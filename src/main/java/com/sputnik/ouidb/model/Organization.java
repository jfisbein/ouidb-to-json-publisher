package com.sputnik.ouidb.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class Organization implements Serializable {
    private String name;
    private Address address;
}

