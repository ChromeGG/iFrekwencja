package com.example.controller;

import com.example.model.User;
import com.example.dataSupplier.PhantomJSController;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/ifrekwencja")
public class HomeController {

    @Autowired
    private PhantomJSController phantomController;

    @GetMapping
    public String home(Model model) {
        phantomController.setupNewConnection();
        BufferedImage captcha = phantomController.getCaptchaImage();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream b64 = new Base64OutputStream(baos);
        try {
            ImageIO.write(captcha, "png", b64);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String captchaImageInBase64;
        captchaImageInBase64 = baos.toString(StandardCharsets.UTF_8);

        model.addAttribute("captchaImage", captchaImageInBase64);
        model.addAttribute("user", new User());
        return "index";
    }

    @PostMapping
    public String login(@ModelAttribute User user){
        phantomController.logIn(user);
        phantomController.goToData();
        phantomController.createStatistic();
        phantomController.close();
        return "redirect:statystyki";
    }
}
