package com.example.dataSupplier;

import com.example.model.Subject;
import com.example.model.User;
import com.example.service.FrontData;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.*;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PhantomJSController implements DataSupplier {

    private static final LocalDateTime TODAY = LocalDateTime.now();

    private PhantomJSDriver driver;
    private User user;

    private final FrontData frontData;


    @Autowired
    public PhantomJSController(FrontData frontData) {
        this.driver = new PhantomJSDriver();
        this.frontData = frontData;
    }

    @Override
    public void setupNewConnection() {
        driver.get("https://iuczniowie.progman.pl/idziennik");
        driver.manage().window().setSize(new Dimension(1920, 1080));
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    }

    @Override
    public String getCaptchaString() {

        //TODO find better way to wait for captcha image
        wait(400);

        byte[] arrScreen = driver.getScreenshotAs(OutputType.BYTES);
        BufferedImage imageScreen = null;

        try {
            imageScreen = ImageIO.read(new ByteArrayInputStream(arrScreen));
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedImage imgCap = getCaptchaImage(imageScreen);


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream b64 = new Base64OutputStream(baos);
        try {
            ImageIO.write(imgCap, "png", b64);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String captchaImageInBase64 = baos.toString(StandardCharsets.UTF_8);
//        captchaImageInBase64;

        return captchaImageInBase64;
    }

    private BufferedImage getCaptchaImage(BufferedImage imageScreen) {
        WebElement cap = driver.findElementById("imgCaptcha");

        Dimension capDimension = cap.getSize();
        Point capLocation = cap.getLocation();

        return imageScreen.getSubimage(capLocation.x, capLocation.y, capDimension.width, capDimension.height);
    }

    @Override
    public void logIn() {

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

    @Override
    public void createStats() {
        goToUserFrequency();
        goToStartMonth();
        List<Subject> completeSubjectList = aggregateData();
        sendSubjectSet(completeSubjectList);
    }

    private void sendSubjectSet(List<Subject> completeSubjectList) {

        frontData.receiveData(completeSubjectList, user);
    }

    private List<Subject> aggregateData() {
        Elements selectedDays = getDaysFromUserRange();
        Set<Subject> subjects = getSubjects(selectedDays);
        return fillSubjectsData(subjects, selectedDays);
    }

    private List<Subject> fillSubjectsData(Set<Subject> subjects, Elements selectedDays) {
        List<Subject> completeSubjectsList = new ArrayList<>(subjects);
        Map<String, Subject> map = subjects.stream().collect(Collectors.toMap(Subject::getName, e -> e));

        for (Element subjectElement : selectedDays) {
            Elements children = subjectElement.children();
            Iterator<Element> iterator = children.iterator();
            iterator.next(); //shift date header (example: 3 September)
            while (iterator.hasNext()) {
                Element element = iterator.next();
                String classString = element.attr("class");
                if (classString.equals("okienko")) {
                    break;
                }
                char presenceCategory = classString.charAt(classString.length() - 1);

                String subjectName = "Erroroo";
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
        return completeSubjectsList;
    }

    private Set<Subject> getSubjects(Elements days) {
        Set<Subject> subjectSet = new HashSet<>();

        for (Element element : days) {
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

        return subjectSet;
    }

    private Elements getDaysFromUserRange() {
        String pageSource = driver.getPageSource();
        Document htmlDocument = Jsoup.parse(pageSource);

        Elements allDays = htmlDocument.getElementsByClass("dzienMiesiaca");
        int startDay = user.getSinceWhen().getDayOfMonth();
        int endDay = user.getUntilWhen().getDayOfMonth();

        int differenceBetweenMonths = user.getUntilWhen().getMonthValue() - user.getSinceWhen().getMonthValue();

        Elements selectedDays = new Elements();

        if (differenceBetweenMonths == 0) {
            selectedDays = parseOnlyOneMonth(startDay, endDay, allDays);
        } else if (differenceBetweenMonths == 1) {
            selectedDays.addAll(parseStartMonth(startDay, allDays));

            clickNextMonthButton(driver.findElementByClassName("nextButton"));

            selectedDays.addAll(parseEndMonth(endDay, allDays));
        } else {
            selectedDays.addAll(parseStartMonth(startDay, allDays));

            do {
                clickNextMonthButton(driver.findElementByClassName("nextButton"));
                selectedDays.addAll(parseFullMonth(allDays));
                differenceBetweenMonths--;
            } while (differenceBetweenMonths <= 1);


            selectedDays.addAll(parseEndMonth(endDay, allDays));

        }

        return selectedDays;

    }

    private Elements parseFullMonth(Elements allDays) {
        Elements aggregatedDays = new Elements();

        for (Element day : allDays) {
            int dayNumber = getDayNumber(day);

            //FIXME (Na przyszlosc)
            // Dni maja czasami nagłowki dni, ale sa puste np.30 Wrzesien
            // Więc może sie bugować i dodawać pusty dzień
            if (dayNumber != 0) {
                aggregatedDays.add(day);
            }
        }

        return aggregatedDays;
    }


    private Elements parseEndMonth(int endDay, Elements allDays) {
        Elements aggregatedDays = new Elements();
        for (Element day : allDays) {
            int dayNumber = getDayNumber(day);

            if (dayNumber <= endDay) {
                aggregatedDays.add(day);
            }
        }

        return aggregatedDays;
    }

    private Elements parseStartMonth(int startDay, Elements allDays) {
        Elements aggregatedDays = new Elements();
        for (Element day : allDays) {
            int dayNumber = getDayNumber(day);

            if (dayNumber >= startDay) {
                aggregatedDays.add(day);
            }
        }

        return aggregatedDays;
    }


    private Elements parseOnlyOneMonth(int startDay, int endDay, Elements allDays) {
        Elements aggregatedDays = new Elements();
        for (Element day : allDays) {
            int dayNumber = getDayNumber(day);

            if (dayNumber >= startDay && (dayNumber < endDay || dayNumber == endDay)) {
                aggregatedDays.add(day);
            }
        }

        return aggregatedDays;
    }

    private void goToUserFrequency() {
        WebElement obecnosciButton = driver.findElementByLinkText("Obecności");
        obecnosciButton.click();

        wait(600);

        List<WebElement> viewsButtons = driver.findElementsByClassName("label");
        WebElement monthlyViewButton = viewsButtons.get(1);
        monthlyViewButton.click();

        wait(600);
    }

    private void goToStartMonth() {
        int prevoiusMonthClicks = TODAY.getMonthValue() - user.getSinceWhen().getMonthValue();

        while (prevoiusMonthClicks > 0) {
            driver.findElementByClassName("prevButton").click();
            prevoiusMonthClicks--;
            wait(600);
        }
    }


    //TODO remove when app will be ready
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

    private int getDayNumber(Element day) {
        String id;
        id = day.id(); //example: dzien_3, but sometimes id = ""

        int dayNumber = 0;
        if (id.length() > 1) {
            dayNumber = Integer.parseInt(id.replaceAll("[^0-9]", "")); //returns only numbers from id

        }
        return dayNumber;
    }

    private void clickNextMonthButton(WebElement nextButton) {
        nextButton.click();
        wait(500);
    }

    private void wait(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public void close() {
        driver.close();
    }
}
