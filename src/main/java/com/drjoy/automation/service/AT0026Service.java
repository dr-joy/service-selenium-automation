package com.drjoy.automation.service;

import com.drjoy.automation.config.Configuration;
import com.drjoy.automation.config.DriverFactory;
import com.drjoy.automation.execution.ExecutionStep;
import com.drjoy.automation.model.setting.TeireiSetting;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Log4j2
public class AT0026Service {

    // ================== 1.2 ==================
    @ExecutionStep(value = "editDayOffRequestReason")
    public static void editDayOffRequestReason(TeireiSetting setting) throws InterruptedException {
        WebDriver driver = DriverFactory.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(Configuration.getBaseUrl() + "at/at0026");
        Thread.sleep(5000);

        String xpathDefault = "//*[@id='container-at-table-content']//div[contains(@class, 'at-table-tr')]/div[2]//span[text()='%s']/../../..%s";

        // 1.2.1 - Chọn loại lý do là RC_DAYOFF
        selectReasonType(wait, "RC_DAYOFF");

        // 1.2.2 - Click checkbox ẩn/hiện reason
        String xPathModified = "//div[1]//span[contains(@class, 'at-checkbox')]";
        By cbDisplayDayOffReason = By.xpath(String.format(xpathDefault, "その他_4", xPathModified));
//        scrollToElement(driver, wait, cbDisplayDayOffReason);
        driver.findElement(cbDisplayDayOffReason).click();

        // 1.2.3 - Sửa tên hiển thị của reason
        xPathModified = "//div[3]//input";
        By displayNameInput = By.xpath(String.format(xpathDefault, "その他_4", xPathModified));
        driver.findElement(displayNameInput).clear();
        driver.findElement(displayNameInput).sendKeys("test automation tool");

        // 1.2.4 - Click chọn day_off_type
        xPathModified = "//div[7]/div";
        By dropdown = By.xpath(String.format(xpathDefault, "その他_4", xPathModified));
        driver.findElement(dropdown).click();

        xPathModified = "//*[@id='dropdown-superposition']/div[text()='個別で付与した日数']";
        By typeItem = By.xpath(String.format(xpathDefault, "その他_4", xPathModified));
        wait.until(ExpectedConditions.elementToBeClickable(typeItem)).click();

        scrollToTop(driver);

        // 1.2.5 - Click button lưu
        xPathModified = "//*[@id='div-master-config']//button[contains(@class, 'master-btn-submit')]";
        By btnSubmit = By.xpath(String.format(xpathDefault, "その他_4", xPathModified));
        driver.findElement(btnSubmit).click();

        // 1.2.6 - Xác nhận popup nếu có
        clickConfirmIfPresent(wait);
    }

    // ================== 3 ==================
    @ExecutionStep(value = "addDayOffReason")
    public static void addDayOffReason(TeireiSetting setting) {
        WebDriver driver = DriverFactory.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(Configuration.getBaseUrl() + "at/at0026");

        selectReasonType(wait, "RC_DAYOFF");

        By btnAdd = By.xpath("//*[@id='div-master-config']//button[contains(@class, 'master-btn-add')]");
        driver.findElement(btnAdd).click();

        By lastCheckbox = By.xpath("//*[@id='container-at-table-content']//div[contains(@class, 'at-table-tr')][last()]/div[1]//span[contains(@class, 'at-checkbox')]");
//        scrollToElement(driver, wait, lastCheckbox);
        driver.findElement(lastCheckbox).click();

        By lastInput = By.xpath("//*[@id='container-at-table-content']//div[contains(@class, 'at-table-tr')][last()]/div[2]//input");
        driver.findElement(lastInput).clear();
        driver.findElement(lastInput).sendKeys("test automation tool");

        By lastDropdown = By.xpath("//*[@id='container-at-table-content']//div[contains(@class, 'at-table-tr')][last()]/div[7]//div[contains(@class, 'at-btn-dropdown')]");
        driver.findElement(lastDropdown).click();

        By dropdownOption = By.xpath("//*[@id='dropdown-superposition']/div[text()='有給休暇']");
        try {
            wait.until(ExpectedConditions.elementToBeClickable(dropdownOption)).click();
        } catch (Exception e) {
            driver.findElement(lastDropdown).click();
            wait.until(ExpectedConditions.elementToBeClickable(dropdownOption)).click();
            throw e;
        }

        scrollToTop(driver);
        By btnSubmit = By.xpath("//*[@id='div-master-config']//button[contains(@class, 'master-btn-submit')]");
        driver.findElement(btnSubmit).click();
    }

