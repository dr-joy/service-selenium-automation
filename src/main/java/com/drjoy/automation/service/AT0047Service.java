package com.drjoy.automation.service;

import com.drjoy.automation.config.Configuration;
import com.drjoy.automation.config.DriverFactory;
import com.drjoy.automation.execution.ExecutionStep;
import com.drjoy.automation.model.setting.ExportTemplateFilterSetting;
import com.drjoy.automation.model.setting.TeireiSetting;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Log4j2
public class AT0047Service {

    @ExecutionStep(value = "AT_AT0047_1_2_5")
    public static void runAT0047_1_2_5(TeireiSetting setting) throws InterruptedException {
        WebDriver driver = DriverFactory.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(Configuration.getBaseUrl() + "/at/at0047");

        Thread.sleep(5000);

        runTestCase(wait);

        // Kiểm tra cảnh báo trùng lặp
        By duplicatedAlertLocator = By.xpath("//*[@id='tbl-sheet']//tr[last()]/td[1]/input[@type='text']/preceding-sibling::span");
        WebElement duplicatedAlertElement = wait.until(ExpectedConditions.visibilityOfElementLocated(duplicatedAlertLocator));
        String duplicatedAlert = duplicatedAlertElement.getText();

        if ("重複しています".equals(duplicatedAlert)) {
            // Gọi test case AT_AT0047_1_3_6
            runAT0047_1_3_6(setting);

            // Chạy lại test case này (runTestCase())
            runTestCase(wait);
        }

        // Xử lý confirm nếu xuất hiện positiveButton
        try {
            By positiveBtn = By.xpath("//*[@id = 'positiveButton']");
            WebElement positiveButton = wait.until(ExpectedConditions.presenceOfElementLocated(positiveBtn));
            if (positiveButton.isDisplayed()) {
                positiveButton.click();
            }
        } catch (Exception ignored) {
        }
    }

    private static void runTestCase(WebDriverWait wait) {
        // Chuyển sang tab "時間休"
        By jikanTabLocator = By.xpath("//div[contains(@class,'item-tab' ) and text()='時間休']");
        WebElement jikanTab = wait.until(ExpectedConditions.elementToBeClickable(jikanTabLocator));
        jikanTab.click();

        // Click nút add-link
        By addLinkLocator = By.xpath("//*[@id='add-link']/a[@class='add-link']");
        WebElement addLinkElement = wait.until(ExpectedConditions.elementToBeClickable(addLinkLocator));
        addLinkElement.click();

        // Nhập text "test automation tool"
        By lastInputLocator = By.xpath("//*[@id='tbl-sheet']//tr[last()]/td[1]/input[@type='text']");
        WebElement lastInput = wait.until(ExpectedConditions.elementToBeClickable(lastInputLocator));
        lastInput.clear();
        lastInput.sendKeys("test automation tool");

        // Chọn '9' cho giờ
        By hourSelectLocator = By.xpath("//*[@id='tbl-sheet']/table/tbody/tr[last()]/td[2]/div/div[1]/div/select");
        WebElement hourSelect = wait.until(ExpectedConditions.elementToBeClickable(hourSelectLocator));
        Select hour = new Select(hourSelect);
        hour.selectByVisibleText("9");

        // Chọn '30' cho phút
        By minSelectLocator = By.xpath("//*[@id='tbl-sheet']/table/tbody/tr[last()]/td[2]/div/div[2]/div/select");
        WebElement minSelect = wait.until(ExpectedConditions.elementToBeClickable(minSelectLocator));
        Select minute = new Select(minSelect);
        minute.selectByVisibleText("30");

        // Click nút Save
        By saveBtnLocator = By.xpath("//*[@id='btn-save']/button");
        WebElement saveBtn = wait.until(ExpectedConditions.elementToBeClickable(saveBtnLocator));
        saveBtn.click();
    }

    @ExecutionStep(value = "AT_AT0047_1_3_6")
    public static void runAT0047_1_3_6(TeireiSetting setting) throws InterruptedException {
        WebDriver driver = DriverFactory.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(Configuration.getBaseUrl() + "/at/at0047");

        // Chuyển sang tab "時間休"
        By jikanTabLocator = By.xpath("//div[contains(@class,'item-tab' ) and text()='時間休']");
        WebElement jikanTab = wait.until(ExpectedConditions.elementToBeClickable(jikanTabLocator));
        jikanTab.click();

        // Kiểm tra phần tử delete nếu có, không thì gọi lại test AT_AT0047_1_2_5
        By deleteLinkLocator = By.xpath("//*[@id='tbl-sheet']/table/tbody/tr[last()]/td[1]/input[@type='text']/../../td[4]/a");
        boolean hasDeleteLink;
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(deleteLinkLocator));
            hasDeleteLink = true;
        } catch (Exception e) {
            hasDeleteLink = false;
        }

        if (!hasDeleteLink) {
            // Gọi lại test AT_AT0047_1_2_5
            runAT0047_1_2_5(setting);
            try {
                Thread.sleep(3000); // WebUI.delay(3)
            } catch (InterruptedException ignored) {}
        }

        // Click link xóa
        WebElement deleteLink = wait.until(ExpectedConditions.elementToBeClickable(deleteLinkLocator));
        deleteLink.click();

        // Click Save
        By saveBtnLocator = By.xpath("//*[@id='btn-save']/button");
        WebElement saveBtn = wait.until(ExpectedConditions.elementToBeClickable(saveBtnLocator));
        saveBtn.click();

        // Xác thực text-danger phải rỗng
        By dangerTextLocator = By.xpath("//*[@id='tbl-sheet']/table/tbody/tr[last()]/td/span[@class='text-danger']");
        WebElement dangerText = wait.until(ExpectedConditions.visibilityOfElementLocated(dangerTextLocator));
        if (!"".equals(dangerText.getText())) {
            throw new AssertionError("Error: Text-danger is not empty!");
        }

        // Click xác nhận
        By positiveBtn = By.xpath("//*[@id = 'positiveButton']");
        WebElement positiveButton = wait.until(ExpectedConditions.elementToBeClickable(positiveBtn));
        positiveButton.click();
    }
}
