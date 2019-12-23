package com.example.controller;

import com.example.dataSupplier.DataSupplier;
import com.example.model.Subject;
import com.example.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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

//    @PostMapping
//    public String createStatistic(@ModelAttribute User user) {
//        //FIXME catch error when user is not correct:
//        dataSupplier.setUser(user);
//        dataSupplier.logIn();
//        dataSupplier.createStats();
//        dataSupplier.close();

//        return "redirect:statystyki";
//    }

    @PostMapping
    public RedirectView createStatistic(@ModelAttribute User user, HttpServletRequest req, RedirectAttributes redir) {
        //FIXME catch error when user is not correct:
        dataSupplier.setUser(user);
        dataSupplier.logIn();
        List<Subject> stats = dataSupplier.createStats();
        dataSupplier.close();

        RedirectView redirectView = new RedirectView("/statystyki",true);
        redir.addFlashAttribute("mapping1Form", stats);
        return redirectView;
    }

//    @PostMapping
//    public String controlMapping1(
//            @ModelAttribute User user,
//            @ModelAttribute("mapping1Form") final List<Subject> mapping1FormObject,
//            final BindingResult mapping1BindingResult,
//            final Model model,
//            final RedirectAttributes redirectAttributes) {
//        //FIXME catch error when user is not correct:
//        dataSupplier.setUser(user);
//        dataSupplier.logIn();
//        List<Subject> stats = dataSupplier.createStats();
//        dataSupplier.close();
//
//        redirectAttributes.addFlashAttribute("mapping1Form", mapping1FormObject);
//
//        return "redirect:statystyki";
//    }
}
