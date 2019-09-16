package com.example.dataSupplier;

import com.example.model.Subject;
import com.example.model.User;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.*;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Controller
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PhantomJSController implements DataSupplier {

    private static final LocalDateTime TODAY = LocalDateTime.now();

    private PhantomJSDriver driver;
    private User user;

    public PhantomJSController() {
        this.driver = new PhantomJSDriver();
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
        aggregateData();

    }

    private void aggregateData() {
        Elements selectedDays = getDaysFromUserRange();
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

            clickNextMonthButton(driver.findElementByClassName("nextButton"), 500);

            selectedDays.addAll(parseEndMonth(endDay, allDays));
        } else {
            selectedDays.addAll(parseStartMonth(startDay, allDays));

            do {
                clickNextMonthButton(driver.findElementByClassName("nextButton"), 500);
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

    private Set<Subject> initSubjects() {
        Set<Subject> subjects = new HashSet<>();
        String pageSource = driver.getPageSource();


        return null;
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

    private void clickNextMonthButton(WebElement nextButton, int i) {
        nextButton.click();
        wait(i);
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
}
