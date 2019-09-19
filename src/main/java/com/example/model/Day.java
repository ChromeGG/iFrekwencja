package com.example.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
public class Day {

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate datum;
    List<Subject> lesions;

}
