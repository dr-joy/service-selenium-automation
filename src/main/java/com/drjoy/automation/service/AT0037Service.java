package com.drjoy.automation.service;

import com.drjoy.automation.config.Configuration;
import com.drjoy.automation.config.DriverFactory;
import com.drjoy.automation.execution.ExecutionStep;
import com.drjoy.automation.model.setting.ExportTemplateFilterSetting;
import com.drjoy.automation.model.setting.TeireiSetting;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Log4j2
public class AT0037Service {

    /**
     * Test case: AT0037_ExportCSV
     * Tìm kiếm user và export CSV trên màn hình AT0037.
     */
    @ExecutionStep(value = "exportCSV")
    public static void exportCSV(TeireiSetting setting) throws InterruptedException {
        WebDriver driver = DriverFactory.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(Configuration.getBaseUrl() + "/at/at0037");

        Thread.sleep(10000);

        // Tìm ô input tìm kiếm theo xpath rồi nhập tên
        By searchInput = By.xpath("//*[@id='tab-content27']//input[contains(@class, 'search-input') and @placeholder='ユーザー名を検索']");
        wait.until(ExpectedConditions.visibilityOfElementLocated(searchInput));
        WebElement input = driver.findElement(searchInput);
        input.clear();
        input.sendKeys(setting.getSearchText1());
        input.sendKeys(Keys.ENTER);

        // Delay 2 giây chờ kết quả load
        Thread.sleep(2000);

        // Click button có text 'csv'
        By btnCsv = By.xpath("//button[text()='csv']");
        wait.until(ExpectedConditions.elementToBeClickable(btnCsv));
        driver.findElement(btnCsv).click();

        // Click button confirm nếu có
        By btnConfirm = By.xpath("//*[@id = 'positiveButton']");
        try {
            wait.until(ExpectedConditions.elementToBeClickable(btnConfirm));
            driver.findElement(btnConfirm).click();
            Thread.sleep(5000);
        } catch (Exception e) {
            log.error("Confirm button not found or not clickable: {}", e.getMessage());
        }
    }

}
