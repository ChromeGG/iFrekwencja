package com.example.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Subject {

    private String name;

    private double frequency;

    //positive
    private int obecny = 0; //obecnosc_0
    private int zwolnionyObecny = 0; //obecnosc_9

    //negative
    private int nieobecnyUsprawiedliwiony = 0; //obecnosc_1
    private int nieobecny = 0; //obecnosc_3
    private int zwolnienie = 0; //obecnosc_4

    //neutral
    private int spozniony = 0; //obecnosc_2
    private int nieOdbylySie = 0; // obecnosc_5
}
