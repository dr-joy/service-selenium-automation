package com.drjoy.automation.service;

import com.drjoy.automation.config.Configuration;
import com.drjoy.automation.config.DriverFactory;
import com.drjoy.automation.execution.ExecutionStep;
import com.drjoy.automation.model.setting.TeireiSetting;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.Duration;

@Service
@Log4j2
public class AT0049Service {

    /**
     * Thực hiện tìm kiếm theo username hiện tại (lấy trên header) tại màn hình AT0049.
     * Yêu cầu: Đã login và đã chuyển hướng tới trang /at/at0049
     */
    @ExecutionStep(value = "searchByCurrentUsername")
    public static void searchByCurrentUsername(TeireiSetting setting) throws InterruptedException {
        WebDriver driver = DriverFactory.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(Configuration.getBaseUrl() + "at/at0049");

        Thread.sleep(5000);

        // 1. Lấy username đang hiển thị trên header (mini-profile)
        By usernameLocator = By.xpath("//span[contains(@class, 'mini-profile-userinfo-name')]");
        WebElement usernameElem = wait.until(ExpectedConditions.visibilityOfElementLocated(usernameLocator));
        String username = usernameElem.getText();

        // 2. Điền username vào ô tìm kiếm
        By inputSearchLocator = By.xpath("//app-at0049//input[contains(@class, 'search-input')]");
        WebElement inputSearch = wait.until(ExpectedConditions.visibilityOfElementLocated(inputSearchLocator));
        inputSearch.clear();
        inputSearch.sendKeys(username);

        // 3. Nhấn ENTER để thực hiện tìm kiếm
        inputSearch.sendKeys(Keys.ENTER);
    }

    /**
     * Click nút "休暇履歴" trên dòng user ở màn hình AT0049b.
     * Yêu cầu: Đã login và đã chuyển hướng tới trang /at/at0049b.
     */
    @ExecutionStep(value = "clickVacationHistoryButton")
    public static void clickVacationHistoryButton(TeireiSetting setting) throws InterruptedException {
        WebDriver driver = DriverFactory.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(Configuration.getBaseUrl() + "at/at0049b");

        // Chờ 5 giây để các thành phần trang tải hoàn tất (tương tự WebUI.delay(5))
        Thread.sleep(5000);

        // Xác định và click nút "休暇履歴"
        By vacationHistoryBtnLocator = By.xpath(
                "//div[contains(@class, 'div-user-name')]/span/../../../following-sibling::td[last()]//button[normalize-space()='休暇履歴']"
        );
        WebElement btn = driver.findElement(vacationHistoryBtnLocator);
        btn.click();
        Thread.sleep(5000);
    }

    @ExecutionStep(value = "importFile")
    public static void importFile(TeireiSetting setting) throws InterruptedException {
        WebDriver driver = DriverFactory.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(Configuration.getBaseUrl() + "at/at0049");
        Thread.sleep(5000);

        // Get path file import
        String pathFile = getAbsolutePath("GrantVacation.csv");

        // Import file csv
        // Find the hidden input element (by tag, class, or another attribute)
        WebElement uploadInput = driver.findElement(By.cssSelector("input[type='file']"));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].classList.remove('d-none')", uploadInput);
        uploadInput.sendKeys(pathFile);
        Thread.sleep(3000);

    }

    private static String getAbsolutePath(String fileName) {
        String resourcePath = String.format("%s/%s", Configuration.getDataBasePath(), fileName);
        File tmpFile = new File(resourcePath);
        return tmpFile.getAbsolutePath();
    }

}
