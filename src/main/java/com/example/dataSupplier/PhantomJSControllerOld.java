package com.example.dataSupplier;

import com.example.model.Subject;
import com.example.model.User;
import com.example.service.SubjectsList;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PhantomJSControllerOld {

    private static final LocalDateTime TODAY = LocalDateTime.now();

    private PhantomJSDriver driver = new PhantomJSDriver();
    private LocalDateTime START_OF_YEAR;

    private SubjectsList subjectList;

    @Autowired
    public PhantomJSControllerOld(SubjectsList subjectList) {
        this.subjectList = subjectList;
    }

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


        return imageScreen.getSubimage(capLocation.x, capLocation.y, capDimension.width, capDimension.height);
    }

    public void logIn(User user) {
//        WebElement nazwaSzkoly = driver.findElementById("NazwaSzkoly");
//        WebElement userName = driver.findElementById("UserName");
//        WebElement password = driver.findElementById("Password");
//        WebElement captcha = driver.findElementById("captcha");
//        WebElement btnLogin = driver.findElementByClassName("btnLogin");
//
//        nazwaSzkoly.sendKeys(user.getSchoolName());
//        userName.sendKeys(user.getName());
//        password.sendKeys(user.getPassword());
//        captcha.sendKeys(user.getCaptcha());
//
//        btnLogin.click();
    }

    private void makeSS() {
//        TakesScreenshot ts = driver;
//        File source = ts.getScreenshotAs(OutputType.FILE);
//        try {
//            FileUtils.copyFile(source, new File("screen.png"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        System.out.println("Screenshot created");
    }

    public void goToData() {
//        WebElement btn_obecnosci = driver.findElementByXPath("//*[@id=\"btn_obecnosci\"]/createStatistic");
//        btn_obecnosci.click();
//
//        new WebDriverWait(driver, 20).until(
//                webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
//
//        try {
//            Thread.sleep(600);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//
//        // TUTAJ TRZEBA COs WYMYSLIC ZEBY KLIKAL JAK BEDZIE KONIEC LADOWANIA
//
//        new WebDriverWait(driver, 20).until(
//                webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
//
//        try {
//            Thread.sleep(600);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

    }

    public void createStatistic() {
        String pageSource = driver.getPageSource();
        Document htmlDocument = Jsoup.parse(pageSource);

        Elements allDays = htmlDocument.getElementsByClass("dzienMiesiaca");
        Elements selectedDays = new Elements();


        for (Element element : allDays) {
            Element dzienMiesiacaHead = element.getElementsByClass("dzienMiesiacaHead").first();
            char dayOfMonth = dzienMiesiacaHead.text().charAt(0);
            int dayOfMonthInt = Integer.parseInt(String.valueOf(dayOfMonth));
            if (dayOfMonthInt < TODAY.getDayOfMonth()) {
                selectedDays.add(element);
            }
        }

        //remove bugged day (2 september)
        selectedDays.remove(0);

        Set<Subject> subjectSet = new HashSet<>();

        for (Element element : selectedDays) {
            Elements children = element.children();
            Iterator<Element> iterator = children.iterator();
            iterator.next(); //shift date header (example: 3 September)
            while (iterator.hasNext()) {
                try {
                    String subjectName = iterator.next().text().substring(4);
                    Subject subject = new Subject();
                    subject.setName(subjectName);
                    subjectSet.add(subject);
                } catch (StringIndexOutOfBoundsException ex) {
                    ex.getStackTrace();
                }
            }
        }

        List<Subject> localSubjectList = new ArrayList<>(subjectSet);
        Map<String, Subject> map = subjectSet.stream().collect(Collectors.toMap(Subject::getName, e -> e));

        for (Element subjectElement : selectedDays) {
            Elements children = subjectElement.children();
            Iterator<Element> iterator = children.iterator();
            iterator.next();
            while (iterator.hasNext()) {
                Element element = iterator.next();
                String classString = element.attr("class");
                char presenceCategory = classString.charAt(classString.length() - 1);


                String subjectName = "Error";
                try {
                    subjectName = element.text().substring(4);
                } catch (StringIndexOutOfBoundsException ex) {
                    ex.printStackTrace();
                }


                Subject subject = map.get(subjectName);


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
        }

        subjectSet.forEach(System.out::println);

        subjectList.transmitList(localSubjectList);
    }

    public void close() {
        driver.close();
    }
}
