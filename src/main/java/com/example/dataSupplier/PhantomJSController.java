package com.example.dataSupplier;

import com.example.model.Day;
import com.example.model.Lesion;
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
    public List<Subject> createStats() {
        goToUserFrequency();
        goToStartMonth();
        List<Subject> completeSubjectList = aggregateData();
        sendSubjectSet(completeSubjectList);
        return completeSubjectList;
    }

    private void sendSubjectSet(List<Subject> completeSubjectList) {
        frontData.receiveData(completeSubjectList, user);
    }

    private List<Subject> aggregateData() {
        List<Day> selectedDays = getDaysFromUserRange();
        Set<Subject> subjects = initSubjects(selectedDays);
        List<Subject> completeSubjectList = assignFrequencyToSubjects(subjects, selectedDays);
        return completeSubjectList;
    }

    private List<Subject> assignFrequencyToSubjects(Set<Subject> subjects, List<Day> selectedDays) {
        List<Subject> completeSubjectsList = new ArrayList<>(subjects);
        Map<String, Subject> map = subjects.stream().collect(Collectors.toMap(Subject::getName, e -> e));

        for (Day day : selectedDays) {
            List<Lesion> lesions = day.getLesions();
            for (Lesion lesion : lesions) {
                String nameLesion = lesion.getName();
                int category = lesion.getCategory();

                Subject subject = map.get(nameLesion);

                switch (category) {
                    case 0:
                        subject.setObecny(subject.getObecny() + 1);
                        break;
                    case 1:
                        subject.setNieobecnyUsprawiedliwiony(subject.getNieobecnyUsprawiedliwiony() + 1);
                        break;
                    case 2:
                        subject.setSpozniony(subject.getSpozniony() + 1);
                        break;
                    case 3:
                        subject.setNieobecny(subject.getNieobecny() + 1);
                        break;
                    case 4:
                        subject.setZwolnienie(subject.getZwolnienie() + 1);
                        break;
                    case 5:
                        subject.setNieOdbylySie(subject.getNieOdbylySie() + 1);
                        break;
                    case 9:
                        subject.setZwolnionyObecny(subject.getZwolnionyObecny() + 1);
                        break;
                    default:
                        System.err.println("Cos sie zjebalo");

                }
            }
        }


        return completeSubjectsList;
    }


    private Set<Subject> initSubjects(List<Day> days) {
        Set<Subject> subjectSet = new HashSet<>();

        for (Day day : days) {
            List<Lesion> lesions = day.getLesions();
            for (Lesion lesion : lesions) {
                String lesionName = lesion.getName();
                Subject subject = new Subject(lesionName);
                subjectSet.add(subject);
            }
        }
        return subjectSet;
    }

    private List<Day> getDaysFromUserRange() {
        String pageSource = driver.getPageSource();
        Document htmlDocument = Jsoup.parse(pageSource);

        Elements htmlAllDays = htmlDocument.getElementsByClass("dzienMiesiaca");
        int startDay = user.getSinceWhen().getDayOfMonth();
        int endDay = user.getUntilWhen().getDayOfMonth();

        int differenceBetweenMonths = user.getUntilWhen().getMonthValue() - user.getSinceWhen().getMonthValue();

        List<Day> selectedDays = new ArrayList<>();

        if (differenceBetweenMonths <= 0) {
            selectedDays.addAll(parseOnlyOneMonth(startDay, endDay, htmlAllDays));
        } else if (differenceBetweenMonths == 1) {
            selectedDays.addAll(parseStartMonth(startDay, htmlAllDays));

            clickNextMonthButton(driver.findElementByClassName("nextButton"));

            selectedDays.addAll(parseEndMonth(endDay, htmlAllDays));
        } else {
            selectedDays.addAll(parseStartMonth(startDay, htmlAllDays));

            do {
                clickNextMonthButton(driver.findElementByClassName("nextButton"));
                selectedDays.addAll(parseFullMonth(htmlAllDays));
                differenceBetweenMonths--;
            } while (differenceBetweenMonths <= 1);

            selectedDays.addAll(parseEndMonth(endDay, htmlAllDays));

        }

        return selectedDays;

    }

    private List<Day> parseFullMonth(Elements allDays) {
        List<Day> days = new ArrayList<>();

        for (Element htmlDay : allDays) {
            int dayNumber = getDayNumber(htmlDay);

            Day day = new Day();
            day.setDayOfMonth(dayNumber);


            //FIXME (Na przyszlosc)
            // Dni maja czasami nagłowki dni, ale sa puste np.30 Wrzesien
            // Więc może sie bugować i dodawać pusty dzień
            if (dayNumber != 0) {
                List<Lesion> lesionsOfDay = getLesionsFromDay(htmlDay);
                day.setLesions(lesionsOfDay);
            }
        }

        return days;
    }


    @SuppressWarnings("Duplicates")
    private List<Day> parseEndMonth(int endDay, Elements allDays) {
        List<Day> days = new ArrayList<>();

        for (Element htmlDay : allDays) {
            int dayNumber = getDayNumber(htmlDay);

            Day day = new Day();
            day.setDayOfMonth(dayNumber);

            if (dayNumber <= endDay) {
                List<Lesion> lesionsOfDay = getLesionsFromDay(htmlDay);
                day.setLesions(lesionsOfDay);
                days.add(day);
            }
        }

        return days;
    }

    @SuppressWarnings("Duplicates")
    private List<Day> parseStartMonth(int startDay, Elements htmlDays) {
        List<Day> days = new ArrayList<>();
        for (Element htmlDay : htmlDays) {
            int dayNumber = getDayNumber(htmlDay);

            Day day = new Day();
            day.setDayOfMonth(dayNumber);

            if (dayNumber >= startDay) {
                List<Lesion> lesionsOfDay = getLesionsFromDay(htmlDay);
                day.setLesions(lesionsOfDay);
                days.add(day);
            }
        }

        return days;
    }


    private List<Day> parseOnlyOneMonth(int startDay, int endDay, Elements htmlAllDays) {
        List<Day> days = new ArrayList<>();
        for (Element htmlDay : htmlAllDays) {
            int dayNumber = getDayNumber(htmlDay);

            Day day = new Day();
            day.setDayOfMonth(dayNumber);

            if (dayNumber >= startDay && (dayNumber < endDay || dayNumber == endDay)) {
                List<Lesion> lesionsOfDay = getLesionsFromDay(htmlDay);
                day.setLesions(lesionsOfDay);
                days.add(day);
            }
        }

        return days;
    }

    private List<Lesion> getLesionsFromDay(Element htmlDay) {
        List<Lesion> lesionsOfDay = new ArrayList<>();

        for (Element lesionHtml : htmlDay.children()) {
            String attributesString = lesionHtml.attr("class");
            //TODO add "ferie" to ignored lesions
            if (attributesString.contains("dzienMiesiaca") || attributesString.contains("okienko")) {
                //ignore day
            } else {
                String lesionName = lesionHtml.text().substring(4);

                int attendanceCategory = Integer.parseInt(attributesString.replaceAll("[^0-9]", ""));
                Lesion lesion = new Lesion(lesionName, attendanceCategory);
                lesionsOfDay.add(lesion);
            }
        }
        return lesionsOfDay;
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
