package com.drjoy.automation.service;

import com.drjoy.automation.config.Configuration;
import com.drjoy.automation.config.DriverFactory;
import com.drjoy.automation.execution.ExecutionStep;
import com.drjoy.automation.model.ExportTemplateFilterSetting;
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
public class AT0048Service {

    /**
     * Test case: AT0047_SelectUserForTimeOff
     * Chọn user cho dòng thời gian nghỉ, tạo mới nếu chưa có.
     */
    @ExecutionStep(value = "electUserForTimeOff")
    public static void selectUserForTimeOff(ExportTemplateFilterSetting setting) throws InterruptedException {
        WebDriver driver = DriverFactory.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        driver.get(Configuration.getBaseUrl() + "/at/at0047");

        // Chuyển sang tab "時間休"
        By jikanTabLocator = By.xpath("//div[contains(@class,'item-tab' ) and text()='時間休']");
        WebElement jikanTab = wait.until(ExpectedConditions.elementToBeClickable(jikanTabLocator));
        jikanTab.click();

        // Kiểm tra nút chọn user (cột thứ 3), nếu không có thì tạo mới dòng
        By userButtonLocator = By.xpath("//*[@id='tbl-sheet']/table/tbody/tr[last()]/td[1]/input/../../td[3]/button");
        boolean hasUserButton;
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(userButtonLocator));
            hasUserButton = true;
        } catch (Exception e) {
            hasUserButton = false;
        }

        if (!hasUserButton) {
            // Gọi lại test AT_AT0047_1_2_5 để tạo mới dòng
            AT0047Service.runAT0047_1_2_5(setting);
            Thread.sleep(5000);
        }

        // Click vào nút chọn user
        WebElement userBtn = wait.until(ExpectedConditions.elementToBeClickable(userButtonLocator));
        userBtn.click();

        // Set text vào ô search user
        By searchInputLocator = By.xpath("//div[@class='select-col1']//div[@class='search']/input[@type='text' and @class='search-input']");
        WebElement searchInput = wait.until(ExpectedConditions.elementToBeClickable(searchInputLocator));
        searchInput.clear();
        searchInput.sendKeys(setting.getSearchText1());

        // Delay tương ứng
        Thread.sleep(5000);

        // Click chọn đúng user
        By userSelectLocator = By.xpath(String.format(
                "//ul[contains(@class, 'select-user')]/virtual-scroll//div[contains(@class, 'select-user-name')]/span[normalize-space(text())='%s']",
                setting.getSearchText1()));
        WebElement userSelect = wait.until(ExpectedConditions.elementToBeClickable(userSelectLocator));
        userSelect.click();

        // Click nút Lưu
        By saveBtnLocator = By.xpath("//button[contains(text(),'保存')]");
        WebElement saveBtn = wait.until(ExpectedConditions.elementToBeClickable(saveBtnLocator));
        saveBtn.click();

        // Click nút xác nhận
        By positiveBtnLocator = By.xpath("//*[@id = 'positiveButton']");
        WebElement positiveBtn = wait.until(ExpectedConditions.elementToBeClickable(positiveBtnLocator));
        positiveBtn.click();
    }

}
