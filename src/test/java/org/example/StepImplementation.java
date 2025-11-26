package org.example;

import com.thoughtworks.gauge.Step;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

// Log4j imports
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StepImplementation {

    // Log4j LOGGER ADDED HERE
    private static final Logger log = LogManager.getLogger(StepImplementation.class);

    private WebDriver driver;

    @Step("Chrome açılır")
    public void openChrome() {
        log.info("Starting Chrome...");

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-infobars");
        options.addArguments("--start-maximized");

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();

        log.info("Chrome launched successfully.");
    }

    @Step("Hepsiburada ana sayfasna gidilir")
    public void goToHepsiburada() {
        log.info("Navigating to Hepsiburada homepage...");

        driver.get("https://www.hepsiburada.com");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        try {
            WebElement cookieButton = wait.until(
                    ExpectedConditions.elementToBeClickable(By.id("onetrust-accept-btn-handler"))
            );
            cookieButton.click();
            log.info("Cookie popup closed.");
        } catch (Exception ignored) {
            log.warn("Cookie popup NOT found. Continuing.");
        }

        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
    }

    @Step("Search box'a <text> yazılır ve arama yapılır")
    public void searchInHepsiburada(String text) {
        log.info("Searching for: " + text);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        WebElement headerSearch = wait.until(
                ExpectedConditions.elementToBeClickable(LocatorRepository.by("txt_searchbox"))
        );

        new Actions(driver)
                .moveToElement(headerSearch)
                .click()
                .pause(Duration.ofMillis(800))
                .sendKeys(text)
                .sendKeys(Keys.ENTER)
                .perform();

        log.info("Search executed: " + text);

        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
    }

    @Step("Arama sonucunda bilgisayar metni kontrol edilir")
    public void verifySearchResultText() {
        log.info("Verifying search result header...");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        WebElement resultHeader = wait.until(
                ExpectedConditions.visibilityOfElementLocated(LocatorRepository.by("label_header"))
        );

        String text = resultHeader.getText();
        log.info("Search result header: " + text);

        if (!text.toLowerCase().contains("bilgisayar")) {
            log.error("Search keyword mismatch!");
            throw new AssertionError("Başlık bilgisayar içermiyor: " + text);
        }

        log.info("Search result successfully verified.");
    }

    @Step("İlk ürün sepete eklenir")
    public void addFirstProductToCart() {
        log.info("Adding first product to cart...");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        List<WebElement> prices = wait.until(
                ExpectedConditions.visibilityOfAllElementsLocatedBy(
                        LocatorRepository.by("label_FirstProductPrice")
                )
        );

        if (prices.isEmpty()) {
            log.error("Product price list is empty!");
            throw new AssertionError("Ürün fiyatı bulunamadı!");
        }

        WebElement firstPrice = prices.get(0);
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", firstPrice
        );

        try {
            firstPrice.click();
            log.info("First product clicked normally.");
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstPrice);
            log.warn("Normal click failed, JS Click executed.");
        }

        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
    }

    @Step("Sepete gidilir")
    public void goToCart() {
        log.info("Navigating to cart page...");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        WebElement cartButton = wait.until(
                ExpectedConditions.elementToBeClickable(LocatorRepository.by("btn_Card"))
        );

        try {
            cartButton.click();
            log.info("Cart button clicked.");
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", cartButton);
            log.warn("Normal click failed on cart button, JS click executed.");
        }

        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
    }

    @Step("Alışveriş tamamlanır")
    public void clickCompleteShopping() {
        log.info("Attempting to click 'Complete Shopping'...");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        WebElement completeBtn = wait.until(
                ExpectedConditions.elementToBeClickable(LocatorRepository.by("btn_CompleteShopping"))
        );

        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", completeBtn
        );

        try {
            completeBtn.click();
            log.info("'Complete Shopping' clicked.");
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", completeBtn);
            log.warn("Normal click failed, JS click executed for 'Complete Shopping'.");
        }

        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
    }

    @Step("Login ekranı doğrulanır")
    public void verifyOnLoginPage() {
        log.info("Verifying login page...");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        boolean urlOk = wait.until(d -> d.getCurrentUrl().contains("giris.hepsiburada.com"));

        if (!urlOk) {
            log.error("Login page NOT opened. URL: " + driver.getCurrentUrl());
            throw new AssertionError("Login sayfası açılmadı: " + driver.getCurrentUrl());
        }

        try {
            WebElement loginBtn = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(
                            By.xpath("//button[contains(.,'Giriş yap')]")
                    )
            );

            log.info("Login screen confirmed. Button text: " + loginBtn.getText());
        } catch (Exception e) {
            log.error("Login button NOT found!", e);
            throw new AssertionError("Login butonu bulunamadı!", e);
        }
    }

    @Step("Browser kapatılır")
    public void closeBrowser() {
        log.info("Closing browser...");
        if (driver != null) driver.quit();
        log.info("Browser closed.");
    }
}
