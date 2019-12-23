package com.example.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Day {


    int DayOfMonth;
    List<Lesion> lesions = new ArrayList<>();

}
