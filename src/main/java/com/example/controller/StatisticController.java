package com.example.controller;

import com.example.model.Subject;
import com.example.service.CompleteSubjects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/statystyki")
public class StatisticController {

    private CompleteSubjects completeSubjects;

    @Autowired
    public StatisticController(CompleteSubjects completeSubjects) {
        this.completeSubjects = completeSubjects;
    }

    @GetMapping
    public String statistic(Model model) {
        List<Subject> subjects = completeSubjects.getSubjectList();
        model.addAttribute("subjectList", subjects);
        return "html/statistic";
    }

}
