package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IdziennikFrekwencjaApplication {

    public static void main(String[] args) {
        System.setProperty("phantomjs.binary.path", "lib/PhantomJS-2.1.1-win64x/phantomjs.exe");
        SpringApplication.run(IdziennikFrekwencjaApplication.class, args);
    }

}
