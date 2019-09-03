package com.example.dataSupplier;

import com.example.model.Subject;
import com.example.model.User;
import com.example.service.SubjectsList;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PhantomJSController implements DataSupplier {

    private PhantomJSDriver driver = new PhantomJSDriver();
    private LocalDateTime START_OF_YEAR;

    private SubjectsList subjectList;

    @Autowired
    public PhantomJSController(SubjectsList subjectList) {

        this.subjectList = subjectList;
    }

    @Override
    public void setupNewConnection() {
        driver.get("https://iuczniowie.progman.pl/idziennik");
        driver.manage().window().setSize(new Dimension(1920, 1080));
    }

    public BufferedImage getCaptchaImage() {

        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        byte[] arrScreen = driver.getScreenshotAs(OutputType.BYTES);
        BufferedImage imageScreen = null;
        try {
            imageScreen = ImageIO.read(new ByteArrayInputStream(arrScreen));
        } catch (IOException e) {
            e.printStackTrace();
        }
        WebElement cap = driver.findElementById("imgCaptcha");

        Dimension capDimension = cap.getSize();
        Point capLocation = cap.getLocation();


        BufferedImage imgCap = imageScreen.getSubimage(capLocation.x, capLocation.y, capDimension.width, capDimension.height);
        return imgCap;
    }

    public void logIn(User user) {
        WebElement nazwaSzkoly = driver.findElementById("NazwaSzkoly");
        WebElement userName = driver.findElementById("UserName");
        WebElement password = driver.findElementById("Password");
        WebElement captcha = driver.findElementById("captcha");
        WebElement btnLogin = driver.findElementByClassName("btnLogin");

        nazwaSzkoly.sendKeys(user.getSchoolName());
        userName.sendKeys(user.getName());
        password.sendKeys(user.getPassword());
        captcha.sendKeys(user.getCaptcha());

        btnLogin.click();
    }

    private void makeSS() {
        TakesScreenshot ts = driver;
        File source = ts.getScreenshotAs(OutputType.FILE);
        try {
            FileUtils.copyFile(source, new File("screen.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Screenshot created");
    }

    public void goToData() {
        WebElement btn_obecnosci = driver.findElementByXPath("//*[@id=\"btn_obecnosci\"]/a");
        btn_obecnosci.click();

        new WebDriverWait(driver, 20).until(
                webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));

        try {
            Thread.sleep(600);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        WebElement widokMiesieczny = driver.findElementByXPath("//*[@id=\"wiadomosci_main\"]/tbody/tr[1]/td/div[2]/div/div");
//        widokMiesieczny.click();

        // TUTAJ TRZEBA COs WYMYSLIC ZEBY KLIKAL JAK BEDZIE KONIEC LADOWANIA

        new WebDriverWait(driver, 20).until(
                webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));

        try {
            Thread.sleep(600);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        WebElement prevButton = driver.findElementByXPath("//*[@id=\"tabelaMainContent\"]/tbody/tr[1]/td/div/div[2]/div[1]");
//        prevButton.click();
//        try {
//            Thread.sleep(600);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        prevButton.click();
//
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

//        makeSS();
    }

    public void createStatistic() {
        List<WebElement> dirtyAllList = driver.findElementsByClassName("przedmiot");
        List<WebElement> clearWebElementsList = new ArrayList<>();

        for (WebElement element : dirtyAllList) {
            String text = element.getText();
            if (!(text.length() == 0 || text.contains("Ferie"))) {
                clearWebElementsList.add(element);
            }
        }

        Set<Subject> subjectSet = new HashSet<>();

        //subject initialization
        for (WebElement element : clearWebElementsList) {
//            char presenceCategory = element.getAttribute("class").charAt(element.getAttribute("class").length()-1);
            String subjectName = element.getText().substring(4);
            Subject subject = new Subject();
            subject.setName(subjectName);
            subjectSet.add(subject);
        }

        List<Subject> localSubjectList = new ArrayList<>(subjectSet);
        Map<String, Subject> map = subjectSet.stream().collect(Collectors.toMap(Subject::getName, e -> e));

        for (WebElement element : clearWebElementsList) {
            char presenceCategory = element.getAttribute("class").charAt(element.getAttribute("class").length() - 1);

            String subjectNameFromElement = element.getText().substring(4);

            Subject subject = map.get(subjectNameFromElement);

            switch (presenceCategory) {
                case '0':
                    subject.setObecny(subject.getObecny() + 1);
                    break;
                case '1':
                    subject.setNieobecnyUsprawiedliwiony(subject.getNieobecnyUsprawiedliwiony() + 1);
                    break;
                case '2':
                    subject.setSpozniony(subject.getSpozniony() + 1);
                    break;
                case '3':
                    subject.setNieobecny(subject.getNieobecny() + 1);
                    break;
                case '4':
                    subject.setZwolnienie(subject.getZwolnienie() + 1);
                    break;
                case '5':
                    subject.setNieOdbylySie(subject.getNieOdbylySie() + 1);
                    break;
                case '9':
                    subject.setZwolnionyObecny(subject.getZwolnionyObecny() + 1);
                    break;
                default:
                    System.err.println("Cos sie zjebalo");

            }
        }

//        subjectList.getSubjectList().addAll(localSubjectList);
        subjectList.transmitList(localSubjectList);
    }

    public void close() {
        driver.close();
    }
}
