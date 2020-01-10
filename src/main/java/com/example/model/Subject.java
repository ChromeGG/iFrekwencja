package com.example.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Subject {

    public Subject(String name) {
        this.name = name;
    }

    private String name;

    private double frequency;

    //positive
    private int present = 0; //obecnosc_0
    private int exemptPresent = 0; //obecnosc_9

    //negative
    private int absentExcused = 0; //obecnosc_1
    private int absent = 0; //obecnosc_3
    private int exemption = 0; //obecnosc_4

    //neutral
    private int belated = 0; //obecnosc_2
    private int notHappen = 0; // obecnosc_5
}
