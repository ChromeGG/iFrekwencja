package com.example.controller;

import com.example.dataSupplier.DataSupplier;
import com.example.dataSupplier.PhantomJSControllerOld;
import com.example.model.User;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
    private PhantomJSControllerOld phantomController;

    private DataSupplier dataSupplier;

    @Autowired
    public HomeController(DataSupplier dataSupplier) {
        this.dataSupplier = dataSupplier;
    }

    @GetMapping
    public String home(Model model) {
        phantomController.setupNewConnection();
        dataSupplier.setupNewConnection();
        String captchaInBase64 = dataSupplier.getCaptchaString();

        //To Remove if captchaInBase64 is correct

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
    public String createStatistic(@ModelAttribute User user) {
        //FIXME catch error when user is not correct:
        //-asd
        dataSupplier.logIn(user);

        dataSupplier.createStats();

        phantomController.logIn(user);
        phantomController.goToData();
        phantomController.createStatistic();
        phantomController.close();
        return "redirect:statystyki";
    }
}