    // ================== 4 ==================
    @ExecutionStep(value = "editOvertimeReason")
    public static void editOvertimeReason(TeireiSetting setting) {
        WebDriver driver = DriverFactory.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(Configuration.getBaseUrl() + "at/at0026");

        selectReasonType(wait, "RC_OVERTIME");

        By firstCheckbox = By.xpath("//*[@id='container-at-table-content']//div[contains(@class, 'at-table-tr')]/div[3]//span[text()='患者対応_外来']/../../../div[2]//span[contains(@class, 'at-checkbox')]");
//        scrollToElement(driver, wait, firstCheckbox);
        driver.findElement(firstCheckbox).click();

        By secondCheckbox = By.xpath("//*[@id='container-at-table-content']//div[contains(@class, 'at-table-tr')]/div[3]//span[text()='患者対応_急患']/../../../div[1]//span[contains(@class, 'at-checkbox')]");
//        scrollToElement(driver, wait, secondCheckbox);
        driver.findElement(secondCheckbox).click();

        By inputEdit = By.xpath("//*[@id='container-at-table-content']//div[contains(@class, 'at-table-tr')]/div[3]//span[text()='患者対応_急患']/../../../div[4]//input");
        driver.findElement(inputEdit).clear();
        driver.findElement(inputEdit).sendKeys("test automation tool");

        By btnSubmit = By.xpath("//*[@id='div-master-config']//button[contains(@class, 'master-btn-submit')]");
//        scrollToElement(driver, wait, btnSubmit);
        driver.findElement(btnSubmit).click();

        clickConfirmIfPresent(wait);
    }

    // ================== 5 ==================
    @ExecutionStep(value = "editResearchReason")
    public static void editResearchReason(TeireiSetting setting) {
        WebDriver driver = DriverFactory.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(Configuration.getBaseUrl() + "at/at0026");

        selectReasonType(wait, "RC_RESEARCH");

        By firstCheckbox = By.xpath("//*[@id='container-at-table-content']//div[contains(@class, 'at-table-tr')]/div[3]//span[text()='学会・勉強会_発表等の準備']/../../../div[2]//span[contains(@class, 'at-checkbox')]");
//        scrollToElement(driver, wait, firstCheckbox);
        driver.findElement(firstCheckbox).click();

        By secondCheckbox = By.xpath("//*[@id='container-at-table-content']//div[contains(@class, 'at-table-tr')]/div[3]//span[text()='学会・勉強会_参加']/../../../div[1]//span[contains(@class, 'at-checkbox')]");
//        scrollToElement(driver, wait, secondCheckbox);
        driver.findElement(secondCheckbox).click();

        By inputEdit = By.xpath("//*[@id='container-at-table-content']//div[contains(@class, 'at-table-tr')]/div[3]//span[text()='学会・勉強会_参加']/../../../div[4]//input");
        driver.findElement(inputEdit).clear();
        driver.findElement(inputEdit).sendKeys("test automation tool");

        By btnSubmit = By.xpath("//*[@id='div-master-config']//button[contains(@class, 'master-btn-submit')]");
//        scrollToElement(driver, wait, btnSubmit);
        driver.findElement(btnSubmit).click();

        clickConfirmIfPresent(wait);
    }

    // ================== 6 ==================
    @ExecutionStep(value = "editWatchReason")
    public static void editWatchReason(TeireiSetting setting) {
        WebDriver driver = DriverFactory.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(Configuration.getBaseUrl() + "at/at0026");

        selectReasonType(wait, "RC_WATCH");

        By firstCheckbox = By.xpath("//*[@id='container-at-table-content']//div[contains(@class, 'at-table-tr')]/div[2]//span[text()='半日直A']/../../../div[1]//span[contains(@class, 'at-checkbox')]");
//        scrollToElement(driver, wait, firstCheckbox);
        driver.findElement(firstCheckbox).click();

        By inputEdit = By.xpath("//*[@id='container-at-table-content']//div[contains(@class, 'at-table-tr')]/div[2]//span[text()='半日直A']/../../../div[3]//input");
        driver.findElement(inputEdit).clear();
        driver.findElement(inputEdit).sendKeys("test automation tool");

        By btnSubmit = By.xpath("//*[@id='div-master-config']//button[contains(@class, 'master-btn-submit')]");
//        scrollToElement(driver, wait, btnSubmit);
        driver.findElement(btnSubmit).click();

        clickConfirmIfPresent(wait);
    }

