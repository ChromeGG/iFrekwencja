package com.example.service;

import com.example.model.Subject;
import com.example.model.User;
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
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE) //if it's commented, it's working, else not
public class FrontData {

    User user;
    List<Subject> subjectList = new ArrayList<>();

    public void receiveData(List<Subject> subjectList, User user) {
        this.subjectList = subjectList;
        this.user = user;
        addFrequency();
    }

    private void addFrequency() {
        for (Subject subject : subjectList) {
            double positiveHours = subject.getObecny() + subject.getZwolnionyObecny();
            double negativeHours = subject.getNieobecny() + subject.getNieobecnyUsprawiedliwiony() + subject.getZwolnienie();
            double allMatterHours = positiveHours + negativeHours;
            double frequency;
            double formattedFrequency;

            if (negativeHours == 0) {
                frequency = 100;
            } else if (positiveHours == 0) {
                frequency = 0;
            } else {
                frequency = positiveHours * 100 / allMatterHours;
            }
            formattedFrequency = Double.parseDouble(String.format("%.2f", frequency));

            System.out.println(subject.getName() + " : " + formattedFrequency);
            subject.setFrequency(formattedFrequency);
        }
    }
}
