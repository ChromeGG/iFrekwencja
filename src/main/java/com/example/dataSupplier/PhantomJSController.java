package com.example.dataSupplier;

import com.example.model.User;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@Controller
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PhantomJSController implements DataSupplier {

    private PhantomJSDriver driver;

    public PhantomJSController() {
        this.driver = new PhantomJSDriver();
    }

    @Override
    public void setupNewConnection() {
        driver.get("https://iuczniowie.progman.pl/idziennik");
        driver.manage().window().setSize(new Dimension(1920, 1080));
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

    @Override
    public void createStats() {
        setupRangeToCount();
    }

    private void setupRangeToCount() {
        private
    }

    private void wait(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
