package com.example.dataSupplier;

import com.example.model.Subject;
import com.example.model.User;

import java.util.List;

public interface DataSupplier {

    void setupNewConnection();

    String getCaptchaString();

    void logIn();

    List<Subject> createStats();

    void setUser(User user);

    void close();
}
