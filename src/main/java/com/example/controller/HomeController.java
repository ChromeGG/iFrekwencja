package com.example.controller;

import com.example.dataSupplier.DataSupplier;
import com.example.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ifrekwencja")
public class HomeController {

    private DataSupplier dataSupplier;

    @Autowired
    public HomeController(DataSupplier dataSupplier) {
        this.dataSupplier = dataSupplier;
    }

    @GetMapping
    public String home(Model model) {
        dataSupplier.setupNewConnection();
        String captchaInBase64 = dataSupplier.getCaptchaString();

        model.addAttribute("captchaImage", captchaInBase64);
        model.addAttribute("user", new User());
        return "index";
    }

    @PostMapping
    public String createStatistic(@ModelAttribute User user) {
        //FIXME catch error when user is not correct:
        dataSupplier.setUser(user);
        dataSupplier.logIn();
        dataSupplier.createStats();
        dataSupplier.close();
//
//        phantomController.logIn(user);
//        phantomController.goToData();
//        phantomController.createStatistic();
//        phantomController.close();
        return "redirect:statystyki";
    }
}
