package com.drjoy.automation.service;

import com.drjoy.automation.config.Configuration;
import com.drjoy.automation.config.DriverFactory;
import com.drjoy.automation.execution.ExecutionStep;
import com.drjoy.automation.model.ExportTemplateFilterSetting;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Log4j2
public class AT0038Service {

    /**
     * Test case: AT0038_EditUserDetailAndSave
     * Tìm kiếm user, vào chi tiết, đổi user và lưu lại.
     */
    @ExecutionStep(value = "editUserDetailAndSave")
    public static void editUserDetailAndSave(ExportTemplateFilterSetting setting) {
        WebDriver driver = DriverFactory.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        driver.get(Configuration.getBaseUrl() + "/at/at0037");

        // Nhập tên user cần tìm kiếm vào ô search
        By searchInputLocator = By.xpath("//*[@id='tab-content27']//input[contains(@class, 'search-input') and @placeholder='ユーザー名を検索']");
        WebElement searchInput = wait.until(ExpectedConditions.elementToBeClickable(searchInputLocator));
        searchInput.clear();
        searchInput.sendKeys(setting.getSearchText1());

        // Gửi phím ENTER
        searchInput.sendKeys(Keys.ENTER);

        // Delay 10s cho load kết quả
        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}

        // Click vào nút chi tiết của user
        By detailBtnLocator = By.xpath(String.format(
                "//*[@id='tbl-sheet']//td[1]//span[text()='%s']/../../../..//div[contains(@class, 'btn-detail')]/button",
                setting.getSearchText1()));
        WebElement detailBtn = wait.until(ExpectedConditions.elementToBeClickable(detailBtnLocator));
        detailBtn.click();

        // Tìm ô search user mới để set user cần chuyển
        By innerSearchInputLocator = By.xpath("//div[@class='select-col1']//div[@class='search']/input[@type='text' and @class='search-input']");
        WebElement innerSearchInput = wait.until(ExpectedConditions.elementToBeClickable(innerSearchInputLocator));
        innerSearchInput.clear();
        innerSearchInput.sendKeys(setting.getSearchText2());

        // Delay 10s cho list user xuất hiện
        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}

        // Gửi phím ENTER
        innerSearchInput.sendKeys(Keys.ENTER);

        // Click chọn đúng user
        By userSelectLocator = By.xpath(String.format(
                "//ul[contains(@class, 'select-user')]/virtual-scroll//div[contains(@class, 'select-user-name')]/span[normalize-space(text())='%s']",
                setting.getSearchText2()));
        WebElement userSelect = wait.until(ExpectedConditions.elementToBeClickable(userSelectLocator));
        userSelect.click();

        // Cuộn xuống vị trí (0, 100)
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.scrollTo(0, 100);");

        // Click nút Lưu
        By saveBtnLocator = By.xpath("//div[contains(@class, 'select-content')]/following-sibling::div/button[normalize-space(text())='保存']");
        WebElement saveBtn = wait.until(ExpectedConditions.elementToBeClickable(saveBtnLocator));
        saveBtn.click();

        // Click xác nhận 2 lần
        By positiveBtnLocator = By.xpath("//*[@id = 'positiveButton']");
        for (int i = 0; i < 2; i++) {
            WebElement positiveBtn = wait.until(ExpectedConditions.elementToBeClickable(positiveBtnLocator));
            positiveBtn.click();
        }
    }

    /**
     * Test case: AT0038_EditUserDetailCol3AndSave
     * Tìm kiếm user, vào chi tiết, đổi user ở select-col3 và lưu lại.
     */
    @ExecutionStep(value = "editUserDetailCol3AndSave")
    public static void editUserDetailCol3AndSave(ExportTemplateFilterSetting setting) throws InterruptedException {
        WebDriver driver = DriverFactory.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(Configuration.getBaseUrl() + "/at/at0037");

        Thread.sleep(5000);

        // Nhập tên user cần tìm kiếm vào ô search
        By searchInputLocator = By.xpath("//*[@id='tab-content27']//input[contains(@class, 'search-input') and @placeholder='ユーザー名を検索']");
        WebElement searchInput = wait.until(ExpectedConditions.elementToBeClickable(searchInputLocator));
        searchInput.clear();
        searchInput.sendKeys(setting.getSearchText1());

        // Gửi phím ENTER
        searchInput.sendKeys(Keys.ENTER);

        // Delay 3s cho load kết quả
        Thread.sleep(2000);

        // Click vào nút chi tiết của user
        By detailBtnLocator = By.xpath(String.format(
                "//*[@id='tbl-sheet']//td[1]//span[text()='%s']/../../../..//div[contains(@class, 'btn-detail')]/button",
                setting.getSearchText1()));
        WebElement detailBtn = wait.until(ExpectedConditions.elementToBeClickable(detailBtnLocator));
        detailBtn.click();

        // Tìm ô search user mới ở select-col3 để set user cần chuyển
        By innerSearchInputLocator = By.xpath("//div[@class='select-col3']//div[@class='search']/input[@type='text' and @class='search-input']");
        WebElement innerSearchInput = wait.until(ExpectedConditions.elementToBeClickable(innerSearchInputLocator));
        innerSearchInput.clear();
        innerSearchInput.sendKeys(setting.getSearchText2());

        // Delay 3s cho list user xuất hiện
        Thread.sleep(2000);

        // Gửi phím ENTER
        innerSearchInput.sendKeys(Keys.ENTER);

        // Click chọn đúng user
        By userSelectLocator = By.xpath(String.format(
                "//ul[contains(@class, 'select-user')]/virtual-scroll//div[contains(@class, 'select-user-name')]/span[normalize-space(text())='%s']",
                setting.getSearchText2()));
        WebElement userSelect = wait.until(ExpectedConditions.elementToBeClickable(userSelectLocator));
        userSelect.click();

        // Cuộn xuống vị trí (0, 100)
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.scrollTo(0, 100);");

        // Click nút Lưu
        By saveBtnLocator = By.xpath("//div[contains(@class, 'select-content')]/following-sibling::div/button[normalize-space(text())='保存']");
        WebElement saveBtn = wait.until(ExpectedConditions.elementToBeClickable(saveBtnLocator));
        saveBtn.click();

        // Click xác nhận 2 lần
        By positiveBtnLocator = By.xpath("//*[@id = 'positiveButton']");
        for (int i = 0; i < 2; i++) {
            WebElement positiveBtn = wait.until(ExpectedConditions.elementToBeClickable(positiveBtnLocator));
            positiveBtn.click();
        }
    }

}
