package com.example.dataSupplier;

import com.example.model.User;

public interface DataSupplier {

    void setupNewConnection();

    String getCaptchaString();

    void logIn(User user);

    void createStats();
}
