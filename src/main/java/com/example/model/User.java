package com.example.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    String schoolName;
    String name;
    String password;
    String captcha;
//    LocalDate sinceWhen;
//    LocalDate untilWhen;

}
