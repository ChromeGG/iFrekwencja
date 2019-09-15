package com.example.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    String schoolName;
    String name;
    String password;
    String captcha;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate sinceWhen;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate untilWhen;

}