    // ================== 7 ==================
    @ExecutionStep(value = "editDayOffWorkingReason")
    public static void editDayOffWorkingReason(TeireiSetting setting) {
        WebDriver driver = DriverFactory.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(Configuration.getBaseUrl() + "at/at0026");

        selectReasonType(wait, "RC_DAYOFF_WORKING");

        By firstCheckbox = By.xpath("//*[@id='container-at-table-content']//div[contains(@class, 'at-table-tr')]/div[2]//span[text()='患者対応_手術']/../../../div[1]//span[contains(@class, 'at-checkbox')]");
//        scrollToElement(driver, wait, firstCheckbox);
        driver.findElement(firstCheckbox).click();

        By inputEdit = By.xpath("//*[@id='container-at-table-content']//div[contains(@class, 'at-table-tr')]/div[2]//span[text()='患者対応_手術']/../../../div[3]//input");
        driver.findElement(inputEdit).clear();
        driver.findElement(inputEdit).sendKeys("test automation tool");

        By btnSubmit = By.xpath("//*[@id='div-master-config']//button[contains(@class, 'master-btn-submit')]");
//        scrollToElement(driver, wait, btnSubmit);
        driver.findElement(btnSubmit).click();

        clickConfirmIfPresent(wait);
    }

    // ================== 8 ==================
    @ExecutionStep(value = "editPreOvertimeReason")
    public static void editPreOvertimeReason(TeireiSetting setting) {
        WebDriver driver = DriverFactory.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(Configuration.getBaseUrl() + "at/at0026");

        selectReasonType(wait, "RC_PRE_OVERTIME");

        By firstCheckbox = By.xpath("//*[@id='container-at-table-content']//div[contains(@class, 'at-table-tr')]/div[2]//span[text()='患者対応_手術']/../../../div[1]//span[contains(@class, 'at-checkbox')]");
//        scrollToElement(driver, wait, firstCheckbox);
        driver.findElement(firstCheckbox).click();

        By inputEdit = By.xpath("//*[@id='container-at-table-content']//div[contains(@class, 'at-table-tr')]/div[2]//span[text()='患者対応_手術']/../../../div[3]//input");
        driver.findElement(inputEdit).clear();
        driver.findElement(inputEdit).sendKeys("test automation tool");

        By btnSubmit = By.xpath("//*[@id='div-master-config']//button[contains(@class, 'master-btn-submit')]");
//        scrollToElement(driver, wait, btnSubmit);
        driver.findElement(btnSubmit).click();

        clickConfirmIfPresent(wait);
    }

    // ================== 9 ==================
    @ExecutionStep(value = "editOtherReason")
    public static void editOtherReason(TeireiSetting setting) {
        WebDriver driver = DriverFactory.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(Configuration.getBaseUrl() + "at/at0026");

        selectReasonType(wait, "RC_OTHER");

        By firstCheckbox = By.xpath("//*[@id='container-at-table-content']//div[contains(@class, 'at-table-tr')]/div[2]//span[text()='外勤']/../../../div[1]//span[contains(@class, 'at-checkbox')]");
//        scrollToElement(driver, wait, firstCheckbox);
        driver.findElement(firstCheckbox).click();

        By inputEdit = By.xpath("//*[@id='container-at-table-content']//div[contains(@class, 'at-table-tr')]/div[2]//span[text()='外勤']/../../../div[3]//input");
        driver.findElement(inputEdit).clear();
        driver.findElement(inputEdit).sendKeys("test automation tool");

        By btnSubmit = By.xpath("//*[@id='div-master-config']//button[contains(@class, 'master-btn-submit')]");
//        scrollToElement(driver, wait, btnSubmit);
        driver.findElement(btnSubmit).click();

        clickConfirmIfPresent(wait);
    }

    // ================== Các hàm hỗ trợ ==================
    private static void selectReasonType(WebDriverWait wait, String value) {
        // Đợi trang load, chọn đúng loại lý do từ dropdown
        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
        By selectLocator = By.xpath("//*[@id=\"div-master-config\"]//select[contains(@class, 'master-dropdown-reasontype')]");
        WebElement selectElement = wait.until(ExpectedConditions.elementToBeClickable(selectLocator));
        new Select(selectElement).selectByValue(value);
    }

    private static void scrollToElement(WebDriver driver, WebDriverWait wait, By locator) {
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
    }

    private static void scrollToTop(WebDriver driver) {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
    }

    private static void clickConfirmIfPresent(WebDriverWait wait) {
        By positiveBtn = By.xpath("//*[@id = 'positiveButton']");
        try {
            WebElement confirm = wait.withTimeout(Duration.ofSeconds(3)).until(ExpectedConditions.elementToBeClickable(positiveBtn));
            confirm.click();
        } catch (TimeoutException ignored) {
            log.error("There are any confirmed popup");
        }
    }

}
