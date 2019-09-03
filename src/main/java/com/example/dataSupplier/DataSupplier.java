package com.example.dataSupplier;

import java.awt.image.BufferedImage;

public interface DataSupplier {

    void setupNewConnection();
    BufferedImage getCaptchaImage();
}
