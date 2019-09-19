package com.example.controller;

import com.example.model.Subject;
import com.example.model.User;
import com.example.service.FrontData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/statystyki")
public class StatisticController {

    private FrontData frontData;

    @Autowired
    public StatisticController(FrontData frontData) {
        this.frontData = frontData;
    }

    @GetMapping
    public String statistic(Model model) {
        List<Subject> subjects = frontData.getSubjectList();
        User user = frontData.getUser();
        model.addAttribute("subjectsList", subjects);
        model.addAttribute("user", user);
        return "html/statistic";
    }
}
