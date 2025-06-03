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
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Log4j2
public class AT0050Service {

    // Kịch bản 1: Cấp 2 ngày nghỉ cho user
    @ExecutionStep(value = "grantLeaveType1")
    public static void grantLeaveType1(TeireiSetting setting) throws InterruptedException {
        WebDriver driver = DriverFactory.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(Configuration.getBaseUrl() + "at/at0049");

        Thread.sleep(5000);

        // 1. Tìm ô input và nhập user
        WebElement searchInput = driver.findElement(By.xpath("//app-at0049//input[contains(@class, 'search-input')]"));
        searchInput.clear();
        searchInput.sendKeys(setting.getSearchText1());
        searchInput.sendKeys(Keys.ENTER);

        // 2. Đợi kết quả tìm kiếm (10s)
        Thread.sleep(5000);

        // 3. Click button 休暇付与 ứng với user vừa tìm
        WebElement grantBtn = driver.findElement(By.xpath(
                String.format("//div[contains(@class, 'div-user-name')]/span[normalize-space()='%s']/../../../following-sibling::td[last()]//button[normalize-space()='休暇付与']", setting.getSearchText1())
        ));
        grantBtn.click();

        // 4. Đợi popup hiện (10s)
        Thread.sleep(5000);

        // 5. Nhập số ngày nghỉ: 2
        WebElement numberInput = driver.findElement(By.xpath("//div[text()='付与数']/following-sibling::div[@class='allocate-box']//input[@type='text']"));
        numberInput.clear();
        numberInput.sendKeys("2");

        // 6. Click button 付与 (cấp phép)
        WebElement allocateBtn = driver.findElement(By.xpath("//button[contains(@class, 'btn-primary') and text()='付与']"));
        allocateBtn.click();

        // 7. Click xác nhận (nếu có)
        WebElement confirmBtn = driver.findElement(By.xpath("//*[@id = 'positiveButton']"));
        confirmBtn.click();
    }

    // Kịch bản 2: Cấp 3 ngày nghỉ cho user, click vào input trước khi nhập số
    @ExecutionStep(value = "grantLeaveType2")
    public static void grantLeaveType2(TeireiSetting setting) throws InterruptedException {
        WebDriver driver = DriverFactory.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(Configuration.getBaseUrl() + "at/at0049");

        Thread.sleep(5000);

        // 1. Tìm ô input và nhập user
        WebElement searchInput = driver.findElement(By.xpath("//app-at0049//input[contains(@class, 'search-input')]"));
        searchInput.clear();
        searchInput.sendKeys(setting.getSearchText1());
        searchInput.sendKeys(Keys.ENTER);

        // 2. Đợi kết quả tìm kiếm (10s)
        Thread.sleep(5000);

        // 3. Click button 休暇付与 ứng với user vừa tìm
        WebElement grantBtn = driver.findElement(By.xpath(
                String.format("//div[contains(@class, 'div-user-name')]/span[normalize-space()='%s']/../../../following-sibling::td[last()]//button[normalize-space()='休暇付与']", setting.getSearchText1())
        ));
        grantBtn.click();

        // 4. Click vào input trước khi nhập số ngày
        WebElement numberInput = driver.findElement(By.xpath("//div[text()='付与数']/following-sibling::div[@class='allocate-box']//input[@type='text']"));
        numberInput.click();
        numberInput.clear();
        numberInput.sendKeys("3");

        // 5. Click button 付与
        WebElement allocateBtn = driver.findElement(By.xpath("//button[contains(@class, 'btn-primary') and text()='付与']"));
        allocateBtn.click();

        // 6. Click xác nhận (nếu có)
        WebElement confirmBtn = driver.findElement(By.xpath("//*[@id = 'positiveButton']"));
        confirmBtn.click();
    }

}
