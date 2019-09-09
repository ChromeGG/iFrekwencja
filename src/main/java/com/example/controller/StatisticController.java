package com.example.controller;

import com.example.model.Subject;
import com.example.service.SubjectsList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/statystyki")
public class StatisticController {

    private SubjectsList subjectsList;

    @Autowired
    public StatisticController(SubjectsList subjectsList) {
        this.subjectsList = subjectsList;
    }

    @GetMapping
    public String statistic(Model model) {
        List<Subject> subjects = subjectsList.getSubjectList();
        model.addAttribute("subjectList", subjects);
        return "statistic";
    }

}
