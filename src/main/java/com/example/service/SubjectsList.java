package com.example.service;

import com.example.model.Subject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Data
@NoArgsConstructor
@AllArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SubjectsList {

    List<Subject> subjectList = new ArrayList<>();

    public void transmitList(List<Subject> localSubjectList) {
        this.subjectList = localSubjectList;
        addFrequency();
    }

    private void addFrequency() {
        for (Subject subject : subjectList) {
            double positiveHours = subject.getObecny() + subject.getZwolnionyObecny();
            double negativeHours = subject.getNieobecny() + subject.getNieobecnyUsprawiedliwiony() + subject.getZwolnienie();
            double allMatterHours = positiveHours + negativeHours;
            double frequency;
            double formetedFrequency;

            if (negativeHours == 0) {
                frequency = 100;
            } else if (positiveHours == 0) {
                frequency = 0;
            } else {
                frequency = positiveHours * 100 / allMatterHours;
            }
            formetedFrequency = Double.parseDouble(String.format("%.2f", frequency));

            System.out.println(subject.getName() + " : " + formetedFrequency);
            subject.setFrequency(formetedFrequency);
        }
    }
}
