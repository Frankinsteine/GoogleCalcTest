package org.example;

import dev.failsafe.internal.util.Assert;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import org.apache.commons.exec.OS;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Objects;
import java.util.stream.Stream;

public class CalcTests {
    private static WebDriver driver;
    private static final String mainLink = "https://www.google.com/search?q=калькулятор";
    private static WebDriverWait wait;

    //parameters
    private static Stream<Arguments> calcParams() {
        return Stream.of(
                Arguments.of("10", "2", "5"),
                Arguments.of("1.5", "3", "0.5"),
                Arguments.of("1", ".", "Error"),
                Arguments.of("1", "()", "Error"),
                Arguments.of("1", "0", "Infinity")
        );
    }

    @DisplayName("Проверка гугл калькулятора")
    @Description("Проверка калькулятора на операцию деления")
    @ParameterizedTest
    @MethodSource("calcParams")
    public void test(String dividend, String divisor, String result) {
        inputNumber(dividend);
        checkResult(dividend, "Делимое введено неверно");
        click('÷');
        checkResult(dividend + " ÷", "Ошибка на этапе ввода знака деления");
        inputNumber(divisor);
        checkResult(dividend + " ÷ " + divisor, "Ошибка на этапе ввода делителя");
        click('=');
        checkResult(result, "Результат не верен");
    }

    @BeforeAll
    public static void before() {
        //set driver path
        if (OS.isFamilyWindows()) {
            //for Windows
            System.setProperty("webdriver.chromedriver.driver", "src/test/resources/chromedriver.exe");
        } else {
            //for Mac and Linux
            System.setProperty("webdriver.chromedriver.driver", "src/test/resources/chromedriver");
        }

        //webdriver init
        driver = new ChromeDriver();
        //browser option
        driver.manage().window().maximize();
        //timeouts
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(10));
        wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        //go to google calculator page
        driver.get(mainLink);
    }

    @AfterEach
    public void afterEach() {
        driver.navigate().refresh();
    }

    @AfterAll
    public static void afterAll() {
        driver.quit();
    }

    @Step("Ввод числа")
    public void inputNumber(String number) {
        for (char ch : number.toCharArray()) {
            click(ch);
        }
    }

    @Step("Клик по кнопке калькулятора")
    public void click(char button) {
        String xpath = String.format("//td/div/div[text()='%s']", button);
        WebElement element = driver.findElement(By.xpath(xpath));
        element.click();
    }

    @Step("Проверка результата")
    public void checkResult(String result, String errMsg) {
        WebElement element = driver.findElement(By.id("cwos"));
        Assert.isTrue(Objects.equals(element.getText(), result), errMsg);
    }
}